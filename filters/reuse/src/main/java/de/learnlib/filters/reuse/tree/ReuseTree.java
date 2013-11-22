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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;

/**
 * TODO JavaDoc.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * 
 * @param <S>
 * @param <I>
 * @param <O>
 */
public class ReuseTree<S, I, O> {
	private ReuseNode<S, I, O> root;

	private boolean useFailureOutputKnowledge = true;
	private boolean useModelInvariantSymbols = true;

	private final Logger logger = Logger.getLogger(getClass().getName());

	private Set<I> invariantInputSymbols;
	private Set<O> failureOutputSymbols;

	private SystemStateHandler<S> systemStateHandler;

	/**
	 * Default constructor. Usage of domain knowledge about 'failure outputs'
	 * and 'model invariant input symbols' is enabled.
	 */
	public ReuseTree() {
		this.root = new ReuseNode<>(Word.<I> epsilon(), Word.<O> epsilon());
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
	 * (indicated by a reflexive {@link ReuseEdge} in the {@link ReuseTree}
	 * ), otherwise only if the MQ ends with a 'model invariant input symbol' or
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
		for (int i = 0; i < query.size(); i++) {
			ReuseNode<S, I, O> node = sink.getTargetNodeForInput(query
					.getSymbol(i));
			ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(query.getSymbol(i));

			if (node == null) {
				return null;
			}

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
		systemStateHandler.dispose(node.getSystemState());
		node.setSystemState(null);
		for (ReuseEdge<S, I, O> edge : node.getEdges()) {
			if (!edge.getTarget().equals(node)) {
				// only for non reflexive edges, there are no circles in a tree
				disposeSystemstates(edge.getTarget());
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
		this.root = new ReuseNode<>(Word.<I> epsilon(), Word.<O> epsilon());
		this.invariantInputSymbols.clear();
		this.failureOutputSymbols.clear();
	}

	/**
	 * Returns a reuseable {@link ReuseNode} or <code>null</code> if none such
	 * exists.
	 * 
	 * @param query
	 *            Not allowed to be <code>null</code>.
	 * @return
	 */
	public ReuseNode<S, I, O> getReuseableSystemState(Word<I> query) {
		if (query == null) {
			String msg = "Query is not allowed to be null.";
			throw new IllegalArgumentException(msg);
		}

		int length = 0;

		List<I> prefixInput = new LinkedList<>();
		List<O> prefixOutput = new LinkedList<>();

		ReuseNode<S, I, O> sink = getRoot();
		ReuseNode<S, I, O> lastState = null;
		if (sink.hasState()) {
			lastState = sink;
		}

		for (int i = 0; i < query.size(); i++) {
			ReuseNode<S, I, O> node = sink.getTargetNodeForInput(query
					.getSymbol(i));
			ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(query.getSymbol(i));

			if (node == null) {
				break;
			}

			prefixInput.add(query.getSymbol(i));
			prefixOutput.add(edge.getOutput());

			sink = node;
			if (sink.hasState()) {
				lastState = sink;
				length = i + 1;
			}
		}

		if (length == 0 && lastState == null) {
			return null;
		}
		return lastState;
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
	 * would be called). If {@link QueryResult#oldInvalidated} is
	 * <code>true</code> the used system state could not be reused again, only
	 * the new fined in {@link QueryResult#newState}.
	 * <p>
	 * This method should only be invoked internally from the
	 * {@link ReuseOracle} unless you know exactly what you are doing (you may
	 * want to create a predefined reuse tree before start learning).
	 * 
	 * @param query
	 * @param sink
	 * @param queryResult
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

		S oldSystemState = sink.getSystemState();
		if (queryResult.oldInvalidated) {
			// systemStateHandler.dispose(oldSystemState);
			sink.setSystemState(null);
		}

		for (int i = 0; i < query.size(); i++) {
			I in = query.getSymbol(i);
			O out = queryResult.output.getSymbol(i);
			ReuseNode<S, I, O> rn;

			ReuseEdge<S, I, O> edge = sink.getEdgeWithInput(in);
			if (edge != null) {
				if (edge.getOutput().equals(out)) {
					sink = edge.getTarget();
					continue;
				}

				// TODO use own exception, override getMessage
				// there is a conflict!!!
				StringBuilder sb = new StringBuilder();
				sb.append("Prefixoutput mitmatch!:\n  adding query input ");
				sb.append(query).append("\n  with query output ");
				sb.append(queryResult.output).append("\n  on system state ");
				sb.append(oldSystemState).append(
						"\n  revealing in system state ");
				sb.append(queryResult.newState).append(
						"\n\n  mismatch on index ");
				sb.append(i).append(":\n  input symbol ").append(in);
				sb.append("\n  output symbol ").append(out);
				sb.append("\n  but tree contains output symbol ");
				sb.append(edge.getOutput());
				sb.append("\n\n  full prefix input/output in tree:\n ");
				WordBuilder<I> treeInput = new WordBuilder<>();
				WordBuilder<O> treeOutput = new WordBuilder<>();
				ReuseNode<S, I, O> node = getRoot();
				for (I inp : sink.getPrefixInput()) {
					treeInput.add(inp);
					treeOutput.add(node.getEdgeWithInput(inp).getOutput());
					node = node.getEdgeWithInput(inp).getTarget();
				}
				for (int j = 0; j < i; j++) {
					I inp = query.getSymbol(j);
					O outp = node.getEdgeWithInput(inp).getOutput();
					treeInput.add(inp);
					treeOutput.add(outp);
					node = node.getTargetNodeForInput(inp);
				}
				sb.append(" ");
				sb.append(treeInput.toWord());
				sb.append("/");
				sb.append(treeOutput.toWord());
				sb.append("\n");
				throw new RuntimeException(sb.toString());
			}

			if (useFailureOutputKnowledge) {
				if (failureOutputSymbols.contains(out)) {
					rn = sink;
				} else if (useModelInvariantSymbols
						&& invariantInputSymbols.contains(in)) {
					rn = sink;
				} else {
					rn = new ReuseNode<>(sink.getPrefixInput().append(in), sink
							.getPrefixOutput().append(out));
				}
			} else {
				if (useModelInvariantSymbols
						&& invariantInputSymbols.contains(in)) {
					rn = sink;
				} else {
					rn = new ReuseNode<>(sink.getPrefixInput().append(in), sink
							.getPrefixOutput().append(out));
				}
			}

			sink.addEdge(new ReuseEdge<>(sink, rn, in, out));
			sink = rn;
		}
		sink.setSystemState(queryResult.newState);
	}
}