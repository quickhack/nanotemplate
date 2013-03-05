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

package com.oldratlee.nanotemplate.internal.scan;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class DfaScanner {
    private static final int MASK_TAG = 0xFF000000;
    private static final int MASK_VALUE = 0x00FFFFFF;

    private static final int TAG_GAP = 0x01000000;

    /**
     * BREAK，结束片段，并回到起始状态，退回的字符将重新读取
     * <p/>
     * state = BREAK + 退回字符数 <br/>
     * state = BREAK + 1 // 结束并退回1个字符，即不包含当前字符
     */
    public static final int TAG_BREAK = TAG_GAP;

    /**
     * PUSH，压栈，并回到指定状态
     * <p/>
     * state = PUSH + 压栈后回到状态数 <br/>
     * state = PUSH + 4 // 压入第2个栈，压栈后回到状态4
     */
    public static final int TAG_PUSH = TAG_BREAK + TAG_GAP;

    /**
     * POP，弹栈，并回到指定状态，栈空回到起始状态0，表示结束片段
     * <p/>
     * state = POP + 弹栈后回到状态数 <br/>
     * state = POP + 4 - EMPTY * 5 // 弹栈后回到状态4，栈空回到状态5
     */
    public static final int TAG_POP = TAG_PUSH + TAG_GAP;

    /**
     * ERROR，解析出错，抛出异常
     * <p/>
     * state = ERROR + 错误码 <br/>
     * state = ERROR + 1 // 出错，并返回错误码为1的异常信息。
     */
    public static final int TAG_ERROR = TAG_POP + TAG_GAP;

    /**
     * TAG的上限
     */
    private static final int TAG_UNDEFINED = 0x04000000;

    public List<Token> scan(Reader reader) throws ScanException, IOException {
        List<Token> tokens = new ArrayList<Token>();
        // 解析时状态 ----
        StringBuilder buffer = new StringBuilder(); // 缓存字符
        StringBuilder remain = new StringBuilder(); // 残存字符
        int pre = 0; // 上一状态
        int state = 0; // 当前状态
        char ch; // 当前字符
        int offset = 0;
        int line = 0;
        int column = 0;

        // 逐字解析 ----
        int i = 0;
        int p = 0;
        while (true) {
            if (remain.length() > 0) { // 先处理残存字符
                ch = remain.charAt(0);
                remain.deleteCharAt(0);
            } else { // 没有残存字符则读取字符流
                int read = reader.read();
                if (read < 0) break;
                
                ch = (char) read;
                offset++;
            }

            buffer.append(ch); // 将字符加入缓存
            state = next(state, ch); // 从状态机图中取下一状态
            int stateValue = (state & MASK_VALUE);
            if (state >= TAG_UNDEFINED) {
                throw new ScanException("Undefined state " + state, line, column);
            }
            if (state >= TAG_ERROR) {
                throw new ScanException("DFAScanner.state.error, error code: " + stateValue, line, column);
            }
            if (state >= TAG_POP) {
                throw new UnsupportedOperationException("TAG_POP Unsupported!");
            } else if (state >= TAG_PUSH) {
                throw new UnsupportedOperationException("TAG_PUSH Unsupported!");
            }
            if (state >= TAG_BREAK) {
                int acceptLength = buffer.length() - stateValue;
                if (acceptLength < 0 || acceptLength > buffer.length())
                    throw new ScanException("DFAScanner.acceptor.error", line, column);
                if (acceptLength != 0) {
                    String message = buffer.substring(0, acceptLength);
                    Token token = new Token(message, offset - buffer.length(), pre);
                    tokens.add(token);// 完成接收
                }
                if (acceptLength != buffer.length())
                    remain.insert(0, buffer.substring(acceptLength)); // 将未接收的缓存记入残存
                buffer.setLength(0); // 清空缓存
                state = 0; // 回归到初始状态
            }
            pre = state;
        }
        // 接收最后缓存中的内容
        if (buffer.length() > 0) {
            String message = buffer.toString();
            tokens.add(new Token(message, offset - message.length(), pre));
        }
        return tokens;
    }

    public abstract int next(int state, char ch);

}
