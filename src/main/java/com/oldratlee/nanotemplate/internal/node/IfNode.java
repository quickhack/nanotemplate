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
