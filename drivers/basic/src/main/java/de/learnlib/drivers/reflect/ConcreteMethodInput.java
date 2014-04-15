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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import de.learnlib.api.SULException;
import de.learnlib.mapper.api.ExecutableInput;

/**
 * A concrete inputs contains the information for one specific method call.
 * 
 * @author falkhowar
 */
public class ConcreteMethodInput implements ExecutableInput<Object> {
    
    /**
     * corresponding abstract input
     */
    private final AbstractMethodInput input;
    
    /**
     * parameter values
     */
    private final Map<String, Object> values;
    
    /**
     * invocation target
     */
    private final Object target;

    public ConcreteMethodInput(AbstractMethodInput input, Map<String, Object> values, Object target) {
        this.input = input;
        this.values = values;
        this.target = target;
    }    

    private Object[] getParameterValues() {
        return this.input.getParameters(values);
    }

    @Override
    public String toString() {
        return target + "." + this.input.getMethod().getName() + Arrays.toString(getParameterValues());
    }
  
    @Override
    public Object execute() throws Exception {
        Object out = null;
        try {                        
            Object ret = this.input.getMethod().invoke(this.target, getParameterValues());
            if (this.input.getMethod().getReturnType().equals(Void.TYPE)) {
                out = Void.TYPE;
            } else {            
                out = ret;
            }
        } 
        catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } 
        catch (InvocationTargetException e) {
        	throw new SULException(e.getCause());
        }        
        return out;                
    }
    
}
