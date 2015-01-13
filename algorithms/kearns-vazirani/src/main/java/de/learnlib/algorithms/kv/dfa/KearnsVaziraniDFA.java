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
package de.learnlib.algorithms.kv.dfa;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.impl.BaseAbstractCounterexample;
import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.discriminationtree.BinaryDTree;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.discriminationtree.DTNode.SplitResult;
import de.learnlib.discriminationtree.DiscriminationTree.LCAInfo;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;


/**
 * The Kearns/Vazirani algorithm for learning DFA, as described in the book
 * "An Introduction to Computational Learning Theory" by Michael Kearns
 * and Umesh Vazirani.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class KearnsVaziraniDFA<I> implements DFALearner<I> {
	
	static final class BuilderDefaults {
		public static boolean repeatedCounterexampleEvaluation() {
			return true;
		}
		public static AcexAnalyzer counterexampleAnalyzer() {
			return AcexAnalyzers.LINEAR_FWD;
		}
	}
	
	private static final TLongList EMPTY_LONG_LIST = new TLongArrayList(0);
	
	/**
	 * The information associated with a state: it's access sequence (or access string),
	 * and the list of incoming transitions.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class StateInfo<I> {
		public final int id;
		public final Word<I> accessSequence;
		private DTNode<I, Boolean, StateInfo<I>> dtNode;
		private TLongList incoming;
		
		public StateInfo(int id, Word<I> accessSequence) {
			this.id = id;
			this.accessSequence = accessSequence.trimmed();
		}
		
		public void addIncoming(int sourceState, int transIdx) {
			long encodedTrans = ((long)sourceState << 32L) | transIdx;
			if(incoming == null) {
				incoming = new TLongArrayList();
			}
			incoming.add(encodedTrans);
		}
		
		public TLongList fetchIncoming() {
			if(incoming == null || incoming.isEmpty()) {
				return EMPTY_LONG_LIST;
			}
			TLongList result = incoming;
			this.incoming = null;
			return result;
		}
		
	}
	
	private class KVAbstractCounterexample extends BaseAbstractCounterexample {
		
		private final Word<I> ceWord;
		private final MembershipOracle<I, Boolean> oracle;
		private final StateInfo<I>[] states;
		private final LCAInfo<I,Boolean,StateInfo<I>>[] lcas;

		@SuppressWarnings("unchecked")
		public KVAbstractCounterexample(Word<I> ceWord, boolean output, MembershipOracle<I, Boolean> oracle) {
			super(ceWord.length());
			this.ceWord = ceWord;
			this.oracle = oracle;
			
			int m = ceWord.length();
			this.states = new StateInfo[m + 1];
			this.lcas = new LCAInfo[m + 1];
			int i = 0;
			
			int currState = hypothesis.getIntInitialState();
			states[i++] = stateInfos.get(currState);
			for(I sym : ceWord) {
				currState = hypothesis.getSuccessor(currState, sym);
				states[i++] = stateInfos.get(currState);
			}
			
			// Acceptance/Non-acceptance separates hypothesis from target
			lcas[m] = new LCAInfo<>(discriminationTree.getRoot(), !output, output);
		}
		
		public StateInfo<I> getStateInfo(int idx) {
			return states[idx];
		}
		
		public LCAInfo<I,Boolean,StateInfo<I>> getLCA(int idx) {
			return lcas[idx];
		}

		@Override
		protected int computeEffect(int index) {
			Word<I> prefix = ceWord.prefix(index);
			StateInfo<I> info = states[index];
			
			// Save the expected outcomes on the path from the leaf representing the state
			// to the root on a stack
			DTNode<I, Boolean, StateInfo<I>> node = info.dtNode;
			Deque<Boolean> expect = new ArrayDeque<>();
			while(!node.isRoot()) {
				expect.push(node.getParentOutcome());
				node = node.getParent();
			}
			
			DTNode<I,Boolean,StateInfo<I>> currNode = discriminationTree.getRoot();
			
			while(!expect.isEmpty()) {
				Word<I> suffix = currNode.getDiscriminator();
				boolean out = MQUtil.output(oracle, prefix, suffix);
				if(out != expect.pop()) {
					lcas[index] = new LCAInfo<>(currNode, !out, out);
					return 1;
				}
				currNode = currNode.child(out);
			}
			
			assert currNode.isLeaf() && expect.isEmpty();
			return 0;
		}
	}
	
	private final Alphabet<I> alphabet;
	private final CompactDFA<I> hypothesis;
	private final MembershipOracle<I,Boolean> oracle;
	private final boolean repeatedCounterexampleEvaluation;
	
	private final BinaryDTree<I, StateInfo<I>> discriminationTree;
		
		
	private final List<StateInfo<I>> stateInfos
		= new ArrayList<>();
	
	private final AcexAnalyzer ceAnalyzer;

	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the membership oracle
	 */
	@GenerateBuilder
	public KearnsVaziraniDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle,
			boolean repeatedCounterexampleEvaluation,
			AcexAnalyzer counterexampleAnalyzer) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactDFA<>(alphabet);
		this.discriminationTree = new BinaryDTree<>(oracle);
		this.oracle = oracle;
		this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
		this.ceAnalyzer = counterexampleAnalyzer;
	}
	
	@Override
	public void startLearning() {
		initialize();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		if(hypothesis.size() == 0) {
			throw new IllegalStateException("Not initialized");
		}
		Word<I> input = ceQuery.getInput();
		boolean output = ceQuery.getOutput();
		if(!refineHypothesisSingle(input, output)) {
			return false;
		}
		if(repeatedCounterexampleEvaluation) {
			while(refineHypothesisSingle(input, output)) {}
		}
		return true;
	}
	
	
	private boolean refineHypothesisSingle(Word<I> input, boolean output) {
		int inputLen = input.length();
		
		if(inputLen < 2) {
			return false;
		}
		
		if(hypothesis.accepts(input) == output) {
			return false;
		}
		
		KVAbstractCounterexample acex = new KVAbstractCounterexample(input, output, oracle);
		int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 1);
		
		Word<I> prefix = input.prefix(idx);
		StateInfo<I> srcStateInfo = acex.getStateInfo(idx);
		I sym = input.getSymbol(idx);
		LCAInfo<I,Boolean,StateInfo<I>> lca = acex.getLCA(idx+1);
		assert lca != null;
		
		splitState(srcStateInfo, prefix, sym, lca);
		
		return true;
	}
	
	private void splitState(StateInfo<I> stateInfo, Word<I> newPrefix, I sym, LCAInfo<I,Boolean,StateInfo<I>> separatorInfo) {
		int state = stateInfo.id;
		boolean oldAccepting = hypothesis.isAccepting(state);
		TLongList oldIncoming = stateInfo.fetchIncoming();
		
		StateInfo<I> newStateInfo = createState(newPrefix, oldAccepting);
		
		DTNode<I, Boolean, StateInfo<I>> stateLeaf = stateInfo.dtNode;
		
		DTNode<I, Boolean, StateInfo<I>> separator = separatorInfo.leastCommonAncestor;
		Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
		
		SplitResult<I, Boolean, StateInfo<I>> split = stateLeaf.split(newDiscriminator, separatorInfo.subtree1Label, separatorInfo.subtree2Label, newStateInfo);
		
		stateInfo.dtNode = split.nodeOld;
		newStateInfo.dtNode = split.nodeNew;
		
		initState(newStateInfo);
		
		updateTransitions(oldIncoming, stateLeaf);
	}
	
	
	private void updateTransitions(TLongList transList, DTNode<I, Boolean, StateInfo<I>> oldDtTarget) {
		int numTrans = transList.size();
		for(int i = 0; i < numTrans; i++) {
			long encodedTrans = transList.get(i);
			
			int sourceState = (int)(encodedTrans >> 32L);
			int transIdx = (int)(encodedTrans & 0xffffffff);
			
			StateInfo<I> sourceInfo = stateInfos.get(sourceState);
			I symbol = alphabet.getSymbol(transIdx);
			
			
			StateInfo<I> succ = sift(oldDtTarget, sourceInfo.accessSequence.append(symbol));
			setTransition(sourceState, transIdx, succ);
		}
	}
	
	private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
		return succDiscriminator.prepend(symbol);
	}

	@Override
	public DFA<?, I> getHypothesisModel() {
		if(hypothesis.size() == 0) {
			throw new IllegalStateException("Not started");
		}
		return hypothesis;
	}


	
	private void initialize() {
		boolean initAccepting = MQUtil.output(oracle, Word.<I>epsilon()).booleanValue();
		StateInfo<I> initStateInfo = createInitialState(initAccepting);
		
		DTNode<I, Boolean, StateInfo<I>> root = discriminationTree.getRoot();
		root.setData(initStateInfo);
		initStateInfo.dtNode = root.split(Word.<I>epsilon(), initAccepting, !initAccepting, null).nodeOld;
		
		
		initState(initStateInfo);
	}
	
	private StateInfo<I> createInitialState(boolean accepting) {
		int state = hypothesis.addIntInitialState(accepting);
		StateInfo<I> si = new StateInfo<>(state, Word.<I>epsilon());
		assert stateInfos.size() == state;
		stateInfos.add(si);
		
		return si;
	}
	
	private StateInfo<I> createState(Word<I> accessSequence, boolean accepting) {
		int state = hypothesis.addIntState(accepting);
		StateInfo<I> si = new StateInfo<>(state, accessSequence);
		assert stateInfos.size() == state;
		stateInfos.add(si);
		
		return si;
	}
	
	private void initState(StateInfo<I> stateInfo) {
		int alphabetSize = alphabet.size();
		
		int state = stateInfo.id;
		Word<I> accessSequence = stateInfo.accessSequence;
		
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			Word<I> transAs = accessSequence.append(sym);
			
			StateInfo<I> succ = sift(transAs);
			setTransition(state, i, succ);
		}
	}
	
	private void setTransition(int state, int symIdx, StateInfo<I> succInfo) {
		succInfo.addIncoming(state, symIdx);
		hypothesis.setTransition(state, symIdx, succInfo.id);
	}
	
	private StateInfo<I> sift(Word<I> prefix) {
		return sift(discriminationTree.getRoot(), prefix);
	}
	
	private StateInfo<I> sift(DTNode<I,Boolean,StateInfo<I>> start, Word<I> prefix) {
		DTNode<I,Boolean,StateInfo<I>> leaf = discriminationTree.sift(start, prefix);
		
		StateInfo<I> succStateInfo = leaf.getData();
		if(succStateInfo == null) {
			// Special case: this is the *first* state of a different
			// acceptance than the initial state
			boolean initAccepting = hypothesis.isAccepting(hypothesis.getIntInitialState());
			succStateInfo = createState(prefix, !initAccepting);
			leaf.setData(succStateInfo);
			succStateInfo.dtNode = leaf;
			
			initState(succStateInfo);
		}
		
		return succStateInfo;
	}

}
