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
package de.learnlib.algorithms.ttt.base;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nonnull;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.smartcollections.UnorderedCollection;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;

import de.learnlib.algorithms.ttt.base.TTTHypothesis.TTTEdge;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

/**
 * The TTT learning algorithm for {@link DFA}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public abstract class BaseTTTLearner<A,I,D> implements LearningAlgorithm<A,I,D>, AccessSequenceTransformer<I>, SuffixOutput<I, D> {
	
	public static class BuilderDefaults {
		public static <I,D> LocalSuffixFinder<? super I, ? super D> suffixFinder() {
			return LocalSuffixFinders.RIVEST_SCHAPIRE;
		}
	}
	
	protected final Alphabet<I> alphabet;
	protected final TTTHypothesis<I,D,?> hypothesis;
	private final MembershipOracle<I, D> oracle;
	
	protected final DiscriminationTree<I,D> dtree;
	// private final SuffixTrie<I> suffixTrie = new SuffixTrie<>();
	
	private final Set<Word<I>> finalDiscriminators = Sets.newHashSet(Word.epsilon());
	
	private final Collection<WeakReference<TTTEventListener<I, D>>> eventListeners = new UnorderedCollection<>();
	
	/**
	 * Open transitions, i.e., transitions that possibly point to a non-leaf
	 * node in the discrimination tree.
	 */
	private final Queue<TTTTransition<I,D>> openTransitions = new ArrayDeque<>();
	
	/**
	 * Suffix finder to be used for counterexample analysis.
	 */
	private final LocalSuffixFinder<? super I, ? super D> suffixFinder;
	
	/**
	 * The size of the hypothesis after the last call to {@link #closeTransitions()}.
	 * This allows classifying states as "old" by means of their ID, which is necessary
	 * to determine whether its transitions need to be added to the list
	 * of "open" transitions.
	 */
	private int lastGeneration;
	
	/**
	 * The blocks during a split operation. A block is a maximal subtree of the
	 * discrimination tree containing temporary discriminators at its root. 
	 */
	protected final BlockList<I,D> blockList = new BlockList<>();
	
	protected BaseTTTLearner(Alphabet<I> alphabet, MembershipOracle<I, D> oracle,
			TTTHypothesis<I, D, ?> hypothesis,
			LocalSuffixFinder<? super I, ? super D> suffixFinder) {
		this.alphabet = alphabet;
		this.hypothesis = hypothesis;
		this.oracle = oracle;
		this.dtree = new DiscriminationTree<>(oracle);
		this.suffixFinder = suffixFinder;
	}
	
	/*
	 * LearningAlgorithm interface methods
	 */

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#startLearning()
	 */
	@Override
	public void startLearning() {
		if(hypothesis.isInitialized()) {
			throw new IllegalStateException();
		}
		
		TTTState<I, D> init = hypothesis.initialize();
		
		DTNode<I, D> initNode = dtree.sift(init);
		
		link(initNode, init);
		
		initializeState(init);
		
		closeTransitions();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	public boolean refineHypothesis(DefaultQuery<I, D> ceQuery) {
		if(!refineHypothesisSingle(ceQuery)) {
			return false;
		}
		
		DefaultQuery<I, D> currCe = ceQuery;
		
		//while(currCe != null) {
			while(refineHypothesisSingle(currCe));
//			currCe = checkHypothesisConsistency();
//		}
		
		return true;
	}
	
	
	/*
	 * Private helper methods.
	 */
	
	
	/**
	 * Initializes a state. Creates its outgoing transition objects, and adds them
	 * to the "open" list.
	 * @param state the state to initialize
	 */
	protected void initializeState(TTTState<I,D> state) {
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			TTTTransition<I,D> trans = createTransition(state, sym);
			trans.setNonTreeTarget(dtree.getRoot());
			state.transitions[i] = trans;
			openTransitions.offer(trans);
		}
	}
	
	protected TTTTransition<I,D> createTransition(TTTState<I,D> state, I sym) {
		return new TTTTransition<I, D>(state, sym);
	}
	
	
	/**
	 * Performs a single refinement of the hypothesis, i.e., without 
	 * repeated counterexample evaluation. The parameter and return value
	 * have the same significance as in {@link #refineHypothesis(DefaultQuery)}.
	 * 
	 * @param ceQuery the counterexample (query) to be used for refinement
	 * @return {@code true} if the hypothesis was refined, {@code false} otherwise
	 */
	private boolean refineHypothesisSingle(DefaultQuery<I, D> ceQuery) {
		TTTState<I,D> state = getState(ceQuery.getPrefix());
		D out = computeHypothesisOutput(state, ceQuery.getSuffix());
		
		if(Objects.equals(out, ceQuery.getOutput())) {
			return false;
		}
		
		// Determine a counterexample decomposition (u, a, v)
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, this, this, oracle);
		assert suffixIdx != -1;
		
		Word<I> ceInput = ceQuery.getInput();
		
		Word<I> u = ceInput.prefix(suffixIdx - 1);
		I a = ceInput.getSymbol(suffixIdx - 1);
		int aIdx = alphabet.getSymbolIndex(a);
		Word<I> v = ceInput.subWord(suffixIdx);
		
		
		TTTState<I,D> pred = getState(u);
		TTTTransition<I,D> trans = pred.transitions[aIdx];
		
		// Split the state reached by ua
		splitState(trans, v);
		
		// "Repair" the hypothesis
		while(!repair()) {}
		
		// Close all open transitions
		closeTransitions();
		
		return true;
	}
	
	/**
	 * Chooses a block root, and finalizes the corresponding discriminator.
	 * @return {@code true} if a splittable block root was found, {@code false}
	 * otherwise.
	 */
	protected boolean finalizeAny() {
		GlobalSplitter<I,D> splitter = findSplitterGlobal();
		if(splitter != null) {
			finalizeDiscriminator(splitter.blockRoot, splitter.localSplitter);
			return true;
		}
		return false;
	}
	
	/**
	 * "Repairs" the data structures of the algorithm by subsequently
	 * finalizing discriminators of block roots. If this alone is insufficient (i.e.,
	 * there are blocks with discriminators that cannot be finalized),
	 * consistency between the discrimination tree and the hypothesis is restored
	 * by calling {@link #makeConsistent(DTNode)}.
	 * <p>
	 * <b>Note:</b> In the latter case, this method has to be called again. Whether
	 * or not this is necessary can be determined by examining the return value.
	 * 
	 * @return {@code true} if the hypothesis was successfully repaired, {@code false}
	 * otherwise (i.e., if a subsequent call to this method is required)
	 */
	private boolean repair() {
		while(finalizeAny()) {}
		if(blockList.isEmpty()) {
			return true;
		}
		DTNode<I,D> blockRoot = blockList.chooseBlock();
		makeConsistent(blockRoot);
		return false;
	}
	
	/**
	 * Restores consistency between the discriminator info contained in the subtree
	 * of the given block, and the hypothesis. As counterexample reevaluation might result
	 * in queries of relatively high length, only a single discriminator and
	 * two states it separated are considered. Hence, this method may have to be invoked
	 * repeatedly in order to allow further discriminator finalization.
	 *  
	 * @param blockRoot the root of the block in which to restore consistency
	 */
	private void makeConsistent(DTNode<I,D> blockRoot) {
		// TODO currently, we have a very simplistic approach: we take the
		// leftmost inner node, its left child, and the leftmost child of its
		// new subtree. While this does not impair correctness, a heuristic
		// trying to minimize the length of discriminators and state access sequences might be worth
		// exploring.
		DTNode<I,D> separator = chooseInnerNode(blockRoot);
		
		for (DTNode<I,D> subtreeRoot : separator.getChildren()) {
			DTNode<I,D> leaf = chooseLeaf(subtreeRoot);
			if (ensureConsistency(leaf.state, separator, subtreeRoot.getParentEdgeLabel())) {
				return;
			}
		}
		
		assert false;
	}
	
	protected DTNode<I,D> chooseInnerNode(DTNode<I,D> root) {
		DTNode<I,D> shortestDiscriminator = null;
		int shortestLen = 0;
		
		for(DTNode<I,D> node : root.innerNodes()) {
			int discrLen = node.getDiscriminator().length();
			if(shortestDiscriminator == null || discrLen < shortestLen) {
				shortestDiscriminator = node;
				shortestLen = discrLen;
			}
		}
		
		return shortestDiscriminator;
	}
	
	protected DTNode<I,D> chooseLeaf(DTNode<I,D> root) {
		DTNode<I,D> shortestPrefix = null;
		int shortestLen = 0;
		
		for(DTNode<I,D> leaf : root.subtreeLeaves()) {
			int asLen = leaf.state.getAccessSequence().length();
			if(shortestPrefix == null || asLen < shortestLen) {
				shortestPrefix = leaf;
				shortestLen = asLen;
			}
		}
		
		return shortestPrefix;
	}
	
	/**
	 * Ensures that the given state's output for the specified suffix in the hypothesis
	 * matches the provided real outcome, as determined by means of a membership query.
	 * This is achieved by analyzing the derived counterexample, if the hypothesis
	 * in fact differs from the provided real outcome.
	 * 
	 * @param state the state
	 * @param suffix the suffix
	 * @param realOutcome the real outcome, previously determined through a membership query
	 * @return {@code true} if the hypothesis was refined (i.e., was inconsistent when
	 * this method was called), {@code false} otherwise
	 */
	private boolean ensureConsistency(TTTState<I,D> state, DTNode<I,D> dtNode, D realOutcome) {
		Word<I> suffix = dtNode.getDiscriminator();
		D hypOutcome = computeHypothesisOutput(state, suffix);
		if(Objects.equals(hypOutcome, realOutcome)) {
			return false;
		}
		
		notifyEnsureConsistency(state, dtNode, realOutcome);
		
		
		DefaultQuery<I, D> query = new DefaultQuery<>(state.getAccessSequence(), suffix, realOutcome);
		
		while(refineHypothesisSingle(query)) {}
		
		return true;
	}
	
	
	/**
	 * Data structure for representing a splitter.
	 * <p>
	 * A splitter is represented by an input symbol, and a DT node
	 * that separates the successors (wrt. the input symbol) of the original
	 * states. From this, a discriminator can be obtained by prepending the input
	 * symbol to the discriminator that labels the separating successor.
	 * <p>
	 * <b>Note:</b> as the discriminator finalization is applied to the root
	 * of a block and affects all nodes, there is no need to store references
	 * to the source states from which this splitter was obtained.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	public static final class Splitter<I,D> {
		public final TTTState<I,D> state1, state2;
		public final int symbolIdx;
		public final DTNode<I,D> succSeparator;
		public final Word<I> discriminator;
		
		public Splitter(TTTState<I,D> state1, TTTState<I,D> state2, int symbolIdx) {
			this.state1 = state1;
			this.state2 = state2;
			
			this.symbolIdx = symbolIdx;
			this.succSeparator = null;
			this.discriminator = Word.epsilon();
		}
		
		public Splitter(TTTState<I,D> state1, TTTState<I,D> state2, int symbolIdx, DTNode<I,D> succSeparator) {
			assert !succSeparator.isTemp() && succSeparator.isInner();
			
			this.state1 = state1;
			this.state2 = state2;
			this.symbolIdx = symbolIdx;
			this.succSeparator = succSeparator;
			this.discriminator = succSeparator.getDiscriminator();
		}
	}
	
	/**
	 * A global splitter. In addition to the information stored in a (local)
	 * {@link Splitter}, this class also stores the block the local splitter
	 * applies to.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class GlobalSplitter<I,D> {
		public final Splitter<I,D> localSplitter;
		public final DTNode<I,D> blockRoot;
		
		public GlobalSplitter(DTNode<I,D> blockRoot, Splitter<I,D> localSplitter) {
			this.blockRoot = blockRoot;
			this.localSplitter = localSplitter;
		}
	}
	
	/**
	 * Determines a global splitter, i.e., a splitter for any block.
	 * This method may (but is not required to) employ heuristics
	 * to obtain a splitter with a relatively short suffix length.
	 * 
	 * @return a splitter for any of the blocks
	 */
	private GlobalSplitter<I,D> findSplitterGlobal() {
		// TODO: Make global option
		boolean optimizeGlobal = true;
		
		DTNode<I,D> bestBlockRoot = null;
		
		Splitter<I,D> bestSplitter = null;
		
		Iterator<DTNode<I,D>> blocksIt = blockList.iterator();
		while(blocksIt.hasNext()) {
			DTNode<I,D> blockRoot = blocksIt.next();
			if (finalDiscriminators.contains(blockRoot.getDiscriminator().subWord(1))) {
				declareFinal(blockRoot);
				continue;
			}
			Splitter<I,D> splitter = findSplitter(blockRoot);
			if(splitter != null) {
				if(bestSplitter == null || splitter.discriminator.length()
						< bestSplitter.discriminator.length()) {
					bestSplitter = splitter;
					bestBlockRoot = blockRoot;
				}
				
				if(!optimizeGlobal) {
					break;
				}
			}
		}
		
		if(bestSplitter == null) {
			return null;
		}
		
		return new GlobalSplitter<>(bestBlockRoot, bestSplitter);
	}
	
	/**
	 * Determines a (local) splitter for a given block. This method may
	 * (but is not required to) employ heuristics to obtain a splitter
	 * with a relatively short suffix.
	 *  
	 * @param blockRoot the root of the block
	 * @return a splitter for this block, or {@code null} if no such splitter
	 * could be found.
	 */
	@SuppressWarnings("unchecked")
	private Splitter<I,D> findSplitter(DTNode<I,D> blockRoot) {
		// TODO: Make global option
		boolean optimizeLocal = true;
		
		Iterator<TTTState<I,D>> statesIt = blockRoot.subtreeStatesIterator();
		
		assert statesIt.hasNext();
		
		Object[] properties = new Object[alphabet.size()];
		DTNode<I,D>[] dtTargets = new DTNode[alphabet.size()];
		
		TTTState<I,D> state = statesIt.next();
		
		for(int i = 0; i < dtTargets.length; i++) {
			TTTTransition<I,D> trans = state.transitions[i];
			dtTargets[i] = updateDTTarget(trans, false);
			properties[i] = trans.getProperty();
		}
		
		TTTState<I,D> state1 = state;
		
		assert statesIt.hasNext();
		
		int bestI = -1;
		DTNode<I,D> bestLCA = null;
		
		TTTState<I,D> state2 = null;
		
		while(statesIt.hasNext()) {
			state = statesIt.next();
			
			for(int i = 0; i < dtTargets.length; i++) {
				TTTTransition<I,D> trans = state.transitions[i];
				if (!Objects.equals(properties[i], trans.getProperty())) {
					return new Splitter<I,D>(state1, state, i);
				}
				
				DTNode<I,D> tgt1 = dtTargets[i];
				DTNode<I,D> tgt2 = updateDTTarget(trans, false);
				
				
				DTNode<I,D> lca = dtree.leastCommonAncestor(tgt1, tgt2);
				if(!lca.isTemp() && lca.isInner()) {
					if(!optimizeLocal) {
						return new Splitter<>(state1, state, i, lca);
					}
					if(bestLCA == null || bestLCA.getDiscriminator().length() > lca.getDiscriminator().length()) {
						bestI = i;
						bestLCA = lca;
						state2 = state;
					}
					dtTargets[i] = lca;
				}
				else {
					dtTargets[i] = lca;
				}
			}
		}
		
		if(bestLCA == null) {
			return null;
		}
		return new Splitter<>(state1, state2, bestI, bestLCA);
	}
	
