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
package de.learnlib.algorithms.discriminationtree;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.discriminationtree.DTNode.SplitResult;
import de.learnlib.discriminationtree.DiscriminationTree;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public abstract class AbstractDTLearner<M extends SuffixOutput<I,D>, I, D, SP, TP> implements LearningAlgorithm<M, I, D> {
	
	public static class BuilderDefaults {
		public static <I,O> LocalSuffixFinder<? super I,? super O> suffixFinder() {
			return LocalSuffixFinders.RIVEST_SCHAPIRE;
		}
		public static boolean repeatedCounterexampleEvaluation() {
			return true;
		}
	}

	private final Alphabet<I> alphabet;
	private final MembershipOracle<I, D> oracle;
	private final LocalSuffixFinder<? super I, ? super D> suffixFinder;
	private final boolean repeatedCounterexampleEvaluation;
	protected final DiscriminationTree<I, D, HState<I,D,SP,TP>> dtree;
	protected final DTLearnerHypothesis<I, D, SP, TP> hypothesis;
	
	private final List<HState<I,D,SP,TP>> newStates = new ArrayList<>();
	private final List<HTransition<I,D,SP,TP>> newTransitions = new ArrayList<>();
	private final Deque<HTransition<I,D,SP,TP>> openTransitions = new ArrayDeque<>();

	protected AbstractDTLearner(Alphabet<I> alphabet, MembershipOracle<I, D> oracle,
			LocalSuffixFinder<? super I, ? super D> suffixFinder,
			boolean repeatedCounterexampleEvaluation,
			DiscriminationTree<I, D, HState<I,D,SP,TP>> dtree) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.suffixFinder = suffixFinder;
		this.hypothesis = new DTLearnerHypothesis<I,D,SP,TP>(alphabet);
		this.dtree = dtree;
		this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I,D> ceQuery) {
		if(!refineHypothesisSingle(ceQuery)) {
			return false;
		}
		if(repeatedCounterexampleEvaluation) {
			while(refineHypothesisSingle(ceQuery)) {}
		}
		return true;
	}
	
	

	@Override
	public void startLearning() {
		HState<I,D,SP,TP> init = hypothesis.getInitialState();
		DTNode<I, D, HState<I,D,SP,TP>> initDt = dtree.sift(init.getAccessSequence());
		if(initDt.getData() != null) {
			throw new IllegalStateException("Decision tree already contains data");
		}
		initDt.setData(init);
		init.setDTLeaf(initDt);
		initializeState(init);
		
		updateHypothesis();
	}
	
	public DiscriminationTree<I, D, HState<I,D,SP,TP>> getDiscriminationTree() {
		return dtree;
	}
	
	public DTLearnerHypothesis<I,D,SP,TP> getHypothesisDS() {
		return hypothesis;
	}
	
	protected boolean refineHypothesisSingle(DefaultQuery<I, D> ceQuery) {
		if(!MQUtil.isCounterexample(ceQuery, getHypothesisModel())) {
			return false;
		}
		
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, hypothesis, getHypothesisModel(),
				oracle);
		
		if(suffixIdx == -1) {
			throw new AssertionError("Suffix finder does not work correctly, found no suffix for valid counterexample");
		}
		
		Word<I> input = ceQuery.getInput();
		Word<I> oldStateAs = input.prefix(suffixIdx);
		HState<I,D,SP,TP> oldState = hypothesis.getState(oldStateAs);
		DTNode<I, D, HState<I,D,SP,TP>> oldDt = oldState.getDTLeaf();
		
		Word<I> newPredAs = input.prefix(suffixIdx - 1);
		HState<I,D,SP,TP> newPred = hypothesis.getState(newPredAs);
		I transSym = input.getSymbol(suffixIdx - 1);
		int transIdx = alphabet.getSymbolIndex(transSym);
		HTransition<I,D,SP,TP> trans = newPred.getTransition(transIdx);
		
		HState<I,D,SP,TP> newState = createState(trans);
		
		Word<I> suffix = input.subWord(suffixIdx);
		
		D oldOut = MQUtil.output(oracle, oldState.getAccessSequence(), suffix);
		D newOut = MQUtil.output(oracle, newState.getAccessSequence(), suffix);
		
		SplitResult<I,D,HState<I,D,SP,TP>> sr = oldDt.split(suffix, oldOut, newOut, newState);
		
		oldState.fetchNonTreeIncoming(openTransitions);
		
		oldState.setDTLeaf(sr.nodeOld);
		newState.setDTLeaf(sr.nodeNew);
		
		updateHypothesis();
		
		return true;
	}
	
	protected void initializeState(HState<I,D,SP,TP> newState) {
		newStates.add(newState);
		
		int size = alphabet.size();
		for(int i = 0; i < size; i++) {
			I sym = alphabet.getSymbol(i);
			HTransition<I,D,SP,TP> newTrans = new HTransition<I,D,SP,TP>(newState, sym, dtree.getRoot());
			newState.setTransition(i, newTrans);
			newTransitions.add(newTrans);
			openTransitions.offer(newTrans);
		}
	}
	
	protected HState<I,D,SP,TP> createState(HTransition<I,D,SP,TP> trans) {
		HState<I,D,SP,TP> newState = hypothesis.createState(trans);
		
		initializeState(newState);
		
		return newState;
	}
	
	protected void updateTransition(HTransition<I,D,SP,TP> trans) {
		if(trans.isTree()) {
			return;
		}
		
		DTNode<I,D,HState<I,D,SP,TP>> currDt = trans.getDT();
		currDt = dtree.sift(currDt, trans.getAccessSequence());
		trans.setDT(currDt);
		
		HState<I,D,SP,TP> state = currDt.getData();
		if(state == null) {
			state = createState(trans);
			currDt.setData(state);
			state.setDTLeaf(currDt);
		}
		else {
			state.addNonTreeIncoming(trans);
		}
	}
	
	protected void updateHypothesis() {
		HTransition<I,D,SP,TP> current;
		while((current = openTransitions.poll()) != null) {
			updateTransition(current);
		}
		
		List<Query<I,D>> queries = new ArrayList<>();
		for(HState<I,D,SP,TP> state : newStates) {
			Query<I,D> spQuery = spQuery(state);
			if(spQuery != null) {
				queries.add(spQuery);
			}
		}
		newStates.clear();
		
		for(HTransition<I,D,SP,TP> trans : newTransitions) {
			Query<I,D> tpQuery = tpQuery(trans);
			if(tpQuery != null) {
				queries.add(tpQuery);
			}
		}
		newTransitions.clear();
		
		oracle.processQueries(queries);
	}

	
	protected abstract Query<I,D> spQuery(HState<I,D,SP,TP> state);
	
	protected abstract Query<I,D> tpQuery(HTransition<I, D, SP, TP> transition);
}
