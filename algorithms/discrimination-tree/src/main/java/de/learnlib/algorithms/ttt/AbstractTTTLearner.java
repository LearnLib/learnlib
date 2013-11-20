package de.learnlib.algorithms.ttt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.dtree.DTNode;
import de.learnlib.algorithms.ttt.dtree.DiscriminationTree;
import de.learnlib.algorithms.ttt.dtree.TempDTNode;
import de.learnlib.algorithms.ttt.hypothesis.HTransition;
import de.learnlib.algorithms.ttt.hypothesis.HypothesisState;
import de.learnlib.algorithms.ttt.hypothesis.TTTHypothesis;
import de.learnlib.algorithms.ttt.suffixtrie.SuffixTrieNode;
import de.learnlib.algorithms.ttt.suffixtrie.SuffixTrie;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

public abstract class AbstractTTTLearner<I, O, SP, TP, M, H extends TTTHypothesis<I, O, SP, TP, ?>> implements
		LearningAlgorithm<M, I, O> {
	
	private final Alphabet<I> alphabet;
	private final DiscriminationTree<I, O, SP, TP> dtree;
	private final SuffixTrie<I> stree = new SuffixTrie<>();
	protected final H hypothesis;
	
	// Open transitions - these point to nodes in the discrimination tree
	// that have since been split (used to be leaves, now inner nodes)
	private final Queue<HTransition<I,O,SP,TP>> openTransitions = new ArrayDeque<>();
	
	// New states and transitions - these are added only once until creation,
	// used for property extraction
	private final List<HypothesisState<I,O,SP,TP>> newStates = new ArrayList<>();
	private final List<HTransition<I,O,SP,TP>> newTransitions = new ArrayList<>();
	
	private final MembershipOracle<I,O> oracle;
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the oracle
	 * @param hypothesis the hypothesis model to use internally
	 */
	protected AbstractTTTLearner(Alphabet<I> alphabet, MembershipOracle<I,O> oracle, H hypothesis) {
		this.alphabet = alphabet;
		this.hypothesis = hypothesis;
		this.dtree = new DiscriminationTree<>();
		this.oracle = oracle;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#startLearning()
	 */
	@Override
	public void startLearning() {
		DTNode<I,O,SP,TP> initNode = dtree.sift(Word.<I>epsilon(), oracle);
		
		if(initNode.getTempRoot() != null) {
			throw new IllegalStateException("Cannot start learning: Discrimination tree already contains states");
		}
		
		HypothesisState<I,O,SP,TP> initState = hypothesis.getInitialState();
		TempDTNode<I, O, SP, TP> tempRoot = new TempDTNode<>(initState, null);
		initNode.setTempRoot(tempRoot);
		initState.setDTLeaf(initNode);
		
		initializeState(initState);
		
		close();
		updateProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	public boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		Word<I> ceWord = ceQuery.getInput();
		
		if(!handleCounterexample(ceWord))
			return false;

		
		close();
		updateProperties();
		
		verify();
		
		return true;
	}
	
	
	/**
	 * Splits a node in the discrimination tree.
	 * @param dtNode the node to split
	 * @param discriminator the discriminator (suffix tree reference) to use for splitting
	 */
	private void splitDT(DTNode<I, O, SP, TP> dtNode, SuffixTrieNode<I> discriminator) {
		System.err.println("splitDT(" + dtNode.getTempRoot() + ")");
		Word<I> suffix = discriminator.getSuffix();
		
		Map<O,TempDTNode<I, O, SP, TP>> splitRes = dtNode.getTempRoot().treeSplit(oracle, suffix);
		
		openTransitions.addAll(dtNode.getNonTreeIncoming());
		dtNode.clearNonTreeIncoming();
		
		dtree.split(dtNode, discriminator, splitRes);
	}
	
	
	protected boolean handleCounterexample(Word<I> ceWord) {
		int i = 0;
		int ceLen = ceWord.length();
		
		HypothesisState<I, O, SP, TP> curr = hypothesis.getInitialState();
		HTransition<I, O, SP, TP> next = null;
		
		boolean found = false;
		
		Word<I> suffix = null;
		O oldOut = null, newOut = null;
		
		while(!found && i < ceLen) {
			I sym = ceWord.getSymbol(i++);
			next = hypothesis.getInternalTransition(curr, sym);
			curr = next.getTreeTarget();
			if(curr != null)
				continue; // tree transition, transformation will have no effect
			
			curr = next.nonTreeTarget(); // non-tree transition
			
			suffix = ceWord.subWord(i);
			
			Word<I> transAs = next.getAccessSequence();
			Word<I> tgtAs = curr.getAccessSequence();
			
			oldOut = MQUtil.output(oracle, tgtAs, suffix);
			newOut = MQUtil.output(oracle, transAs, suffix);
			
			if(!Objects.equals(oldOut, newOut))
				found = true;
		}

		if (!found)
			return false;
		
		HypothesisState<I, O, SP, TP> oldState = curr;
		HypothesisState<I, O, SP, TP> newState = createHypothesisState(next);
		
		TempDTNode<I, O, SP, TP> tmpDt = oldState.getTempDT();
		// Split the last visited state using the suffix
		tmpDt.split(suffix, oldOut, newState, newOut);
		
		
		// Worklist, in which we maintain pairs of states that possibly have to be
		// split in the discrimination tree
		Deque<Pair<HypothesisState<I, O, SP, TP>,HypothesisState<I, O, SP, TP>>> stack
			= new ArrayDeque<>();
		
		stack.push(Pair.make(oldState, newState));
		
		while(!stack.isEmpty()) {
			Pair<HypothesisState<I, O, SP, TP>,HypothesisState<I, O, SP, TP>> pair = stack.pop();
			System.err.println("Current: " + pair);
			
			oldState = pair.getFirst();
			newState = pair.getSecond();
			
			DTNode<I, O, SP, TP> dt = oldState.getDTLeaf();
			if(newState.getDTLeaf() != dt) {
				System.err.println("Already split: " + newState.getDTLeaf().getTempRoot() + " vs " + dt.getTempRoot());
				continue; // States are already split, nothing to do here
			}
			
			
			TempDTNode<I, O, SP, TP> oldTmpDt = oldState.getTempDT();
			TempDTNode<I, O, SP, TP> newTmpDt = newState.getTempDT();
			
			TempDTNode<I, O, SP, TP> tmpSplit = TempDTNode.commonAncestor(oldTmpDt, newTmpDt);
			
			suffix = tmpSplit.getSuffix();
			
			if(suffix.isEmpty()) {
				// We can split using the empty suffix (root of the suffix tree)
				splitDT(dt, stree.getRoot());
				continue;
			}
			
			I sym = suffix.firstSymbol();
			
			HTransition<I, O, SP, TP> oldTrans = hypothesis.getInternalTransition(oldState, sym);
			HTransition<I, O, SP, TP> newTrans = hypothesis.getInternalTransition(newState, sym);
			
			DTNode<I, O, SP, TP> oldTransDt = updateTransition(oldTrans);
			DTNode<I, O, SP, TP> newTransDt = updateTransition(newTrans);
			
			if(oldTransDt != newTransDt) {
				// Successors belong to different DT nodes
				// We have a suffix tree node which we can use for splitting
				DTNode<I, O, SP, TP> split = dtree.commonAncestor(oldTransDt, newTransDt);
				SuffixTrieNode<I> succDiscr = split.getDiscriminator();
				SuffixTrieNode<I> thisDiscr = stree.add(sym, succDiscr);
				splitDT(dt, thisDiscr);
				continue;
			}
			
			
			TempDTNode<I, O, SP, TP> oldTransTempDt = findTempDT(oldTrans);
			TempDTNode<I, O, SP, TP> newTransTempDt = findTempDT(newTrans);

			oldState = oldTransTempDt.getState();
			
			suffix = suffix.subWord(1);
			
			if(oldTransTempDt == newTransTempDt) {
				// No difference wrt. our discrimination trees
				// between transition successors, but states originate
				// from a counterexample
				
				// We have to distinguish between two cases:
				// 1) the successor states only appear equal because our
				//    discrimination tree is too coarse. Hence, the successor
				//    state also needs to be split
				// 2) we have a "confluence", because only the currently considered
				//    transition constitutes the difference between our two states,
				//    not the successor state (as may be the case for Mealy machines).
				//    We can therefore use the single-letter suffix in the discrimination
				//    tree.
				
				Word<I> tgtAs = oldState.getAccessSequence();
				
				oldOut = MQUtil.output(oracle, tgtAs, suffix);
				
				HTransition<I, O, SP, TP> potNew;
				if(!newTrans.isTree()) {
					newOut = MQUtil.output(oracle, newTrans.getAccessSequence(), suffix);
					potNew = newTrans;
				}
				else {// !oldTrans.isTree()
					newOut = MQUtil.output(oracle, oldTrans.getAccessSequence(), suffix);
					potNew = oldTrans;
				}
				
				if(!Objects.equals(oldOut, newOut)) {
					// No confluence, split next states
					newState = createHypothesisState(potNew);
					oldTransTempDt.split(suffix, oldOut, newState, newOut);
					stack.push(pair);
					stack.push(Pair.make(oldState, newState));
				}
				else {
					// TODO check [oldTransAs]?
					// Confluence - transition exposes difference
					SuffixTrieNode<I> thisDiscr = stree.add(sym, stree.getRoot());
					splitDT(dt, thisDiscr);
				}
			}
			else {
				newState = newTransTempDt.getState();
				oldOut = MQUtil.output(oracle, oldState.getAccessSequence(), suffix);
				newOut = MQUtil.output(oracle, newState.getAccessSequence(), suffix);
				
				if(Objects.equals(oldOut, newOut)) {
					// Confluence
					SuffixTrieNode<I> thisDiscr = stree.add(sym, stree.getRoot());
					splitDT(dt, thisDiscr);
					continue;
				}
				
				TempDTNode<I, O, SP, TP> ca = TempDTNode.commonAncestor(oldTransTempDt, newTransTempDt);
				
				replaceSuffix(ca, suffix);
				
				stack.push(pair);
				stack.push(Pair.make(oldState, newState));
			}
		}
		
		return true;
	}
	
	private TempDTNode<I, O, SP, TP> findTempDT(
			HTransition<I, O, SP, TP> oldTrans) {
		Word<I> as = oldTrans.getAccessSequence();
		TempDTNode<I, O, SP, TP> tempDt = oldTrans.currentDTTarget().getTempRoot();
		tempDt = tempDt.sift(oracle, as);
		if(tempDt.getState() == null) {
			tempDt.setState(createHypothesisState(oldTrans));
		}
		return tempDt;
	}

	private void replaceSuffix(TempDTNode<I, O, SP, TP> tempDt, Word<I> newSuffix) {
		Map<O,TempDTNode<I,O,SP,TP>> splitRes = tempDt.treeSplit(oracle, newSuffix);
		tempDt.replace(newSuffix, splitRes);
	}
	
	
	public SuffixTrie<I> getSuffixTree() {
		return stree;
	}
	
	public DiscriminationTree<I, O, SP, TP> getDiscriminationTree() {
		return dtree;
	}
	
	public H getHypothesisTree() {
		return hypothesis;
	}
	
	protected void initializeState(HypothesisState<I,O,SP,TP> state) {
		newStates.add(state);
		
		int numSyms = alphabet.size();
		for(int i = 0; i < numSyms; i++) {
			I sym = alphabet.getSymbol(i);
			HTransition<I, O, SP, TP> trans = new HTransition<I,O,SP,TP>(state, sym, dtree.getRoot());
			state.setTransition(i, trans);
			newTransitions.add(trans);
			openTransitions.offer(trans);
		}
	}
	
	protected HypothesisState<I, O, SP, TP> createHypothesisState(HTransition<I,O,SP,TP> treeIncoming) {
		HypothesisState<I,O,SP,TP> state = hypothesis.createState(treeIncoming);
		state.setDTLeaf(treeIncoming.getDT());
		treeIncoming.makeTree(state);
		initializeState(state);
		
		return state;
	}
		
	
	protected DTNode<I, O, SP, TP> updateTransition(HTransition<I, O, SP, TP> transition) {
		if(transition.isTree())
			return transition.getTreeTarget().getDTLeaf();
		
		Word<I> as = transition.getAccessSequence();
		DTNode<I,O,SP,TP> leaf = dtree.sift(transition.getDT(), as, oracle);
		TempDTNode<I, O, SP, TP> tempRoot = leaf.getTempRoot();
		
		transition.updateDTTarget(leaf);
		if(tempRoot == null) {
			HypothesisState<I, O, SP, TP> state = createHypothesisState(transition);
			leaf.setTempRoot(new TempDTNode<>(state, null));
		}
		else {
			leaf.getNonTreeIncoming().add(transition);
		}
		
		return leaf;
	}
	
	protected void close() {
		HTransition<I,O,SP,TP> curr;
		
		while((curr = openTransitions.poll()) != null) {
			updateTransition(curr);
		}
	}
	
	protected void updateProperties() {
		List<Query<I,O>> queries = new ArrayList<>();
		
		for(HypothesisState<I,O,SP,TP> s : newStates) {
			Query<I,O> q = stateProperty(s);
			if(q != null)
				queries.add(q);
		}
		
		for(HTransition<I,O,SP,TP> t : newTransitions) {
			Query<I,O> q = transitionProperty(t);
			if(q != null)
				queries.add(q);
		}
		
		if(!queries.isEmpty())
			oracle.processQueries(queries);
	}
	
	protected void verify() {
		for(HypothesisState<I, O, SP, TP> state : hypothesis) {
			Word<I> as = state.getAccessSequence();
			DTNode<I, O, SP, TP> tgt = dtree.sift(as, oracle);
			if(tgt.getTempRoot().getState() != state)
				throw new IllegalStateException("State " + state + " with access sequence " + as + " mapped to " + tgt.getTempRoot());
		}
	}

	protected Query<I,O> stateProperty(HypothesisState<I,O,SP,TP> state) {
		return null;
	}
	
	protected Query<I,O> transitionProperty(HTransition<I,O,SP,TP> trans) {
		return null;
	}

}
