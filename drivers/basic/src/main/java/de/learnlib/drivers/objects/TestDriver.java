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
package de.learnlib.drivers.objects;

import de.learnlib.drivers.api.SULInput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author falkhowar
 */
public class TestDriver extends de.learnlib.drivers.api.TestDriver<Object> {
 
    private final Collection<SULInput> inputs;
    
    public TestDriver(Constructor c, Object ... cParams) {
        super(new DataMapper(c, cParams));
        this.inputs = new LinkedList<>();
    }
    
    public SULInput addInput(String name, Method m, Object ... params) {
        SULInput i = new SULInput(name, m, new HashMap<String, Integer>(), params);
        inputs.add(i);
        return i;
    }

    /**
     * @return the inputs
     */
    public Collection<SULInput> getInputs() {
        return inputs;
    }
    
}
