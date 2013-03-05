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


import com.oldratlee.nanotemplate.internal.util.StringUtils;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public final class Token {

    public static final int UNKNOWN_TYPE = -1;

    private final String message;

    private final int line;
    
    private final int column;

    private final int type;

    /**
     * Create Token.
     *
     * @param message message
     * @param line  line
     */
    public Token(String message, int line, int column) {
        this(message, line, column, UNKNOWN_TYPE);
    }

    /**
     * Create Token with type.
     *
     * @param message message
     * @param line  line
     * @param type    type
     */
    public Token(String message, int line, int column, int type) {
        if (StringUtils.isEmpty(message))
            throw new IllegalArgumentException("message == null");
        this.message = message;
        this.line = line;
        this.column = column;
        this.type = type;
    }

    /**
     * Get message.
     *
     * @return message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get line.
     *
     * @return line
     */
    public int getLine() {
        return line;
    }

    /**
     * Get Column. 
     * 
     * @return column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get type.
     *
     * @return type.
     */
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + line;
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Token other = (Token) obj;
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) return false;
        if (line != other.line) return false;
        if (type != other.type) return false;
        return true;
    }

}
