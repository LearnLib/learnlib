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

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

public class DFABFInclusionOracleTest extends AbstractBFInclusionOracleTest<DFA<?, Character>, Boolean> {

    @Mock
    private MembershipOracle.DFAMembershipOracle<Character> mo;

    @BeforeMethod
    public void setUp() {
        super.setUp();

        Mockito.doAnswer(invocation -> {
            final DefaultQuery<Character, Boolean> q = invocation.getArgument(0);
            if (q.getInput().equals(Word.fromSymbols('a'))) {
                q.answer(false);
            } else {
                q.answer(true);
            }
            return null;
        }).when(mo).processQuery(ArgumentMatchers.any());
    }

    @Override
    protected DefaultQuery<Character, Boolean> createQuery() {
        return new DefaultQuery<>(Word.fromSymbols('a'), false);
    }

    @Override
    protected AbstractBFInclusionOracle<DFA<?, Character>, Character, Boolean> createBreadthFirstInclusionOracle() {
        return new DFABFInclusionOracle<>(mo, MULTIPLIER);
    }

    @Override
    protected DFA<?, Character> createAutomaton() {
        return AutomatonBuilders.forDFA(new CompactDFA<>(ALPHABET)).
                from("q0").on('a').loop().withAccepting("q0").withInitial("q0").create();
    }
}