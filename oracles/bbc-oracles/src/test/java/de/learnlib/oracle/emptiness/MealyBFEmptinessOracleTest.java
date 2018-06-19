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

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.mealy.ExampleCoffeeMachine;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.oracle.AbstractBreadthFirstOracle;
import de.learnlib.oracle.emptiness.AbstractBreadthFirstEmptinessOracle.MealyBreadthFirstEmptinessOracle;
import net.automatalib.automata.transout.MealyMachine;
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
public class MealyBFEmptinessOracleTest
        extends AbstractBreadthFirstEmptinessOracleTest<MealyMachine<?, Input, ?, String>, Input, Word<String>> {

    @Mock
    private MealyMembershipOracle<Input, String> mealyMembershipOracle;

    @Mock
    private MealyMembershipOracle<Character, String> mealyMembershipOracle2;

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.doAnswer(invocation -> {
            final DefaultQuery<Input, Word<String>> q = invocation.getArgument(0);
            if (q.getInput().equals(Word.fromSymbols(Input.POD))) {
                q.answer(Word.fromSymbols(ExampleCoffeeMachine.OUT_OK));
            } else {
                q.answer(Word.fromSymbols("not-an-output"));
            }
            return null;
        }).when(mealyMembershipOracle).processQuery(ArgumentMatchers.any());
    }

    @Override
    protected AbstractBreadthFirstEmptinessOracle<MealyMachine<?, Input, ?, String>, Input, Word<String>> createBreadthFirstEmptinessOracle() {
        return new MealyBreadthFirstEmptinessOracle<>(5, mealyMembershipOracle);
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
        return new DefaultQuery<>(Word.epsilon(),
                                  Word.fromSymbols(Input.POD),
                                  Word.fromSymbols(ExampleCoffeeMachine.OUT_OK));
    }

    @Override
    protected AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, Word<String>, DefaultQuery<Character, Word<String>>> createBreadthFirstOracle(
            int maxWords) {
        return new MealyBreadthFirstEmptinessOracle<>(maxWords, mealyMembershipOracle2);
    }
}
