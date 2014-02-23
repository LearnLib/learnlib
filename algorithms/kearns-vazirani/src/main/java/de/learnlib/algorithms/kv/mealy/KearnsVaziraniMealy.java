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
package de.learnlib.algorithms.kv.mealy;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.github.misberner.jdtree.multi.MDTEvaluator;
import com.github.misberner.jdtree.multi.MDTLCAInfo;
import com.github.misberner.jdtree.multi.MDTNode;
import com.github.misberner.jdtree.multi.MultiDTree;

import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;


/**
 * An adaption of the Kearns/Vazirani algorithm for Mealy machines.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class KearnsVaziraniMealy<I,O> implements MealyLearner<I,O> {
	
	private static final TLongList EMPTY_LONG_LIST = new TLongArrayList(0);
	
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
	private static final class WordEval<I,O> implements MDTEvaluator<Word<I>, Word<I>,Word<O>> {
		
		private final MembershipOracle<I, Word<O>> oracle;
		
		public WordEval(MembershipOracle<I,Word<O>> oracle) {
			this.oracle = oracle;
		}
		@Override
		public Word<O> evaluate(Word<I> prefix, Word<I> discriminator) {
			return MQUtil.output(oracle, prefix, discriminator);
		}
	}
	
	private final Alphabet<I> alphabet;
	private final CompactMealy<I,O> hypothesis;
	private final MembershipOracle<I,Word<O>> oracle;
	
	private final MultiDTree<Word<I>,Word<O>> discriminationTree
		= new MultiDTree<>();
		
	private final List<StateInfo<I>> stateInfos
		= new ArrayList<>();
		
	private final MDTEvaluator<Word<I>, Word<I>, Word<O>> prefixEval;

	
	public KearnsVaziraniMealy(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> oracle) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactMealy<>(alphabet);
		this.oracle = oracle;
		this.prefixEval = new WordEval<>(oracle);
	}
	
	@Override
	public void startLearning() {
		initialize();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
		if(hypothesis.size() == 0) {
			throw new IllegalStateException("Not initialized");
		}
		boolean refined = false;
		Word<I> input = ceQuery.getInput();
		Word<O> output = ceQuery.getOutput();
		while(!hypothesis.computeOutput(input).equals(output)) {
			refined |= refineHypothesisSingle(input, output);
		}
		
		return refined;
	}
	
	
	private boolean refineHypothesisSingle(Word<I> input, Word<O> output) {
		int inputLen = input.length();
		
		if(inputLen < 2) {
			return false;
		}
		
		int hypState = hypothesis.getInitialState();
		
		Iterator<I> symIt = input.iterator();
		
		I firstSym = symIt.next();
		hypState = hypothesis.getSuccessor(hypState, firstSym);
		
		Word<I> prefix = Word.fromLetter(firstSym);
		
		Iterator<O> outputIt = output.iterator();
		outputIt.next();
		
		int i = 2;
		
		// Note that the empty word and all words of length 1
		// have *always* been sifted into the tree already
		while(symIt.hasNext()) {
			Word<I> nextPrefix = input.prefix(i);
			I sym = symIt.next();
			
			CompactMealyTransition<O> trans = hypothesis.getTransition(hypState, sym);
			
			
			O hypOutput = hypothesis.getTransitionOutput(trans);
			O ceOutput = outputIt.next();
			
			
			if(!Objects.equals(hypOutput, ceOutput)) {
				// Output differs
				splitState(hypState, prefix, Word.fromLetter(sym), Word.fromLetter(hypOutput),
						Word.fromLetter(ceOutput));
				return true;
			}
			
			int nextHypState = hypothesis.getIntSuccessor(trans);
			
			int siftState = sift(nextPrefix);
			if(siftState != nextHypState) {
				// Successor differs
				updateTree(hypState, sym, nextHypState, siftState, hypOutput, prefix);
				return true;
			}
			
			hypState = nextHypState;
			prefix = nextPrefix;
			i++;
		}

		
		return false;
	}
	
	
	private void splitState(int hypState, Word<I> newAs, Word<I> newDiscriminator, Word<O> oldOutcome,
			Word<O> newOutcome) {
		MDTNode<Word<I>, Word<O>> hypStateLeaf = discriminationTree.getLeaf(hypState);
		
		int newState = hypothesis.addIntState();
		
		StateInfo<I> hypStateInfo = stateInfos.get(hypState);
		TLongList oldIncoming = hypStateInfo.fetchIncoming();
		
		discriminationTree.split(hypStateLeaf, newDiscriminator, oldOutcome, newOutcome);
		
		initState(newState, newAs);
		updateTransitions(oldIncoming, hypStateLeaf);
	}
	
	private void updateTree(int hypState, I sym, int hypSucc, int siftSucc, O transOut, Word<I> prefix) {
		MDTNode<Word<I>,Word<O>> hypSuccLeaf = discriminationTree.getLeaf(hypSucc);
		MDTNode<Word<I>,Word<O>> siftSuccLeaf = discriminationTree.getLeaf(siftSucc);
		
		MDTLCAInfo<Word<I>, Word<O>> lcaInfo = discriminationTree.leastCommonAncestor(hypSuccLeaf, siftSuccLeaf);
		
		MDTNode<Word<I>,Word<O>> separator = lcaInfo.leastCommonAncestor;
		
		Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
		
		
		// query-less composition of new outcomes
		Word<O> oldStateOutcome = newOutcome(transOut, lcaInfo.firstOutcome);
		Word<O> newStateOutcome = newOutcome(transOut, lcaInfo.secondOutcome);
		
		splitState(hypState, prefix, newDiscriminator, oldStateOutcome, newStateOutcome);
	}
	
	private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
		return succOutcome.prepend(transOutput);
	}
	
	
	private void updateTransitions(TLongList transList, MDTNode<Word<I>,Word<O>> oldDtTarget) {
		int numTrans = transList.size();
		for(int i = 0; i < numTrans; i++) {
			long encodedTrans = transList.get(i);
			
			int sourceState = (int)(encodedTrans >> 32L);
			int transIdx = (int)(encodedTrans & 0xffffffff);
			
			StateInfo<I> sourceInfo = stateInfos.get(sourceState);
			I symbol = alphabet.getSymbol(transIdx);
			
			int succ = sift(oldDtTarget, sourceInfo.accessSequence.append(symbol));
			
			O output = hypothesis.getTransition(sourceState, transIdx).getOutput();
			setTransition(sourceState, transIdx, succ, output);
		}
	}
	
	private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
		return succDiscriminator.prepend(symbol);
	}

	@Override
	public MealyMachine<?,I,?,O> getHypothesisModel() {
		if(hypothesis.size() == 0) {
			throw new IllegalStateException("Not started");
		}
		return hypothesis;
	}


	
	private void initialize() {
		int init = hypothesis.addInitialState();
		assert init == 0;
		
		initState(init, Word.<I>epsilon());
	}
	
	private void initState(int state, Word<I> accessSequence) {
		assert state == stateInfos.size();
		stateInfos.add(new StateInfo<>(accessSequence));
		
		int alphabetSize = alphabet.size();
		
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			O output = MQUtil.output(oracle, accessSequence, Word.fromLetter(sym)).firstSymbol();
			
			Word<I> transAs = accessSequence.append(sym);
			
			
			int succ = sift(transAs);
			setTransition(state, i, succ, output);
		}
	}
	
	private void setTransition(int state, int symIdx, int succ, O output) {
		StateInfo<I> succInfo = stateInfos.get(succ);
		assert succInfo != null;
		succInfo.addIncoming(state, symIdx);
		hypothesis.setTransition(state, symIdx, succ, output);
	}
	
	private int sift(Word<I> prefix) {
		return sift(discriminationTree.getRoot(), prefix);
	}
	
	private int sift(MDTNode<Word<I>,Word<O>> start, Word<I> prefix) {
		MDTNode<Word<I>,Word<O>> leaf = discriminationTree.sift(start, prefix, prefixEval);
		
		int succState = leaf.getLeafId();
		if(succState >= hypothesis.size()) {
			// Special case: this is the *first* state with a different output
			// for some discriminator
			int newState = hypothesis.addIntState();
			assert newState == succState;
			initState(newState, prefix);
		}
		
		return succState;
	}

}
