package com.oldratlee.nanotemplate.internal.node;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class IfNode extends BlockNode {
    String varName;

    public IfNode(String varName, List<Node> nodes) {
        super(nodes);
        this.varName = varName;
    }

    public void execute(Map<String, Object> context, Writer result) throws IOException {
        if (isTrue(context.get(varName))) {
            super.execute(context, result);
        }
    }

    public static boolean isTrue(Object var) {
        if (var == null) return false;
        if (var instanceof Boolean) return (Boolean) var;

        return true;
    }
}
