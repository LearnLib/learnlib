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

package de.learnlib.mealy.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.words.Word;
import java.util.List;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class MealyContractOracle<I, O> implements MembershipOracle<I, Word<O>> {

    private MembershipOracle<I, Word<O>> nextOracle;
    
    public MealyContractOracle(MembershipOracle<I, Word<O>> oracle) {
        this.nextOracle = oracle;
    }
    
    
    
    @Override
    public void processQueries(List<Query<I, Word<O>>> queries) {
        // let the next oracle in chain process the queries
        nextOracle.processQueries(queries);
        
        // now, let's see if everything is okay
        for(int i = 0; i < queries.size(); ++i) {
            Query<I, Word<O>> query = queries.get(i);
            
            // somebody punched holes into our query batch
            if(query == null) {
                throw new RuntimeException("Query batch is incomplete: Query is null at index " + i);
            }
            
            // is there actual output?
            if(query.getOutput() == null) {
                throw new RuntimeException("Query batch is not answered: Output is null for Query with index " + i);
            }
            
            // does the output have fitting size?
            int expected = query.getOutput().size();
            int actual = query.getInput().size();
            if(actual != expected) {
                throw new RuntimeException("Query output in query batch with index " + i + " does not have fitting size: Expected size is " + expected + ", actual size is " + actual);
            }
            
            // Check that no element of the output is null
            for(O output : query.getOutput()) {
                if(output == null) {
                    throw new RuntimeException("Query batch is answered incompletely: Output contains null for Query at index " + i);
                }
            }
            
            
        }
        
    }
    
}
