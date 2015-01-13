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
package de.learnlib.algorithms.ttt.dfa;

import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.ttt.base.BaseTTTLearner;
import de.learnlib.algorithms.ttt.base.DTNode;
import de.learnlib.algorithms.ttt.base.TTTHypothesis.TTTEdge;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;


public class TTTLearnerDFA<I> extends BaseTTTLearner<DFA<?,I>,I,Boolean> implements DFALearner<I> {

	@GenerateBuilder(defaults = BaseTTTLearner.BuilderDefaults.class)
	public TTTLearnerDFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle,
			LocalSuffixFinder<? super I, ? super Boolean> suffixFinder) {
		super(alphabet, oracle, new TTTHypothesisDFA<>(alphabet), suffixFinder);
		
		split(dtree.getRoot(), Word.<I>epsilon(), false, true);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public TTTHypothesisDFA<I> getHypothesisDS() {
		return (TTTHypothesisDFA<I>) hypothesis;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DFA<?, I> getHypothesisModel() {
		return (TTTHypothesisDFA<I>) hypothesis;
	}
	
	@Override
	protected void initializeState(TTTState<I,Boolean> state) {
		super.initializeState(state);
		
		TTTStateDFA<I> dfaState = (TTTStateDFA<I>)state;
		dfaState.accepting = dtree.getRoot().subtreeLabel(dfaState.getDTLeaf());
	}
	

	@Override
	protected Boolean predictSuccOutcome(TTTTransition<I, Boolean> trans,
			DTNode<I, Boolean> succSeparator) {
		return succSeparator.subtreeLabel(trans.getDTTarget());
	}

	@Override
	protected Boolean computeHypothesisOutput(TTTState<I, Boolean> state,
			Iterable<? extends I> suffix) {
		TTTStateDFA<I> endState = (TTTStateDFA<I>) getState(state, suffix);
		return endState.accepting;
	}


	@Override
	public GraphDOTHelper<TTTState<I,Boolean>, TTTEdge<I, Boolean>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<TTTState<I,Boolean>,TTTEdge<I,Boolean>>() {
			@Override
			public boolean getNodeProperties(TTTState<I, Boolean> node,
					Map<String, String> properties) {
				TTTStateDFA<I> dfaState = (TTTStateDFA<I>) node;
				if (dfaState.isAccepting()) {
					properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
				}
				return true;
			}
		};
	}
}
