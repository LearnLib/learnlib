package de.learnlib.algorithms.discriminationtree;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.discriminationtree.DTNode.SplitResult;
import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

public abstract class AbstractDTLearner<M extends SuffixOutput<I,O>, I, O, SP, TP> implements LearningAlgorithm<M, I, O> {
	
	public static final class BuilderDefaults {
		public static <I,O> LocalSuffixFinder<? super I,? super O> suffixFinder() {
			return LocalSuffixFinders.RIVEST_SCHAPIRE;
		}
	}

	private final Alphabet<I> alphabet;
	private final MembershipOracle<I, O> oracle;
	private final LocalSuffixFinder<? super I, ? super O> suffixFinder;
	private final DiscriminationTree<I, O, HState<I,O,SP,TP>> dtree;
	protected final DTLearnerHypothesis<I, O, SP, TP> hypothesis;
	
	private final List<HState<I,O,SP,TP>> newStates = new ArrayList<>();
	private final List<HTransition<I,O,SP,TP>> newTransitions = new ArrayList<>();
	private final Deque<HTransition<I,O,SP,TP>> openTransitions = new ArrayDeque<>();

	public AbstractDTLearner(Alphabet<I> alphabet, MembershipOracle<I, O> oracle, LocalSuffixFinder<? super I, ? super O> suffixFinder) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.suffixFinder = suffixFinder;
		this.hypothesis = new DTLearnerHypothesis<I,O,SP,TP>(alphabet);
		HState<I,O,SP,TP> init = hypothesis.getInitialState();
		this.dtree = new DiscriminationTree<>(init, oracle);
		init.setDTLeaf(dtree.getRoot());
		initializeState(init);
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, hypothesis, getHypothesisModel(),
				oracle);
		
		if(suffixIdx == -1) {
			return false;
		}
		
		Word<I> input = ceQuery.getInput();
		Word<I> oldStateAs = input.prefix(suffixIdx);
		HState<I,O,SP,TP> oldState = hypothesis.getState(oldStateAs);
		DTNode<I, O, HState<I,O,SP,TP>> oldDt = oldState.getDTLeaf();
		
		Word<I> newPredAs = input.prefix(suffixIdx - 1);
		HState<I,O,SP,TP> newPred = hypothesis.getState(newPredAs);
		I transSym = input.getSymbol(suffixIdx - 1);
		int transIdx = alphabet.getSymbolIndex(transSym);
		HTransition<I,O,SP,TP> trans = newPred.getTransition(transIdx);
		
		HState<I,O,SP,TP> newState = createState(trans);
		
		Word<I> suffix = input.subWord(suffixIdx);
		
		O oldOut = MQUtil.output(oracle, oldState.getAccessSequence(), suffix);
		O newOut = MQUtil.output(oracle, newState.getAccessSequence(), suffix);
		
		SplitResult<I,O,HState<I,O,SP,TP>> sr = oldDt.split(suffix, oldOut, newOut, newState);
		
		oldState.fetchNonTreeIncoming(openTransitions);
		
		oldState.setDTLeaf(sr.nodeOld);
		newState.setDTLeaf(sr.nodeNew);
		
		updateHypothesis();
		
		return true;
	}

	@Override
	public void startLearning() {
		updateHypothesis();
	}
	
	public DiscriminationTree<I, O, HState<I,O,SP,TP>> getDiscriminationTree() {
		return dtree;
	}
	
	public DTLearnerHypothesis<I,O,SP,TP> getHypothesisDS() {
		return hypothesis;
	}
	
	protected void initializeState(HState<I,O,SP,TP> newState) {
		newStates.add(newState);
		
		int size = alphabet.size();
		for(int i = 0; i < size; i++) {
			I sym = alphabet.getSymbol(i);
			HTransition<I,O,SP,TP> newTrans = new HTransition<I,O,SP,TP>(newState, sym, dtree.getRoot());
			newState.setTransition(i, newTrans);
			newTransitions.add(newTrans);
			openTransitions.offer(newTrans);
		}
	}
	
	protected HState<I,O,SP,TP> createState(HTransition<I,O,SP,TP> trans) {
		HState<I,O,SP,TP> newState = hypothesis.createState(trans);
		
		initializeState(newState);
		
		return newState;
	}
	
	protected void updateTransition(HTransition<I,O,SP,TP> trans) {
		if(trans.isTree())
			return;
		DTNode<I,O,HState<I,O,SP,TP>> currDt = trans.getDT();
		currDt = dtree.sift(currDt, trans.getAccessSequence());
		trans.setDT(currDt);
		HState<I,O,SP,TP> state = currDt.getData();
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
		HTransition<I,O,SP,TP> current;
		while((current = openTransitions.poll()) != null) {
			updateTransition(current);
		}
		
		List<Query<I,O>> queries = new ArrayList<>();
		for(HState<I,O,SP,TP> state : newStates) {
			Query<I,O> spQuery = spQuery(state);
			if(spQuery != null) {
				queries.add(spQuery);
			}
		}
		for(HTransition<I,O,SP,TP> trans : newTransitions) {
			Query<I,O> tpQuery = tpQuery(trans);
			if(tpQuery != null) {
				queries.add(tpQuery);
			}
		}
		oracle.processQueries(queries);
	}

	
	protected abstract Query<I,O> spQuery(HState<I,O,SP,TP> state);
	
	protected abstract Query<I,O> tpQuery(HTransition<I, O, SP, TP> transition);
}
