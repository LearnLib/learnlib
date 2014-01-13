/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.filters.reuse.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filters.reuse.ReuseException;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.tree.ReuseNode.NodeResult;

/**
 * The {@link ReuseTree} is a tree like structure consisting of nodes (see
 * {@link ReuseNode}) and edges (see {@link ReuseEdge}) that is used by the
 * {@link ReuseOracle}:
 * <ul>
 * <li>Nodes may contain a system state (see {@link ReuseNode#getSystemState()})
 * that could be used for executing suffixes of membership queries. Each node
 * consists of a (possible empty) set of outgoing edges.
 * <li>Edges consists beside source and target node of input and output
 * behavior.
 * </ul>
 * The {@link ReuseTree} is the central data structure that maintains observed
 * behavior from the SUL and maintains also available system states. The
 * {@link ReuseTree} is only 'tree like' since it may contain reflexive edges at
 * nodes (only possible if {@link #useFailureOutputKnowledge(boolean)} is set to
 * <code>true</code> and and {@link #failureOutputSymbols} is not empty or
 * {@link #useModelInvariantSymbols(boolean)} is set to <code>true</code> and
 * {@link #invariantInputSymbols} is not empty).
 * <p>
 * The {@link ReuseTree} is not meant to be used directly! It should only be
 * configured once (retrieved via {@link ReuseOracle#getReuseTree()}) or
 * reseted via {@link #clearTree()} or {@link #disposeSystemstates()}.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * 
 * @param <S>
 * @param <I>
 * @param <O>
 */
