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

import de.learnlib.oracle.MembershipOracle;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.word.Word;

/**
 * A test case will be computed for every k-combination or k-permutation of states with additional random walk at the
 * end.
 */
public class KWayStateCoverageEQOracle<A extends UniversalDeterministicAutomaton<S, I, T, ?, ?> & Output<I, D>, S, I, T, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final Random random;
    private final int k;
    private final int randomWalkLen;
    private final CombinationMethod method;

    public KWayStateCoverageEQOracle(MembershipOracle<I, D> oracle) {
        this(oracle, new Random(), 1);
    }

    /**
     * Initializes the KWayStateCoverageEqOracle.
     *
     * @param oracle
     *         system under learning
     */
    public KWayStateCoverageEQOracle(MembershipOracle<I, D> oracle, Random random, int batchSize) {
        this(oracle, random, batchSize, 2, 20, CombinationMethod.Permutations);
    }

    /**
     * Initializes the KWayStateCoverageEqOracle.
     *
     * @param oracle
     *         system under learning
     * @param k
     *         k value used for k-wise combinations/permutations of states
     * @param randomWalkLen
     *         length of random walk performed at the end of each combination/permutation
     * @param method
     *         either 'combinations' or 'permutations'
     */
    public KWayStateCoverageEQOracle(MembershipOracle<I, D> oracle,
                                     Random random,
                                     int batchSize,
                                     int k,
                                     int randomWalkLen,
                                     CombinationMethod method) {
        super(oracle, batchSize);
        this.random = random;
        this.k = k;
        this.randomWalkLen = randomWalkLen;
        this.method = method;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return IteratorUtil.stream(new KWayStateCoverTestsIterator<>(hypothesis,
                                                                     inputs,
                                                                     random,
                                                                     k,
                                                                     randomWalkLen,
                                                                     method));
    }
}