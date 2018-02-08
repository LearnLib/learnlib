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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Implements an equivalence test by applying the W-method test on the given hypothesis automaton. Generally the
 * Wp-method performs better in finding counter examples. Instead of enumerating the test suite in order, this is a
 * sampling implementation: 1. sample uniformly from the states for a prefix 2. sample geometrically a random word 3.
 * sample a word from the set of suffixes / state identifiers There are two parameters: minimalSize determines the
 * minimal size of the random word, this is useful when one first performs a W(p)-method with some depth and continue
 * with this randomized tester from that depth onward. The second parameter rndLength determines the expected length of
 * the random word. (The expected length in effect is minimalSize + rndLength.) In the unbounded case it will not
 * terminate for a correct hypothesis.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Joshua Moerman
 */
public class RandomWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int minimalSize;
    private final int rndLength;
    private final int bound;
    private final Random rand;

    /**
     * Constructor for an unbounded testing oracle.
     *
     * @param sulOracle
     *         oracle which answers tests.
     * @param minimalSize
     *         minimal size of the random word
     * @param rndLength
     *         expected length (in addition to minimalSize) of random word
     */
    public RandomWMethodEQOracle(MembershipOracle<I, D> sulOracle, int minimalSize, int rndLength) {
        this(sulOracle, minimalSize, rndLength, 0);
    }

    /**
     * Constructor for a bounded testing oracle.
     *
     * @param sulOracle
     *         oracle which answers tests.
     * @param minimalSize
     *         minimal size of the random word
     * @param rndLength
     *         expected length (in addition to minimalSize) of random word
     * @param bound
     *         specifies the bound (set to 0 for unbounded).
     */
    public RandomWMethodEQOracle(MembershipOracle<I, D> sulOracle,
                                 int minimalSize,
                                 int rndLength,
                                 int bound) {
        this(sulOracle, minimalSize, rndLength, bound, new Random(), 1);
    }

    /**
     * Constructor for a bounded testing oracle with a specific batch size.
     *
     * @param sulOracle
     *         oracle which answers tests.
     * @param minimalSize
     *         minimal size of the random word
     * @param rndLength
     *         expected length (in addition to minimalSize) of random word
     * @param bound
     *         specifies the bound (set to 0 for unbounded).
     * @param batchSize
     *         size of the batches sent to the membership oracle
     */
    public RandomWMethodEQOracle(MembershipOracle<I, D> sulOracle,
                                 int minimalSize,
                                 int rndLength,
                                 int bound,
                                 int batchSize) {
        this(sulOracle, minimalSize, rndLength, bound, new Random(), batchSize);
    }

    /**
     * Constructor for a bounded testing oracle with a specific batch size.
     *
     * @param sulOracle
     *         oracle which answers tests.
     * @param minimalSize
     *         minimal size of the random word
     * @param rndLength
     *         expected length (in addition to minimalSize) of random word
     * @param bound
     *         specifies the bound (set to 0 for unbounded).
     * @param random
     *          custom Random generator.
     * @param batchSize
     *         size of the batches sent to the membership oracle
     */
    public RandomWMethodEQOracle(MembershipOracle<I, D> sulOracle,
                                 int minimalSize,
                                 int rndLength,
                                 int bound,
                                 Random random,
                                 int batchSize) {
        super(sulOracle, batchSize);
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = bound;
        this.rand = random;
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
        // Note that we want to use ArrayLists because we want constant time random access
        // We will sample from this for a prefix
        ArrayList<Word<I>> stateCover = new ArrayList<>(hypothesis.size());
        Covers.stateCover(hypothesis, inputs, stateCover);

        // Then repeatedly from this for a random word
        ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);

        // Finally we test the state with a suffix
        ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
        Automata.characterizingSet(hypothesis, inputs, globalSuffixes);

        final Stream<Word<I>> result =
                Stream.generate(() -> generateSingleTestWord(stateCover, arrayAlphabet, globalSuffixes));

        return bound > 0 ? result.limit(bound) : result;
    }

    private Word<I> generateSingleTestWord(ArrayList<Word<I>> stateCover,
                                           ArrayList<I> arrayAlphabet,
                                           ArrayList<Word<I>> globalSuffixes) {
        final WordBuilder<I> wb = new WordBuilder<>(minimalSize + rndLength + 1);

        // pick a random state
        wb.append(stateCover.get(rand.nextInt(stateCover.size())));

        // construct random middle part (of some expected length)
        int size = minimalSize;
        while ((size > 0) || (rand.nextDouble() > 1 / (rndLength + 1.0))) {
            wb.append(arrayAlphabet.get(rand.nextInt(arrayAlphabet.size())));
            if (size > 0) {
                size--;
            }
        }

        // pick a random suffix for this state
        if (!globalSuffixes.isEmpty()) {
            wb.append(globalSuffixes.get(rand.nextInt(globalSuffixes.size())));
        }

        return wb.toWord();
    }

    public static class DFARandomWMethodEQOracle<I>
            extends RandomWMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFARandomWMethodEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                        int minimalSize,
                                        int rndLength) {
            super(mqOracle, minimalSize, rndLength);
        }

        public DFARandomWMethodEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                        int minimalSize,
                                        int rndLength,
                                        int bound) {
            super(mqOracle, minimalSize, rndLength, bound);
        }

        public DFARandomWMethodEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                        int minimalSize,
                                        int rndLength,
                                        int bound,
                                        int batchSize) {
            super(mqOracle, minimalSize, rndLength, bound, batchSize);
        }

        public DFARandomWMethodEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                        int minimalSize,
                                        int rndLength,
                                        int bound,
                                        Random random,
                                        int batchSize) {
            super(mqOracle, minimalSize, rndLength, bound, random, batchSize);
        }

    }

    public static class MealyRandomWMethodEQOracle<I, O>
            extends RandomWMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        public MealyRandomWMethodEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                          int minimalSize,
                                          int rndLength) {
            super(mqOracle, minimalSize, rndLength);
        }

        public MealyRandomWMethodEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                          int minimalSize,
                                          int rndLength,
                                          int bound) {
            super(mqOracle, minimalSize, rndLength, bound);
        }

        public MealyRandomWMethodEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                          int minimalSize,
                                          int rndLength,
                                          int bound,
                                          int batchSize) {
            super(mqOracle, minimalSize, rndLength, bound, batchSize);
        }

        public MealyRandomWMethodEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                          int minimalSize,
                                          int rndLength,
                                          int bound,
                                          Random random,
                                          int batchSize) {
            super(mqOracle, minimalSize, rndLength, bound, random, batchSize);
        }

    }
}
