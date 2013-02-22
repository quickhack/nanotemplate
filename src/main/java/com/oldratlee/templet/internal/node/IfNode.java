package com.oldratlee.templet.internal.node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class IfNode extends ContainerNode {
    String varName;

    public IfNode(String varName, Node subNode) {
        super(subNode);
        this.varName = varName;
    }

    public void execute(Map<String, Object> context, Writer result) throws IOException {
        if (isTrue(context.get(varName))) {
            subNode.execute(context, result);
        }
    }

    public static boolean isTrue(Object var) {
        if (var == null) return false;
        if (var instanceof Boolean) return (Boolean) var;

        return true;
    }
}
