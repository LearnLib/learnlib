/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.algorithms.lstar;

import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.algorithms.nlstar.NLStarLearner;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.cache.dfa.DFACaches;
import de.learnlib.oracles.CounterOracle.DFACounterOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

public class RandomTest {
	
	public static void main(String[] args) {
		
		Alphabet<Integer> alphabet = Alphabets.integers(0, 9);
		
		CompactDFA<Integer> dfa
			= RandomAutomata.randomDFA(new Random(), 50, alphabet);
		
		System.err.println("Random DFA has " + dfa.size() + " states");
		
		DFASimulatorOracle<Integer> oracle = new DFASimulatorOracle<>(dfa);
		
		DFACounterOracle<Integer> mqCounter = new DFACounterOracle<>(oracle, "MQs");
		
		DFACacheOracle<Integer> cache = DFACaches.createTreeCache(alphabet, mqCounter);
		
		NLStarLearner<Integer> nlstar = new NLStarLearner<>(alphabet, cache);
		
		nlstar.startLearning();
		
		boolean refined = false;
		do {
			CompactDFA<Integer> detHyp = NFAs.determinize(nlstar.getHypothesisModel());
			
			Word<Integer> sepWord = Automata.findSeparatingWord(dfa, detHyp, alphabet);
			
			refined = false;
			if(sepWord != null) {
				DefaultQuery<Integer,Boolean> ce = MQUtil.query(cache, sepWord);
				refined = nlstar.refineHypothesis(ce);
			}
		} while(refined);
		
		System.err.println("Final model has " + nlstar.getHypothesisModel().size() + " states");
		System.err.println("Required " + mqCounter.getCount() + " queries");
		
		DFACounterOracle<Integer> mqCounter2 = new DFACounterOracle<>(oracle, "MQs");
		
		DFACacheOracle<Integer> cache2 = DFACaches.createTreeCache(alphabet, mqCounter2);
		
		ExtensibleLStarDFA<Integer> lstar  = new ExtensibleLStarDFABuilder<Integer>()
				.withAlphabet(alphabet)
				.withOracle(cache2)
				.withCexHandler(ObservationTableCEXHandlers.MALER_PNUELI)
				.create();
		
		lstar.startLearning();
		
		refined = false;
		do {
			DFA<?,Integer> detHyp = lstar.getHypothesisModel();
			
			Word<Integer> sepWord = Automata.findSeparatingWord(dfa, detHyp, alphabet);
			
			refined = false;
			if(sepWord != null) {
				DefaultQuery<Integer,Boolean> ce = MQUtil.query(cache2, sepWord);
				refined = lstar.refineHypothesis(ce);
			}
		} while(refined);
		
		System.err.println("Final model has " + lstar.getHypothesisModel().size() + " states");
		System.err.println("Required " + mqCounter2.getCount() + " queries");
	}
}
