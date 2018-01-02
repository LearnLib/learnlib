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

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.LearningExample;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.exception.UndefinedPropertyAccessException;
import net.automatalib.words.Alphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyAutomatonOracleTest {

    @Test
    public void testEmptyDFA() throws Exception {
        testEmptyAutomaton(ExamplePaulAndMary.createExample(), new CompactDFA.Creator<>());
    }

    @Test
    public void testEmptyMealy() throws Exception {
        Assert.assertThrows(UndefinedPropertyAccessException.class,
                            () -> testEmptyAutomaton(ExampleStack.createExample(), new CompactMealy.Creator<>()));
    }

    private <I, D, A extends DetSuffixOutputAutomaton<?, I, ?, D> & UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testEmptyAutomaton(
            LearningExample<I, D, A> example,
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
