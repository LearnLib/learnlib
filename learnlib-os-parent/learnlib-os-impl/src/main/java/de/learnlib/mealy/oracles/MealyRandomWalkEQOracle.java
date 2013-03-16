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

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.automata.base.fast.FastMutableDet;
import de.ls5.automata.transout.MealyMachine;
import de.ls5.words.Alphabet;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class MealyRandomWalkEQOracle<I, O> implements EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {

    private MembershipOracle<I, Word<O>> oracle;
    private int maxTests, minLength, maxLength;
    private long seed;
    
    public MealyRandomWalkEQOracle(MembershipOracle<I, Word<O>> mqOracle, int minLength, int maxLength, int maxTests, long seed) {
        this.oracle = mqOracle;
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.seed = seed;
    }
    
    
    @Override
    public Query<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis) {

        Alphabet<I> alpha = null;
        
        // FIXME: This is a horrible hack, there has to be another way to retrieve the input alphabet!
        if(hypothesis instanceof FastMutableDet) {
            FastMutableDet<?, I, ?, ?, ?> fmd = (FastMutableDet<?, I, ?, ?, ?>) hypothesis;
            alpha = fmd.getInputAlphabet();
        } else {
            throw new IllegalArgumentException("hypothesis does not implement interface to retrieve input alphabet");
        }
        
        Random r = new Random(seed);
        
        for(int i = 0; i < maxTests; ++i) {
            int length = minLength + r.nextInt(maxLength - minLength);
            
            Word<I> testtrace = new ArrayWord<>();
            for(int j = 0; j < length; ++j) {
                int symidx = r.nextInt(alpha.size() - 1);
                I sym = alpha.getSymbol(symidx);
                testtrace.set(j, sym);
            }
            
            List<Query<I, Word<O>>> queries = new ArrayList<>(1);
            Query<I, Word<O>> query = new Query<>(testtrace);
            queries.add(query);
            
            // query oracle
            oracle.processQueries(queries);
            Word<O> oracleoutput = query.getOutput();
            
            // trace hypothesis
            Word<O> traceoutput = new ArrayWord<>();
            hypothesis.trace(testtrace, traceoutput);
            
            // compare output of hypothesis and oracle
            if(!oracleoutput.equals(traceoutput)) {
                Query<I, Word<O>> cex = new Query<>(testtrace);
                cex.setOutput(oracleoutput);
                return cex;
            }
        }
        
        // no counterexample found
        return null;
    }
    
}
