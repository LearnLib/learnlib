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

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.discriminationtree.DTNode.SplitResult;
import de.learnlib.discriminationtree.DiscriminationTree;
import de.learnlib.discriminationtree.DiscriminationTree.LCAInfo;
import de.learnlib.discriminationtree.MultiDTree;
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
	
	static final class BuilderDefaults {
		public static boolean repeatedCounterexampleEvaluation() {
			return true;
		}
	}
	
	private static final class StateInfo<I,O> {
		public final int id;
		public final Word<I> accessSequence;
		public DTNode<I, Word<O>, StateInfo<I,O>> dtNode;
		private TLongList incoming;
		
		public StateInfo(int id, Word<I> accessSequence) {
			this.accessSequence = accessSequence.trimmed();
			this.id = id;
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
	private final CompactMealy<I,O> hypothesis;
	private final MembershipOracle<I,Word<O>> oracle;
	private final boolean repeatedCounterexampleEvaluation;
	
	private final DiscriminationTree<I,Word<O>,StateInfo<I,O>> discriminationTree;
		
	private final List<StateInfo<I,O>> stateInfos
		= new ArrayList<>();

	@GenerateBuilder
	public KearnsVaziraniMealy(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> oracle,
			boolean repeatedCounterexampleEvaluation) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactMealy<>(alphabet);
		this.oracle = oracle;
		this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
		this.discriminationTree = new MultiDTree<>(oracle);
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
		Word<I> input = ceQuery.getInput();
		Word<O> output = ceQuery.getOutput();
		if(!refineHypothesisSingle(input, output)) {
			return false;
		}
		if(repeatedCounterexampleEvaluation) {
			while(refineHypothesisSingle(input, output)) {}
		}
		return true;
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
			
			StateInfo<I,O> siftStateInfo = sift(nextPrefix);
			if(siftStateInfo.id != nextHypState) {
				// Successor differs
				updateTree(hypState, sym, nextHypState, siftStateInfo, hypOutput, prefix);
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
		StateInfo<I,O> hypStateInfo = stateInfos.get(hypState);
		
		DTNode<I, Word<O>, StateInfo<I,O>> hypStateLeaf = hypStateInfo.dtNode; 
		
		StateInfo<I,O> newStateInfo = createState(newAs);
		
		TLongList oldIncoming = hypStateInfo.fetchIncoming();
		
		SplitResult<I, Word<O>, StateInfo<I,O>> split = hypStateLeaf.split(newDiscriminator, oldOutcome, newOutcome, newStateInfo);
		hypStateInfo.dtNode = split.nodeOld;
		newStateInfo.dtNode = split.nodeNew;
		
		initState(newStateInfo);
		updateTransitions(oldIncoming, hypStateLeaf);
	}
	
	
	private void updateTree(int hypState, I sym, int hypSucc, StateInfo<I,O> siftSucc, O transOut, Word<I> prefix) {
		DTNode<I,Word<O>,StateInfo<I,O>> hypSuccLeaf = dtNode(hypSucc);
		DTNode<I,Word<O>,StateInfo<I,O>> siftSuccLeaf = siftSucc.dtNode;
		
		LCAInfo<I,Word<O>,StateInfo<I,O>> lcaInfo = discriminationTree.lcaInfo(hypSuccLeaf, siftSuccLeaf);
		
		DTNode<I,Word<O>,StateInfo<I,O>> separator = lcaInfo.leastCommonAncestor;
		
		Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
		
		
		// query-less composition of new outcomes
		Word<O> oldStateOutcome = newOutcome(transOut, lcaInfo.subtree1Label);
		Word<O> newStateOutcome = newOutcome(transOut, lcaInfo.subtree2Label);
		
		splitState(hypState, prefix, newDiscriminator, oldStateOutcome, newStateOutcome);
	}
	
	private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
		return succOutcome.prepend(transOutput);
	}
	
	
	private void updateTransitions(TLongList transList, DTNode<I,Word<O>,StateInfo<I,O>> oldDtTarget) {
		int numTrans = transList.size();
		for(int i = 0; i < numTrans; i++) {
			long encodedTrans = transList.get(i);
			
			int sourceState = (int)(encodedTrans >> 32L);
			int transIdx = (int)(encodedTrans & 0xffffffff);
			
			StateInfo<I,O> sourceInfo = stateInfos.get(sourceState);
			I symbol = alphabet.getSymbol(transIdx);
			
			StateInfo<I,O> succInfo = sift(oldDtTarget, sourceInfo.accessSequence.append(symbol));
			
			O output = hypothesis.getTransition(sourceState, transIdx).getOutput();
			setTransition(sourceState, transIdx, succInfo, output);
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


	
	private StateInfo<I,O> createInitialState() {
		int state = hypothesis.addIntInitialState();
		assert state == stateInfos.size();
		
		StateInfo<I,O> stateInfo = new StateInfo<>(state, Word.<I>epsilon());
		stateInfos.add(stateInfo);
		
		return stateInfo;
	}
	
	private StateInfo<I,O> createState(Word<I> prefix) {
		int state = hypothesis.addIntState();
		assert state == stateInfos.size();
		
		StateInfo<I, O> stateInfo = new StateInfo<>(state, prefix);
		stateInfos.add(stateInfo);
		
		return stateInfo;
	}
	
	private void initialize() {
		StateInfo<I, O> init = createInitialState();
		discriminationTree.getRoot().setData(init);
		init.dtNode = discriminationTree.getRoot();
		initState(init);
	}
	
	private void initState(StateInfo<I,O> stateInfo) {
		int alphabetSize = alphabet.size();
		
		int state = stateInfo.id;
		Word<I> accessSequence = stateInfo.accessSequence;
		
		for(int i = 0; i < alphabetSize; i++) {
			I sym = alphabet.getSymbol(i);
			
			O output = MQUtil.output(oracle, accessSequence, Word.fromLetter(sym)).firstSymbol();
			
			Word<I> transAs = accessSequence.append(sym);
			
			StateInfo<I,O> succInfo = sift(transAs);
			setTransition(state, i, succInfo, output);
		}
	}
	
	private void setTransition(int state, int symIdx, StateInfo<I,O> succInfo, O output) {
		succInfo.addIncoming(state, symIdx);
		hypothesis.setTransition(state, symIdx, succInfo.id, output);
	}
	
	
	private DTNode<I, Word<O>, StateInfo<I,O>> dtNode(int state) {
		return stateInfos.get(state).dtNode;
	}
	
	private StateInfo<I,O> sift(Word<I> prefix) {
		return sift(discriminationTree.getRoot(), prefix);
	}
	
	private StateInfo<I,O> sift(DTNode<I,Word<O>,StateInfo<I,O>> start, Word<I> prefix) {
		DTNode<I,Word<O>,StateInfo<I,O>> leaf = discriminationTree.sift(start, prefix);
		
		StateInfo<I,O> succStateInfo = leaf.getData();
		if(succStateInfo == null) {
			// Special case: this is the *first* state with a different output
			// for some discriminator
			succStateInfo = createState(prefix);
			
			leaf.setData(succStateInfo);
			succStateInfo.dtNode = leaf;

			initState(succStateInfo);
		}
		
		return succStateInfo;
	}

}
