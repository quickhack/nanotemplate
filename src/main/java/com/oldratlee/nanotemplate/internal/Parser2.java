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

package com.oldratlee.nanotemplate.internal;

import com.oldratlee.nanotemplate.internal.node.*;
import com.oldratlee.nanotemplate.internal.scan.DfaScanner;
import com.oldratlee.nanotemplate.internal.scan.ScanException;
import com.oldratlee.nanotemplate.internal.scan.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class Parser2 {
    private final static int B = DfaScanner.TAG_BREAK;
    private final static int B1 = DfaScanner.TAG_BREAK + 1;
    private final static int B2 = DfaScanner.TAG_BREAK + 2;

    static final int states[][] = {
            // 0.$ 1.字母 2.数字 3.{ 4.} 5.\r 6.\n 7.空白 8.others  
            {3, 1, 1, 1, 1, 1, 1, 1, 1,}, // 0.初始
            {2, 4, 2, 6, 2, 2, 2, 2, 2,}, // 1.元字符
            {3, 2, 2, 2, 2, 2, 2, 2, 2,}, // 2.文本
            {2, B2, 2, 2, 2, 2, 2, 2, 2,}, // 3.文本转义
            {B, 4, 4, 4, 4, 5, B, 4, 4,}, // 4.指令
            {B1, B1, B1, B1, B1, B1, B, B1, B1,},// 5.指令尾\r
            {4, 4, 4, 4, B1, 4, 4, 4, 4,}, // 6.变量
    };

    static int getCharType(char ch) {
        if (ch == '$') return 0;
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') return 1;
        if (ch >= '0' && ch <= '9') return 2;
        if (ch == '{') return 3;
        if (ch == '}') return 4;
        if (ch == '\r') return 5;
        if (ch == '\n') return 6;
        if (ch == ' ' || ch == '\t' || ch == '\f') return 7;
        return 8;
    }

    static DfaScanner tokenizer = new DfaScanner() {
        @Override
        public int next(int state, char ch) {
            return states[state][getCharType(ch)];
        }
    };

    public static Node parse(Reader reader) throws IOException, ScanException {
        List<Token> tokens = tokenizer.scan(reader);

        List<Node> roots = new ArrayList<Node>();
        Stack<List<Node>> stack = new Stack<List<Node>>();
        stack.push(roots);

        for (Token token : tokens) {
            switch (type(token.getMessage())) {
                case T_LITERAL:
                    LiteralNode literalNode = genLiteralNode(token);
                    stack.peek().add(literalNode);
                    break;
                case T_VAR:
                    VarNode varNode = genVarNode(token);
                    stack.peek().add(varNode);
                    break;
                case T_IF:
                    IfNode ifNode = genIfNode(token);
                    stack.peek().add(ifNode);
                    stack.push(ifNode.getNodes());
                    break;
                case T_FOR:
                    ForNode forNode = genForNode(token);
                    stack.peek().add(forNode);
                    stack.push(forNode.getNodes());
                    break;
                case T_END:
                    stack.pop();
            }
        }

        if (roots.isEmpty()) return Node.EMPTY;
        if (roots.size() == 1) return roots.get(0);
        return new BlockNode(roots);
    }

    static LiteralNode genLiteralNode(Token token) {
        String literal = token.getMessage().replace("$$", "$");
        return new LiteralNode(literal);
    }

    static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\f';
    }


    static boolean isVarNameChar(int c) {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9' ||
                c == '_';
    }

    private static final char META = '$';

    static IfNode genIfNode(Token token) {
        char[] chars = token.getMessage().toCharArray();
        int idx = 3;
        StringBuilder varName = new StringBuilder();
        char read;
        while (true) {
            if (idx > chars.length) {
                throw new IllegalStateException("no var for if");
            }
            read = chars[idx++];
            if (!isWhitespace(read)) {
                break;
            }
        }
        while (true) {
            if (idx > chars.length) {
                break;
            }
            varName.append(read);
            read = chars[idx++];
            if (!isVarNameChar(read)) {
                --idx;
                break;
            }
        }
        while (true) {
            if (idx > chars.length) {
                break;
            }
            read = chars[idx++];
            if (!isWhitespace(read)) {
                break;
            }
        }

        if (idx <= chars.length - 1) {
            read = chars[idx];
            if (read != META && read != '\r' && read != '\n') {
                throw new IllegalStateException("no end $ for $if");
            }
        }

        return new IfNode(varName.toString(), new ArrayList<Node>());
    }

    static ForNode genForNode(Token token) {
        return null;
    }

    static VarNode genVarNode(Token token) {
        return null;
    }

    static final int T_LITERAL = 0;
    static final int T_IF = 1;
    static final int T_FOR = 2;
    static final int T_VAR = 3;
    static final int T_END = 4;

    static final Pattern D_IF = Pattern.compile("^$if[ \\t\\f].*");
    static final Pattern D_FOR = Pattern.compile("^$for[ \\t\\f].*");
    static final Pattern D_END = Pattern.compile("^$end[ \\t\\f].*");

    static int type(String token) {
        if (token.startsWith("${")) return T_VAR;

        if (D_IF.matcher(token).matches()) return T_IF;
        if (D_FOR.matcher(token).matches()) return T_FOR;
        if (D_END.matcher(token).matches()) return T_END;

        if (token.startsWith("$"))
            throw new IllegalStateException("Undefined Direct!");

        return T_LITERAL;
    }

}
