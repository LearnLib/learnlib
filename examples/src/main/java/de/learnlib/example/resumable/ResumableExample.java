/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.example.resumable;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.thoughtworks.xstream.XStream;
import de.learnlib.Resumable;
import de.learnlib.algorithm.lstar.dfa.ClassicLStarDFA;
import de.learnlib.filter.cache.dfa.DFACacheOracle;
import de.learnlib.filter.cache.dfa.DFACaches;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.DFASimulatorEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.alphabet.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.util.automaton.random.RandomAutomata;

/**
 * An example to demonstrate the {@link Resumable} feature of LearnLib to continue learning setups from previously
 * stored snapshots.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ResumableExample {

    private static final CompactDFA<Character> TARGET;
    private static final Alphabet<Character> INITIAL_ALPHABET;
    private static final XStream X_STREAM;

    static {
        final int seed = 42;
        final int size = 100;

        TARGET = RandomAutomata.randomDFA(new Random(seed), size, Alphabets.characters('a', 'd'));
        INITIAL_ALPHABET = Alphabets.characters('a', 'b');

        X_STREAM = new XStream();
        X_STREAM.allowTypesByRegExp(new String[] {"net.automatalib.*", "de.learnlib.*"});
    }

    private ResumableExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {

        final Setup setup = new Setup();

        // construct initial model with inputs 'a' and 'b'
        setup.learner.startLearning();
        DefaultQuery<Character, Boolean> ce;
        while ((ce = setup.eqo.findCounterExample(setup.learner.getHypothesisModel(), INITIAL_ALPHABET)) != null) {
            setup.learner.refineHypothesis(ce);
        }

        System.out.println("## Initial setup");
        printStats(setup);

        // serialize the current state of the learning setup which may be stored somewhere external
        final byte[] learnerData = toBytes(setup.learner);
        final byte[] cacheData = toBytes(setup.cache);

        // continue exploring the previous snapshot with new input symbol 'c'
        continueExploring(learnerData, cacheData, 'c');

        // continue exploring the previous snapshot with new input symbol 'd'. Note that in this scenario we have never added 'c'.
        continueExploring(learnerData, cacheData, 'd');
    }

    private static void continueExploring(byte[] learnerData, byte[] cacheData, char newSymbol) {

        // re-initialize setup
        final Setup setup = new Setup();

        // resume from previous states and add new symbol
        setup.cache.resume(fromBytes(cacheData));
        setup.cache.addAlphabetSymbol(newSymbol);

        setup.learner.resume(fromBytes(learnerData));
        setup.learner.addAlphabetSymbol(newSymbol);

        // continue learning-loop
        DefaultQuery<Character, Boolean> ce;
        while ((ce = setup.eqo.findCounterExample(setup.learner.getHypothesisModel(), INITIAL_ALPHABET)) != null) {
            setup.learner.refineHypothesis(ce);
        }

        System.out.println("## After exploring '" + newSymbol + '\'');
        printStats(setup);
    }

    private static void printStats(Setup setup) {
        System.out.println("Hypothesis size: " + setup.learner.getHypothesisModel().size());
        System.out.println(setup.counter.getStatisticalData().getSummary());
        System.out.println();
    }

    private static <T> byte[] toBytes(Resumable<T> resumable) {
        return X_STREAM.toXML(resumable.suspend()).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromBytes(byte[] bytes) {
        return (T) X_STREAM.fromXML(new String(bytes, StandardCharsets.UTF_8));
    }

    private static class Setup {

        private final DFACounterOracle<Character> counter;
        private final DFACacheOracle<Character> cache;
        private final DFAEquivalenceOracle<Character> eqo;
        private final ClassicLStarDFA<Character> learner;

        Setup() {
            final DFAMembershipOracle<Character> mqo = new DFASimulatorOracle<>(TARGET);
            this.counter = new DFACounterOracle<>(mqo);
            this.cache = DFACaches.createCache(new GrowingMapAlphabet<>(INITIAL_ALPHABET), counter);
            this.eqo = new DFASimulatorEQOracle<>(TARGET);
            this.learner = new ClassicLStarDFA<>(new GrowingMapAlphabet<>(INITIAL_ALPHABET), cache);
        }
    }
}
