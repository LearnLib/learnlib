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
import net.automatalib.automaton.UniversalDeterministicAutomaton;
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
 *
 * @see KWayTransitionCoverTestsIterator
 */
public class KWayTransitionCoverEQOracle<A extends UniversalDeterministicAutomaton<S, I, T, ?, ?> & Output<I, D>, S, I, T, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int k;
    private final GenerationMethod generationMethod;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final OptimizationMetric optimizationMetric;
    private final int randomWalkLen;
    private final Random random;

    /**
     * Constructor.
     *
     * @param oracle
     *         the oracle for accessing the system under learning
     * @param random
     *         the random number generator to use
     * @param randomWalkLen
     *         the number of steps that are added by 'prefix' generated paths
     * @param numGeneratePaths
     *         number of random queries used to find the optimal subset
     * @param maxPathLen
     *         the maximum step size of a generated path
     * @param maxNumberOfSteps
     *         maximum number of steps that will be executed on the automaton (<=0 = no limit)
     * @param k
     *         k value used for K-Way transitions, i.e the number of steps between the start and the end of a
     *         transition
     * @param generationMethod
     *         defines how the queries are generated 'random' or 'prefix'
     * @param optimizationMetric
     *         minimize either the number of 'steps' or 'queries' that are executed
     * @param batchSize
     *         size of the batches sent to the membership oracle
     *
     * @see KWayTransitionCoverTestsIterator#KWayTransitionCoverTestsIterator(UniversalDeterministicAutomaton,
     * Collection, Random, int, int, int, int, int, GenerationMethod, OptimizationMetric)
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public KWayTransitionCoverEQOracle(MembershipOracle<I, D> oracle,
                                       Random random,
                                       int randomWalkLen,
                                       int numGeneratePaths,
                                       int maxPathLen,
                                       int maxNumberOfSteps,
                                       int k,
                                       GenerationMethod generationMethod,
                                       OptimizationMetric optimizationMetric,
                                       int batchSize) {
        super(oracle, batchSize);
        this.random = random;
        this.k = k;
        this.generationMethod = generationMethod;
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.optimizationMetric = optimizationMetric;
        this.randomWalkLen = randomWalkLen;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return IteratorUtil.stream(new KWayTransitionCoverTestsIterator<>(hypothesis,
                                                                          inputs,
                                                                          random,
                                                                          randomWalkLen,
                                                                          numGeneratePaths,
                                                                          maxPathLen,
                                                                          maxNumberOfSteps,
                                                                          k,
                                                                          generationMethod,
                                                                          optimizationMetric));
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
