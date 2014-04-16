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
package de.learnlib.mapper.api;

import de.learnlib.api.SULException;


/**
 * An executable input is a concrete input produced by a data mapper
 * and can be executed directly. 
 * 
 * @author falkhowar
 * 
 * @param <CO> concrete output 
 */
public interface ExecutableInput<CO> {
   
    /**
     * executes the input.
     * 
     * @return concrete output for this input 
     */
    public CO execute() throws SULException, Exception;
    
}
