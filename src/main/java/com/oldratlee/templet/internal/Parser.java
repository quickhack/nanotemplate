package com.oldratlee.templet.internal;

import com.oldratlee.templet.internal.node.*;

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
        return doParse(pushbackReader, false);
    }

    static Node doParse(PushbackReader pushbackReader, boolean nested) throws IOException {
        List<Node> nodeList = new ArrayList<Node>();
        while (true) {
            int read1 = pushbackReader.read();
            if (read1 == -1) break;

            if (read1 != META) {
                pushbackReader.unread(read1);
                nodeList.add(parseLiteral(pushbackReader));
            } else {
                int read2 = pushbackReader.read();
                if (read2 == META) {
                    pushbackReader.unread(read2);
                    pushbackReader.unread(read1);
                    nodeList.add(parseLiteral(pushbackReader));
                } else if (read2 == 'i') {
                    int read3 = pushbackReader.read();
                    if (read3 == 'f') {
                        pushbackReader.unread(read3);
                        pushbackReader.unread(read2);
                        pushbackReader.unread(read1);
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
            }
        }

        if (nodeList.isEmpty()) return Node.EMPTY;
        if (nodeList.size() == 1) return nodeList.get(0);
        return new BlockNode(nodeList);
    }

    static Node parseLiteral(PushbackReader pushbackReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = pushbackReader.read();
            if(read < 0) break;

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

        assert sb.length() > 0;
        return new LiteralNode(sb.toString());
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
            if (!Character.isSpaceChar(read)) {
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
        Node node = doParse(pushbackReader, true);

        eatEndDir(pushbackReader, "no $end$ dir for $if");

        return new IfNode(varName.toString(), node);
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

        Node subNode = doParse(pushbackReader, true);

        eatEndDir(pushbackReader, "no $end$ dir for for");

        if (varName2 == null) {
            return new ForNode(varName, forVarName, subNode);
        } else {
            return new ForNode(varName, varName2, forVarName, subNode);
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

    static void eatSpace(PushbackReader pushbackReader) throws IOException {
        while (true) {
            int read = pushbackReader.read();
            if (read == -1) {
                return;
            }
            if (!Character.isSpaceChar(read)) {
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
                varName.append((char)read);
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
    }

    static boolean isVarNameChar(int c) {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9' ||
                c == '_';
    }
}
