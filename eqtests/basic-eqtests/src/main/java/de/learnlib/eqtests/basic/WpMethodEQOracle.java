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
package de.learnlib.eqtests.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import de.learnlib.api.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;

/**
 * Implements an equivalence test by applying the Wp-method test on the given hypothesis automaton, as described in
 * "Test Selection Based on Finite State Models" by S. Fujiwara et al.
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
public class WpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int maxDepth;

    /**
     * Constructor.
     *
     * @param maxDepth
     *         the maximum length of the "middle" part of the test cases
     * @param sulOracle
     *         interface to the system under learning
     */
    public WpMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle) {
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
    public WpMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle, int batchSize) {
        super(sulOracle, batchSize);
        this.maxDepth = maxDepth;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
        return doGenerateTestWords(aut, inputs);
    }

    /*
     * Delegate target, used to bind the state-parameter of the automaton
     */
    private <S> Stream<Word<I>> doGenerateTestWords(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
                                                    Collection<? extends I> inputs) {

        final List<Word<I>> stateCover = new ArrayList<>(hypothesis.size());
        final List<Word<I>> transitionCover = new ArrayList<>(hypothesis.size() * (inputs.size() - 1));

        Automata.cover(hypothesis, inputs, stateCover, transitionCover);

        List<Word<I>> characterizingSet = Automata.characterizingSet(hypothesis, inputs);
        if (characterizingSet.isEmpty()) {
            characterizingSet = Collections.singletonList(Word.<I>epsilon());
        }

        // TODO maybe we can skip this wasted word allocation?
        final Iterable<Word<I>> middleTuples =
                Iterables.transform(CollectionsUtil.allTuples(inputs, 0, maxDepth), Word::fromList);

        // Phase 1: state cover * middle part * global suffixes
        final Stream<Word<I>> firstPhaseStream =
                Streams.stream(CollectionsUtil.allCombinations(stateCover, middleTuples, characterizingSet))
                       .map(Word::fromWords);

        // Phase 2: transitions (not in state cover) * middle part * local suffixes
        final MutableMapping<S, List<Word<I>>> localSuffixSets = hypothesis.createStaticStateMapping();
        final Iterable<List<Word<I>>> accessSequenceIter =
                CollectionsUtil.allCombinations(transitionCover, middleTuples);

        final Stream<Word<I>> secondPhaseStream = Streams.stream(accessSequenceIter)
                                                         .flatMap(as -> appendLocalSuffixes(hypothesis,
                                                                                            inputs,
                                                                                            as,
                                                                                            localSuffixSets));

        return Stream.concat(firstPhaseStream, secondPhaseStream);
    }

    private <S> Stream<Word<I>> appendLocalSuffixes(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
                                                    Collection<? extends I> inputs,
                                                    List<Word<I>> accessSequence,
                                                    MutableMapping<S, List<Word<I>>> cache) {

        final Word<I> as = Word.fromWords(accessSequence);

        final S state = hypothesis.getState(as);
        List<Word<I>> localSuffixes = cache.get(state);

        if (localSuffixes == null) {
            localSuffixes = Automata.stateCharacterizingSet(hypothesis, inputs, state);
            if (localSuffixes.isEmpty()) {
                localSuffixes = Collections.singletonList(Word.epsilon());
            }
            cache.put(state, localSuffixes);
        }

        return localSuffixes.stream().map(as::concat);
    }

    public static class DFAWpMethodEQOracle<I> extends WpMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFAWpMethodEQOracle(int maxDepth, MembershipOracle<I, Boolean> sulOracle) {
            super(maxDepth, sulOracle);
        }

        public DFAWpMethodEQOracle(int maxDepth, MembershipOracle<I, Boolean> sulOracle, int batchSize) {
            super(maxDepth, sulOracle, batchSize);
        }
    }

    public static class MealyWpMethodEQOracle<I, O> extends WpMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {

        public MealyWpMethodEQOracle(int maxDepth, MembershipOracle<I, Word<O>> sulOracle) {
            super(maxDepth, sulOracle);
        }

        public MealyWpMethodEQOracle(int maxDepth, MembershipOracle<I, Word<O>> sulOracle, int batchSize) {
            super(maxDepth, sulOracle, batchSize);
        }
    }

}
