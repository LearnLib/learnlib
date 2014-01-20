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
import java.util.Set;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filters.reuse.tree.ReuseNode;
import de.learnlib.filters.reuse.tree.SystemStateHandler;
import de.learnlib.filters.reuse.tree.ReuseNode.NodeResult;
import de.learnlib.filters.reuse.tree.ReuseTree.ReuseTreeBuilder;
import de.learnlib.filters.reuse.tree.ReuseTree;

/**
 * The reuse oracle is a {@link MealyMembershipOracle} that is able to
 * <ul>
 * <li>Cache queries: Each processed query will not be delegated again (instead
 * the answer will be retrieved from the {@link ReuseTree})</li>
 * <li>Pump queries: If the {@link ReuseTree} is configured to know which
 * symbols are model invariant input symbols via
 * {@link ReuseOracleBuilder#withInvariantInputs(Set)} (like a read from a
 * database which does not change the SUL) or configured for failure output
 * symbols via {@link ReuseOracleBuilder#withFailureOutputs(Set)} (e.g. a roll
 * back mechanism exists for the invoked symbol) the oracle could ''pump'' those
 * symbols inside a query once seen.</li>
 * <li>Reuse system states: There are a lot of situations where a prefix of a
 * query is already known and a system state is available. In this situation the
 * oracle is able to reuse the available system state and only process the
 * remaining suffix. Whether or not a system state will be removed after it is
 * used is decided upon construction 
 * (see {@link ReuseOracleBuilder#ReuseOracleBuilder(Alphabet, ReuseCapableOracle, boolean)} .</li>
 * </ul>
 * through an internal {@link ReuseTree}.
 * 
 * The usage of model invariant input symbols and failure output symbols is
 * disabled by default and can be enabled upon construction (see {@link ReuseOracleBuilder#withFailureOutputs(Set)} and
 * {@link ReuseOracleBuilder#withInvariantInputs(Set)}).
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * 
 * @param <S> system state class
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class ReuseOracle<S, I, O> implements MealyMembershipOracle<I, O> {
	public static class ReuseOracleBuilder<S,I,O> {
		private final Alphabet<I> alphabet;
		private final ReuseCapableOracle<S,I,O> sul;
		
		private boolean invalidateSystemstates = true;
		private SystemStateHandler<S> systemStateHandler;
		private Set<I> invariantInputSymbols;
		private Set<O> failureOutputSymbols;	
		
		public ReuseOracleBuilder(
				Alphabet<I> alphabet, 
				ReuseCapableOracle<S, I, O> sul) {
			this.alphabet = alphabet;
			this.sul = sul;
		}
		
		public ReuseOracleBuilder<S,I,O> withSystemStateHandler(SystemStateHandler<S> systemStateHandler) {
			this.systemStateHandler = systemStateHandler;
			return this;
		}

		public ReuseOracleBuilder<S,I,O> withEnabledSystemstateInvalidation(boolean invalidate) {
			this.invalidateSystemstates = invalidate;
			return this;
		}
		
		public ReuseOracleBuilder<S,I,O> withInvariantInputs(Set<I> inputs) {
			this.invariantInputSymbols = inputs;
			return this;
		}
		
		public ReuseOracleBuilder<S,I,O> withFailureOutputs(Set<O> outputs) {
			this.failureOutputSymbols = outputs;
			return this;
		}
		
		public ReuseOracle<S, I, O> build() {
			return new ReuseOracle<>(this);
		}
	}
	
	/**
	 * The {@link ReuseCapableOracle} to execute the query (or any suffix of a
	 * query).
	 */
	private ReuseCapableOracle<S, I, O> reuseCapableOracle;

	private ReuseTree<S, I, O> tree;

	/**
	 * Default constructor.
	 * 
	 * @param sul
	 *            An instance of {@link ReuseCapableOracle} to delegate queries
	 *            to.
	 */
	private ReuseOracle(ReuseOracleBuilder<S,I,O> builder) {
		this.reuseCapableOracle = builder.sul;
		this.tree = new ReuseTreeBuilder<S,I,O>(builder.alphabet)
				.withSystemStateHandler(builder.systemStateHandler)
				.withFailureOutputs(builder.failureOutputSymbols)
				.withInvariantInputs(builder.invariantInputSymbols)
				.withEnabledSystemstateInvalidation(builder.invalidateSystemstates)
				.build();
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
	 * This methods returns the full output to the input query.
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

		NodeResult<S,I,O> nodeResult = tree.fetchSystemState(query);
		
		if (nodeResult == null) {
			QueryResult<S, O> res = reuseCapableOracle.processQuery(query);
			tree.insert(query, res);

			return res.output;
		} else {
			Word<I> suffix = query.suffix(query.size() - nodeResult.prefixLength);
			Word<I> prefix = query.prefix(nodeResult.prefixLength);
			
			ReuseNode<S, I, O> reuseNode = nodeResult.reuseNode;
			S systemState = nodeResult.systemState;
			
			QueryResult<S, O> queryResult;
			queryResult = reuseCapableOracle.continueQuery(suffix, systemState);
			this.tree.insert(suffix, reuseNode, queryResult);
			
			Word<O> prefixOutput = tree.getOutput(prefix); // TODO don't compute twice
			Word<O> suffixOutput = queryResult.output;
			return new WordBuilder<>(prefixOutput).append(suffixOutput).toWord();
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