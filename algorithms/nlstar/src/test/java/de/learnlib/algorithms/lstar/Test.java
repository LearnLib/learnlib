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

import java.io.Writer;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.algorithms.nlstar.NLStarLearner;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

public class Test {

	public static void main(String[] args) throws Exception {
		Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
		
		CompactNFA<Integer> nfa = new CompactNFA<Integer>(alphabet);
		
		int q0 = nfa.addIntInitialState(false);
		int q1 = nfa.addIntState(false);
		int q2 = nfa.addIntState(false);
		int q3 = nfa.addIntState(true);
		
		nfa.addTransition(q0, 0, q1);
		nfa.addTransition(q0, 0, q0);
		nfa.addTransition(q0, 1, q0);
		
		nfa.addTransition(q1, 0, q1);
		nfa.addTransition(q1, 0, q2);
		nfa.addTransition(q1, 1, q2);
		nfa.addTransition(q1, 0, q0);
		nfa.addTransition(q1, 1, q0);
		
		nfa.addTransition(q2, 0, q0);
		nfa.addTransition(q2, 1, q0);
		nfa.addTransition(q2, 0, q3);
		nfa.addTransition(q2, 1, q3);
		nfa.addTransition(q2, 0, q1);
		
		nfa.addTransition(q3, 0, q1);
		nfa.addTransition(q3, 0, q0);
		nfa.addTransition(q3, 1, q0);
		
		DFA<?, Integer> dfa = NFAs.determinize(nfa);
		
		DFASimulatorOracle<Integer> oracle = new DFASimulatorOracle<>(dfa);
		
		NLStarLearner<Integer> learner = new NLStarLearner<>(alphabet, oracle);
		
		learner.startLearning();
		
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(learner.getHypothesisModel(), alphabet, w);
		}
		
		boolean refined;
		do {
			DFA<?,Integer> detHypothesis = NFAs.determinize(learner.getHypothesisModel());
			
			Word<Integer> sepWord = Automata.findSeparatingWord(dfa, detHypothesis, alphabet);
			refined = false;
			if(sepWord != null) {
				System.err.println("Sepword: " + sepWord);
				DefaultQuery<Integer,Boolean> ce = MQUtil.query(oracle, sepWord);
				refined = learner.refineHypothesis(ce);
			}
		} while(refined);
		
		DefaultQuery<Integer, Boolean> ce = MQUtil.query(oracle, Word.fromSymbols(0, 1, 1));
		
		learner.refineHypothesis(ce);
		
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(learner.getHypothesisModel(), alphabet, w);
		}
	}
}
