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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.discriminationtree.BinaryDTree;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.discriminationtree.DTNode.SplitResult;
import de.learnlib.discriminationtree.DiscriminationTree.LCAInfo;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;


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
	
	private final Alphabet<I> alphabet;
	private final CompactDFA<I> hypothesis;
	private final MembershipOracle<I,Boolean> oracle;
	private final boolean repeatedCounterexampleEvaluation;
	
	private final BinaryDTree<I, StateInfo<I>> discriminationTree;
		
		
	private final List<StateInfo<I>> stateInfos
		= new ArrayList<>();

	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the membership oracle
	 */
	@GenerateBuilder
	public KearnsVaziraniDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle,
			boolean repeatedCounterexampleEvaluation) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactDFA<>(alphabet);
		this.discriminationTree = new BinaryDTree<>(oracle);
		this.oracle = oracle;
		this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
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
		
		
		int hypState = hypothesis.getInitialState();
		
		Iterator<I> symIt = input.iterator();
		
		I firstSym = symIt.next();
		hypState = hypothesis.getSuccessor(hypState, firstSym);
		
		Word<I> prefix = Word.fromLetter(firstSym);
		
		int i = 2;
		
		// Note that the empty word and all words of length 1
		// have *always* been sifted into the tree already
		while(symIt.hasNext()) {
			Word<I> nextPrefix = input.prefix(i);
			I sym = symIt.next();
			
			int nextHypState = hypothesis.getSuccessor(hypState, sym);
			
			StateInfo<I> siftState = sift(nextPrefix);
			if(siftState.id != nextHypState) {
				updateTree(hypState, sym, nextHypState, siftState, prefix);
				return true;
			}
			
			hypState = nextHypState;
			prefix = nextPrefix;
			i++;
		}

		
		return false;
	}
	
	
	private DTNode<I,Boolean,StateInfo<I>> dtNode(int state) {
		return stateInfos.get(state).dtNode;
	}
	
	private void updateTree(int hypState, I sym, int hypSucc, StateInfo<I> siftSucc, Word<I> prefix) {
		DTNode<I,Boolean,StateInfo<I>> hypStateLeaf = dtNode(hypState);
		
		DTNode<I,Boolean,StateInfo<I>> hypSuccLeaf = dtNode(hypSucc);
		DTNode<I,Boolean,StateInfo<I>> siftSuccLeaf = siftSucc.dtNode;
		
		LCAInfo<I, Boolean, StateInfo<I>> separatorInfo = discriminationTree.lcaInfo(hypSuccLeaf, siftSuccLeaf);
		Word<I> succDiscriminator = separatorInfo.leastCommonAncestor.getDiscriminator();
		
		Word<I> newDiscriminator = newDiscriminator(sym, succDiscriminator);
		
		StateInfo<I> hypStateInfo = stateInfos.get(hypState);
		
		boolean oldAccepting = hypothesis.isAccepting(hypState);
		TLongList oldIncoming = hypStateInfo.fetchIncoming();
		
		StateInfo<I> newStateInfo = createState(prefix, oldAccepting);
		
		SplitResult<I, Boolean, StateInfo<I>> split = hypStateLeaf.split(newDiscriminator, separatorInfo.subtree1Label, separatorInfo.subtree2Label, newStateInfo);
		
		hypStateInfo.dtNode = split.nodeOld;
		newStateInfo.dtNode = split.nodeNew;
		
		initState(newStateInfo);
		
		updateTransitions(oldIncoming, hypStateLeaf);
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
