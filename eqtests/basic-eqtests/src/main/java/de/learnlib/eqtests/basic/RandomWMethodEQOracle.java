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
import java.util.Objects;
import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.util.automata.Automata;
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
        implements EquivalenceOracle<A, I, D> {

    private final MembershipOracle<I, D> sulOracle;
    private final int minimalSize;
    private final int rndLength;
    private final int bound;

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
        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = 0;
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
    public RandomWMethodEQOracle(MembershipOracle<I, D> sulOracle, int minimalSize, int rndLength, int bound) {
        this.sulOracle = sulOracle;
        this.minimalSize = minimalSize;
        this.rndLength = rndLength;
        this.bound = bound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
        return doFindCounterExample(aut, hypothesis, inputs);
    }

    /*
     * Delegate target, used to bind the state-parameter of the automaton
     */
    private <S> DefaultQuery<I, D> doFindCounterExample(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
                                                        Output<I, D> output,
                                                        Collection<? extends I> inputs) {
        // Note that we want to use ArrayLists because we want constant time random access
        // We will sample from this for a prefix
        ArrayList<Word<I>> stateCover = new ArrayList<>(hypothesis.size());
        Automata.cover(hypothesis, inputs, stateCover, null);

        // Then repeatedly from this for a random word
        ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);

        // Finally we test the state with a suffix
        ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
        Automata.characterizingSet(hypothesis, inputs, globalSuffixes);

        Random rand = new Random();
        int currentBound = bound;
        while (bound == 0 || currentBound-- > 0) {
            WordBuilder<I> wb = new WordBuilder<>(minimalSize + rndLength + 1);

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

            Word<I> queryWord = wb.toWord();
            DefaultQuery<I, D> query = new DefaultQuery<>(queryWord);
            D hypOutput = output.computeOutput(queryWord);
            sulOracle.processQueries(Collections.singleton(query));
            if (!Objects.equals(hypOutput, query.getOutput())) {
                return query;
            }
        }

        // no counter example found within the bound
        return null;
    }
}
