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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.learnlib.drivers.api.TestDriver;
import net.automatalib.commons.util.ReflectUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;

/**
 * Simple test driver for plain java objects. Uses a very simple data mapper
 * without state or storage. Inputs cannot have abstract parameters.
 * 
 * @author falkhowar
 */
public final class SimplePOJOTestDriver extends 
        TestDriver<AbstractMethodInput, AbstractMethodOutput, ConcreteMethodInput, Object> {
 
    private final FastAlphabet<AbstractMethodInput> inputs = new FastAlphabet<>();
    
    private final Class<?> instanceClass;
    
    public SimplePOJOTestDriver(Constructor<?> c, Object ... cParams) {
        super(new SimplePOJODataMapper(c, cParams));
        this.instanceClass = c.getDeclaringClass();
    }
    
    public AbstractMethodInput addInput(String name, String methodName, Object... params) {
    	Method m = ReflectUtil.findMatchingMethod(instanceClass, methodName, params);
    	if (m == null) {
    		throw new IllegalArgumentException();
    	}
    	return addInput(name, m, params);
    }
    
    public AbstractMethodInput addInput(String name, Method m, Object ... params) {
        AbstractMethodInput i = new AbstractMethodInput(name, m, new HashMap<String, Integer>(), params);
        inputs.add(i);
        return i;
    }

    /**
     * @return the inputs
     */
    public Alphabet<AbstractMethodInput> getInputs() {
        return this.inputs;
    }
    
}
