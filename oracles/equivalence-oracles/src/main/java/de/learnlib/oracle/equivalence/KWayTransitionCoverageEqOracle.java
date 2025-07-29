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

import de.learnlib.oracle.MembershipOracle;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.Method;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.Optimize;
import net.automatalib.word.Word;

/**
 * This Equivalence oracle selects test cases based on k-way transitions coverage. It does that by generating random
 * queries and finding the smallest subset with the highest coverage. In other words, this oracle finds counter examples
 * by running random paths that cover all pairwise / k-way transitions.
 */
public class KWayTransitionCoverageEqOracle<A extends UniversalDeterministicAutomaton<S, I, T, ?, ?> & Output<I, D>, S, I, T, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int k;
    private final Method method;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final Optimize optimize;
    private final int randomWalkLen;
    private final Random random;

    public KWayTransitionCoverageEqOracle(MembershipOracle<I, D> oracle,
                                          Random random,
                                          int batchSize,
                                          int k,
                                          Method method,
                                          int numGeneratePaths,
                                          int maxPathLen,
                                          int maxNumberOfSteps,
                                          Optimize optimize,
                                          int randomWalkLen) {
        super(oracle, batchSize);

        this.random = random;
        this.k = k;
        this.method = method;
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.optimize = optimize;
        this.randomWalkLen = randomWalkLen;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return IteratorUtil.stream(new KWayTransitionCoverTestsIterator<>(hypothesis,
                                                                          inputs,
                                                                          random,
                                                                          k,
                                                                          method,
                                                                          numGeneratePaths,
                                                                          maxPathLen,
                                                                          maxNumberOfSteps,
                                                                          optimize,
                                                                          randomWalkLen));
    }
}
