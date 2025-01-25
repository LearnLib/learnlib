/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.counterexample;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;

/**
 * Suffix-based local counterexample analyzer.
 * <p>
 * Given a query {@code (u, v)} which is a counterexample (i.e., the suffix-output for {@code (u,v)} is distinct from
 * the target system's output for {@code (u,v)}), it calculates the index {@code i} of the suffix such that
 * {@code w[i:]} ({@code w = uv}) still allows to expose a behavioral difference for an adequate prefix. This adequate
 * prefix can be determined as {@code {w[:i]}}, where {@code {.}} denotes the access sequence of the corresponding
 * word.
 * <p>
 * The effect of adding such a suffix can be described as follows: {@code {w[:i]}} and {@code {w[:i-1]}w[i-1]} both
 * lead to the same state in the hypothesis, but a local suffix finder chooses the index i such that the output for
 * {@code ({w[:i]}, w[i:])} and {@code ({w[:i-1]}w[i-1], w[i:])} will differ. Hence, the transition to the state
 * reached by {@code {w[:i]}} from {@code {w[:i-1]}} is disproved.
 * <p>
 * Please note that the type parameters of these class only constitute <i>upper</i> bounds for the respective input
 * symbol and output classes, denoting the requirements of the process in general. A suffix finder which does not
 * exploit any properties of the used classes will implement this interface with {@code <Object,Object>} generic
 * arguments only. The genericity is still maintained due to the {@code RI} and {@code RO} generic parameters in the
 * {@link #findSuffixIndex(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)} method.
 *
 * @param <I>
 *         input symbol type upper bound
 * @param <D>
 *         output domain type upper bound
 */
public interface LocalSuffixFinder<I, D> {

    /**
     * Finds, for a given counterexample, a "split index", such that: - the part of the query word <i>before this
     * index</i> leads to the state being split - the part of the query word <i>from this index on</i> is a suffix
     * capable of splitting this state.
     *
     * @param <RI>
     *         real input symbol class used for *this* counterexample analysis
     * @param <RD>
     *         real output class used for *this* counterexample analysis
     * @param ceQuery
     *         the counterexample query that triggered the refinement. Note that the same restrictions as in {@link
     *         LearningAlgorithm#refineHypothesis(DefaultQuery)} apply.
     * @param asTransformer
     *         an {@link AccessSequenceTransformer} used for access sequence transformation, if applicable.
     * @param hypOutput
     *         interface to the output generation of the hypothesis, with the aim of comparing outputs of the hypothesis
     *         and the SUL.
     * @param oracle
     *         interface to the System Under Learning (SUL).
     *
     * @return an adequate split index, or {@code -1} if the counterexample could not be analyzed.
     */
    <RI extends I, RD extends D> int findSuffixIndex(Query<RI, RD> ceQuery,
                                                     AccessSequenceTransformer<RI> asTransformer,
                                                     SuffixOutput<RI, RD> hypOutput,
                                                     MembershipOracle<RI, RD> oracle);

}
