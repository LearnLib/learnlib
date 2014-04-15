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
import de.learnlib.mapper.ExecutableInputSUL;
import de.learnlib.mapper.Mappers;
import de.learnlib.mapper.api.ExecutableInput;
import de.learnlib.mapper.api.Mapper;

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

	private final SUL<AI, AO> sul;

    public TestDriver(Mapper<AI, AO, CI, CO> mapper) {
    	this.sul = Mappers.apply(mapper, new ExecutableInputSUL<CI,CO>());
    }    
    
    @Override
    public AO step(AI i) {
        return sul.step(i);
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
    }

}
