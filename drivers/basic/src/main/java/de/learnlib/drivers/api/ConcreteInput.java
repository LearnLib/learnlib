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
package de.learnlib.drivers.api;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * A concrete inputs contains the information for one specific method call.
 * 
 * @author falkhowar
 */
final public class ConcreteInput {
    
    /**
     * corresponding abstract input
     */
    private final SULInput input;
    
    /**
     * parameter values
     */
    private final Map<String, Object> values;
    
    /**
     * invocation target
     */
    private final Object target;

    public ConcreteInput(SULInput input, Map<String, Object> values, Object target) {
        this.input = input;
        this.values = values;
        this.target = target;
    }    

    public Method getMethod() {
        return this.input.getMethod();
    }

    public Object[] getParameterValues() {
        return this.input.getParameters(values);
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return target + "." + this.input.getMethod().getName() + Arrays.toString(getParameterValues());
    }
    
}