public class ReuseTree<S, I, O> extends AbstractGraph<ReuseNode<S, I, O>, ReuseEdge<S, I, O>>
	implements DOTPlottableGraph<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> {
	
	private ReuseNode<S, I, O> root;
	private Alphabet<I> alphabet;
	private int alphabetSize;
	
	private boolean useFailureOutputKnowledge = true;
	private boolean useModelInvariantSymbols = true;

	private final Logger logger = Logger.getLogger(getClass().getName());

	private Set<I> invariantInputSymbols;
	private Set<O> failureOutputSymbols;

	private final boolean invalidateSystemstates;
	private SystemStateHandler<S> systemStateHandler;
	private int nodeCount = 0;
	
	/**
	 * Default constructor. Usage of domain knowledge about 'failure outputs'
	 * and 'model invariant input symbols' is enabled.
	 */
	public ReuseTree(Alphabet<I> alphabet, boolean invalidateSystemstates) {
		this.alphabet = alphabet;
		this.invalidateSystemstates = invalidateSystemstates;
		this.alphabetSize = alphabet.size();
		this.root = new ReuseNode<>(nodeCount++, alphabetSize);
		this.invariantInputSymbols = new HashSet<>();
		this.failureOutputSymbols = new HashSet<>();
		this.systemStateHandler = new SystemStateHandler<S>() {
			@Override
			public void dispose(final S state) {
			}
		};
	}

	/**
	 * Sets a specific implementation of a system state handler that could
	 * handle concrete disposings of system states.
	 * 
	 * @param handler
	 */
	public final void setSystemStateHandler(final SystemStateHandler<S> handler) {
		this.systemStateHandler = handler;
	}

	/**
	 * Adds a 'model invariant' input symbol to the set of 'read only' input
	 * symbols.
	 * 
	 * @param input
	 *            not allowed to be <code>null</code>.
	 * @see #useModelInvariantSymbols(boolean)
	 */
	public final void addInvariantInputSymbol(final I input) {
		if (input == null) {
			String msg = "Input is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}
		if (this.logger.isLoggable(Level.FINE)) {
			this.logger.fine("Added invariant input symbol: " + input);
		}
		this.invariantInputSymbols.add(input);
	}

	/**
	 * Adds a 'failure output' symbol to the set of failure output symbols.
	 * 
	 * @param output
	 *            not allowed to be <code>null</code>.
	 * @see #useFailureOutputKnowledge(boolean)
	 */
	public final void addFailureOutputSymbol(final O output) {
		if (output == null) {
			String msg = "Output is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}
		if (this.logger.isLoggable(Level.FINE)) {
			this.logger.fine("Added failure output: " + output);
		}
		this.failureOutputSymbols.add(output);
	}

	/**
	 * Whether to use domain knowledge about 'failure' outputs. If a membership
	 * query MQ is answered with some specific 'failure' output, the
	 * {@link SystemState} can be reused for the direct prefix of this query
	 * (indicated by a reflexive {@link ReuseEdge} in the {@link ReuseTree} ),
	 * otherwise only if the MQ ends with a 'model invariant input symbol' or
	 * the new query is a continuation of this query.
	 * 
	 * @param b
	 * @see #addFailureOutputSymbol(V)
	 */
	public final void useFailureOutputKnowledge(final boolean b) {
		this.useFailureOutputKnowledge = b;
	}

	/**
	 * Whether to use domain knowledge about 'model invariant' input symbols.
	 * Symbols are 'model invariant' if they never changes a state of the
	 * inferred model (independently from the output), i.e all transitions under
	 * the input are reflexive. This will be reflected by a reflexive
	 * {@link ReuseEdge} in the {@link ReuseTree}.
	 * 
	 * @param b
	 * @see #addInvariantInputSymbol(S)
	 */
	public final void useModelInvariantSymbols(final boolean b) {
		this.useModelInvariantSymbols = b;
	}

	/**
	 * Returns the root {@link ReuseNode} of the {@link ReuseTree}.
	 * 
	 * @return root The root of the tree, never <code>null</code>.
	 */
	public final ReuseNode<S, I, O> getRoot() {
		return this.root;
	}

	/**
	 * Returns the known output for the given query or <code>null</code> if not
	 * known.
	 * 
	 * @param query
	 *            Not allowed to be <code>null</code>.
	 * @return The output for <code>query</code> if already known from the
	 *         {@link ReuseTree} or <code>null</code> if unknown.
	 */
	public final Word<O> getOutput(final Word<I> query) {
		if (query == null) {
			String msg = "Query is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}

		WordBuilder<O> output = new WordBuilder<>();

		ReuseNode<S, I, O> sink = getRoot();
		ReuseNode<S, I, O> node;
		ReuseEdge<S, I, O> edge;
		for (I symbol : query) {
			int index = alphabet.getSymbolIndex(symbol);
			edge = sink.getEdgeWithInput(index);

			if (edge == null) {
				return null;
			}

			node = edge.getTarget();
			output.add(edge.getOutput());
			sink = node;
		}

		return output.toWord();
	}

	/**
	 * This method removes all system states from the tree. The tree structure
	 * remains, but there will be nothing for reusage.
	 * <p>
	 * The {@link SystemStateHandler} (
	 * {@link #setSystemStateHandler(SystemStateHandler)}) will be informed
	 * about all disposings.
	 */
	public final void disposeSystemstates() {
		disposeSystemstates(getRoot());
	}

	private void disposeSystemstates(ReuseNode<S, I, O> node) {
		if (node.getSystemState() != null) {
			systemStateHandler.dispose(node.getSystemState());
		}
		node.setSystemState(null);
		for (ReuseEdge<S, I, O> edge : node.getEdges()) {
			if (edge != null) {
				if (!edge.getTarget().equals(node)) {
					// only for non reflexive edges, there are no circles in a tree
					disposeSystemstates(edge.getTarget());
				}
			}
		}
	}

	/**
	 * Clears the whole tree which means the root will be reinitialized by a new
	 * {@link ReuseNode} and all invariant input symbols as well as all failure
	 * output symbols will be removed.
	 * <p>
	 * The {@link SystemStateHandler} (
	 * {@link #setSystemStateHandler(SystemStateHandler)}) will <b>not</b> be
	 * informed about any disposings.
	 */
	public void clearTree() {
		this.root = new ReuseNode<>(nodeCount++, alphabetSize);
		this.invariantInputSymbols.clear();
		this.failureOutputSymbols.clear();
	}

	/**
	 * Returns a reuseable {@link NodeResult} with system state 
	 * or <code>null</code> if none such exists. If
	 * ''oldInvalidated'' was set to <code>true</code> (in the {@link ReuseOracle})
	 * the system state is already removed from the tree whenever
	 * one was available.
	 * 
	 * @param query
	 *            Not allowed to be <code>null</code>.
	 * @return
	 */
	public NodeResult<S,I,O> fetchSystemState(Word<I> query) {
		if (query == null) {
			String msg = "Query is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}

		int length = 0;

		ReuseNode<S, I, O> sink = getRoot();
		ReuseNode<S, I, O> lastState = null;
		if (sink.getSystemState() != null) {
			lastState = sink;
		}

		ReuseNode<S, I, O> node;
		for (int i = 0; i < query.size(); i++) {
			node = sink.getTargetNodeForInput(alphabet.getSymbolIndex(query.getSymbol(i)));

			if (node == null) {
				// we have reached longest known prefix
				break;
			}

			sink = node;
			if (sink.getSystemState() != null) {
				lastState = sink;
				length = i + 1;
			}
		}

		if (lastState == null) {
			return null;
		} else {
			S systemState = lastState.getSystemState();
			if (invalidateSystemstates) {
				lastState.setSystemState(null);
			}
			return new NodeResult<>(lastState, systemState, length);
		}
	}

	/**
	 * Inserts the given {@link Word} with {@link QueryResult} into the tree
	 * starting from the root node of the tree. For the longest known prefix of
	 * the given {@link Word} there will be no new nodes or edges created.
	 * <p>
	 * Will be called from the {@link ReuseOracle} if no system state was
	 * available for reusage for the query (otherwise
	 * {@link #insert(Word, ReuseNode, QueryResult)} would be called). The last
	 * node reached by the last symbol of the query will hold the system state
	 * from the given {@link QueryResult}.
	 * <p>
	 * This method should only be invoked internally from the
	 * {@link ReuseOracle} unless you know exactly what you are doing (you may
	 * want to create a predefined reuse tree before start learning).
	 * 
	 * @param query
	 * @param queryResult
	 * 
	 * @throws ReuseException if non deterministic behavior is detected
	 */
	public void insert(Word<I> query, QueryResult<S, O> queryResult) {
		insert(query, getRoot(), queryResult);
	}

	/**
	 * Inserts the given {@link Word} (suffix of a membership query) with
	 * {@link QueryResult} (suffix output) into the tree starting from the
	 * {@link ReuseNode} (contains prefix with prefix output) in the tree. For
	 * the longest known prefix of the suffix from the given {@link Word} there
	 * will be no new nodes or edges created.
	 * <p>
	 * Will be called from the {@link ReuseOracle} if an available system state
	 * was reused for the query (otherwise {@link #insert(Word, QueryResult)}
	 * would be called). The old system state was already removed from the
	 * {@link ReuseNode} (through {@link #fetchSystemState(Word)}) 
	 * if the ''invalidateSystemstates'' flag in the {@link ReuseOracle}
	 * was set to <code>true</code>.
	 * <p>
	 * This method should only be invoked internally from the
	 * {@link ReuseOracle} unless you know exactly what you are doing (you may
	 * want to create a predefined reuse tree before start learning).
	 * 
	 * @param query
	 * @param sink
	 * @param queryResult
	 * 
	 * @throws ReuseException if non deterministic behavior is detected
	 */
	public void insert(Word<I> query, ReuseNode<S, I, O> sink,
			QueryResult<S, O> queryResult) {
		if (queryResult == null) {
			String msg = "The queryResult is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}
		if (sink == null) {
			String msg = "Node is not allowed to be null, called wrong method?";
			throw new IllegalArgumentException(msg);
		}
		if (query.size() != queryResult.output.size()) {
			String msg = "Size mismatch: " + query + "/" + queryResult.output;
			throw new IllegalArgumentException(msg);
		}

		for (int i = 0; i < query.size(); i++) {
			I in = query.getSymbol(i);
			O out = queryResult.output.getSymbol(i);
			ReuseNode<S, I, O> rn;

			ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(alphabet.getSymbolIndex(in));
			if (edge != null) {
				if (Objects.equals(edge.getOutput(), out)) {
					sink = edge.getTarget();
					continue;
				}

				StringBuilder sb = new StringBuilder();
				sb.append("Conflict: input '");
				sb.append(query).append("', output '");
				sb.append(queryResult.output).append("', i=");
				sb.append(i).append(", cached output '");
				sb.append(edge.getOutput()).append("'");
				throw new ReuseException(sb.toString());
			}

			if (useFailureOutputKnowledge) {
				if (failureOutputSymbols.contains(out)) {
					rn = sink;
				} else if (useModelInvariantSymbols
						&& invariantInputSymbols.contains(in)) {
					rn = sink;
				} else {
					rn = new ReuseNode<>(nodeCount++, alphabetSize);
				}
			} else {
				if (useModelInvariantSymbols
						&& invariantInputSymbols.contains(in)) {
					rn = sink;
				} else {
					rn = new ReuseNode<>(nodeCount++, alphabetSize);
				}
			}

			int index = alphabet.getSymbolIndex(in);
			sink.addEdge(index, new ReuseEdge<>(sink, rn, in, out));
			sink = rn;
		}
		sink.setSystemState(queryResult.newState);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.Graph#getNodes()
	 */
	@Override
	public Collection<ReuseNode<S, I, O>> getNodes() {
		Collection<ReuseNode<S, I, O>> collection = new ArrayList<>();
		recursiveNoder(collection, getRoot());
		return collection;
	}
	
	private void recursiveNoder(Collection<ReuseNode<S, I, O>> nodes, ReuseNode<S, I, O> current) {
		nodes.add(current);
		for (int i=0; i<alphabetSize; i++) {
			ReuseEdge<S, I, O> reuseEdge = current.getEdgeWithInput(i);
			if (reuseEdge == null) {
				continue;
			}
			if (!current.equals(reuseEdge.getTarget())) {
				recursiveNoder(nodes, reuseEdge.getTarget());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getOutgoingEdges(java.lang.Object)
	 */
	@Override
	public Collection<ReuseEdge<S, I, O>> getOutgoingEdges(	ReuseNode<S, I, O> node) {
		return node.getEdges();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getTarget(java.lang.Object)
	 */
	@Override
	public ReuseNode<S, I, O> getTarget(ReuseEdge<S, I, O> edge) {
		if (edge != null)
			return edge.getTarget();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DOTPlottableGraph#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> getGraphDOTHelper() {
		return new ReuseTreeDotHelper<>();
	}
}