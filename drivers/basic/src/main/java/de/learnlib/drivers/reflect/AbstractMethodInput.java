/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.drivers.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;

/**
 * abstract method input, may have abstract parameters.
 * 
 * @author falkhowar
 */
public class AbstractMethodInput extends AbstractMutableNumericID {
    
    private final String name;
    
    private final Method method;
        
    private final Map<String, Integer> parameters;

    private final Object[] values;

    public AbstractMethodInput(String name, Method method, Map<String, Integer> parameters, Object[] values) {
        this.name = name;
        this.method = method;
        this.parameters = parameters;
        this.values = values;        
    }
        
    public String name() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.name() + Arrays.toString(this.parameters.keySet().toArray());
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
    
    public Class<?> getParameterType(String name) {
        int id = parameters.get(name);
        return this.method.getParameterTypes()[id];
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

    public Method getMethod() {
        return this.method;
    }
    
}
