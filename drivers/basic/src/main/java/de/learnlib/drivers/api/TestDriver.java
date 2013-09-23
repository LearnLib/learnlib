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

import de.learnlib.api.SUL;

/**
 * A test driver executes
 * 
 * 
 * @author falkhowar
 * 
 * @param <AI> abstract input type
 * @param <CI> concrete input type
 * @param <AO> abstract output type
 * @param <CO> concrete output type
 */
public class TestDriver<AI, AO, CI extends ExecutableInput<CO>, CO> implements SUL<AI, AO> {

    private final DataMapper<AI, AO, CI, CO> mapper;

    public TestDriver(DataMapper<AI, AO, CI, CO> mapper) {
        this.mapper = mapper;
    }    
    
    @Override
    public AO step(AI i) {
        ExecutableInput<CO> ci = this.mapper.input(i);
        try {
            CO out = ci.execute();
            return this.mapper.output(out);
        } 
        catch (SULException e) {
            return this.mapper.exception(e);
        }        
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
