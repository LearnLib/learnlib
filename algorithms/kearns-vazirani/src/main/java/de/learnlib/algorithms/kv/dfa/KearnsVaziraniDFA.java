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

import com.github.misberner.jdtree.binary.BDTEvaluator;
import com.github.misberner.jdtree.binary.BDTNode;
import com.github.misberner.jdtree.binary.BinaryDTree;

import de.learnlib.api.LearningAlgorithm.DFALearner;
import de.learnlib.api.MembershipOracle;
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
		public final Word<I> accessSequence;
		private TLongList incoming;
		
		public StateInfo(Word<I> accessSequence) {
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
	
	private static final class WordEval<I> implements BDTEvaluator<Word<I>, Word<I>> {
		
		private final MembershipOracle<I, Boolean> oracle;
		
		public WordEval(MembershipOracle<I,Boolean> oracle) {
			this.oracle = oracle;
		}
		@Override
		public boolean evaluate(Word<I> prefix, Word<I> discriminator) {
			return MQUtil.output(oracle, prefix, discriminator).booleanValue();
		}
	}
	
	/*
	 * Implementation note: We will ensure that, by construction, the integer
	 * IDs used for (a) the hypothesis states of the CompactDFA and (b) the leaves
	 * in the BinaryDTree will always match. Both classes provide guarantees wrt.
	 * the IDs used for newly introduced states/leaves which allow for doing so.
	 * 
	 * Although this hard-wires the implementation to these classes, it actually safes
	 * a lot of work required for mapping states to discrimination tree leaves and
	 * vice versa. 
	 */
	
	private final Alphabet<I> alphabet;
	private final CompactDFA<I> hypothesis;
	private final MembershipOracle<I,Boolean> oracle;
	
	private final BinaryDTree<Word<I>> discriminationTree
		= new BinaryDTree<>();
		
	private final List<StateInfo<I>> stateInfos
		= new ArrayList<>();
		
	private final BDTEvaluator<Word<I>, Word<I>> prefixEval;

	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the membership oracle
	 */
	public KearnsVaziraniDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactDFA<>(alphabet);
		this.oracle = oracle;
		this.prefixEval = new WordEval<>(oracle);
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
		boolean refined = false;
		Word<I> input = ceQuery.getInput();
		boolean output = ceQuery.getOutput().booleanValue();
		while(hypothesis.accepts(input) != output) {
			refined |= refineHypothesisSingle(input);
		}
		
		return refined;
	}
	
	
	private boolean refineHypothesisSingle(Word<I> input) {
		int inputLen = input.length();
		
		if(inputLen < 2) {
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
			
			int siftState = sift(nextPrefix);
			if(siftState != nextHypState) {
				updateTree(hypState, sym, nextHypState, siftState, prefix);
				return true;
			}
			
			hypState = nextHypState;
			prefix = nextPrefix;
			i++;
		}

		
		return false;
	}
	
	private void updateTree(int hypState, I sym, int hypSucc, int siftSucc, Word<I> prefix) {
		BDTNode<Word<I>> hypStateLeaf = discriminationTree.getLeaf(hypState);
		
		BDTNode<Word<I>> hypSuccLeaf = discriminationTree.getLeaf(hypSucc);
		BDTNode<Word<I>> siftSuccLeaf = discriminationTree.getLeaf(siftSucc);
		
		BDTNode<Word<I>> separator = discriminationTree.leastCommonAncestor(hypSuccLeaf, siftSuccLeaf);
		
		Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
		
		StateInfo<I> hypStateInfo = stateInfos.get(hypState);
		
		boolean oldAccepting = MQUtil.output(oracle, hypStateInfo.accessSequence, newDiscriminator);
		
		TLongList oldIncoming = hypStateInfo.fetchIncoming();
		
		int newState = hypothesis.addIntState(hypothesis.isAccepting(hypState));
		discriminationTree.split(hypStateLeaf, newDiscriminator, oldAccepting);
		initState(newState, prefix);
		
		updateTransitions(oldIncoming, hypStateLeaf);
	}
	
	
	private void updateTransitions(TLongList transList, BDTNode<Word<I>> oldDtTarget) {
		int numTrans = transList.size();
		for(int i = 0; i < numTrans; i++) {
			long encodedTrans = transList.get(i);
			
			int sourceState = (int)(encodedTrans >> 32L);
			int transIdx = (int)(encodedTrans & 0xffffffff);
			
			StateInfo<I> sourceInfo = stateInfos.get(sourceState);
			I symbol = alphabet.getSymbol(transIdx);
			
			int succ = sift(oldDtTarget, sourceInfo.accessSequence.append(symbol));
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
		BDTNode<Word<I>> root = discriminationTree.getRoot();
		
		boolean initAccepting = MQUtil.output(oracle, Word.<I>epsilon()).booleanValue();
		
		int init = hypothesis.addInitialState(initAccepting);
		assert init == 0;
		
		discriminationTree.split(root, Word.<I>epsilon(), initAccepting);
		
		initState(init, Word.<I>epsilon());
	}
	
	private void initState(int state, Word<I> accessSequence) {
		assert state == stateInfos.size();
		stateInfos.add(new StateInfo<>(accessSequence));
		
		int alphabetSize = alphabet.size();
		
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			Word<I> transAs = accessSequence.append(sym);
			
			int succ = sift(transAs);
			setTransition(state, i, succ);
		}
	}
	
	private void setTransition(int state, int symIdx, int succ) {
		StateInfo<I> succInfo = stateInfos.get(succ);
		assert succInfo != null;
		succInfo.addIncoming(state, symIdx);
		hypothesis.setTransition(state, symIdx, succ);
	}
	
	private int sift(Word<I> prefix) {
		return sift(discriminationTree.getRoot(), prefix);
	}
	
	private int sift(BDTNode<Word<I>> start, Word<I> prefix) {
		BDTNode<Word<I>> leaf = discriminationTree.sift(start, prefix, prefixEval);
		
		int succState = leaf.getLeafId();
		if(succState >= hypothesis.size()) {
			// Special case: this is the *first* state of a different
			// acceptance than the initial state
			int newState = hypothesis.addIntState(!hypothesis.isAccepting(0));
			assert newState == succState;
			initState(newState, prefix);
		}
		
		return succState;
	}

}
