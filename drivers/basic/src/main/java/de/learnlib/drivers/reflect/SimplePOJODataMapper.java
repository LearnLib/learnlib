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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.api.SULException;
import de.learnlib.mapper.AbstractMapper;

/**
 * Basic stateless data mapper for objects.
 * 
 * @author falkhowar
 */
public class SimplePOJODataMapper extends AbstractMapper<AbstractMethodInput, AbstractMethodOutput, ConcreteMethodInput, Object> {

    private final Constructor<?> initMethod;
    private final Object[] initParams;
                
    protected Object _this; 

    protected SimplePOJODataMapper(Constructor<?> initMethod, Object... initParams) {
        this.initMethod = initMethod;
        this.initParams = initParams;
    }
    
    @Override
    public void pre() {
        try {
            _this = initMethod.newInstance(initParams);
        } catch (InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void post() {
        _this = null;
    }
    

	@Override
	public ConcreteMethodInput mapInput(AbstractMethodInput abstractInput) {
		Map<String, Object> params = new HashMap<>();
        
        return new ConcreteMethodInput(abstractInput, params, _this);
	}

	@Override
	public AbstractMethodOutput mapOutput(Object concreteOutput) {
        return new ReturnValue(concreteOutput);
	}

	@Override
	public de.learnlib.mapper.api.Mapper.MappedException<? extends AbstractMethodOutput> mapUnwrappedException(
			RuntimeException exception) throws SULException, RuntimeException {
		return MappedException.repeatOutput(new Error(exception.getCause()), Unobserved.INSTANCE);
	}
	
	

}
