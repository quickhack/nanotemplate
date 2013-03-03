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

package com.oldratlee.nanotemplate;

import com.oldratlee.nanotemplate.internal.Parser;
import com.oldratlee.nanotemplate.internal.node.Node;

import java.io.*;
import java.util.Map;

/**
 * <p>支持语法：</p>
 * <ol>
 * <li><code>${var}</code>
 * <li><code>$if var$</code>
 * <li><code>$for var : list$</code>, <code>$for key value : list$</code>
 * <li><code>$end$</code>
 * <li><code>$$</code> 输出一个常理<code>$</code>
 * </ol>
 *
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class NanoTemplate {
    public static NanoTemplate getTemplate(Reader input) throws IOException {
        return new NanoTemplate(Parser.parse(input));
    }

    public static String render(String template, Map<String, Object> context) throws IOException {
        StringWriter output = new StringWriter();
        render(new StringReader(template), context, output);
        return output.toString();
    }

    public static String renderFromClassResource(Class<?> clazz, String templateName, Map<String, Object> context) throws IOException {
        StringWriter output = new StringWriter();
        renderFromClassResource(clazz, templateName, context, output);
        return output.toString();
    }

    public static void renderFromClassResource(Class<?> clazz, String templateName, Map<String, Object> context, Writer output) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(templateName);
        InputStreamReader reader = new InputStreamReader(resourceAsStream);
        render(reader, context, output);
    }

    public static void render(Reader template, Map<String, Object> context, Writer output) throws IOException {
        Node node = Parser.parse(template);
        node.execute(context, output);
    }

    public void render(Map<String, Object> context, Writer result) throws IOException {
        node.execute(context, result);
    }

    private Node node;

    private NanoTemplate(Node node) {
        this.node = node;
    }
}
