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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filters.reuse.tree.ReuseNode;
import de.learnlib.filters.reuse.tree.ReuseTree;

/**
 * The reuse oracle is a {@link MealyMembershipOracle} that is able to
 * <ul>
 * <li>Cache queries: Each processed query will not be delegated again (instead
 * the answer will be retrieved from the {@link ReuseTree})</li>
 * <li>Pump queries: If the {@link ReuseTree} is configured to know which
 * symbols are model invariant input symbols via
 * {@link ReuseTree#addInvariantInputSymbol(Object)} (like a read from a
 * database which does not change the SUL) or configured for failure output
 * symbols via {@link ReuseTree#addFailureOutputSymbol(Object)} (e.g. a roll
 * back mechanism exists for the invoked symbol) the oracle could ''pump'' those
 * symbols inside a query once seen.</li>
 * <li>Reuse system states: There are a lot of situations where a prefix of a
 * query is already known and a system state is available. In this situation the
 * oracle is able to reuse the available system state and only process the
 * remaining suffix. Whether or not a system state will be removed after it is
 * used is decided by {@link QueryResult#oldInvalidated}.</li>
 * </ul>
 * through an internal {@link ReuseTree}.
 * 
 * The usage of model invariant input symbols and failure output symbols is
 * enabled by default but will only be used if symbols are provided via
 * {@link ReuseTree#addFailureOutputSymbol(Object)} or
 * {@link ReuseTree#addInvariantInputSymbol(Object)}.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * 
 * @param <S>
 *            The type of used system state, e.g. Integer, Map<String,Object>
 *            etc.
 * @param <I>
 *            The type of input symbols used.
 * @param <O>
 *            The type of output symbols used.
 */
public class ReuseOracle<S, I, O> implements MealyMembershipOracle<I, O> {
	/**
	 * The {@link ReuseCapableOracle} to execute the query (or any suffix of a
	 * query).
	 */
	private ReuseCapableOracle<S, I, O> reuseCapableOracle;

	private ReuseTree<S, I, O> tree;

	/**
	 * Default constructor.
	 * 
	 * @param sut
	 *            An instance of {@link ReuseCapableOracle} to delegate queries
	 *            to.
	 */
	public ReuseOracle(ReuseCapableOracle<S, I, O> sut) {
		this.reuseCapableOracle = sut;
		this.tree = new ReuseTree<>();
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> query : queries) {
			Word<O> output = processQuery(query.getInput());
			query.answer(output.suffix(query.getSuffix().size()));
		}
	}

	/**
	 * This methods returns with a result of same length as the input query.
	 * <p>
	 * It is possible that the query is already known (answer provided by
	 * {@link ReuseTree#getOutput(Word)}, the query is new and no system state
	 * could be found for reusage ({@link ReuseCapableOracle#processQuery(Word)}
	 * will be invoked) or there exists a prefix that (maybe epsilon) could be
	 * reused so save reset invocation (
	 * {@link ReuseCapableOracle#continueQuery(Word, ReuseNode)} will be invoked
	 * with remaining suffix and the corresponding {@link ReuseNode} of the
	 * {@link ReuseTree}).
	 * 
	 * @param query
	 * @return
	 */
	private synchronized final Word<O> processQuery(final Word<I> query) {
		Word<O> knownOutput = tree.getOutput(query);
		if (knownOutput != null) {
			return knownOutput;
		}

		ReuseNode<S, I, O> node = tree.getReuseableSystemState(query);

		if (node == null) {
			QueryResult<S, O> res = reuseCapableOracle.processQuery(query);
			tree.insert(query, res);

			return res.output;
		} else {
			Word<I> inputQuery = node.getPrefixInput();
			Word<O> outputQuery = node.getPrefixOutput();

			int index = 0;
			WordBuilder<O> prefixOutput = new WordBuilder<>();
			List<I> prefixInput = new LinkedList<>();
			for (int i = 0; i <= inputQuery.size() - 1; i++) {

				I a = inputQuery.getSymbol(i);
				I q = query.getSymbol(index);

				if (a.equals(q)) {
					prefixOutput.add(outputQuery.getSymbol(i));
					prefixInput.add(query.getSymbol(index));

					index++;
				}
			}

			Word<I> suffix = query.suffix(query.size() - index);
			QueryResult<S, O> res = reuseCapableOracle.continueQuery(suffix,
					node);

			this.tree.insert(suffix, node, res);

			// Make sure the whole output will be returned not just that for the
			// executed suffix!

			/*
			 * suppose: inserted 101 okokok and 0 is reflexive
			 * 
			 * now query 1011 so reuse is 101 but prefix of node is 11 so we
			 * execute suffix 1 and prepend known output of 101 before returning
			 * full output.
			 */

			Word<O> prefixOut = getReuseTree().getOutput(query.prefix(index));
			WordBuilder<O> wbOut = new WordBuilder<>(prefixOut);
			for (O o : res.output) {
				wbOut.append(o);
			}

			if (wbOut.toWord().size() != query.size()) {
				throw new IllegalArgumentException("Inputlength "
						+ query.size() + " does not equal outputlength "
						+ wbOut.toWord().size() + ": " + query + "/"
						+ wbOut.toWord());
			}

			return wbOut.toWord();
		}
	}

	/**
	 * Returns the {@link ReuseTree} used by this instance.
	 * 
	 * @return
	 */
	public ReuseTree<S, I, O> getReuseTree() {
		return this.tree;
	}

	/**
	 * Returns the {@link ReuseCapableOracle} used by this instance.
	 * 
	 * @return
	 */
	public ReuseCapableOracle<S, I, O> getReuseCapableOracle() {
		return this.reuseCapableOracle;
	}
}