package com.oldratlee.templet.internal.node;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class BlockNode implements Node {
    List<Node> nodes;

    public BlockNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void execute(Map<String, Object> context, Writer result) throws IOException {
        for (Node node : nodes) {
            node.execute(context, result);
        }
    }
}