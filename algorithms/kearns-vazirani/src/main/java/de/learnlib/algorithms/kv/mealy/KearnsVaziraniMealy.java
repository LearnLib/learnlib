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
package de.learnlib.algorithms.kv.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.impl.BaseAbstractCounterexample;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.discriminationtree.DTNode.SplitResult;
import de.learnlib.discriminationtree.DiscriminationTree;
import de.learnlib.discriminationtree.DiscriminationTree.LCAInfo;
import de.learnlib.discriminationtree.MultiDTree;
import de.learnlib.mealy.MealyUtil;
import de.learnlib.oracles.DefaultQuery;


/**
 * An adaption of the Kearns/Vazirani algorithm for Mealy machines.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class KearnsVaziraniMealy<I,O> implements MealyLearner<I,O> {
	
	static final class BuilderDefaults {
		public static boolean repeatedCounterexampleEvaluation() {
			return true;
		}
		public static AcexAnalyzer counterexampleAnalyzer() {
			return AcexAnalyzers.LINEAR_FWD;
		}
	}
	
	private static final class StateInfo<I,O> {
		public final int id;
		public final Word<I> accessSequence;
		public DTNode<I, Word<O>, StateInfo<I,O>> dtNode;
//		private TLongList incoming;
		private List<Long> incoming; // TODO: replace with primitive specialization
		
		public StateInfo(int id, Word<I> accessSequence) {
			this.accessSequence = accessSequence.trimmed();
			this.id = id;
		}
		
		public void addIncoming(int sourceState, int transIdx) {
			long encodedTrans = ((long)sourceState << 32L) | transIdx;
			if(incoming == null) {
//				incoming = new TLongArrayList();
				incoming = new ArrayList<>(); // TODO: replace with primitive specialization
			}
			incoming.add(encodedTrans);
		}
		
//		public TLongList fetchIncoming() {
		public List<Long> fetchIncoming() { // TODO: replace with primitive specialization
			if(incoming == null || incoming.isEmpty()) {
//				return EMPTY_LONG_LIST;
				return Collections.emptyList(); // TODO: replace with primitive specialization
			}
//			TLongList result = incoming;
			List<Long> result = incoming;
			this.incoming = null;
			return result;
		}
	}
	
	private class KVAbstractCounterexample extends BaseAbstractCounterexample {
		
		private final Word<I> ceWord;
		private final MembershipOracle<I, Word<O>> oracle;
		private final StateInfo<I,O>[] states;
		private final LCAInfo<I,Word<O>,StateInfo<I,O>>[] lcas;

		@SuppressWarnings("unchecked")
		public KVAbstractCounterexample(Word<I> ceWord, Word<O> output, MembershipOracle<I, Word<O>> oracle) {
			super(ceWord.length());
			this.ceWord = ceWord;
			this.oracle = oracle;
			
			int m = ceWord.length();
			this.states = new StateInfo[m+1];
			this.lcas = new LCAInfo[m+1];
			
			int currState = hypothesis.getIntInitialState();
			int i = 0;
			states[i++] = stateInfos.get(currState);
			for (I sym : ceWord) {
				currState = hypothesis.getSuccessor(currState, sym);
				states[i++] = stateInfos.get(currState);
			}
			
			// Output of last transition separates hypothesis from target
			O lastHypOut = hypothesis.getOutput(states[m-1].id, ceWord.lastSymbol());
			lcas[m] = new LCAInfo<I,Word<O>,StateInfo<I,O>>(null,
					Word.fromLetter(lastHypOut), Word.fromLetter(output.lastSymbol()));
		}
		
		public StateInfo<I,O> getStateInfo(int idx) {
			return states[idx];
		}
		
		public LCAInfo<I,Word<O>,StateInfo<I,O>> getLCA(int idx) {
			return lcas[idx];
		}

		@Override
		protected int computeEffect(int index) {
			Word<I> prefix = ceWord.prefix(index);
			StateInfo<I,O> info = states[index];
			
			// Save the expected outcomes on the path from the leaf representing the state
			// to the root on a stack
			DTNode<I, Word<O>, StateInfo<I,O>> node = info.dtNode;
			Deque<Word<O>> expect = new ArrayDeque<>();
			while(!node.isRoot()) {
				expect.push(node.getParentOutcome());
				node = node.getParent();
			}
			
			DTNode<I,Word<O>,StateInfo<I,O>> currNode = discriminationTree.getRoot();
			
			while(!expect.isEmpty()) {
				Word<I> suffix = currNode.getDiscriminator();
				Word<O> out = oracle.answerQuery(prefix, suffix);
				Word<O> e = expect.pop();
				if(!Objects.equals(out, e)) {
					lcas[index] = new LCAInfo<>(currNode, e, out);
					return 1;
				}
				currNode = currNode.child(out);
			}
			
			assert currNode.isLeaf() && expect.isEmpty();
			return 0;
		}
	}
	
	private final Alphabet<I> alphabet;
	private final CompactMealy<I,O> hypothesis;
	private final MembershipOracle<I,Word<O>> oracle;
	private final boolean repeatedCounterexampleEvaluation;
	
	private final DiscriminationTree<I,Word<O>,StateInfo<I,O>> discriminationTree;
		
	private final List<StateInfo<I,O>> stateInfos
		= new ArrayList<>();
	
	private final AcexAnalyzer ceAnalyzer;

	@GenerateBuilder
	public KearnsVaziraniMealy(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> oracle,
			boolean repeatedCounterexampleEvaluation,
			AcexAnalyzer counterexampleAnalyzer) {
		this.alphabet = alphabet;
		this.hypothesis = new CompactMealy<>(alphabet);
		this.oracle = oracle;
		this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
		this.discriminationTree = new MultiDTree<>(oracle);
		this.ceAnalyzer = counterexampleAnalyzer;
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
		
		int mismatchIdx = MealyUtil.findMismatch(hypothesis, input, output);
		
		if (mismatchIdx == MealyUtil.NO_MISMATCH) {
			return false;
		}
		
		Word<I> effInput = input.prefix(mismatchIdx+1);
		Word<O> effOutput = output.prefix(mismatchIdx+1);
		
		KVAbstractCounterexample acex = new KVAbstractCounterexample(effInput, effOutput, oracle);
		int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 0);
		
		Word<I> prefix = effInput.prefix(idx);
		StateInfo<I,O> srcStateInfo = acex.getStateInfo(idx);
		I sym = effInput.getSymbol(idx);
		LCAInfo<I,Word<O>,StateInfo<I,O>> lca = acex.getLCA(idx+1);
		assert lca != null;
		
		splitState(srcStateInfo, prefix, sym, lca);
		
		return true;
	}
	
	
	private void splitState(StateInfo<I,O> stateInfo, Word<I> newPrefix, I sym, LCAInfo<I,Word<O>,StateInfo<I,O>> separatorInfo) {
		int state = stateInfo.id;
		
//		TLongList oldIncoming = stateInfo.fetchIncoming();
		List<Long> oldIncoming = stateInfo.fetchIncoming(); // TODO: replace with primitive specialization
		
		StateInfo<I,O> newStateInfo = createState(newPrefix);
		
		DTNode<I, Word<O>, StateInfo<I,O>> stateLeaf = stateInfo.dtNode;
		
		DTNode<I, Word<O>, StateInfo<I,O>> separator = separatorInfo.leastCommonAncestor;
		Word<I> newDiscriminator;
		Word<O> oldOut, newOut;
		if (separator == null) {
			newDiscriminator = Word.fromLetter(sym);
			oldOut = separatorInfo.subtree1Label;
			newOut = separatorInfo.subtree2Label;
		}
		else {
			newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
			O transOut = hypothesis.getOutput(state, sym);
			oldOut = newOutcome(transOut, separatorInfo.subtree1Label);
			newOut = newOutcome(transOut, separatorInfo.subtree2Label);
		}
		
		SplitResult<I, Word<O>, StateInfo<I,O>> split = stateLeaf.split(newDiscriminator, oldOut, newOut, newStateInfo);
		
		stateInfo.dtNode = split.nodeOld;
		newStateInfo.dtNode = split.nodeNew;
		
		initState(newStateInfo);
		
		updateTransitions(oldIncoming, stateLeaf);
	}
	
	private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
		return succOutcome.prepend(transOutput);
	}
	
	
//	private void updateTransitions(TLongList transList, DTNode<I,Word<O>,StateInfo<I,O>> oldDtTarget) {
	private void updateTransitions(List<Long> transList, DTNode<I,Word<O>,StateInfo<I,O>> oldDtTarget) { // TODO: replace with primitive specialization
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
			
			O output = oracle.answerQuery(accessSequence, Word.fromLetter(sym)).firstSymbol();
			
			Word<I> transAs = accessSequence.append(sym);
			
			StateInfo<I,O> succInfo = sift(transAs);
			setTransition(state, i, succInfo, output);
		}
	}
	
	private void setTransition(int state, int symIdx, StateInfo<I,O> succInfo, O output) {
		succInfo.addIncoming(state, symIdx);
		hypothesis.setTransition(state, symIdx, succInfo.id, output);
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
