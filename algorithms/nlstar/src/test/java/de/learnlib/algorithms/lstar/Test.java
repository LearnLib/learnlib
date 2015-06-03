/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
