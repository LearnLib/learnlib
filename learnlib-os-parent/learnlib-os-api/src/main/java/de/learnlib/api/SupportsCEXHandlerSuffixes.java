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

package de.learnlib.api;

/**
 * This interface marks algorithms that can make use of counterexample
 * handling strategies implementing the {@link CEXHandlerSuffixes} 
 * inferface contract.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 * 
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public interface SupportsCEXHandlerSuffixes<I, O> {
    /**
     * Provide a counterexample handling strategy as defined by
     * {@link CEXHandlerSuffixes}.
     * 
     * @param handler The counterexamle handling strategy
     */
    public void setCEXHandlerSuffixes(CEXHandlerSuffixes<I, O> handler);
    
}
