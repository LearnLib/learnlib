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

import static net.automatalib.examples.dfa.ExamplePaulAndMary.constructMachine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
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
		
		// Empty set of suffixes => minimum compliant set
		List<Word<Symbol>> suffixes = Collections.emptyList();
		
		
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
