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

import de.learnlib.api.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.emptiness.AbstractLassoAutomatonEmptinessOracle.MealyLassoMealyEmptinessOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.MealyLassoImpl;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

/**
 * @author Jeroen Meijer
 */
public class MealyLassoMealyEmptinessOracleTest
        extends AbstractLassoAutomatonEmptinessTest<MealyLasso<?, String, ?, String>, Word<String>> {

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
        }).when(mealyOmegaMembershipOracle).processQuery(ArgumentMatchers.any());
        Mockito.when(mealyOmegaMembershipOracle.isSameState(ArgumentMatchers.any(),
                                                            ArgumentMatchers.any(),
                                                            ArgumentMatchers.any(),
                                                            ArgumentMatchers.any())).thenReturn(true);
    }

    @Override
    protected AbstractLassoAutomatonEmptinessOracle<MealyLasso<?, String, ?, String>, ?, String, ?> createLassoAutomatonEmptinessOracle() {
        return new MealyLassoMealyEmptinessOracle<>(mealyOmegaMembershipOracle);
    }

    @Override
    protected MealyLasso<?, String, ?, String> createLasso() {
        final MealyMachine<?, String, ?, String> mealy =
                AutomatonBuilders.forMealy(new CompactMealy<String, String>(ALPHABET)).
                        from("q0").on("a").withOutput("1").loop().withInitial("q0").create();
        return new MealyLassoImpl<>(mealy, ALPHABET, 3);
    }

    @Override
    protected DefaultQuery<String, Word<String>> createQuery() {
        return new DefaultQuery<>(Word.epsilon(), Word.fromSymbols("a", "a", "a"), Word.fromSymbols("1", "1", "1"));
    }

    @Override
    protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, Word<String>, OmegaQuery<Integer, Character, Word<String>>> createBreadthFirstOracle(
            int maxWords) {
        return new MealyLassoMealyEmptinessOracle<>(mealyOmegaMembershipOracle2);
    }
}
