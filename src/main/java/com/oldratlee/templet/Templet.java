package com.oldratlee.templet;

import com.oldratlee.templet.internal.Parser;
import com.oldratlee.templet.internal.node.Node;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
public class Templet {
    public static Templet getTemplet(Reader input) throws IOException {
        return new Templet(Parser.parse(input));
    }

    public static void render(Reader template, Map<String, Object> context, Writer result) throws IOException {
        Node node = Parser.parse(template);
        node.execute(context, result);
    }

    public void render(Map<String, Object> context, Writer result) throws IOException {
        node.execute(context, result);
    }

    private Node node;

    private Templet(Node node) {
        this.node = node;
    }
}
