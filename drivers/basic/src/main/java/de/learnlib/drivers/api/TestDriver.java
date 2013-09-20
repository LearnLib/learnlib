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

import de.learnlib.api.Query;
import de.learnlib.api.SUL;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.automatalib.words.Word;

/**
 * A test driver executes
 * 
 * 
 * @author falkhowar
 * 
 * @param <O> abstract output type
 */
public class TestDriver<O> implements SUL<SULInput, O> {

    private final DataMapper<O> mapper;

    public TestDriver(DataMapper<O> mapper) {
        this.mapper = mapper;
    }    
    
    @Override
    public O step(SULInput i) {
        ConcreteInput ci = this.mapper.input(i);
        O out = null;
        try {
            Object ret = ci.getMethod().invoke(ci.getTarget(), ci.getParameterValues());
            if (ci.getMethod().getReturnType().equals(Void.TYPE)) {
                out = this.mapper.output(Void.TYPE);
            } else {            
                out = this.mapper.output(ret);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // catch exceptions specific to invocation
            // TODO: check that the exception really originated here.
            throw new RuntimeException(e);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = t.getCause();
            }
            out = this.mapper.exception(t);
        }        
        return out;
    }

    @Override
    public void pre() {
        mapper.pre();
    }

    @Override
    public void post() {
        mapper.post();
    }

}
