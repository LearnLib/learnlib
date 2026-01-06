/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.oracle.equivalence;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.word.Word;

/**
 * An {@link EquivalenceOracle} based on the concepts of mutation testing as described in the paper <a
 * href="https://doi.org/10.1007/978-3-319-57288-8_2">Learning from Faults: Mutation Testing in Active Automata
 * Learning</a> by Bernhard K. Aichernig and Martin Tappler.
 * <p>
 * A test case will be computed for every k-combination or k-permutation of states with additional random walk at the
 * end.
 * <p>
 * <b>Implementation detail:</b> Note that this test generator heavily relies on the sampling of states. If the given
 * automaton has very few or very many states, the number of generated test cases may be very low or high, respectively.
 * As a result, it may be advisable to {@link EQOracleChain combine} this generator with other generators or limit the
 * number of generated test cases.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain
 *
 * @see KWayStateCoverTestsIterator
 */
public class KWayStateCoverEQOracle<A extends DeterministicAutomaton<?, I, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final Random random;
    private final int randomWalkLen;
    private final int k;
    private final CombinationMethod combinationMethod;

    /**
     * Constructor.
     *
     * @param oracle
     *         the oracle for accessing the system under learning
     * @param random
     *         the random number generator to use
     * @param randomWalkLen
     *         length of random walk performed at the end of each combination/permutation
     * @param k
     *         k value used for k-wise combinations/permutations of states
     * @param combinationMethod
     *         the method for computing combinations
     * @param batchSize
     *         size of the batches sent to the membership oracle
     *
     * @see KWayStateCoverTestsIterator#KWayStateCoverTestsIterator(DeterministicAutomaton, Collection, Random, int,
     * int, CombinationMethod)
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public KWayStateCoverEQOracle(MembershipOracle<I, D> oracle,
                                  Random random,
                                  int randomWalkLen,
                                  int k,
                                  CombinationMethod combinationMethod,
                                  int batchSize) {
        super(oracle, batchSize);
        this.random = random;
        this.randomWalkLen = randomWalkLen;
        this.k = k;
        this.combinationMethod = combinationMethod;
    }

    @Override
    public Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        final DeterministicAutomaton<?, I, ?> casted = hypothesis;
        return doGenerateTestWords(casted, inputs, this.random, this.randomWalkLen, this.k, this.combinationMethod);
    }

    private static <A extends DeterministicAutomaton<S, I, ?>, S, I> Stream<Word<I>> doGenerateTestWords(A hypothesis,
                                                                                                         Collection<? extends I> inputs,
                                                                                                         Random random,
                                                                                                         int randomWalkLen,
                                                                                                         int k,
                                                                                                         CombinationMethod combinationMethod) {
        return IteratorUtil.stream(new KWayStateCoverTestsIterator<>(hypothesis,
                                                                     inputs,
                                                                     random,
                                                                     randomWalkLen,
                                                                     k,
                                                                     combinationMethod));
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        static Random random() {
            return new Random();
        }

        static int batchSize() {
            return 1;
        }

        static int k() {
            return KWayStateCoverTestsIterator.DEFAULT_K;
        }

        static int randomWalkLen() {
            return KWayStateCoverTestsIterator.DEFAULT_R_WALK_LEN;
        }

        static CombinationMethod combinationMethod() {
            return CombinationMethod.PERMUTATIONS;
        }
    }
}
