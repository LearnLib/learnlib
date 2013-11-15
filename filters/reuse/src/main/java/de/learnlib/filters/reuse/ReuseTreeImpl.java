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
package de.learnlib.filters.reuse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.automatalib.words.Word;
import de.learnlib.filters.reuse.api.ReuseEdge;
import de.learnlib.filters.reuse.api.ReuseNode;
import de.learnlib.filters.reuse.api.SystemStateRef;
import de.learnlib.logging.LearnLogger;

/**
 * The {@link ReuseTreeImpl} holds all informations about already seen input/output
 * behavior from the system under test. It provides the possibility to
 * reference the internal system state.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class ReuseTreeImpl<S extends SystemStateRef<?, I, O>, I, O> {
	private ReuseNode<S,I,O> root;

	private boolean useFailureOutputKnowledge = true;
	private boolean useModelInvariantSymbols = true;

	private final LearnLogger logger = LearnLogger.getLogger(ReuseTreeImpl.class.getName());

	private Set<I> readOnlyInputSymbols;
	private Set<O> failureOutputSymbols;

	/**
	 * Default constructor. Usage of domain knowledge about 'failure outputs' and
	 * 'model invariant input symbols' is enabled.
	 */
	public ReuseTreeImpl() {
		this.root = new ReuseNode<>("root");
		this.readOnlyInputSymbols = new HashSet<>();
		this.failureOutputSymbols = new HashSet<>();
	}

	/**
	 * Adds a 'model invariant' input symbol to the set of 'read only' input
	 * symbols.
	 *
	 * @param input
	 * 		not allowed to be <code>null</code>.
	 * @see #useModelInvariantSymbols(boolean)
	 */
	public void addReadOnlyInputSymbol(I input) {
		this.logger.info("Added read only symbol: " + input);
		if (input == null) {
			throw new IllegalArgumentException("Input is not allowed to be null.");
		}
		this.readOnlyInputSymbols.add(input);
	}

	/**
	 * Adds a 'failure output' symbol to the set of failure output symbols.
	 *
	 * @param output
	 * 		not allowed to be <code>null</code>.
	 * @see #useFailureOutputKnowledge(boolean)
	 */
	public void addFailureOutputSymbol(O output) {
		this.logger.info("Added failure output: " + output);
		if (output == null) {
			throw new IllegalArgumentException("Output is not allowed to be null.");
		}
		this.failureOutputSymbols.add(output);
	}

	/**
	 * Whether to use domain knowledge about 'failure' outputs. If a membership
	 * query MQ is answered with some specific 'failure' output, the
	 * {@link SystemState} can be reused for the direct prefix of this query
	 * (indicated by a reflexive {@link ReuseEdge} in the {@link ReuseTreeImpl}),
	 * otherwise only if the MQ ends with a 'model invariant input symbol' or
	 * the new query is a continuation of this query.
	 *
	 * @param b
	 * @see #addFailureOutputSymbol(V)
	 */
	public void useFailureOutputKnowledge(boolean b) {
		this.useFailureOutputKnowledge = b;
	}

	/**
	 * Whether to use domain knowledge about 'model invariant' input symbols.
	 * Symbols are 'model invariant' if they never changes a state of the
	 * inferred model (independently from the output), i.e all transitions under
	 * the input are reflexive. This will be reflected by a reflexive
	 * {@link ReuseEdge} in the {@link ReuseTreeImpl}.
	 *
	 * @param b
	 * @see #addReadOnlyInputSymbol(S)
	 */
	public void useModelInvariantSymbols(boolean b) {
		this.useModelInvariantSymbols = b;
	}

	/**
	 * Returns the root {@link ReuseNode} of the {@link ReuseTreeImpl}.
	 *
	 * @return root The root of the tree, never <code>null</code>.
	 */
	public ReuseNode<S,I,O> getRoot() {
		return this.root;
	}

	/**
	 * Returns the known output for the given query or <code>null</code> if not
	 * known.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return The output for <code>query</code> if already known from the
	 *         {@link ReuseTreeImpl} or <code>null</code> if unknown.
	 */
	public Word<O> getOutput(final Word<I> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query is not allowed to be null.");
		}

		List<O> output = new ArrayList<>(query.size());

		ReuseNode<S,I,O> sink = getRoot();
		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode<S,I,O> node = sink.getTargetNodeForInput(query.getSymbol(i));
			ReuseEdge<S,I,O> edge = sink.getEdgeWithInput(query.getSymbol(i));

			if (node == null) {
				return null;
			}

			output.add(edge.getOutput());
			sink = node;
		}

		return Word.fromList(output);
	}

	/**
	 * Checks whether there exists a prefix of <code>query</code> (possibly the
	 * empty word) that has a {@link SystemState} that could be reused.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return Whether there exists a reuseable {@link SystemState} or not.
	 */
	public boolean hasReuseableSystemState(final Word<I> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query not allowed to be null.");
		}

		int length = 0;

		List<I> prefixInput = new LinkedList<>();
		List<O> prefixOutput = new LinkedList<>();

		ReuseNode<S,I,O> sink = getRoot();
		ReuseNode<S,I,O> lastState = null;
		if (sink.hasState()) {
			lastState = sink;
		}

		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode<S,I,O> node = sink.getTargetNodeForInput(query.getSymbol(i));
			ReuseEdge<S,I,O> edge = sink.getEdgeWithInput(query.getSymbol(i));

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
			return false;
		}

		return true;
	}

	/**
	 * Returns a reuseable {@link SystemState} or <code>null</code> if none such
	 * exists.
	 *
	 * @param query
	 * 		Not allowed to be <code>null</code>.
	 * @return
	 * @see #hasReuseableSystemState(Word)
	 */
	public S getReuseableSystemState(Word<I> query) {
		if (query == null) {
			throw new IllegalArgumentException("Query is not allowed to be null.");
		}

		int length = 0;

		List<I> prefixInput = new LinkedList<>();
		List<O> prefixOutput = new LinkedList<>();

		ReuseNode<S,I,O> sink = getRoot();
		ReuseNode<S,I,O> lastState = null;
		if (sink.hasState()) {
			lastState = sink;
		}

		for (int i = 0; i <= query.size() - 1; i++) {
			ReuseNode<S,I,O> node = sink.getTargetNodeForInput(query.getSymbol(i));
			ReuseEdge<S,I,O> edge = sink.getEdgeWithInput(query.getSymbol(i));

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

		S systemState = lastState.getSystemState();
		lastState.setSystemState(null);

		systemState.setPrefixInput(Word.fromList(prefixInput).prefix(length));
		systemState.setPrefixOutput(Word.fromList(prefixOutput).prefix(length));
		
		return systemState;
	}

	/**
	 * Adds a {@link SystemState} the leaf that will be created in the
	 * {@link ReuseTreeImpl}. If there already exists a prefix of the corresponding
	 * input {@link Word} from the {@link SystemState}, this method will also
	 * check that be prefix consists of the same output symbols as the new one.
	 * If not a {@link RuntimeException} will be thrown.
	 *
	 * @param map
	 * 		Not allowed to be <code>null</code>.
	 */
	public void insert(S map) {
		if (map == null) {
			throw new IllegalArgumentException("The systemstate is not allowed to be null.");
		}

		ReuseNode<S,I,O> sink = getRoot();

		List<I> suffixInput = new LinkedList<>();
		List<O> suffixOutput = new LinkedList<>();

		Word<I> input = map.getPrefixInput();
		Word<O> output = map.getPrefixOutput();

		for (int i = 0; i <= input.size() - 1; i++) {
			I ii = input.getSymbol(i);
			O oi = output.getSymbol(i);

			ReuseEdge<S,I, O> e = sink.getEdgeWithInput(ii);
			if (e != null) {
				if (!e.getOutput().equals(oi.toString().trim())) {
					StringBuilder sb = new StringBuilder();
					sb.append("Error while inserting system state: \n:  ");
					sb.append("Input  = ").append(input).append("\n:  ");
					sb.append("Output = ").append(output).append("\n:  ");
					sb.append("inputsymbol ").append(ii);
					sb.append(" at position ").append(i);
					sb.append("\n:  has outputsymbol ").append(oi);
					sb.append("\n:  but the output should have been ");
					sb.append(e.getOutput());

					logger.warning(sb.toString());

					throw new RuntimeException(sb.toString());
				}
			}

			ReuseNode<S,I,O> n = sink.getTargetNodeForInput(ii);
			if (n == null) {
				for (int j = i; j <= input.size() - 1; j++) {
					suffixInput.add(input.getSymbol(j));
					suffixOutput.add(output.getSymbol(j));
				}

				break;
			}
			sink = n;
		}

		for (int i = 0; i <= suffixInput.size() - 1; i++) {
			I in = Word.fromList(suffixInput).getSymbol(i);
			O out = Word.fromList(suffixOutput).getSymbol(i);
			ReuseNode<S,I,O> rn;

			if (useFailureOutputKnowledge) {
				if (failureOutputSymbols.contains(out)) {
					rn = sink;
				}
				else if (useModelInvariantSymbols && readOnlyInputSymbols.contains(in)) {
					rn = sink;
				}
				else {
					rn = new ReuseNode<>(sink.getName() + " " + in);
				}
			}
			else {
				if (useModelInvariantSymbols
						&& readOnlyInputSymbols.contains(in)) {
					rn = sink;
				}
				else {
					rn = new ReuseNode<>(sink.getName() + " " + in);
				}
			}

			sink.addEdge(new ReuseEdge<>(sink, rn, in, out));
			sink = rn;
		}

		sink.setSystemState(map);
	}
}
