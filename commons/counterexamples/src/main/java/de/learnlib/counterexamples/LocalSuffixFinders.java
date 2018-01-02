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
package de.learnlib.counterexamples;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.automata.concepts.SuffixOutput;

/**
 * A collection of suffix-based local counterexample analyzers.
 *
 * @author Malte Isberner
 * @see LocalSuffixFinder
 */
public final class LocalSuffixFinders {

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations in
     * linear ascending order.
     *
     * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static final LocalSuffixFinder<Object, Object> FIND_LINEAR =
            new AcexLocalSuffixFinder(AcexAnalyzers.LINEAR_FWD, true, "FindLinear");

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations in
     * linear descending order.
     *
     * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static final LocalSuffixFinder<Object, Object> FIND_LINEAR_REVERSE =
            new AcexLocalSuffixFinder(AcexAnalyzers.LINEAR_BWD, true, "FindLinear-Reverse");

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations
     * using a binary search, as proposed by Rivest &amp; Schapire.
     *
     * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static final LocalSuffixFinder<Object, Object> RIVEST_SCHAPIRE =
            new AcexLocalSuffixFinder(AcexAnalyzers.BINARY_SEARCH_BWD, true, "RivestSchapire");

    // prevent instantiation
    private LocalSuffixFinders() {
    }

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations in
     * linear ascending order.
     *
     * @param ceQuery
     *         the initial counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output, for checking whether the oracle output contradicts the hypothesis
     * @param oracle
     *         interface to the SUL
     *
     * @return the index of the respective suffix, or <tt>-1</tt> if no counterexample could be found
     *
     * @see LocalSuffixFinder
     */
    public static <S, I, D> int findLinear(Query<I, D> ceQuery,
                                           AccessSequenceTransformer<I> asTransformer,
                                           SuffixOutput<I, D> hypOutput,
                                           MembershipOracle<I, D> oracle) {

        return AcexLocalSuffixFinder.findSuffixIndex(AcexAnalyzers.LINEAR_FWD,
                                                     true,
                                                     ceQuery,
                                                     asTransformer,
                                                     hypOutput,
                                                     oracle);
    }

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations in
     * linear descending order.
     *
     * @param ceQuery
     *         the initial counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output, for checking whether the oracle output contradicts the hypothesis
     * @param oracle
     *         interface to the SUL
     *
     * @return the index of the respective suffix, or <tt>-1</tt> if no counterexample could be found
     *
     * @see LocalSuffixFinder
     */
    public static <I, D> int findLinearReverse(Query<I, D> ceQuery,
                                               AccessSequenceTransformer<I> asTransformer,
                                               SuffixOutput<I, D> hypOutput,
                                               MembershipOracle<I, D> oracle) {

        return AcexLocalSuffixFinder.findSuffixIndex(AcexAnalyzers.LINEAR_BWD,
                                                     true,
                                                     ceQuery,
                                                     asTransformer,
                                                     hypOutput,
                                                     oracle);
    }

    /**
     * Searches for a distinguishing suffixes by checking for counterexample yielding access sequence transformations
     * using a binary search, as proposed by Rivest &amp; Schapire.
     *
     * @param ceQuery
     *         the initial counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output, for checking whether the oracle output contradicts the hypothesis
     * @param oracle
     *         interface to the SUL
     *
     * @return the index of the respective suffix, or <tt>-1</tt> if no counterexample could be found
     *
     * @see LocalSuffixFinder
     */
    public static <I, D> int findRivestSchapire(Query<I, D> ceQuery,
                                                AccessSequenceTransformer<I> asTransformer,
                                                SuffixOutput<I, D> hypOutput,
                                                MembershipOracle<I, D> oracle) {

        return AcexLocalSuffixFinder.findSuffixIndex(AcexAnalyzers.BINARY_SEARCH_BWD,
                                                     true,
                                                     ceQuery,
                                                     asTransformer,
                                                     hypOutput,
                                                     oracle);
    }

    @SuppressWarnings("unchecked")
    public static LocalSuffixFinder<Object, Object>[] values() {
        return new LocalSuffixFinder[] {FIND_LINEAR, FIND_LINEAR_REVERSE, RIVEST_SCHAPIRE};
    }
}
