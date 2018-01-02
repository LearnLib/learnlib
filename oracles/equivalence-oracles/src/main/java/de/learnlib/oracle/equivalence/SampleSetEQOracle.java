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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

/**
 * An equivalence oracle that tests a hypothesis against a fixed set of sample queries.
 * <p>
 * Sample queries are provided through one of the {@code add(...)} or {@code addAll(...)} methods of this class. A query
 * consists of an <i>input word</i> (split into a <i>prefix</i> and a <i>suffix</i>), and an expected <i>output</i>.
 * During an equivalence query, for each of those queries if the respective actual suffix output of the hypothesis
 * equals the expected output.
 * <p>
 * This oracle will always repeatedly test queries from the sample set if they turned out to be counterexamples.
 * However, the oracle can be configured to remove queries from the sample set if they did not serve as
 * counterexamples.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public class SampleSetEQOracle<I, D> implements EquivalenceOracle<SuffixOutput<I, D>, I, D> {

    private final boolean removeUnsuccessful;
    private final List<DefaultQuery<I, D>> testQueries;

    /**
     * Constructor. Initializes the oracle with an empty sample set.
     *
     * @param removeUnsuccessful
     *         if set to {@code true}, queries will be removed from the sample set if they did not reveal a
     *         counterexample. Otherwise, all queries from the sample set will always be tested upon each invocation of
     *         {@link #findCounterExample(SuffixOutput, Collection)}.
     */
    public SampleSetEQOracle(boolean removeUnsuccessful) {
        this.removeUnsuccessful = removeUnsuccessful;
        if (!removeUnsuccessful) {
            testQueries = new ArrayList<>();
        } else {
            testQueries = new LinkedList<>(); // for O(1) removal of elements
        }
    }

    /**
     * Adds a query word along with its expected output to the sample set.
     *
     * @param input
     *         the input word
     * @param expectedOutput
     *         the expected output for this word
     *
     * @return {@code this}, to enable chained {@code add} or {@code addAll} calls
     */
    public SampleSetEQOracle<I, D> add(Word<I> input, D expectedOutput) {
        testQueries.add(new DefaultQuery<>(input, expectedOutput));
        return this;
    }

    /**
     * Adds several query words to the sample set. The expected output is determined by means of the specified
     * membership oracle.
     *
     * @param oracle
     *         the membership oracle used to determine expected outputs
     * @param words
     *         the words to be added to the sample set
     *
     * @return {@code this}, to enable chained {@code add} or {@code addAll} calls
     */
    @SafeVarargs
    public final SampleSetEQOracle<I, D> addAll(MembershipOracle<I, D> oracle, Word<I>... words) {
        return addAll(oracle, Arrays.asList(words));
    }

    /**
     * Adds words to the sample set. The expected output is determined by means of the specified membership oracle.
     *
     * @param oracle
     *         the membership oracle used to determine the expected output
     * @param words
     *         the words to add
     *
     * @return {@code this}, to enable chained {@code add} or {@code addAll} calls
     */
    public SampleSetEQOracle<I, D> addAll(MembershipOracle<I, D> oracle, Collection<? extends Word<I>> words) {
        if (words.isEmpty()) {
            return this;
        }
        List<DefaultQuery<I, D>> newQueries = new ArrayList<>(words.size());
        for (Word<I> w : words) {
            newQueries.add(new DefaultQuery<>(w));
        }
        oracle.processQueries(newQueries);

        testQueries.addAll(newQueries);
        return this;
    }

    /**
     * Adds queries to the sample set. These must be {@link DefaultQuery}s, which allow for retrieving the corresponding
     * (expected) output.
     *
     * @param newTestQueries
     *         the queries to add to the sample set
     *
     * @return {@code this}, to enable chained {@code add} or {@code addAll} calls
     */
    @SafeVarargs
    public final SampleSetEQOracle<I, D> addAll(DefaultQuery<I, D>... newTestQueries) {
        return addAll(Arrays.asList(newTestQueries));
    }

    /**
     * Adds queries to the sample set. These must be {@link DefaultQuery}s, which allow for retrieving the corresponding
     * (expected) output.
     *
     * @param newTestQueries
     *         the queries to add to the sample set
     *
     * @return {@code this}, to enable chained {@code add} or {@code addAll} calls
     */
    public SampleSetEQOracle<I, D> addAll(Collection<? extends DefaultQuery<I, D>> newTestQueries) {
        testQueries.addAll(newTestQueries);
        return this;
    }

    @Override
    public DefaultQuery<I, D> findCounterExample(SuffixOutput<I, D> hypothesis, Collection<? extends I> inputs) {
        Iterator<DefaultQuery<I, D>> queryIt = testQueries.iterator();

        while (queryIt.hasNext()) {
            DefaultQuery<I, D> query = queryIt.next();

            if (checkInputs(query, inputs)) {
                if (!test(query, hypothesis)) {
                    return query;
                } else if (removeUnsuccessful) {
                    queryIt.remove();
                }
            }
        }
        return null;
    }

    /**
     * Tests if the input word of the given {@link Query} consists entirely of symbols in {@code inputs}.
     *
     * @param query
     *         the query to test
     * @param inputs
     *         the set of allowed inputs
     *
     * @return {@code true} if the input word of {@code query} consists entirely of symbols in {@code inputs}, {@code
     * false} otherwise
     */
    private static <I> boolean checkInputs(Query<I, ?> query, Collection<? extends I> inputs) {
        for (I sym : query.getPrefix()) {
            if (!inputs.contains(sym)) {
                return false;
            }
        }
        for (I sym : query.getSuffix()) {
            if (!inputs.contains(sym)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the suffix output of the given hypothesis matches the expected output stored in the query.
     *
     * @param query
     *         the query, containing the expected output
     * @param hypOutput
     *         the suffix output portion of the hypothesis
     *
     * @return {@code true} if the suffix output by {@code hypOutput} matches the expected output stored in {@code
     * query}, {@code false} otherwise.
     */
    private static <I, D> boolean test(DefaultQuery<I, D> query, SuffixOutput<I, D> hypOutput) {
        D hypOut = hypOutput.computeSuffixOutput(query.getPrefix(), query.getSuffix());

        return Objects.equals(hypOut, query.getOutput());
    }

}
