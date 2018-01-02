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

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.words.Word;

/**
 * Implements an equivalence test by applying the W-method test on the given hypothesis automaton, as described in
 * "Testing software design modeled by finite state machines" by T.S. Chow.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public class WMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private int maxDepth;

    /**
     * Constructor.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param maxDepth
 *         the maximum length of the "middle" part of the test cases
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int maxDepth) {
        this(sulOracle, maxDepth, 1);
    }

    /**
     * Constructor.
     *  @param sulOracle
     *         interface to the system under learning
     * @param maxDepth
     *         the maximum length of the "middle" part of the test cases
     * @param batchSize
     *         size of the batches sent to the membership oracle
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int maxDepth, int batchSize) {
        super(sulOracle, batchSize);
           this.maxDepth = maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return Streams.stream(new WMethodTestsIterator<>(hypothesis, inputs, maxDepth));
    }

    public static class DFAWMethodEQOracle<I> extends WMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFAWMethodEQOracle(MembershipOracle<I, Boolean> sulOracle, int maxDepth) {
            super(sulOracle, maxDepth);
        }

        public DFAWMethodEQOracle(MembershipOracle<I, Boolean> sulOracle, int maxDepth, int batchSize) {
            super(sulOracle, maxDepth, batchSize);
        }
    }

    public static class MealyWMethodEQOracle<I, O> extends WMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        public MealyWMethodEQOracle(MembershipOracle<I, Word<O>> sulOracle, int maxDepth) {
            super(sulOracle, maxDepth);
        }

        public MealyWMethodEQOracle(MembershipOracle<I, Word<O>> sulOracle, int maxDepth, int batchSize) {
            super(sulOracle, maxDepth, batchSize);
        }
    }

}