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

/**
 * A data mapper transforms abstract inputs into concrete inputs and
 * concrete outputs into abstract outputs. A mapper can be stateful.
 * Mappers are used by test drivers.
 * 
 * @author falkhowar
 * 
 * @param <AI> abstract input type
 * @param <CI> concrete input type
 * @param <AO> abstract output type
 * @param <CO> concrete output type
 */
public interface DataMapper<AI, AO, CI extends ExecutableInput<CO>, CO> {

    /**
     * called by a test driver before execution of a test case
     */
    public void pre();
    
    /**
     * called by a test driver after execution of a test case
     */
    public void post();

    /**
     * called to transform an abstract into a concrete input
     */
    public CI input(AI i);
    
    /**
     * called to transform a concrete output into an abstract one
     */
    public AO output(CO o);
    
    /**
     * called to transform a concrete error into an abstract one
     */
    public AO exception(SULException t);
    
}
