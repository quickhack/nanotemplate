package com.oldratlee.nanotemplate.internal.node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public interface Node {
    void execute(Map<String, Object> context, Writer result) throws IOException;

    Node EMPTY = new Node() {
        public void execute(Map<String, Object> context, Writer result) throws IOException {
        }
    };
}
