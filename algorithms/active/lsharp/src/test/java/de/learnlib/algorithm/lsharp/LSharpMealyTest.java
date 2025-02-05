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
package de.learnlib.algorithm.lsharp;

import java.util.Collection;
import java.util.Random;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LSharpMealyTest {

    /**
     * Checks that {@link LSharpMealy#getHypothesisModel()} is free of side-effects. For details, see <a
     * href="https://github.com/LearnLib/learnlib/issues/145">issue 145</a>.
     */
    @Test
    public void testIssue145() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        CompactMealy<Integer, Character> mealy =
                RandomAutomata.randomMealy(new Random(42), 20, alphabet, Alphabets.characters('a', 'c'));

        ValidatingOracle<Integer, Character> mqo =
                new ValidatingOracle<>(new SULAdaptiveOracle<>(new MealySimulatorSUL<>(mealy)));
        MealySimulatorEQOracle<Integer, Character> eqo = new MealySimulatorEQOracle<>(mealy);
        LSharpMealy<Integer, Character> learner = new LSharpMealyBuilder<Integer, Character>().withAlphabet(alphabet)
                                                                                              .withOracle(mqo)
                                                                                              .withRandom(new Random(42))
                                                                                              .create();

        mqo.mutable = true;
        learner.startLearning();
        mqo.mutable = false;
        MealyMachine<?, Integer, ?, Character> hyp = learner.getHypothesisModel();
        DefaultQuery<Integer, Word<Character>> cex;

        while ((cex = eqo.findCounterExample(hyp, alphabet)) != null) {
            mqo.mutable = true;
            learner.refineHypothesis(cex);
            mqo.mutable = false;
            hyp = learner.getHypothesisModel();
        }

        Assert.assertTrue(Automata.testEquivalence(mealy, learner.getHypothesisModel(), alphabet));
    }

    private static final class ValidatingOracle<I, O> implements AdaptiveMembershipOracle<I, O> {

        private final AdaptiveMembershipOracle<I, O> delegate;
        private boolean mutable;

        ValidatingOracle(AdaptiveMembershipOracle<I, O> delegate) {
            this.delegate = delegate;
            this.mutable = false;
        }

        @Override
        public void processQueries(Collection<? extends AdaptiveQuery<I, O>> adaptiveQueries) {
            Assert.assertTrue(mutable);
            this.delegate.processQueries(adaptiveQueries);

        }
    }

}
