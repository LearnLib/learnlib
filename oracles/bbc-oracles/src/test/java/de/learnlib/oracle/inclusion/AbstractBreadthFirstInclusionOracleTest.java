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
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.AbstractBreadthFirstOracleTest;
import de.learnlib.oracle.inclusion.AbstractBreadthFirstInclusionOracle.DFABreadthFirstInclusionOracle;
import de.learnlib.oracle.inclusion.AbstractBreadthFirstInclusionOracle.MealyBreadthFirstInclusionOracle;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractBreadthFirstInclusionOracleTest<A extends SimpleDTS<?, I> & Output<I, D>, I, D> extends
                                                                                                              AbstractBreadthFirstOracleTest<D> {

    private AbstractBreadthFirstInclusionOracle<A, I, D> bfeo;

    private A automaton;

    private Alphabet<I> alphabet;

    private DefaultQuery<I, ?> query;

    protected abstract AbstractBreadthFirstInclusionOracle<A, I, D> createBreadthFirstInclusionOracle();

    protected abstract A createAutomaton();

    protected abstract Alphabet<I> createAlphabet();

    protected abstract DefaultQuery<I, ?> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        bfeo = createBreadthFirstInclusionOracle();
        automaton = createAutomaton();
        alphabet = createAlphabet();
        query = createQuery();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = bfeo.findCounterExample(automaton, alphabet);
        Assert.assertEquals(query, cex);
    }

    public static class DFABreadthFirstInclusionOracleTest
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
            }).when(dfaMembershipOracle).processQuery(Matchers.any());
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

    public static class MealyBreadthFirstInclusionOracleTest
            extends AbstractBreadthFirstInclusionOracleTest<MealyMachine<?, Input, ?, String>, Input, Word<String>> {

        @Mock
        private MealyMembershipOracle<Input, String> mealyMembershipOracle;

        @Mock
        private MealyMembershipOracle<Character, String> mealyMembershipOracle2;

        @BeforeMethod
        public void setUp() {
            super.setUp();
            Mockito.doAnswer(invocation -> {
                final DefaultQuery<Input, Word<String>> q = invocation.getArgument(0);
                if (q.getInput().equals(Word.fromSymbols(Input.WATER))) {
                    q.answer(Word.fromSymbols(ExampleCoffeeMachine.OUT_OK));
                } else {
                    q.answer(Word.fromSymbols("not-an-output"));
                }
                return null;
            }).when(mealyMembershipOracle).processQuery(Matchers.any());

        }

        @Override
        protected AbstractBreadthFirstInclusionOracle<MealyMachine<?, Input, ?, String>, Input, Word<String>>
                createBreadthFirstInclusionOracle() {
            return new MealyBreadthFirstInclusionOracle<>(5, mealyMembershipOracle);
        }

        @Override
        protected MealyMachine<?, Input, ?, String> createAutomaton() {
            return ExampleCoffeeMachine.constructMachine();
        }

        @Override
        protected Alphabet<Input> createAlphabet() {
            return ExampleCoffeeMachine.createInputAlphabet();
        }

        @Override
        protected DefaultQuery<Input, Word<String>> createQuery() {
            return new DefaultQuery<>(Word.epsilon(), Word.fromSymbols(Input.POD), Word.fromSymbols("not-an-output"));
        }

        @Override
        protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>,
                                             Character, Word<String>, DefaultQuery<Character, Word<String>>>
                createBreadthFirstOracle(int maxWords) {
            return new MealyBreadthFirstInclusionOracle<>(maxWords, mealyMembershipOracle2);
        }
    }
}
