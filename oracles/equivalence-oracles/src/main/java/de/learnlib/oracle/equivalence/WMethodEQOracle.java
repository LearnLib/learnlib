/* Copyright (C) 2013-2017 TU Dortmund
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
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
     * @param maxDepth
     *         the maximum length of the "middle" part of the test cases
     * @param sulOracle
     *         interface to the system under learning
     */
    public WMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle) {
        this(maxDepth, sulOracle, 1);
    }

    /**
     * Constructor.
     *
     * @param maxDepth
     *         the maximum length of the "middle" part of the test cases
     * @param sulOracle
     *         interface to the system under learning
     * @param batchSize
     *         size of the batches sent to the membership oracle
     */
    public WMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle, int batchSize) {
        super(sulOracle, batchSize);
        this.maxDepth = maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {

        final List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
        final Iterable<Word<I>> middleTuples =
                Iterables.transform(CollectionsUtil.allTuples(inputs, 0, maxDepth), Word::fromList);
        List<Word<I>> characterizingSet = Automata.characterizingSet(hypothesis, inputs);

        // Special case: List of characterizing suffixes may be empty,
        // but in this case we still need to test!
        if (characterizingSet.isEmpty()) {
            characterizingSet = Collections.singletonList(Word.epsilon());
        }

        final Iterable<List<Word<I>>> wMethodIter =
                CollectionsUtil.allCombinations(transCover, middleTuples, characterizingSet);

        return Streams.stream(wMethodIter).map(Word::fromWords);
    }

    public static class DFAWMethodEQOracle<I> extends WMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFAWMethodEQOracle(int maxDepth, MembershipOracle<I, Boolean> sulOracle) {
            super(maxDepth, sulOracle);
        }

        public DFAWMethodEQOracle(int maxDepth, MembershipOracle<I, Boolean> sulOracle, int batchSize) {
            super(maxDepth, sulOracle, batchSize);
        }
    }

    public static class MealyWMethodEQOracle<I, O> extends WMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        public MealyWMethodEQOracle(int maxDepth, MembershipOracle<I, Word<O>> sulOracle) {
            super(maxDepth, sulOracle);
        }

        public MealyWMethodEQOracle(int maxDepth, MembershipOracle<I, Word<O>> sulOracle, int batchSize) {
            super(maxDepth, sulOracle, batchSize);
        }
    }

}