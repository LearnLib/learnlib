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

import java.util.Collections;
import java.util.List;

import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

/**
 * A collection of suffix-based global counterexample analyzers.
 *
 * @author Malte Isberner
 * @see GlobalSuffixFinder
 */
public final class GlobalSuffixFinders {

    /**
     * Adds all suffixes of the input word, as suggested by Maler &amp; Pnueli.
     *
     * @see #findMalerPnueli(Query)
     */
    public static final GlobalSuffixFinder<Object, Object> MALER_PNUELI = new GlobalSuffixFinder<Object, Object>() {

        @Override
        public <RI, RD> List<Word<RI>> findSuffixes(Query<RI, RD> ceQuery,
                                                    AccessSequenceTransformer<RI> asTransformer,
                                                    SuffixOutput<RI, RD> hypOutput,
                                                    MembershipOracle<RI, RD> oracle) {
            return findMalerPnueli(ceQuery);
        }

        @Override
        public String toString() {
            return "MalerPnueli";
        }
    };

    /**
     * Adds all suffixes of the remainder of the input word, after stripping a maximal one-letter extension of an access
     * sequence.
     *
     * @see #findShahbaz(Query, AccessSequenceTransformer)
     */
    public static final GlobalSuffixFinder<Object, Object> SHAHBAZ = new GlobalSuffixFinder<Object, Object>() {

        @Override
        public <RI, RD> List<Word<RI>> findSuffixes(Query<RI, RD> ceQuery,
                                                    AccessSequenceTransformer<RI> asTransformer,
                                                    SuffixOutput<RI, RD> hypOutput,
                                                    MembershipOracle<RI, RD> oracle) {
            return findShahbaz(ceQuery, asTransformer);
        }

        @Override
        public String toString() {
            return "Shahbaz";
        }
    };

