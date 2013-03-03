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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class Stash {

    private static class Holder {
        String key;
        boolean hasValue;
        Object value;

        public Holder(String key, boolean hasValue, Object value) {
            this.key = key;
            this.hasValue = hasValue;
            this.value = value;
        }
    }

    private final List<Holder> holders;
    private final Map<String, Object> context;

    public Stash(Map<String, Object> context, String... keys) {
        this.context = context;
        holders = new ArrayList<Holder>(keys.length);
        for (String key : keys) {
            holders.add(new Holder(key, context.containsKey(key), context.get(key)));
        }
    }

    public void pop() {
        for (Holder holder : holders) {
            if (holder.hasValue) {
                context.put(holder.key, holder.value);
            } else {
                context.remove(holder.key);
            }
        }
    }
}
