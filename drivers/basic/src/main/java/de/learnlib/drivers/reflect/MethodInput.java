/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.drivers.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * abstract method input, may have abstract parameters.
 *
 * @author falkhowar
 */
public class MethodInput {

    private final String name;

    private final Method method;

    private final Map<String, Integer> parameters;

    private final Object[] values;

    public MethodInput(String name, Method method, Map<String, Integer> parameters, Object[] values) {
        this.name = name;
        this.method = method;
        this.parameters = parameters;
        this.values = values;
    }

    @Override
    public String toString() {
        return this.name() + Arrays.toString(this.parameters.keySet().toArray());
    }

    public String name() {
        return this.name;
    }

    public String getCall() {
        Map<String, Object> names = new HashMap<>();
        for (String p : getParameterNames()) {
            names.put(p, p);
        }
        return this.method.getName() + Arrays.toString(getParameters(names));
    }

    public Collection<String> getParameterNames() {
        return this.parameters.keySet();
    }

    public Object[] getParameters(Map<String, Object> fill) {
        Object[] ret = new Object[this.values.length];
        System.arraycopy(this.values, 0, ret, 0, this.values.length);
        for (Entry<String, Object> e : fill.entrySet()) {
            Integer idx = this.parameters.get(e.getKey());
            ret[idx] = e.getValue();
        }
        return ret;
    }

    public Class<?> getParameterType(String name) {
        int id = parameters.get(name);
        return this.method.getParameterTypes()[id];
    }

    public Method getMethod() {
        return this.method;
    }

}
