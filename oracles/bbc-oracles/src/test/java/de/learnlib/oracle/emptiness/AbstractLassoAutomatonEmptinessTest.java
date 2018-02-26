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
package de.learnlib.oracle.emptiness;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import de.learnlib.api.oracle.OmegaMembershipOracle.DFAOmegaMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.AbstractBreadthFirstOracleTest;
import de.learnlib.oracle.emptiness.AbstractLassoAutomatonEmptinessOracle.DFALassoDFAEmptinessOracle;
import de.learnlib.oracle.emptiness.AbstractLassoAutomatonEmptinessOracle.MealyLassoMealyEmptinessOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractLassoAutomatonEmptinessTest<L extends Lasso<?, ?, String, D>, D> extends
                                                                                               AbstractBreadthFirstOracleTest<D> {

    public static final Alphabet<String> ALPHABET = Alphabets.fromArray("a");

    private AbstractLassoAutomatonEmptinessOracle<L, ?, String, ?> laeo;

    private L lasso;

    private DefaultQuery<String, ?> query;

    protected abstract AbstractLassoAutomatonEmptinessOracle<L, ?, String, ?> createLassoAutomatonEmptinessOracle();

    protected abstract L createLasso();

    protected abstract DefaultQuery<String, ?> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        laeo = createLassoAutomatonEmptinessOracle();
        lasso = createLasso();
        query = createQuery();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = laeo.findCounterExample(lasso, ALPHABET);
        Assert.assertEquals(query, cex);
    }

    public static class DFALassoDFAEmptinessOracleTest
            extends AbstractLassoAutomatonEmptinessTest<DFALasso<?, String>, Boolean> {

        @Mock
        private DFAOmegaMembershipOracle<Integer, String> dfaOmegaMembershipOracle;

        @Mock
        private DFAOmegaMembershipOracle<Integer, Character> dfaOmegaMembershipOracle2;

        @BeforeMethod
        public void setUp() {
            super.setUp();
            Mockito.doAnswer(invocation -> {
                final OmegaQuery<Integer, String, Boolean> q = invocation.getArgument(0);
                if (q.getInput().equals(Word.fromSymbols("a", "a", "a"))) {
                    q.answer(true);
                    final List<Integer> states = new ArrayList<>();
                    states.add(0);
                    states.add(0);
                    states.add(0);
                    states.add(0);
                    q.setStates(states);
                } else {
                    q.answer(false);
                }
                return null;
            }).when(dfaOmegaMembershipOracle).processQuery(Matchers.any());
            Mockito.when(dfaOmegaMembershipOracle.isSameState(Matchers.any(),
                                                              Matchers.any(),
                                                              Matchers.any(),
                                                              Matchers.any())).thenReturn(true);
        }

        @Override
        protected AbstractLassoAutomatonEmptinessOracle<DFALasso<?, String>, ?, String, ?>
                createLassoAutomatonEmptinessOracle() {
            return new DFALassoDFAEmptinessOracle<>(dfaOmegaMembershipOracle);
        }

        @Override
        protected DFALasso<?, String> createLasso() {
            final DFA<?, String> dfa = AutomatonBuilders.forDFA(new CompactDFA<>(ALPHABET)).
                    from("q0").on("a").loop().withAccepting("q0").withInitial("q0").create();
            return new DFALasso<>(dfa, ALPHABET, 3);
        }

        @Override
        protected DefaultQuery<String, ?> createQuery() {
            return new DefaultQuery<>(Word.fromSymbols("a", "a", "a"), true);
        }

        @Override
        protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>,
                                             Character,
                                             Boolean,
                                             OmegaQuery<Integer, Character, Boolean>> createBreadthFirstOracle(int maxWords) {
            return new DFALassoDFAEmptinessOracle<>(dfaOmegaMembershipOracle2);
        }
    }

    public static class MealyLassoMealyEmptinessOracleTest
            extends AbstractLassoAutomatonEmptinessTest<MealyLasso<?, String, String>, Word<String>> {

        @Mock
        private MealyOmegaMembershipOracle<Integer, String, String> mealyOmegaMembershipOracle;

        @Mock
        private MealyOmegaMembershipOracle<Integer, Character, String> mealyOmegaMembershipOracle2;

        @BeforeMethod
        public void setUp() {
            super.setUp();
            Mockito.doAnswer(invocation -> {
                final OmegaQuery<Integer, String, Word<String>> q = invocation.getArgument(0);
                if (q.getInput().equals(Word.fromSymbols("a", "a", "a"))) {
                    q.answer(Word.fromSymbols("1", "1", "1"));
                    final List<Integer> states = new ArrayList<>();
                    states.add(0);
                    states.add(0);
                    states.add(0);
                    states.add(0);
                    q.setStates(states);
                } else {
                    q.answer(Word.fromSymbols("not-an-output"));
                }
                return null;
            }).when(mealyOmegaMembershipOracle).processQuery(Matchers.any());
            Mockito.when(mealyOmegaMembershipOracle.isSameState(Matchers.any(),
                                                                Matchers.any(),
                                                                Matchers.any(),
                                                                Matchers.any())).thenReturn(true);
        }

        @Override
        protected AbstractLassoAutomatonEmptinessOracle<MealyLasso<?, String, String>, ?, String, ?>
                createLassoAutomatonEmptinessOracle() {
            return new MealyLassoMealyEmptinessOracle<>(mealyOmegaMembershipOracle);
        }

        @Override
        protected MealyLasso<?, String, String> createLasso() {
            final MealyMachine<?, String, ?, String> mealy = AutomatonBuilders.forMealy(
                    new CompactMealy<String, String>(ALPHABET)).
                    from("q0").on("a").withOutput("1").loop().withInitial("q0").create();
            return new MealyLasso<>(mealy, ALPHABET, 3);
        }

        @Override
        protected DefaultQuery<String, Word<String>> createQuery() {
            return new DefaultQuery<>(Word.epsilon(), Word.fromSymbols("a", "a", "a"), Word.fromSymbols("1", "1", "1"));
        }

        @Override
        protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>,
                                            Character,
                                            Word<String>,
                                            OmegaQuery<Integer, Character, Word<String>>> createBreadthFirstOracle(int maxWords) {
            return new MealyLassoMealyEmptinessOracle<>(mealyOmegaMembershipOracle2);
        }
    }
}
