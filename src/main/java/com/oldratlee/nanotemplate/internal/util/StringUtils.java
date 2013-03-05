
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
package com.oldratlee.nanotemplate.internal.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+(\\.[.0-9]+)?[BSILFDbsilfd]?$");

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^(_a-zA-Z0-9)]");

    public static String getVaildName(String name) {
        return SYMBOL_PATTERN.matcher(name).replaceAll("_");
    }

    public static boolean isNumber(String value) {
        return isEmpty(value) ? false : NUMBER_PATTERN.matcher(value).matches();
    }

    public static boolean isNumber(char[] value) {
        if (value == null || value.length == 0) {
            return false;
        }
        for (char ch : value) {
            if (ch != '.' && (ch <= '0' || ch >= '9')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(byte[] value) {
        if (value == null || value.length == 0) {
            return false;
        }
        for (byte ch : value) {
            if (ch != '.' && (ch <= '0' || ch >= '9')) {
                return false;
            }
        }
        return true;
    }

    private static final Pattern NAMED_PATTERN = Pattern.compile("^[_A-Za-z][_0-9A-Za-z]*$");

    public static boolean isNamed(String value) {
        return NAMED_PATTERN.matcher(value).matches();
    }

    private static final Pattern TYPED_PATTERN = Pattern.compile("^[_A-Za-z][_.0-9A-Za-z]*$");

    public static boolean isTyped(String value) {
        return TYPED_PATTERN.matcher(value).matches();
    }

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\.[_A-Za-z][_0-9A-Za-z]*$");

    public static boolean isFunction(String value) {
        return FUNCTION_PATTERN.matcher(value).matches();
    }

    public static boolean isEmpty(byte[] value) {
        return value == null || value.length == 0;
    }

    public static boolean isNotEmpty(byte[] value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(char[] value) {
        return value == null || value.length == 0;
    }

    public static boolean isNotEmpty(char[] value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isBlank(String value) {
        if (StringUtils.isNotEmpty(value)) {
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                switch (ch) {
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                    case '\b':
                    case '\f':
                        break;
                    default:
                        return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static byte[] toBytes(String src, String encoding) {
        try {
            return src.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

}