//	/**
//	 * Checks whether the hypothesis is consistent with the discrimination tree.
//	 * If an inconsistency is discovered, it is returned in the form of a counterexample.
//	 * 
//	 * @return a counterexample uncovering an inconsistency, or {@code null}
//	 * if the hypothesis is consistent with the discrimination tree
//	 */
//	// TODO can be removed
//	private DefaultQuery<I, D> checkHypothesisConsistency() {
//		for(DTNode<I,D> leaf : dtree.getRoot().subtreeLeaves()) {
//			TTTState<I,D> state = leaf.state;
//			if(state == null) {
//				continue;
//			}
//			
//			DTNode<I,D> curr = state.dtLeaf;
//			DTNode<I,D> next = curr.getParent();
//			
//			while(next != null) {
//				Word<I> discr = next.getDiscriminator();
//				D expected = curr.getParentEdgeLabel();
//				
//				if(!Objects.equals(computeHypothesisOutput(state, discr), expected)) {
//					return new DefaultQuery<>(state.getAccessSequence(), discr, expected);
//				}
//				curr = next;
//				next = curr.getParent();
//			}
//		}
//		
//		return null;
//	}
	
	/**
	 * Creates a state in the hypothesis. This method cannot be used for the initial
	 * state, which has no incoming tree transition.
	 * 
	 * @param transition the "parent" transition in the spanning tree
	 * @param accepting whether or not the new state state is accepting
	 * @return the newly created state
	 */
	private TTTState<I,D> createState(@Nonnull TTTTransition<I,D> transition) {
		TTTState<I,D> newState = hypothesis.createState(transition);
		
		return newState;
	}
	
	
	/**
	 * Retrieves the target state of a given transition. This method works for both tree
	 * and non-tree transitions. If a non-tree transition points to a non-leaf node,
	 * it is updated accordingly before a result is obtained.
	 * 
	 * @param trans the transition
	 * @return the target state of this transition (possibly after it having been updated)
	 */
	protected TTTState<I,D> getTarget(TTTTransition<I,D> trans) {
		if(trans.isTree()) {
			return trans.getTreeTarget();
		}
		return updateTarget(trans);
	}
	
	/**
	 * Retrieves the successor for a given state and a suffix sequence.
	 * 
	 * @param start the originating state
	 * @param suffix the sequence of input symbols to process
	 * @return the state reached after processing {@code suffix}, starting from
	 * {@code start}
	 */
	protected TTTState<I,D> getState(TTTState<I,D> start, Iterable<? extends I> suffix) {
		TTTState<I,D> curr = start;
		
		for(I sym : suffix) {
			TTTTransition<I,D> trans = hypothesis.getInternalTransition(curr, sym);
			curr = getTarget(trans);
		}
		
		return curr;
	}
	
	/**
	 * Retrieves the state reached by the given sequence of symbols, starting
	 * from the initial state.
	 * @param suffix the sequence of symbols to process
	 * @return the state reached after processing the specified symbols
	 */
	private TTTState<I,D> getState(Iterable<? extends I> suffix) {
		return getState(hypothesis.getInitialState(), suffix);
	}
	
	/**
	 * Finalize a discriminator. Given a block root and a {@link Splitter},
	 * replace the discriminator at the block root by the one derived from the
	 * splitter, and update the discrimination tree accordingly.
	 * 
	 * @param blockRoot the block root whose discriminator to finalize
	 * @param splitter the splitter to use for finalization
	 */
	private void finalizeDiscriminator(DTNode<I,D> blockRoot, Splitter<I,D> splitter) {
		assert blockRoot.isBlockRoot();
		
		notifyPreFinalizeDiscriminator(blockRoot, splitter);
		
		Word<I> finalDiscriminator = prepareSplit(blockRoot, splitter);
		Map<D,DTNode<I,D>> repChildren = createMap();
			
		for (D label : blockRoot.splitData.getLabels()) {
			repChildren.put(label, extractSubtree(blockRoot, label));
		}
		blockRoot.replaceChildren(repChildren);
	
		blockRoot.setDiscriminator(finalDiscriminator);

		declareFinal(blockRoot);
		
		notifyPostFinalizeDiscriminator(blockRoot, splitter);
	}
	
	protected void declareFinal(DTNode<I,D> blockRoot) {
		blockRoot.temp = false;
		blockRoot.splitData = null;
		
		blockRoot.removeFromBlockList();
		finalDiscriminators.add(blockRoot.getDiscriminator());
		
		for (DTNode<I,D> subtree : blockRoot.getChildren()) {
			assert subtree.splitData == null;
			blockRoot.setChild(subtree.getParentEdgeLabel(), subtree);
			// Register as blocks, if they are non-trivial subtrees
			if (subtree.isInner()) {
				blockList.insertBlock(subtree);
			}
		}
	}
	
	/**
	 * Prepare a split operation on a block, by marking all the nodes and
	 * transitions in the subtree (and annotating them with
	 * {@link SplitData} objects).
	 * 
	 * @param node the block root to be split
	 * @param splitter the splitter to use for splitting the block
	 * @return the discriminator to use for splitting
	 */
	private Word<I> prepareSplit(DTNode<I,D> node, Splitter<I,D> splitter) {
		int symbolIdx = splitter.symbolIdx;
		I symbol = alphabet.getSymbol(symbolIdx);
		Word<I> discriminator = splitter.discriminator.prepend(symbol);
		
		Deque<DTNode<I,D>> dfsStack = new ArrayDeque<>();
		
		DTNode<I,D> succSeparator = splitter.succSeparator;
		
		
		dfsStack.push(node);
		assert node.splitData == null;
		
		while(!dfsStack.isEmpty()) {
			DTNode<I,D> curr = dfsStack.pop();
			assert curr.splitData == null;
			
			curr.splitData = new SplitData<>();
			
			
			for(TTTTransition<I,D> trans : curr.getIncoming()) {
				D outcome = query(trans, discriminator);
				curr.splitData.getIncoming(outcome).insertIncoming(trans);
				markAndPropagate(curr, outcome);
			}
			
			if(curr.isInner()) {
				for (DTNode<I,D> child : curr.getChildren()) {
					dfsStack.push(child);
				}
			}
			else {
				TTTState<I,D> state = curr.state;
				assert state != null;
				
				// Try to deduct the outcome from the DT target of
				// the respective transition
				TTTTransition<I,D> trans = state.transitions[symbolIdx];
				// This used to be updateDTTarget(), but this would make 
				// the "incoming" information inconsistent!
				D outcome = predictSuccOutcome(trans, succSeparator);
				if (outcome == null) {
					// OK, we need to do a membership query here
					outcome = query(state, discriminator);
				}
				curr.splitData.setStateLabel(outcome);
				markAndPropagate(curr, outcome);
			}
			
		}
		
		return discriminator;
	}
	
	protected D predictSuccOutcome(TTTTransition<I, D> trans, DTNode<I, D> succSeparator) {
		return null;
	}
	
	/**
	 * Marks a node, and propagates the label up to all nodes on the path from the block
	 * root to this node.
	 * 
	 * @param node the node to mark
	 * @param label the label to mark the node with
	 */
	private static <I,D> void markAndPropagate(DTNode<I,D> node, D label) {
		DTNode<I,D> curr = node;
		
		while(curr != null && curr.splitData != null) {
			if(!curr.splitData.mark(label)) {
				return;
			}
			curr = curr.getParent();
		}
	}
	
	/**
	 * Data structure required during an extract operation. The latter basically
	 * works by copying nodes that are required in the extracted subtree, and this
	 * data structure is required to associate original nodes with their extracted copies.
	 *  
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 */
	private static final class ExtractRecord<I,D> {
		public final DTNode<I,D> original;
		public final DTNode<I,D> extracted;
		
		public ExtractRecord(DTNode<I,D> original, DTNode<I,D> extracted) {
			this.original = original;
			this.extracted = extracted;
		}
	}
	
	/**
	 * Extract a (reduced) subtree containing all nodes with the given label
	 * from the subtree given by its root. "Reduced" here refers to the fact that
	 * the resulting subtree will contain no inner nodes with only one child.
	 * <p>
	 * The tree returned by this method (represented by its root) will have
	 * as a parent node the root that was passed to this method.
	 *  
	 * @param root the root of the subtree from which to extract
	 * @param label the label of the nodes to extract
	 * @return the extracted subtree
	 */
	private DTNode<I,D> extractSubtree(DTNode<I,D> root, D label) {
		assert root.splitData != null;
		assert root.splitData.isMarked(label);
		
		Deque<ExtractRecord<I,D>> stack = new ArrayDeque<>();
		
		DTNode<I,D> firstExtracted = new DTNode<>(root, label);
		
		stack.push(new ExtractRecord<>(root, firstExtracted));
		while(!stack.isEmpty()) {
			ExtractRecord<I,D> curr = stack.pop();
			
			DTNode<I,D> original = curr.original;
			DTNode<I,D> extracted = curr.extracted;
			
			moveIncoming(extracted, original, label);
			
			if(original.isLeaf()) {
				if(Objects.equals(original.splitData.getStateLabel(), label)) {
					link(extracted, original.state);
				}
				else {
					createNewState(extracted);
				}
				extracted.updateIncoming();
			}
			else {
				List<DTNode<I,D>> markedChildren = new ArrayList<>();
				
				for (DTNode<I,D> child : original.getChildren()) {
					if (child.splitData.isMarked(label)) {
						markedChildren.add(child);
					}
				}
				
				if (markedChildren.size() > 1) {
					Map<D,DTNode<I,D>> childMap = createMap();
					for (DTNode<I,D> c : markedChildren) {
						D childLabel = c.getParentEdgeLabel();
						DTNode<I,D> extractedChild = new DTNode<>(extracted, childLabel);
						childMap.put(childLabel, extractedChild);
						stack.push(new ExtractRecord<>(c, extractedChild));
					}
					extracted.split(original.getDiscriminator(), childMap);
					extracted.updateIncoming();
					extracted.temp = true;
				}
				else if (markedChildren.size() == 1) {
					stack.push(new ExtractRecord<>(markedChildren.get(0), extracted));
				}
				else { // markedChildren.isEmppty()
					createNewState(extracted);
					extracted.updateIncoming();
				}
			}	
			
			assert extracted.splitData == null;
		}
		
		return firstExtracted;
	}
	
	protected <V> Map<D,V> createMap() {
		return new HashMap<D,V>();
	}
	
	/**
	 * Moves all transition from the "incoming" list (for a given label) of an
	 * old node to the "incoming" list of a new node.
	 *   
	 * @param newNode the new node
	 * @param oldNode the old node
	 * @param label the label to consider
	 */
	private static <I,D> void moveIncoming(DTNode<I,D> newNode, DTNode<I,D> oldNode, D label) {
		newNode.getIncoming().insertAllIncoming(oldNode.splitData.getIncoming(label));
	}
	
	/**
	 * Create a new state during extraction on-the-fly. This is required if a node
	 * in the DT has an incoming transition with a certain label, but in its subtree
	 * there are no leaves with this label as their state label.
	 * 
	 * @param newNode the extracted node
	 */
	private void createNewState(DTNode<I,D> newNode) {
		TTTTransition<I,D> newTreeTrans = newNode.getIncoming().choose();
		assert newTreeTrans != null;
		
		TTTState<I,D> newState = createState(newTreeTrans);
		link(newNode, newState);
		initializeState(newState);
	}
	
	protected abstract D computeHypothesisOutput(TTTState<I,D> state, Iterable<? extends I> suffix);
	
	/**
	 * Establish the connection between a node in the discrimination tree
	 * and a state of the hypothesis.
	 * 
	 * @param dtNode the node in the discrimination tree
	 * @param state the state in the hypothesis
	 */
	private static <I,D> void link(DTNode<I,D> dtNode, TTTState<I,D> state) {
		assert dtNode.isLeaf();
		
		dtNode.state = state;
		state.dtLeaf = dtNode;
	}

	/*
	 * Access Sequence Transformer API
	 */
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.Output#computeOutput(java.lang.Iterable)
	 */
	@Override
	public D computeOutput(Iterable<? extends I> input) {
		return computeHypothesisOutput(hypothesis.getInitialState(), input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.SuffixOutput#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public D computeSuffixOutput(Iterable<? extends I> prefix,
			Iterable<? extends I> suffix) {
		TTTState<I,D> prefixState = getState(prefix);
		return computeHypothesisOutput(prefixState, suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#transformAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		return getState(word).getAccessSequence();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#isAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public boolean isAccessSequence(Word<I> word) {
		TTTState<I,D> curr = hypothesis.getInitialState();
		for(I sym : word) {
			TTTTransition<I,D> trans = hypothesis.getInternalTransition(curr, sym);
			if(!trans.isTree()) {
				return false;
			}
			curr = trans.getTarget();
		}
		
		return true;
	}
	
	public TTTHypothesis<I, D, ?> getHypothesisDS() {
		return hypothesis;
	}
	
	public DiscriminationTree<I, D>.GraphView dtGraphView() {
		return dtree.graphView();
	}
	
	public GraphDOTHelper<TTTState<I,D>, TTTEdge<I, D>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<>();
	}
	
	/**
	 * Splits a state in the hypothesis, using a temporary discriminator. The state
	 * to be split is identified by an incoming non-tree transition. This transition is
	 * subsequently turned into a spanning tree transition.
	 * 
	 * @param transition the transition
	 * @param tempDiscriminator the temporary discriminator
	 * @return the discrimination tree node separating the old and the new node, labeled
	 * by the specified temporary discriminator
	 */
	private DTNode<I,D> splitState(TTTTransition<I,D> transition, Word<I> tempDiscriminator) {
		assert !transition.isTree();
		
		notifyPreSplit(transition, tempDiscriminator);
		
		DTNode<I,D> dtNode = transition.getNonTreeTarget();
		TTTState<I,D> oldState = dtNode.state;
		assert oldState != null;
		
		TTTState<I,D> newState = createState(transition);
		
		D oldOut = query(oldState, tempDiscriminator);
		D newOut = query(newState, tempDiscriminator);
		
		DTNode<I,D>[] children = split(dtNode, tempDiscriminator, oldOut, newOut);
		
		link(children[0], oldState);
		link(children[1], newState);
		
		initializeState(newState);
		
		if(isOld(oldState)) {
			for(TTTTransition<I,D> incoming : dtNode.getIncoming()) {
				openTransitions.offer(incoming);
			}
		}
		
		dtNode.temp = true;
		
		if(dtNode.getParent() == null || !dtNode.getParent().isTemp()) {
			blockList.insertBlock(dtNode);
		}
		
		notifyPostSplit(transition, tempDiscriminator);
		
		return dtNode;
	}
	
	/**
	 * Checks whether the given state is old, i.e., was added to the hypothesis before the
	 * most recent call to {@link #closeTransitions()}.
	 * 
	 * @param state the state to check
	 * @return {@code true} if this state is old, {@code false} otherwise
	 */
	private boolean isOld(@Nonnull TTTState<I,D> state) {
		return state.id < lastGeneration;
	}

	/**
	 * Ensures that all non-tree transitions in the hypothesis point to leaf nodes.
	 */
	private void closeTransitions() {
		while(!openTransitions.isEmpty()) {
			TTTTransition<I,D> trans = openTransitions.poll();
			closeTransition(trans);
		}
		this.lastGeneration = hypothesis.size();
	}
	
	/**
	 * Ensures that the specified transition points to a leaf-node. If the transition
	 * is a tree transition, this method has no effect.
	 * 
	 * @param trans the transition
	 */
	private void closeTransition(TTTTransition<I,D> trans) {
		if(trans.isTree()) {
			return;
		}
		
		updateTarget(trans);
	}
	
	/**
	 * Updates the transition to point to a leaf in the discrimination tree, and
	 * returns this leaf.
	 * 
	 * @param transition the transition
	 * @return the DT leaf corresponding to the transition's target state
	 */
	private DTNode<I,D> updateDTTarget(TTTTransition<I,D> transition) {
		return updateDTTarget(transition, true);
	}
	
	private TTTState<I, D> updateTarget(TTTTransition<I, D> trans) {
		DTNode<I,D> node = updateDTTarget(trans);
		
		TTTState<I,D> state = node.state;
		if (state == null) {
			state = createState(trans);
			link(node, state);
			initializeState(state);
		}
		
		return state;
	}
	
	/**
	 * Updates the transition to point to either a leaf in the discrimination tree,
	 * or---if the {@code hard} parameter is set to {@code false}---to a block
	 * root.
	 * 
	 * @param transition the transition
	 * @param hard whether to consider leaves as sufficient targets only
	 * @return the new target node of the transition
	 */
	private DTNode<I,D> updateDTTarget(TTTTransition<I,D> transition, boolean hard) {
		if(transition.isTree()) {
			return transition.getTreeTarget().dtLeaf;
		}
		
		DTNode<I,D> dt = transition.getNonTreeTarget();
		dt = dtree.sift(dt, transition, hard);
		transition.setNonTreeTarget(dt);
		
		return dt;
	}
	
	
	/**
	 * Performs a membership query.
	 * 
	 * @param prefix the prefix part of the query
	 * @param suffix the suffix part of the query
	 * @return the output
	 */
	protected D query(Word<I> prefix, Word<I> suffix) {
		return MQUtil.output(oracle, prefix, suffix);
	}
	
	/**
	 * Performs a membership query, using an access sequence as its prefix.
	 * 
	 * @param accessSeqProvider the object from which to obtain the access sequence
	 * @param suffix the suffix part of the query
	 * @return the output
	 */
	protected D query(AccessSequenceProvider<I> accessSeqProvider, Word<I> suffix) {
		return query(accessSeqProvider.getAccessSequence(), suffix);
	}
	
	/**
	 * Returns the discrimination tree.
	 * @return the discrimination tree
	 */
	public DiscriminationTree<I,D> getDiscriminationTree() {
		return dtree;
	}

	@SafeVarargs
	protected final DTNode<I,D>[] split(DTNode<I, D> node, Word<I> discriminator, D... outputs) {
		return node.split(discriminator, this.<DTNode<I,D>>createMap(), outputs);
	}
	
	
	private void notifyPreFinalizeDiscriminator(DTNode<I, D> blockRoot, Splitter<I,D> splitter) {
		for (TTTEventListener<I, D> listener : eventListeners()) {
			listener.preFinalizeDiscriminator(blockRoot, splitter);
		}
	}
	
	private void notifyPostFinalizeDiscriminator(DTNode<I, D> blockRoot, Splitter<I,D> splitter) {
		for (TTTEventListener<I, D> listener : eventListeners()) {
			listener.postFinalizeDiscriminator(blockRoot, splitter);
		}
	}
	
	private void notifyEnsureConsistency(TTTState<I,D> state, DTNode<I,D> discriminator, D realOutcome) {
		for (TTTEventListener<I, D> listener : eventListeners()) {
			listener.ensureConsistency(state, discriminator, realOutcome);
		}
	}
	
	private void notifyPreSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator) {
		for (TTTEventListener<I, D> listener : eventListeners()) {
			listener.preSplit(transition, tempDiscriminator);
		}
	}
	
	private void notifyPostSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator) {
		for (TTTEventListener<I, D> listener : eventListeners()) {
			listener.postSplit(transition, tempDiscriminator);
		}
	}
	
	private Iterable<TTTEventListener<I, D>> eventListeners() {
		return new Iterable<TTTEventListener<I,D>>() {
			@Override
			public Iterator<TTTEventListener<I, D>> iterator() {
				final Iterator<WeakReference<TTTEventListener<I, D>>> iterator = eventListeners.iterator();
				return new AbstractIterator<TTTEventListener<I,D>>() {
					@Override
					protected TTTEventListener<I, D> computeNext() {
						while (iterator.hasNext()) {
							WeakReference<TTTEventListener<I, D>> ref = iterator.next();
							TTTEventListener<I, D> listener = ref.get();
							if (listener != null) {
								return listener;
							}
							iterator.remove();
						}
						return endOfData();
					}
					
				};
			}
		};
	}
	
	public void addEventListener(TTTEventListener<I, D> listener) {
		eventListeners.add(new WeakReference<TTTEventListener<I,D>>(listener));
	}
	
	public void removeEventListener(TTTEventListener<I, D> listener) {
		eventListeners.remove(listener);
	}
	
}
