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

import java.util.Collections;
import java.util.Random;

import net.automatalib.automata.concepts.OutputAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.MutableWord;
import net.automatalib.words.impl.ArrayWord;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class RandomWordsEQOracle<I,O, A extends OutputAutomaton<?,I,?,O>> implements EquivalenceOracle<A,I,O> {
	
    private MembershipOracle<I, O> oracle;
    private int maxTests, minLength, maxLength;
    private final Random random;
    
    public RandomWordsEQOracle(MembershipOracle<I, O> mqOracle, int minLength, int maxLength, int maxTests, Random random) {
        this.oracle = mqOracle;
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.random = random;
    }
    
    
    @Override
    public Query<I, O> findCounterExample(A hypothesis, Alphabet<I> alpha) {

        for(int i = 0; i < maxTests; ++i) {
            int length = minLength + random.nextInt((maxLength - minLength) + 1);
            
            MutableWord<I> testtrace = new ArrayWord<>();
            for(int j = 0; j < length; ++j) {
                int symidx = random.nextInt(alpha.size());
                I sym = alpha.getSymbol(symidx);
                testtrace.add(sym);
            }
            
            Query<I, O> query = new Query<>(testtrace);
            
            // query oracle
            oracle.processQueries(Collections.singletonList(query));
            O oracleoutput = query.getOutput();
            
            // trace hypothesis
            O hypOutput = hypothesis.computeOutput(testtrace);
            
            // compare output of hypothesis and oracle
            if(!oracleoutput.equals(hypOutput))
                return query;
        }
        
        // no counterexample found
        return null;
    }
    
}
