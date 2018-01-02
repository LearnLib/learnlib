/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.filter.reuse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.filter.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filter.reuse.tree.BoundedDeque.AccessPolicy;
import de.learnlib.filter.reuse.tree.BoundedDeque.EvictPolicy;
import de.learnlib.filter.reuse.tree.ReuseNode;
import de.learnlib.filter.reuse.tree.ReuseNode.NodeResult;
import de.learnlib.filter.reuse.tree.ReuseTree;
import de.learnlib.filter.reuse.tree.ReuseTree.ReuseTreeBuilder;
import de.learnlib.filter.reuse.tree.SystemStateHandler;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * The reuse oracle is a {@link MealyMembershipOracle} that is able to <ul> <li>Cache queries: Each processed query will
 * not be delegated again (instead the answer will be retrieved from the {@link ReuseTree})</li> <li>Pump queries: If
 * the {@link ReuseTree} is configured to know which symbols are model invariant input symbols via {@link
 * ReuseOracleBuilder#withInvariantInputs(Set)} (like a read from a database which does not change the SUL) or
 * configured for failure output symbols via {@link ReuseOracleBuilder#withFailureOutputs(Set)} (e.g. a roll back
 * mechanism exists for the invoked symbol) the oracle could ''pump'' those symbols inside a query once seen.</li>
 * <li>Reuse system states: There are a lot of situations where a prefix of a query is already known and a system state
 * is available. In this situation the oracle is able to reuse the available system state and only process the remaining
 * suffix. Whether or not a system state will be removed after it is used is decided upon construction (see {@link
 * ReuseOracleBuilder#ReuseOracleBuilder(Alphabet, Supplier)}.</li> </ul> through an internal {@link ReuseTree}.
 * <p>
 * The usage of model invariant input symbols and failure output symbols is disabled by default and can be enabled upon
 * construction (see {@link ReuseOracleBuilder#withFailureOutputs(Set)} and {@link
 * ReuseOracleBuilder#withInvariantInputs(Set)}).
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Oliver Bauer
 */
public final class ReuseOracle<S, I, O> implements SingleQueryOracleMealy<I, O> {

    private final ThreadLocal<ReuseCapableOracle<S, I, O>> executableOracles;

    private final ReuseTree<S, I, O> tree;

    /**
     * Default constructor.
     */
    private ReuseOracle(ReuseOracleBuilder<S, I, O> builder) {
        this.executableOracles = ThreadLocal.withInitial(builder.oracleSupplier::get);
        this.tree = new ReuseTreeBuilder<S, I, O>(builder.alphabet).withSystemStateHandler(builder.systemStateHandler)
                                                                   .withFailureOutputs(builder.failureOutputSymbols)
                                                                   .withInvariantInputs(builder.invariantInputSymbols)
                                                                   .withEnabledSystemstateInvalidation(builder.invalidateSystemstates)
                                                                   .withMaxSystemStates(builder.maxSystemStates)
                                                                   .withAccessPolicy(builder.accessPolicy)
                                                                   .withEvictPolicy(builder.evictPolicy)
                                                                   .build();
    }

    @Override
    public Word<O> answerQuery(Word<I> prefix, Word<I> suffix) {
        return processQuery(prefix.concat(suffix)).suffix(suffix.length());
    }

    @Override
    public Word<O> answerQuery(Word<I> input) {
        return processQuery(input);
    }

    /**
     * This methods returns the full output to the input query.
     * <p>
     * It is possible that the query is already known (answer provided by {@link ReuseTree#getOutput(Word)}, the query
     * is new and no system state could be found for reusage ({@link ReuseCapableOracle#processQuery(Word)} will be
     * invoked) or there exists a prefix that (maybe epsilon) could be reused so save reset invocation ( {@link
     * ReuseCapableOracle#continueQuery(Word, Object)} will be invoked with remaining suffix and the corresponding
     * {@link ReuseNode} of the {@link ReuseTree}).
     */
    private Word<O> processQuery(final Word<I> query) {
        Word<O> knownOutput = tree.getOutput(query);

        if (knownOutput != null) {
            return knownOutput;
        }

        // Search for system state
        final NodeResult<S, I, O> nodeResult = tree.fetchSystemState(query);
        final ReuseCapableOracle<S, I, O> oracle = getReuseCapableOracle();
        final Word<O> output;

        // No system state available
        if (nodeResult == null) {
            final QueryResult<S, O> newResult =
                    filterAndProcessQuery(query, tree.getPartialOutput(query), oracle::processQuery);

            tree.insert(query, newResult);

            output = newResult.output;
        } else { // System state available -> reuse
            final int suffixLen = query.size() - nodeResult.prefixLength;
            final Word<I> suffix = query.suffix(suffixLen);

            final Word<O> partialOutput = tree.getPartialOutput(query);
            final Word<O> partialSuffixOutput = partialOutput.suffix(suffixLen);

            final ReuseNode<S, I, O> reuseNode = nodeResult.reuseNode;
            final S systemState = nodeResult.systemState;

            final QueryResult<S, O> suffixQueryResult = filterAndProcessQuery(suffix,
                                                                              partialSuffixOutput,
                                                                              filteredInput -> oracle.continueQuery(
                                                                                      filteredInput,
                                                                                      systemState));

            this.tree.insert(suffix, reuseNode, suffixQueryResult);

            final Word<O> prefixOutput = tree.getOutput(query.prefix(nodeResult.prefixLength));
            output = new WordBuilder<>(prefixOutput).append(suffixQueryResult.output).toWord();
        }
        return output;
    }

    /**
     * Returns the {@link ReuseCapableOracle} used by this instance.
     */
    public ReuseCapableOracle<S, I, O> getReuseCapableOracle() {
        return executableOracles.get();
    }

    /**
     * Filters all the query elements corresponding to "reflexive" edges in the reuse tree, executes the shorter query,
     * and fills the filtered outputs into the resulting output word.
     *
     * @param query
     *         the input query with "reflexive" symbols (may be a suffix of the original query, if a system state is
     *         reused).
     * @param partialOutput
     *         the output information from the tree with {@code null} entries for all "non-reflexive" edges.
     * @param processQuery
     *         a function that actually processes the (shortened) query.
     *
     * @return the query result including the outputs of the "reflexive" symbol executions.
     */
    private QueryResult<S, O> filterAndProcessQuery(Word<I> query,
                                                    Word<O> partialOutput,
                                                    Function<Word<I>, QueryResult<S, O>> processQuery) {
        final LinkedList<I> filteredQueryList = new LinkedList<>(query.asList());
        final Iterator<I> queryIterator = filteredQueryList.iterator();

        // filter "reflexive" edges
        for (final O outputSymbol : partialOutput) {
            queryIterator.next();
            if (outputSymbol != null) {
                queryIterator.remove();
            }
        }

        // process the query
        final QueryResult<S, O> res = processQuery.apply(Word.fromList(filteredQueryList));

        final WordBuilder<O> wordBuilder = new WordBuilder<>();
        final Iterator<O> resultIterator = res.output.iterator();

        // insert back the a priori available outputs of "reflexive" edges
        for (final O output : partialOutput) {
            if (output == null) {
                wordBuilder.add(resultIterator.next());
            } else {
                wordBuilder.add(output);
            }
        }

        return new QueryResult<>(wordBuilder.toWord(), res.newState);
    }

    /**
     * Returns the {@link ReuseTree} used by this instance.
     */
    public ReuseTree<S, I, O> getReuseTree() {
        return this.tree;
    }

    public static class ReuseOracleBuilder<S, I, O> {

        private final Alphabet<I> alphabet;
        private final Supplier<? extends ReuseCapableOracle<S, I, O>> oracleSupplier;

        private boolean invalidateSystemstates = true;
        private SystemStateHandler<S> systemStateHandler;
        private Set<I> invariantInputSymbols;
        private Set<O> failureOutputSymbols;
        private int maxSystemStates = -1;
        private AccessPolicy accessPolicy = AccessPolicy.LIFO;
        private EvictPolicy evictPolicy = EvictPolicy.EVICT_OLDEST;

        public ReuseOracleBuilder(Alphabet<I> alphabet,
                                  Supplier<? extends ReuseCapableOracle<S, I, O>> oracleSupplier) {
            this.alphabet = alphabet;
            this.oracleSupplier = oracleSupplier;
        }

        public ReuseOracleBuilder<S, I, O> withSystemStateHandler(SystemStateHandler<S> systemStateHandler) {
            this.systemStateHandler = systemStateHandler;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withEnabledSystemstateInvalidation(boolean invalidate) {
            this.invalidateSystemstates = invalidate;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withInvariantInputs(Set<I> inputs) {
            this.invariantInputSymbols = inputs;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withFailureOutputs(Set<O> outputs) {
            this.failureOutputSymbols = outputs;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withMaxSystemStates(int maxSystemStates) {
            this.maxSystemStates = maxSystemStates;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withAccessPolicy(AccessPolicy accessPolicy) {
            this.accessPolicy = accessPolicy;
            return this;
        }

        public ReuseOracleBuilder<S, I, O> withEvictPolicy(EvictPolicy evictPolicy) {
            this.evictPolicy = evictPolicy;
            return this;
        }

        public ReuseOracle<S, I, O> build() {
            return new ReuseOracle<>(this);
        }
    }

}
