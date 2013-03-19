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

import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ArrayWord;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class MealySimulatorOracle<I, O> implements MembershipOracle<I, Word<O>> {
    
    private MealyMachine<?, I, ?, O> mealy;
    
    
    public MealySimulatorOracle(MealyMachine<?, I, ?, O> mealyMachine) {
        this.mealy = mealyMachine;
    }
    

    @Override
    public void processQueries(List<Query<I, Word<O>>> queries) {
        for(Query<I, Word<O>> q : queries)
        		processQuery(mealy, q);
    }
    
    /*
     * Private static method used to bind wildcard type parameters
     */
    private static final <S,I,T,O> void processQuery(MealyMachine<S,I,T,O> mealy, Query<I,Word<O>> query) {
    	Word<I> prefix = query.getPrefix();
    	S prefixState = mealy.getState(prefix);
    	Word<O> output = new ArrayWord<>();
    	Word<I> suffix = query.getSuffix();
    	mealy.trace(prefixState, suffix, output);
    	
    	query.setOutput(output);
    }
    
}
