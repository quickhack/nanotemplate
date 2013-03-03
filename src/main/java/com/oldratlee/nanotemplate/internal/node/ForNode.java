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

import com.oldratlee.nanotemplate.internal.util.Stash;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class ForNode extends BlockNode {
    String varName;
    String varName2;
    String forVarName;

    public ForNode(String varName, String forVarName, List<Node> nodes) {
        super(nodes);
        this.varName = varName;
        this.forVarName = forVarName;
    }


    public ForNode(String varName, String varName2, String forVarName, List<Node> nodes) {
        super(nodes);
        this.varName = varName;
        this.varName2 = varName2;
        this.forVarName = forVarName;
    }

    public void execute(Map<String, Object> context, Writer result) throws IOException {
        Object forVar = context.get(forVarName);
        if (null == forVar) return;

        if (varName2 == null) {
            if (!(forVar instanceof Collection || forVar.getClass().isArray())) {
                throw new IllegalStateException("for var is not Collection or array!");
            }

            if (forVar instanceof Collection) {
                Stash stash = new Stash(context, varName);
                for (Object ele : (Collection) forVar) {
                    context.put(varName, ele);
                    super.execute(context, result);
                }
                stash.pop();
            } else {
                final int len = Array.getLength(forVar);
                Stash stash = new Stash(context, varName);
                for (int i = 0; i < len; ++i) {
                    context.put(varName, Array.get(forVar, i));
                    super.execute(context, result);
                }
                stash.pop();
            }
        } else {
            if (!(forVar instanceof Map)) {
                throw new IllegalStateException("for var is not Map!");
            }
            Stash stash = new Stash(context, varName, varName2);
            @SuppressWarnings("unchecked")
            Set<Map.Entry> set = ((Map) forVar).entrySet();
            for (Map.Entry entry : set) {
                context.put(varName, entry.getKey());
                context.put(varName2, entry.getValue());
                super.execute(context, result);
            }
            stash.pop();
        }
    }
}
