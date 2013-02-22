package com.oldratlee.templet.internal;

import com.oldratlee.templet.internal.node.CompositeNode;
import com.oldratlee.templet.internal.node.LiteralNode;
import com.oldratlee.templet.internal.node.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
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
        return new CompositeNode(nodeList);
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

    static Node parseIf(PushbackReader pushbackReader) throws IOException {


        return null;
    }

    static Node parseFor(PushbackReader pushbackReader) throws IOException {
        return null;
    }

    static Node parseVar(PushbackReader pushbackReader) throws IOException {
        return null;
    }
}
