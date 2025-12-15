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
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.GenerationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.OptimizationMetric;
import net.automatalib.word.Word;

/**
 * An {@link EquivalenceOracle} based on the concepts of mutation testing as described in the paper <a
 * href="https://doi.org/10.1007/978-3-319-57288-8_2">Learning from Faults: Mutation Testing in Active Automata
 * Learning</a> by Bernhard K. Aichernig and Martin Tappler.
 * <p>
 * This Equivalence oracle selects test cases based on k-way transitions coverage. It does that by generating random
 * queries and finding the smallest subset with the highest coverage. In other words, this oracle finds counter examples
 * by running random paths that cover all pairwise / k-way transitions.
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
 * @see KWayTransitionCoverTestsIterator
 */
public class KWayTransitionCoverEQOracle<A extends DeterministicAutomaton<?, I, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final Random random;
    private final int randomWalkLen;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final int k;
    private final OptimizationMetric optimizationMetric;
    private final GenerationMethod generationMethod;

    /**
     * Constructor.
     *
     * @param oracle
     *         the oracle for accessing the system under learning
     * @param random
     *         the random number generator to use
     * @param randomWalkLen
     *         the number of steps that are added by {@link GenerationMethod#PREFIX prefix}-generated paths
     * @param numGeneratePaths
     *         number of {@link GenerationMethod#RANDOM randomly}-generated queries used to find the optimal subset
     * @param maxPathLen
     *         the maximum step size of {@link GenerationMethod#RANDOM randomly}-generated queries
     * @param maxNumberOfSteps
     *         threshold for the number of steps after which no more new test words will be generated (a value less than
     *         zero means no limit)
     * @param k
     *         k value used for K-Way transitions, i.e the number of steps between the start and the end of a
     *         transition
     * @param optimizationMetric
     *         the metric after which test queries are minimized
     * @param generationMethod
     *         defines how the queries are generated
     * @param batchSize
     *         size of the batches sent to the membership oracle
     *
     * @see KWayTransitionCoverTestsIterator#KWayTransitionCoverTestsIterator(DeterministicAutomaton, Collection,
     * Random, int, int, int, int, int, OptimizationMetric, GenerationMethod)
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public KWayTransitionCoverEQOracle(MembershipOracle<I, D> oracle,
                                       Random random,
                                       int randomWalkLen,
                                       int numGeneratePaths,
                                       int maxPathLen,
                                       int maxNumberOfSteps,
                                       int k,
                                       OptimizationMetric optimizationMetric,
                                       GenerationMethod generationMethod,
                                       int batchSize) {
        super(oracle, batchSize);
        this.random = random;
        this.randomWalkLen = randomWalkLen;
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.k = k;
        this.optimizationMetric = optimizationMetric;
        this.generationMethod = generationMethod;
    }

    @Override
    public Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        final DeterministicAutomaton<?, I, ?> casted = hypothesis;
        return doGenerateTestWords(casted,
                                   inputs,
                                   this.random,
                                   this.randomWalkLen,
                                   this.numGeneratePaths,
                                   this.maxPathLen,
                                   this.maxNumberOfSteps,
                                   this.k,
                                   this.optimizationMetric,
                                   this.generationMethod);
    }

    private static <A extends DeterministicAutomaton<S, I, ?>, S, I> Stream<Word<I>> doGenerateTestWords(A hypothesis,
                                                                                                         Collection<? extends I> inputs,
                                                                                                         Random random,
                                                                                                         int randomWalkLen,
                                                                                                         int numGeneratePaths,
                                                                                                         int maxPathLen,
                                                                                                         int maxNumberOfSteps,
                                                                                                         int k,
                                                                                                         OptimizationMetric optimizationMetric,
                                                                                                         GenerationMethod generationMethod) {
        return IteratorUtil.stream(new KWayTransitionCoverTestsIterator<>(hypothesis,
                                                                          inputs,
                                                                          random,
                                                                          randomWalkLen,
                                                                          numGeneratePaths,
                                                                          maxPathLen,
                                                                          maxNumberOfSteps,
                                                                          k,
                                                                          optimizationMetric,
                                                                          generationMethod));
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
            return KWayTransitionCoverTestsIterator.DEFAULT_K;
        }

        static GenerationMethod generationMethod() {
            return GenerationMethod.RANDOM;
        }

        static int numGeneratePaths() {
            return KWayTransitionCoverTestsIterator.DEFAULT_NUM_GEN_PATHS;
        }

        static int maxPathLen() {
            return KWayTransitionCoverTestsIterator.DEFAULT_MAX_PATH_LENGTH;
        }

        static int maxNumberOfSteps() {
            return KWayTransitionCoverTestsIterator.DEFAULT_MAX_NUM_STEPS;
        }

        static OptimizationMetric optimizationMetric() {
            return OptimizationMetric.STEPS;
        }

        static int randomWalkLen() {
            return KWayTransitionCoverTestsIterator.DEFAULT_R_WALK_LEN;
        }
    }
}
