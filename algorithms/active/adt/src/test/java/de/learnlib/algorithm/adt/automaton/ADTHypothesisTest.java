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
package de.learnlib.algorithm.adt.automaton;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.StateIDs;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for the {@link ADTHypothesis} class.
 */
public class ADTHypothesisTest {

    @Test
    public void testAutomaton() {

        final int states = 10;
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'd');
        final ADTHypothesis<Character, Integer> automaton = new ADTHypothesis<>(alphabet);

        automaton.addInitialState();

        for (int i = 1; i < states; i++) {
            automaton.addState();
        }

        Assert.assertEquals(automaton.size(), states);
        automaton.getStates().forEach(x -> Assert.assertTrue(x.getIncomingTransitions().isEmpty()));

        final StateIDs<ADTState<Character, Integer>> stateIds = automaton.stateIDs();

        final ADTState<Character, Integer> init = automaton.getInitialState();
        Assert.assertNotNull(init);

        for (int s = 0; s < automaton.size(); s++) {
            for (Character i : alphabet) {
                automaton.addTransition(stateIds.getState(s), i, init, 0);
            }
        }

        Assert.assertEquals(states * alphabet.size(), init.getIncomingTransitions().size());

        final ADTState<Character, Integer> s1 = stateIds.getState(1), s2 = stateIds.getState(2), s3 =
                stateIds.getState(3);

        automaton.removeAllTransitions(s1);

        Assert.assertEquals((states - 1) * alphabet.size(), init.getIncomingTransitions().size());

        automaton.removeAllTransitions(s2, alphabet.getSymbol(0));

        Assert.assertEquals((states - 1) * alphabet.size() - 1, init.getIncomingTransitions().size());

        automaton.addTransition(s2, alphabet.getSymbol(0), s1, 0);

        for (int i = 1; i < alphabet.size(); i++) {
            ADTTransition<Character, Integer> transition = automaton.getTransition(s2, alphabet.getSymbol(i));
            Assert.assertNotNull(transition);
            transition.setTarget(s1);
        }

        Assert.assertEquals(alphabet.size(), s1.getIncomingTransitions().size());

        for (int i = 0; i < alphabet.size(); i++) {
            automaton.setTransition(s3, alphabet.getSymbol(i), s1, 0);
        }

        Assert.assertEquals(alphabet.size() * 2, s1.getIncomingTransitions().size());
    }

}
