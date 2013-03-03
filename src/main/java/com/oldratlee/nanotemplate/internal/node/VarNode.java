package com.oldratlee.nanotemplate.internal.node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class VarNode implements Node {
    String varName;

    public VarNode(String varName) {
        this.varName = varName;
    }

    public void execute(Map<String, Object> context, Writer result) throws IOException {
        result.write(context.get(varName).toString());
    }
}
