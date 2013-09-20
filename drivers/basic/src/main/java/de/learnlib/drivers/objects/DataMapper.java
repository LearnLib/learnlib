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

import de.learnlib.drivers.api.ConcreteInput;
import de.learnlib.drivers.api.SULInput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic stateless data mapper for objects.
 * 
 * @author falkhowar
 */
public class DataMapper implements de.learnlib.drivers.api.DataMapper<Object> {

    private final Constructor initMethod;
    private final Object[] initParams;
    private final SULInput doNothing;
                
    private Object _this;    
    private boolean error;

    DataMapper(Constructor initMethod, Object[] initParams) {
        this.initMethod = initMethod;
        this.initParams = initParams;
        try {
            Method dn = this.getClass().getMethod("doNothing", new Class<?>[] {});
            this.doNothing = new SULInput("dn", dn, new HashMap<String, Integer>(), new Object[] {});            
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public void pre() {
        try {
            _this = initMethod.newInstance(initParams);
        } catch (InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        this.error = false;
    }

    @Override
    public void post() {
        _this = null;
    }

    @Override
    public ConcreteInput input(SULInput i) {
        Map<String, Object> params = new HashMap<>();
        
        if (this.error) {
            return new ConcreteInput(doNothing, params, null);
        }
        
        return new ConcreteInput(i, params, _this);
    }

    @Override
    public Object output(Object o) {        
        if (this.error) {
            return Unobserved.INSTANCE;
        }
            
        return new ReturnValue(o);
    }

    @Override
    public Object exception(Throwable t) {
        if (this.error) {
            return Unobserved.INSTANCE;
        }

        this.error = true;
        return new Error(t);
    }
    
    
    public static void doNothing() {
    }

}
