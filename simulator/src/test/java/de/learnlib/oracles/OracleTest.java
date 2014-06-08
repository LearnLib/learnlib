/* Copyright (C) 2013-2014 TU Dortmund
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

import static de.learnlib.examples.dfa.ExamplePaulAndMary.IN_LOVES;
import static de.learnlib.examples.dfa.ExamplePaulAndMary.IN_MARY;
import static de.learnlib.examples.dfa.ExamplePaulAndMary.IN_PAUL;
import static de.learnlib.examples.dfa.ExamplePaulAndMary.constructMachine;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Maik Merten 
 */
public class OracleTest {

    
    
    @Test
    public void testDFASimulatorOracle() {
        
        DFA<?, Symbol> dfa = constructMachine();
        
        SimulatorOracle<Symbol,Boolean> dso = new SimulatorOracle<>(dfa);
        SafeOracle<Symbol,Boolean> oracle = new SafeOracle<>(dso);
        
        List<DefaultQuery<Symbol, Boolean>> queries = new ArrayList<>();
        
        DefaultQuery<Symbol, Boolean> q1 = new DefaultQuery<>(Word.fromSymbols(IN_PAUL, IN_LOVES, IN_MARY));
        DefaultQuery<Symbol, Boolean> q2 = new DefaultQuery<>(Word.fromSymbols(IN_MARY, IN_LOVES, IN_PAUL));
        queries.add(q1);
        queries.add(q2);
        
        Assert.assertEquals(queries.get(0).getInput().size(), 3);
        Assert.assertEquals(queries.get(1).getInput().size(), 3);
        
        oracle.processQueries(queries);
        
        
        // Paul loves Mary...
        Assert.assertEquals(queries.get(0).getOutput(), Boolean.TRUE);
        
        // ... but Mary does not love Paul :-(
        Assert.assertEquals(queries.get(1).getOutput(), Boolean.FALSE);
        
    }
    
    
}
