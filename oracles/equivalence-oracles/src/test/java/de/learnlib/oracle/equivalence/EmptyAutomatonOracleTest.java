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

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.LearningExample;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import de.learnlib.testsupport.example.mealy.ExampleStack;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.DetSuffixOutputAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.UndefinedPropertyAccessException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyAutomatonOracleTest {

    @Test
    public void testEmptyDFA() {
        testEmptyAutomaton(ExamplePaulAndMary.createExample(), new CompactDFA.Creator<>());
    }

    @Test
    public void testEmptyMealy() {
        Assert.assertThrows(UndefinedPropertyAccessException.class,
                            () -> testEmptyAutomaton(ExampleStack.createExample(), new CompactMealy.Creator<>()));
    }

    private <I, D, A extends DetSuffixOutputAutomaton<?, I, ?, D> & UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testEmptyAutomaton(
            LearningExample<I, A> example,
            AutomatonCreator<? extends A, I> emptyCreator) {

        final A automaton = example.getReferenceAutomaton();
        final Alphabet<I> alphabet = example.getAlphabet();

        final MembershipOracle<I, D> oracle = new SimulatorOracle<>(automaton);
        final EquivalenceOracle<Output<I, D>, I, D> eqTest = new CompleteExplorationEQOracle<>(oracle, automaton.size());
        final A emptyHypothesis = emptyCreator.createAutomaton(alphabet);

        final DefaultQuery<I, D> counterExample = eqTest.findCounterExample(emptyHypothesis, alphabet);

        Assert.assertNotNull(counterExample);
    }
}