    /**
     * Adds the single suffix found by the access sequence transformation in ascending linear order.
     *
     * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> FIND_LINEAR =
            fromLocalFinder(LocalSuffixFinders.FIND_LINEAR, false);

    /**
     * Adds the suffix found by the access sequence transformation in ascending linear order, and all of its suffixes.
     *
     * @see #findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> FIND_LINEAR_ALLSUFFIXES =
            fromLocalFinder(LocalSuffixFinders.FIND_LINEAR, true);

    /**
     * Adds the single suffix found by the access sequence transformation in descending linear order.
     *
     * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> FIND_LINEAR_REVERSE =
            fromLocalFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, false);

    /**
     * Adds the suffix found by the access sequence transformation in descending linear order, and all of its suffixes.
     *
     * @see #findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> FIND_LINEAR_REVERSE_ALLSUFFIXES =
            fromLocalFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, true);

    /**
     * Adds the single suffix found by the access sequence transformation using binary search.
     *
     * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> RIVEST_SCHAPIRE =
            fromLocalFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, false);

    /**
     * Adds the suffix found by the access sequence transformation using binary search, and all of its suffixes.
     *
     * @see #findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle, boolean)
     */
    public static final GlobalSuffixFinder<Object, Object> RIVEST_SCHAPIRE_ALLSUFFIXES =
            fromLocalFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, true);

    // prevent instantiation
    private GlobalSuffixFinders() {
    }

    /**
     * Transforms a {@link LocalSuffixFinder} into a global one. This is a convenience method, behaving like
     * <tt>fromLocalFinder(localFinder, false)</tt>.
     *
     * @see #fromLocalFinder(LocalSuffixFinder, boolean)
     */
    public static <I, D> GlobalSuffixFinder<I, D> fromLocalFinder(LocalSuffixFinder<I, D> localFinder) {
        return fromLocalFinder(localFinder, false);
    }

    /**
     * Transforms a {@link LocalSuffixFinder} into a global one. Since local suffix finders only return a single suffix,
     * suffix-closedness of the set of distinguishing suffixes might not be preserved. Note that for correctly
     * implemented local suffix finders, this does not impair correctness of the learning algorithm. However, without
     * suffix closedness, intermediate hypothesis models might be non-canonical, if no additional precautions are taken.
     * For that reasons, the <tt>allSuffixes</tt> parameter can be specified to control whether or not the list returned
     * by {@link GlobalSuffixFinder#findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)} of
     * the returned global suffix finder should not only contain the single suffix, but also all of its suffixes,
     * ensuring suffix-closedness.
     *
     * @param localFinder
     *         the local suffix finder
     * @param allSuffixes
     *         whether or not all suffixes of the found local suffix should be added
     *
     * @return a global suffix finder using the analysis method from the specified local suffix finder
     */
    public static <I, D> GlobalSuffixFinder<I, D> fromLocalFinder(final LocalSuffixFinder<I, D> localFinder,
                                                                  final boolean allSuffixes) {

        return new GlobalSuffixFinder<I, D>() {

            @Override
            public <RI extends I, RD extends D> List<Word<RI>> findSuffixes(Query<RI, RD> ceQuery,
                                                                                      AccessSequenceTransformer<RI> asTransformer,
                                                                                      SuffixOutput<RI, RD> hypOutput,
                                                                                      MembershipOracle<RI, RD> oracle) {
                int idx = localFinder.findSuffixIndex(ceQuery, asTransformer, hypOutput, oracle);
                return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
            }

            @Override
            public String toString() {
                return localFinder.toString() + (allSuffixes ? "-AllSuffixes" : "");
            }
        };
    }

    /**
     * Transforms a suffix index returned by a {@link LocalSuffixFinder} into a list containing the single
     * distinguishing suffix.
     */
    public static <I, D> List<Word<I>> suffixesForLocalOutput(Query<I, D> ceQuery, int localSuffixIdx) {
        return suffixesForLocalOutput(ceQuery, localSuffixIdx, false);
    }

    /**
     * Transforms a suffix index returned by a {@link LocalSuffixFinder} into a list of distinguishing suffixes. This
     * list always contains the corresponding local suffix. Since local suffix finders only return a single suffix,
     * suffix-closedness of the set of distinguishing suffixes might not be preserved. Note that for correctly
     * implemented local suffix finders, this does not impair correctness of the learning algorithm. However, without
     * suffix closedness, intermediate hypothesis models might be non-canonical, if no additional precautions are taken.
     * For that reasons, the <tt>allSuffixes</tt> parameter can be specified to control whether or not the list returned
     * by {@link GlobalSuffixFinder#findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)} of
     * the returned global suffix finder should not only contain the single suffix, but also all of its suffixes,
     * ensuring suffix-closedness.
     */
    public static <I, D> List<Word<I>> suffixesForLocalOutput(Query<I, D> ceQuery,
                                                              int localSuffixIdx,
                                                              boolean allSuffixes) {

        if (localSuffixIdx == -1) {
            return Collections.emptyList();
        }

        Word<I> suffix = ceQuery.getInput().subWord(localSuffixIdx);

        if (!allSuffixes) {
            return Collections.singletonList(suffix);
        }

        return suffix.suffixes(false);
    }

    /**
     * Returns all suffixes of the counterexample word as distinguishing suffixes, as suggested by Maler &amp; Pnueli.
     *
     * @param ceQuery
     *         the counterexample query
     *
     * @return all suffixes of the counterexample input
     */
    public static <I, D> List<Word<I>> findMalerPnueli(Query<I, D> ceQuery) {
        return ceQuery.getInput().suffixes(false);
    }

    /**
     * Returns all suffixes of the counterexample word as distinguishing suffixes, after stripping a maximal one-letter
     * extension of an access sequence, as suggested by Shahbaz.
     *
     * @param ceQuery
     *         the counterexample query
     * @param asTransformer
     *         the access sequence transformer
     *
     * @return all suffixes from the counterexample after stripping a maximal one-letter extension of an access
     * sequence.
     */
    public static <I, D> List<Word<I>> findShahbaz(Query<I, D> ceQuery, AccessSequenceTransformer<I> asTransformer) {
        Word<I> queryWord = ceQuery.getInput();
        int queryLen = queryWord.length();

        Word<I> prefix = ceQuery.getPrefix();
        int i = prefix.length();

        while (i <= queryLen) {
            Word<I> nextPrefix = queryWord.prefix(i);

            if (!asTransformer.isAccessSequence(nextPrefix)) {
                break;
            }
            i++;
        }

        return queryWord.subWord(i).suffixes(false);
    }

    /**
     * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by the access sequence
     * transformation in ascending linear order.
     *
     * @param ceQuery
     *         the counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output
     * @param oracle
     *         interface to the SUL output
     * @param allSuffixes
     *         whether or not to include all suffixes of the found suffix
     *
     * @return the distinguishing suffixes
     *
     * @see LocalSuffixFinders#findLinear(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static <I, D> List<Word<I>> findLinear(Query<I, D> ceQuery,
                                                  AccessSequenceTransformer<I> asTransformer,
                                                  SuffixOutput<I, D> hypOutput,
                                                  MembershipOracle<I, D> oracle,
                                                  boolean allSuffixes) {
        int idx = LocalSuffixFinders.findLinear(ceQuery, asTransformer, hypOutput, oracle);
        return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
    }

    /**
     * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by the access sequence
     * transformation in descending linear order.
     *
     * @param ceQuery
     *         the counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output
     * @param oracle
     *         interface to the SUL output
     * @param allSuffixes
     *         whether or not to include all suffixes of the found suffix
     *
     * @return the distinguishing suffixes
     *
     * @see LocalSuffixFinders#findLinearReverse(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static <I, D> List<Word<I>> findLinearReverse(Query<I, D> ceQuery,
                                                         AccessSequenceTransformer<I> asTransformer,
                                                         SuffixOutput<I, D> hypOutput,
                                                         MembershipOracle<I, D> oracle,
                                                         boolean allSuffixes) {
        int idx = LocalSuffixFinders.findLinearReverse(ceQuery, asTransformer, hypOutput, oracle);
        return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
    }

    /**
     * Returns the suffix (plus all of its suffixes, if <tt>allSuffixes</tt> is true) found by the binary search access
     * sequence transformation.
     *
     * @param ceQuery
     *         the counterexample query
     * @param asTransformer
     *         the access sequence transformer
     * @param hypOutput
     *         interface to the hypothesis output
     * @param oracle
     *         interface to the SUL output
     * @param allSuffixes
     *         whether or not to include all suffixes of the found suffix
     *
     * @return the distinguishing suffixes
     *
     * @see LocalSuffixFinders#findRivestSchapire(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)
     */
    public static <I, O> List<Word<I>> findRivestSchapire(Query<I, O> ceQuery,
                                                          AccessSequenceTransformer<I> asTransformer,
                                                          SuffixOutput<I, O> hypOutput,
                                                          MembershipOracle<I, O> oracle,
                                                          boolean allSuffixes) {
        int idx = LocalSuffixFinders.findRivestSchapire(ceQuery, asTransformer, hypOutput, oracle);
        return suffixesForLocalOutput(ceQuery, idx, allSuffixes);
    }

    @SuppressWarnings("unchecked")
    public static GlobalSuffixFinder<Object, Object>[] values() {
        return new GlobalSuffixFinder[] {MALER_PNUELI,
                                         SHAHBAZ,
                                         FIND_LINEAR,
                                         FIND_LINEAR_ALLSUFFIXES,
                                         FIND_LINEAR_REVERSE,
                                         FIND_LINEAR_REVERSE_ALLSUFFIXES,
                                         RIVEST_SCHAPIRE,
                                         RIVEST_SCHAPIRE_ALLSUFFIXES};
    }

}
