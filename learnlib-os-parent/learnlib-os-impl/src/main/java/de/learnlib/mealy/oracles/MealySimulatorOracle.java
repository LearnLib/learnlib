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
import de.ls5.automata.transout.MealyMachine;
import de.ls5.words.MutableWord;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;
import java.util.List;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class MealySimulatorOracle<I, O> implements MembershipOracle<I, Word<O>> {
    
    private MealyMachine<Object, I, Object, O> mealy;
    
    
    public MealySimulatorOracle(MealyMachine<Object, I, Object, O> mealyMachine) {
        this.mealy = mealyMachine;
    }
    

    @Override
    public void processQueries(List<Query<I, Word<O>>> queries) {
        for(Query<I, Word<O>> q : queries) {
            Word<I> input = q.getInput();
            
            MutableWord<O> output = new ArrayWord<O>();
            mealy.trace(input, output);
           
            q.setOutput(output);
        }
    }
    
}
