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
package de.learnlib.oracle.inclusion;

import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.inclusion.AbstractBreadthFirstInclusionOracle.DFABreadthFirstInclusionOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class DFABreadthFirstInclusionOracleTest
        extends AbstractBreadthFirstInclusionOracleTest<DFA<?, Integer>, Integer, Boolean> {

    @Mock
    private DFAMembershipOracle<Integer> dfaMembershipOracle;

    @Mock
    private DFAMembershipOracle<Character> dfaMembershipOracle2;

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.doAnswer(invocation -> {
            final DefaultQuery<Integer, Boolean> q = invocation.getArgument(0);
            if (q.getInput().equals(Word.fromSymbols(0, 0))) {
                q.answer(true);
            } else {
                q.answer(false);
            }
            return null;
        }).when(dfaMembershipOracle).processQuery(ArgumentMatchers.any());
    }

    @Override
    protected DFA<Integer, Integer> createAutomaton() {
        return ExampleAngluin.constructMachine();
    }

    @Override
    protected AbstractBreadthFirstInclusionOracle<DFA<?, Integer>, Integer, Boolean> createBreadthFirstInclusionOracle() {
        return new DFABreadthFirstInclusionOracle<>(5, dfaMembershipOracle);
    }

    @Override
    protected Alphabet<Integer> createAlphabet() {
        return ExampleAngluin.createInputAlphabet();
    }

    @Override
    protected DefaultQuery<Integer, ?> createQuery() {
        return new DefaultQuery<>(Word.fromSymbols(1, 1), false);
    }

    @Override
    protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, Boolean, DefaultQuery<Character, Boolean>>
            createBreadthFirstOracle(int maxWords) {
        return new DFABreadthFirstInclusionOracle<>(maxWords, dfaMembershipOracle2);
    }
}
