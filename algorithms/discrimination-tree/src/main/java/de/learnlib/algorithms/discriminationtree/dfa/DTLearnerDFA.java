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
package de.learnlib.algorithms.discriminationtree.dfa;


import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.discriminationtree.BinaryDTree;
import de.learnlib.oracles.AbstractQuery;

/**
 * Algorithm for learning DFA using the Discrimination Tree algorithm.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol class
 */
public class DTLearnerDFA<I> extends AbstractDTLearner<DFA<?,I>, I, Boolean, Boolean, Void> implements DFALearner<I> {
	
	public static class BuilderDefaults extends AbstractDTLearner.BuilderDefaults {
		public static boolean epsilonRoot() { return true; }
	}
	
	private final HypothesisWrapperDFA<I> hypWrapper;

	/**
	 * Constructor.
	 * @param alphabet the input alphabet
	 * @param oracle the membership oracle
	 * @param suffixFinder method to use for analyzing counterexamples
	 * @param epsilonRoot whether or not to ensure the root of the discrimination tree is always labeled
	 * using the empty word.
	 */
	@GenerateBuilder
	public DTLearnerDFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle,
			LocalSuffixFinder<? super I, ? super Boolean> suffixFinder,
			boolean repeatedCounterexampleEvaluation,
			boolean epsilonRoot) {
		super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new BinaryDTree<I,HState<I,Boolean,Boolean,Void>>(oracle));
		this.hypWrapper = new HypothesisWrapperDFA<I>(hypothesis);
		if(epsilonRoot) {
			dtree.getRoot().split(Word.<I>epsilon(), false, true, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#getHypothesisModel()
	 */
	@Override
	public DFA<?, I> getHypothesisModel() {
		return hypWrapper;
	}
	
	

	@Override
	public GraphDOTHelper<HState<I, Boolean, Boolean, Void>, HTransition<I, Boolean, Boolean, Void>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<HState<I,Boolean,Boolean,Void>,HTransition<I,Boolean,Boolean,Void>>() {
			@Override
			public boolean getNodeProperties(
					HState<I, Boolean, Boolean, Void> node,
					Map<String, String> properties) {
				if (node.getProperty()) {
					properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
				}
				return true;
			}			
		};
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.discriminationtree.AbstractDTLearner#spQuery(de.learnlib.algorithms.discriminationtree.hypothesis.HState)
	 */
	@Override
	protected Query<I, Boolean> spQuery(final HState<I, Boolean, Boolean, Void> state) {
		return new AbstractQuery<I,Boolean>(state.getAccessSequence(), Word.<I>epsilon()) {
			@Override
			public void answer(Boolean val) {
				state.setProperty(val);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.discriminationtree.AbstractDTLearner#tpQuery(de.learnlib.algorithms.discriminationtree.hypothesis.HTransition)
	 */
	@Override
	protected Query<I, Boolean> tpQuery(
			HTransition<I, Boolean, Boolean, Void> transition) {
		return null;
	}
}
