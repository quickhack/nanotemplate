/*
 * Copyright 2013 NanoTemplate Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oldratlee.nanotemplate.internal;

import com.oldratlee.nanotemplate.internal.node.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class Parser {
    private static final char META = '$';

    public static Node parse(Reader reader) throws IOException {
        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }
        PushbackReader pushbackReader = new PushbackReader(reader, 1024);
        List<Node> nodeList = doParse(pushbackReader, false);

        if (nodeList.isEmpty()) return Node.EMPTY;
        if (nodeList.size() == 1) return nodeList.get(0);
        return new BlockNode(nodeList);
    }

    private static void cutLastSpaceLine(List<Node> nodeList) {
        if (nodeList.isEmpty()) return;
        Node node = nodeList.get(nodeList.size() - 1);

        if (!(node instanceof LiteralNode)) return;
        LiteralNode literalNode = (LiteralNode) node;

        String literal = literalNode.getLiteral();

        for (int i = literal.length() - 1; i >= 0; --i) {
            char c = literal.charAt(i);
            if (!Character.isWhitespace(c))
                break;
            if (c == '\r' || c == '\n') {
                if (i != literal.length() - 1)
                    literalNode.setLiteral(literal.substring(0, i + 1));
                break;
            } else if (i == 0) {
                literalNode.setLiteral("");
            }
        }
    }

    static List<Node> doParse(PushbackReader pushbackReader, boolean nested) throws IOException {
        boolean cutFirstSpaceLine = nested;

        List<Node> nodeList = new ArrayList<Node>();
        while (true) {
            int read1 = pushbackReader.read();
            if (read1 == -1) break;

            if (read1 != META) {
                pushbackReader.unread(read1);
                nodeList.add(parseLiteral(pushbackReader, cutFirstSpaceLine));
            } else {
                int read2 = pushbackReader.read();
                if (read2 == META) {
                    pushbackReader.unread(read2);
                    pushbackReader.unread(read1);
                    nodeList.add(parseLiteral(pushbackReader, cutFirstSpaceLine));
                } else if (read2 == 'i') {
                    int read3 = pushbackReader.read();
                    if (read3 == 'f') {
                        pushbackReader.unread(read3);
                        pushbackReader.unread(read2);
                        pushbackReader.unread(read1);

                        cutLastSpaceLine(nodeList);
                        nodeList.add(parseIf(pushbackReader));
                    } else {
                        throw new IllegalStateException("Unknown direct: " +
                                (char) read1 + (char) read2 + (char) read3);
                    }
                } else if (read2 == 'f') {
                    int read3 = pushbackReader.read();
                    int read4 = pushbackReader.read();
                    if (read3 != 'o' && read4 != 'r') {
                        throw new IllegalStateException("Unknown direct: " +
                                (char) read1 + (char) read2 + (char) read3 + (char) read4);
                    } else {
                        pushbackReader.unread(read4);
                        pushbackReader.unread(read3);
                        pushbackReader.unread(read2);
                        pushbackReader.unread(read1);

                        cutLastSpaceLine(nodeList);
                        nodeList.add(parseFor(pushbackReader));
                    }
                } else if (read2 == '{') {
                    pushbackReader.unread(read2);
                    pushbackReader.unread(read1);
                    nodeList.add(parseVar(pushbackReader));
                } else if (nested) {
                    pushbackReader.unread(read2);
                    pushbackReader.unread(read1);
                    break;
                } else {
                    throw new IllegalStateException("Illegal format: " +
                            (char) read1 + (char) read2);
                }

                cutFirstSpaceLine = false;
            }
        }
        return nodeList;
    }

    static Node parseLiteral(PushbackReader pushbackReader, boolean cutFirstSpaceLine) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = pushbackReader.read();
            if (read < 0) break;

            if (read != META) {
                sb.append((char) read);
            } else {
                int read2 = pushbackReader.read();
                if (read2 == META) {
                    sb.append(META);
                } else {
                    pushbackReader.unread(read2);
                    pushbackReader.unread(read);
                    break;
                }
            }
        }

        if (cutFirstSpaceLine) {
            int state = 0;
            for (int i = 0; i < sb.length(); ++i) {
                char c = sb.charAt(i);
                if (state == 0) {
                    if (!Character.isWhitespace(c)) {
                        break;
                    }
                    if (c == '\r') {
                        state = 1;
                    } else if (c == '\n') {
                        sb.delete(0, i + 1);
                        break;
                    }
                } else if (state == 1) {
                    if (!Character.isWhitespace(c)) {
                        sb.delete(0, i);
                    } else if (c == '\n') {
                        sb.delete(0, i + 1);
                        break;
                    }
                }
            }
        }
        return new LiteralNode(sb.toString());
    }

    static Node parseVar(PushbackReader pushbackReader) throws IOException {
        pushbackReader.read(); // $
        pushbackReader.read(); // {

        eatSpace(pushbackReader);
        String varName = readVarName(pushbackReader);
        if (varName.length() == 0) {
            throw new IllegalStateException("no varname for ${}");
        }
        eatSpace(pushbackReader);
        int read = pushbackReader.read();
        if (read != '}') {
            throw new IllegalStateException("no end } for ${}");
        }
        return new VarNode(varName);
    }

    private static final char[] DIR_END = new char[]{'$', 'e', 'n', 'd', '$'};

    static Node parseIf(PushbackReader pushbackReader) throws IOException {
        pushbackReader.read(); // $
        pushbackReader.read(); // i
        pushbackReader.read(); // f

        StringBuilder varName = new StringBuilder();
        int read;
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no var for if");
            }
            if (!Character.isWhitespace(read)) {
                break;
            }
        }
        while (true) {
            varName.append((char) read);
            read = pushbackReader.read();
            if (!isVarNameChar(read)) {
                pushbackReader.unread(read);
                break;
            }
        }

        eatSpace(pushbackReader);

        read = pushbackReader.read();
        if (read != META) {
            throw new IllegalStateException("no end $ for $if");
        }
        List<Node> nodeList = doParse(pushbackReader, true);

        cutLastSpaceLine(nodeList);
        eatEndDir(pushbackReader, "no $end$ dir for $if");

        return new IfNode(varName.toString(), nodeList);
    }

    static Node parseFor(PushbackReader pushbackReader) throws IOException {
        pushbackReader.read(); // $
        pushbackReader.read(); // f
        pushbackReader.read(); // o
        pushbackReader.read(); // r

        eatSpace(pushbackReader);
        String varName = readVarName(pushbackReader);
        if (varName.length() == 0) {
            throw new IllegalStateException("no var name for $for");
        }
        eatSpace(pushbackReader);

        int read = pushbackReader.read();
        String varName2 = null;
        String forVarName;
        if (read == ':') {
            eatSpace(pushbackReader);
            forVarName = parseForDirTail(pushbackReader);
        } else {
            pushbackReader.unread(read);
            varName2 = readVarName(pushbackReader);
            if (varName2.length() == 0) {
                throw new IllegalStateException("no varname2 for $for");
            }

            eatSpace(pushbackReader);
            read = pushbackReader.read();
            if (read != ':') {
                throw new IllegalStateException("no : for $for");
            }

            eatSpace(pushbackReader);
            forVarName = parseForDirTail(pushbackReader);
        }

        List<Node> nodeList = doParse(pushbackReader, true);

        cutLastSpaceLine(nodeList);
        eatEndDir(pushbackReader, "no $end$ dir for for");

        if (varName2 == null) {
            return new ForNode(varName, forVarName, nodeList);
        } else {
            return new ForNode(varName, varName2, forVarName, nodeList);
        }
    }

    private static String parseForDirTail(PushbackReader pushbackReader) throws IOException {
        String varName = readVarName(pushbackReader);
        eatSpace(pushbackReader);

        int read = pushbackReader.read();
        if (read != META) {
            throw new IllegalStateException("no end $ for $for");
        }
        return varName;
    }

    static void eatSpace(PushbackReader pushbackReader) throws IOException {
        while (true) {
            int read = pushbackReader.read();
            if (read == -1) {
                return;
            }
            if (!Character.isWhitespace(read)) {
                pushbackReader.unread(read);
                return;
            }
        }
    }

    static String readVarName(PushbackReader pushbackReader) throws IOException {
        StringBuilder varName = new StringBuilder();
        while (true) {
            int read = pushbackReader.read();
            if (read == -1) {
                break;
            }
            if (isVarNameChar(read)) {
                varName.append((char) read);
            } else {
                pushbackReader.unread(read);
                break;
            }
        }
        return varName.toString();
    }

    static void eatEndDir(PushbackReader pushbackReader, String msg) throws IOException {
        char[] end = new char[DIR_END.length];
        int count = pushbackReader.read(end);
        if (count < DIR_END.length) {
            throw new IllegalStateException(msg);
        }
        if (!Arrays.equals(end, DIR_END)) {
            throw new IllegalStateException(msg);
        }

        int state = 0;
        List<Integer> history = new ArrayList<Integer>();
        while (true) {
            int read = pushbackReader.read();
            history.add(read);

            if (state == 0) {
                if (read != -1 && !Character.isWhitespace(read)) {
                    if (!history.isEmpty()) {
                        for (int i = history.size() - 1; i >= 0; --i) {
                            pushbackReader.unread(history.get(i));
                        }
                        break;
                    }
                }
                if (read == '\r') state = 1;
                else if (read == '\n' || read == -1) break;
            } else if (state == 1) {
                if (read != -1 && !Character.isWhitespace(read)) {
                    pushbackReader.unread(read);
                    break;
                }
                if (read == '\n') {
                    state = 1;
                } else if (read == '\n' || read == -1) break;
            }
        }
    }

    static boolean isVarNameChar(int c) {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9' ||
                c == '_';
    }
}
