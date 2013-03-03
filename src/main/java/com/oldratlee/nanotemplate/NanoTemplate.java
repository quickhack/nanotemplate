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
    public static NanoTemplate getTemplet(Reader input) throws IOException {
        return new NanoTemplate(Parser.parse(input));
    }

    public static String render(String templet, Map<String, Object> context) throws IOException {
        StringWriter output = new StringWriter();
        render(new StringReader(templet), context, output);
        return output.toString();
    }

    public static String renderFromClassResource(Class<?> clazz, String templetName, Map<String, Object> context) throws IOException {
        StringWriter output = new StringWriter();
        renderFromClassResource(clazz, templetName, context, output);
        return output.toString();
    }

    public static void renderFromClassResource(Class<?> clazz, String templetName, Map<String, Object> context, Writer output) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(templetName);
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
