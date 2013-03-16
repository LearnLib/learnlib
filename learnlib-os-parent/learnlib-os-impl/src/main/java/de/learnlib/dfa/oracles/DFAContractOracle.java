/* Copyright (C) 2013 TU Dortmund
   This file is part of LearnLib 

   LearnLib is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License version 3.0 as published by the Free Software Foundation.

   LearnLib is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with LearnLib; if not, see
   <http://www.gnu.de/documents/lgpl.en.html>.  */

package de.learnlib.dfa.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import java.util.List;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class DFAContractOracle<I> implements MembershipOracle<I, Boolean> {
    
    private MembershipOracle<I, Boolean> nextOracle;
    
    public DFAContractOracle(MembershipOracle<I, Boolean> nextOracle) {
        this.nextOracle = nextOracle;
    }
    

    @Override
    public void processQueries(List<Query<I, Boolean>> queries) {
        // let the next oracle in chain process the queries
        nextOracle.processQueries(queries);
        
        // now, let's see if everything is okay
        for(int i = 0; i < queries.size(); ++i) {
            Query<I, Boolean> query = queries.get(i);
            
            // somebody punched holes into our query batch
            if(query == null) {
                throw new RuntimeException("Query batch is incomplete: Query is null at index " + i);
            }
            
            // is there actual output?
            if(query.getOutput() == null) {
                throw new RuntimeException("Query batch is not answered: Output is null for Query with index " + i);
            }

        }
        
    }
    
}
