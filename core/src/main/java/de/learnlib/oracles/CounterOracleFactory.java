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

package de.learnlib.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;
import de.learnlib.components.LLComponent;

/**
 *
 * @author falkhowar
 */
    @LLComponent(
            name="CounterOracle", 
            description="A simple oracle that counts queries", 
            type= MembershipOracle.class)
public class CounterOracleFactory<I,O> implements LLComponentFactory<CounterOracle<I,O>> {

    private MembershipOracle<I,O> next = null;
    
    private String name = "queries";
    
    @LLComponentParameter(name="next", description="oracle to actually use", required=true)
    public void setNext(MembershipOracle<I,O> next) {
        this.next = next;
    }
    
    @LLComponentParameter(name="name", description="name of the counter")
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public CounterOracle<I, O> instantiate() {
        if (next == null) {
            throw new IllegalStateException("next cannot be null");
        }
        return new CounterOracle<>(next, name);
    }
    
}
