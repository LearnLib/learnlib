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

import de.learnlib.api.Filter;
import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;

/**
 * factory for filter chains.
 * 
 * @author falkhowar
 */
@LLComponent(
        name="FilterChain",
        description="Chain of filters with oracle as an endpoint",
        type=MembershipOracle.class)
public class FilterChainFactory<I,O> implements LLComponentFactory<MembershipOracle<I,O>> {

	@SuppressWarnings("unchecked")
    private Filter<I,O>[] chain = new Filter[0]; 
    
    private MembershipOracle<I,O> endpoint = null;
    
    @LLComponentParameter(name="chain", description="chain of filters")
    public void setChain(Filter<I,O>[] chain) {
        this.chain = chain;
    }
    
    @LLComponentParameter(name="endpoint", description="endpoint of this chain", required=true)
    public void setEndpoint(MembershipOracle<I,O> endpoint) {
        this.endpoint = endpoint;
    }    
    
    @Override
    public MembershipOracle<I, O> instantiate() {
        if (endpoint == null) {
            throw new IllegalStateException("endpoint cannot be null");
        }
        if (chain == null) {
            throw new IllegalStateException("chain cannot be null");
        }
        return new FilterChain<>(endpoint, chain);
    }
   
    
    
}
