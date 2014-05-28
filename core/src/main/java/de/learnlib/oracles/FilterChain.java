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
import de.learnlib.api.Query;
import java.util.Collection;

/**
 * A chain of oracles.
 * 
 * @author falkhowar
 */
public class FilterChain<I,D> implements MembershipOracle<I, D> {
        
    private final MembershipOracle<I,D> oracle;

    @SafeVarargs
    public FilterChain(MembershipOracle<I,D> endpoint, Filter<I,D> ... chain) {
        if (chain.length < 1) {
            this.oracle = endpoint;
            return;
        }
        
        this.oracle = chain[0];        
        for (int i=0;i<chain.length-1;i++) {
            chain[i].setNext(chain[i+1]);
        }
        chain[chain.length-1].setNext(endpoint);
    }
    
    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        this.oracle.processQueries(queries);
    }
    
}
