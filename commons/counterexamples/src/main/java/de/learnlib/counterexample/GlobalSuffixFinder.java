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

import java.util.List;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;

/**
 * Interface for a global suffix finder. A global suffix finder takes a counterexample (plus other supplemental
 * information), and returns a list of words that, when used as distinguishing suffixes, will expose at least one
 * additional state in the hypothesis.
 * <p>
 * Please note that the type parameters of these class only constitute <i>upper</i> bounds for the respective input
 * symbol and output classes, denoting the requirements of the process in general. A suffix finder which does not
 * exploit any properties of the used classes will implement this interface with {@code <Object,Object>} generic
 * arguments only. The genericity is still maintained due to the {@code RI} and {@code RO} generic parameters in the
 * {@link #findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)} method.
 *
 * @param <I>
 *         input symbol type upper bound
 * @param <D>
 *         output domain type upper bound
 */
public interface GlobalSuffixFinder<I, D> {

    /**
     * Finds a set of distinguishing suffixes which will allow to expose at least one additional state in the
     * hypothesis.
     *
     * @param <RI>
     *         real input symbol type used for *this* counterexample analysis
     * @param <RD>
     *         real output domain type used for *this* counterexample analysis
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
     * @return a list of distinguishing suffixes, or the empty set if the counterexample could not be analyzed.
     */
    <RI extends I, RD extends D> List<Word<RI>> findSuffixes(Query<RI, RD> ceQuery,
                                                             AccessSequenceTransformer<RI> asTransformer,
                                                             SuffixOutput<RI, RD> hypOutput,
                                                             MembershipOracle<RI, RD> oracle);

}
