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
package de.learnlib.oracle.emptiness;

import java.util.Objects;

import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.word.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

public class DFABFEmptinessOracleTest extends AbstractBFEmptinessOracleTest<DFA<?, Character>, Boolean> {

    @Mock
    private DFAMembershipOracle<Character> mo;

    @Override
    protected AbstractBFEmptinessOracle<DFA<?, Character>, Character, Boolean> createBreadthFirstEmptinessOracle(double multiplier) {
        return new DFABFEmptinessOracle<>(mo, multiplier);
    }

    @Override
    protected DFA<?, Character> createAutomaton() {
        return AutomatonBuilders.forDFA(new CompactDFA<>(ALPHABET)).
                from("q0").on('a').loop().withAccepting("q0").withInitial("q0").create();
    }

    @Override
    protected DefaultQuery<Character, Boolean> createQuery() {
        return new DefaultQuery<>(Word.fromSymbols('a'), true);
    }

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.doAnswer(invocation -> {
            final DefaultQuery<Character, Boolean> q = Objects.requireNonNull(invocation.getArgument(0));
            if (q.getInput().equals(Word.fromSymbols('a'))) {
                q.answer(true);
            } else {
                q.answer(false);
            }
            return null;
        }).when(mo).processQuery(ArgumentMatchers.any());
    }
}
