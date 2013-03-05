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

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class ScanException extends Exception {
    private final int line;
    private final int column;

    public ScanException(String s, int line, int column) {
        super(s);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
