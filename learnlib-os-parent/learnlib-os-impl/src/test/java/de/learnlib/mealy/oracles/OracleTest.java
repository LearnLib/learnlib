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

import de.learnlib.api.Query;
import de.ls5.automata.transout.impl.FastMealy;
import de.ls5.automata.transout.impl.FastMealyState;
import de.ls5.words.Alphabet;
import de.ls5.words.MutableWord;
import de.ls5.words.Word;
import de.ls5.words.impl.ArrayWord;
import de.ls5.words.impl.FastAlphabet;
import de.ls5.words.impl.SharedWord;
import de.ls5.words.impl.Symbol;
import de.ls5.words.util.Words;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class OracleTest {
    
    private final static Symbol in_a = new Symbol("a");
    private final static Symbol in_b = new Symbol("b");
    
    private final static String out_ok = "ok";
    private final static String out_error = "error";
    
    private FastMealy<Symbol, String> constructMachine() {
        Alphabet<Symbol> alpha = new FastAlphabet<Symbol>();
        alpha.add(in_a);
        alpha.add(in_b);
    
        
        FastMealy<Symbol, String> fm = new FastMealy<Symbol, String>(alpha);
        
        FastMealyState<String> s0 = fm.addInitialState(),
                s1 = fm.addState(),
                s2 = fm.addState();
        
        fm.addTransition(s0, in_a, s1, out_ok);
        fm.addTransition(s0, in_b, s0, out_error);
        
        fm.addTransition(s1, in_a, s2, out_ok);
        fm.addTransition(s1, in_b, s0, out_ok);
        
        fm.addTransition(s2, in_a, s2, out_error);
        fm.addTransition(s2, in_b, s1, out_ok);
        
        return fm;
    }
    
    @Test
    public void testMealySimulatorOracle() {
        
        FastMealy<Symbol,String> fm = constructMachine();
        
        MealySimulatorOracle<Symbol, String> mso = new MealySimulatorOracle<>(fm);
        MealyContractOracle<Symbol, String> oracle = new MealyContractOracle<>(mso);
        
        List<Query<Symbol, Word<String>>> queries = new ArrayList<>();
        
        
        Word<Symbol> prefix = Words.asWord(in_a);
        Word<Symbol> suffix = Words.asWord(in_a, in_a);
        
        
        Query<Symbol, Word<String>> query = new Query<>(prefix, suffix);
        queries.add(query);
        
        Assert.assertEquals(queries.get(0).getInput().size(), 3);
        
        oracle.processQueries(queries);
        
        Assert.assertEquals(queries.get(0).getOutput().size(), 2);
        Assert.assertEquals(queries.get(0).getOutput().get(0), out_ok);
        Assert.assertEquals(queries.get(0).getOutput().get(1), out_error);
        
        
    }
    
    
}
