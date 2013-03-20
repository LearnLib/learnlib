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
package de.learnlib.lstar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

import org.junit.Test;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.ce.ClassicLStarCEXHandler;
import de.learnlib.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.lstar.ce.ShahbazCEXHandler;
import de.learnlib.lstar.ce.Suffix1by1CEXHandler;
import de.learnlib.lstar.closing.CloseFirstStrategy;
import de.learnlib.lstar.closing.CloseLexMinStrategy;
import de.learnlib.lstar.closing.CloseRandomStrategy;
import de.learnlib.lstar.closing.CloseShortestStrategy;
import de.learnlib.lstar.closing.ClosingStrategy;
import de.learnlib.lstar.dfa.ExtensibleLStarDFA;
import de.learnlib.oracles.SimulatorOracle;

public class LStarDFATest extends LearningTest {

	private final static Symbol in_paul = new Symbol("Paul");
    private final static Symbol in_loves = new Symbol("loves");
    private final static Symbol in_mary = new Symbol("Mary");

    public FastDFA<Symbol> constructMachine() {
        
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(in_paul);
        alpha.add(in_loves);
        alpha.add(in_mary);
        
        FastDFA<Symbol> dfa = new FastDFA<>(alpha);
        
        FastDFAState s0 = dfa.addInitialState(false),
                s1 = dfa.addState(false),
                s2 = dfa.addState(false),
                s3 = dfa.addState(true),
                s4 = dfa.addState(false);

        dfa.addTransition(s0, in_paul, s1);
        dfa.addTransition(s0, in_loves, s4);
        dfa.addTransition(s0, in_mary, s4);
        
        dfa.addTransition(s1, in_paul, s4);
        dfa.addTransition(s1, in_loves, s2);
        dfa.addTransition(s1, in_mary, s4);
        
        dfa.addTransition(s2, in_paul, s4);
        dfa.addTransition(s2, in_loves, s4);
        dfa.addTransition(s2, in_mary, s3);
        
        dfa.addTransition(s3, in_paul, s4);
        dfa.addTransition(s3, in_loves, s4);
        dfa.addTransition(s3, in_mary, s4);
        
        dfa.addTransition(s4, in_paul, s4);
        dfa.addTransition(s4, in_loves, s4);
        dfa.addTransition(s4, in_mary, s4);

        return dfa;
    }
    
	@Test
	public void testLStar() {
		FastDFA<Symbol> targetDFA = constructMachine();
		Alphabet<Symbol> alphabet = targetDFA.getInputAlphabet();
		
		MembershipOracle<Symbol, Boolean> dfaOracle = new SimulatorOracle<>(targetDFA);
		
		List<ObservationTableCEXHandler<Symbol,Boolean>> cexHandlers
			= Arrays.asList(ClassicLStarCEXHandler.<Symbol,Boolean>getInstance(),
			ShahbazCEXHandler.<Symbol,Boolean>getInstance(),
			Suffix1by1CEXHandler.<Symbol,Boolean>getInstance());
		
		List<ClosingStrategy<Symbol,Boolean>> closingStrategies
			= Arrays.asList(CloseFirstStrategy.<Symbol,Boolean>getInstance(),
					CloseLexMinStrategy.<Symbol,Boolean>getInstance(),
					CloseRandomStrategy.<Symbol,Boolean>getInstance(),
					CloseShortestStrategy.<Symbol,Boolean>getInstance());
		
		List<Word<Symbol>> suffixes = Collections.singletonList(Word.<Symbol>epsilon());
		
		
		for(ObservationTableCEXHandler<Symbol,Boolean> handler : cexHandlers) {
			for(ClosingStrategy<Symbol,Boolean> strategy : closingStrategies) {
				
				LearningAlgorithm<? extends DFA<?,Symbol>,Symbol,Boolean> learner 
					= new ExtensibleLStarDFA<>(alphabet, dfaOracle, suffixes,
							handler, strategy);
					
				testLearnModel(targetDFA, alphabet, learner);
			}
		}
	}
	

}
