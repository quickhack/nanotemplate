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
    private static final int states[][] = {};

    private static final char META = '$';

    public static Node parse(Reader reader) throws IOException {
        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }
        PushbackReader pushbackReader = new PushbackReader(reader);
        return doParse(pushbackReader);
    }

    static Node doParse(PushbackReader pushbackReader) throws IOException {
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
            assert read > 0;

            if (read != META) {
                sb.append((char) read);
            } else {
                int read2 = pushbackReader.read();
                if (read2 == META) {
                    sb.append(META);
                } else {
                    pushbackReader.unread(read2);
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
            if (Character.isSpaceChar(read)) {
                break;
            }
        }
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no end $ for if");
            }
            if (!Character.isSpaceChar(read)) {
                break;
            }
        }

        if (read != META) {
            throw new IllegalStateException("no end $ for if");
        }
        Node node = doParse(pushbackReader);

        char[] end = new char[DIR_END.length];
        read = pushbackReader.read(end);
        if (read < DIR_END.length) {
            throw new IllegalStateException("no $end$ dir for if");
        }
        if (!Arrays.equals(end, DIR_END)) {
            throw new IllegalStateException("no $end$ dir for if");
        }
        return new IfNode(varName.toString(), node);
    }

    static Node parseFor(PushbackReader pushbackReader) throws IOException {
        pushbackReader.read(); // $
        pushbackReader.read(); // f
        pushbackReader.read(); // o
        pushbackReader.read(); // r

        StringBuilder varName = new StringBuilder();
        StringBuilder varName2 = new StringBuilder();
        int read;
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no var for $for");
            }
            if (!Character.isSpaceChar(read)) {
                break;
            }
        }
        while (true) {
            if(Character.isLetter((char)read)) {
                varName.append((char) read);
            }
            read = pushbackReader.read();
            if (!Character.isLetter(read)) {
                break;
            }
        }


        return null;
    }

    static Node parseVar(PushbackReader pushbackReader) throws IOException {
        pushbackReader.read(); // $
        pushbackReader.read(); // {

        StringBuilder varName = new StringBuilder();
        int read;
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no var for ${}");
            }
            if (!Character.isSpaceChar(read)) {
                break;
            }
        }
        while (true) {
            varName.append((char) read);
            read = pushbackReader.read();
            if (Character.isSpaceChar(read)) {
                break;
            }
        }
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no } for ${}");
            }
            if (!Character.isSpaceChar(read)) {
                break;
            }
        }
        while (true) {
            read = pushbackReader.read();
            if (read == -1) {
                throw new IllegalStateException("no end } for ${}");
            }
            if (!Character.isSpaceChar(read)) {
                break;
            }
        }
        if (read != '}') {
            throw new IllegalStateException("no end } for ${}");
        }
        return new VarNode(varName.toString());
    }
}
