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
package de.learnlib.algorithm.lstar;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mealy.ClassicLStarMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.equivalence.mealy.SymbolEQOracleWrapper;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.testsupport.example.mealy.ExampleStack;
import de.learnlib.testsupport.example.mealy.ExampleStack.Input;
import de.learnlib.testsupport.example.mealy.ExampleStack.Output;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

@Test
public class LStarMealyTest extends LearningTest {

    @Test
    public void testClassicLStarMealy() {
        ExampleStack stackExample = ExampleStack.createExample();

        MealyMachine<?, Input, ?, Output> mealy = stackExample.getReferenceAutomaton();
        Alphabet<Input> alphabet = stackExample.getAlphabet();

        MealyMembershipOracle<Input, Output> oracle = new MealySimulatorOracle<>(mealy);
        MembershipOracle<Input, Output> symOracle = MealyUtil.wrapWordOracle(oracle);

        EquivalenceOracle<? super MealyMachine<?, Input, ?, Output>, Input, Word<Output>> mealyEqOracle =
                new SimulatorEQOracle<>(mealy);

        EquivalenceOracle<? super MealyMachine<?, Input, ?, Output>, Input, Output> mealySymEqOracle =
                new SymbolEQOracleWrapper<>(mealyEqOracle);

        for (ObservationTableCEXHandler<? super Input, ? super Output> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super Input, ? super Output> strategy : LearningTest.CLOSING_STRATEGIES) {
                LearningAlgorithm<MealyMachine<?, Input, ?, Output>, Input, Output> learner =
                        new ClassicLStarMealy<>(alphabet, symOracle, handler, strategy);

                testLearnModel(mealy, alphabet, learner, mealySymEqOracle);
            }
        }
    }

    @Test
    public void testOptimizedLStarMealy() {
        ExampleStack stackExample = ExampleStack.createExample();
        MealyMachine<?, Input, ?, Output> mealy = stackExample.getReferenceAutomaton();
        Alphabet<Input> alphabet = stackExample.getAlphabet();

        MealyMembershipOracle<Input, Output> oracle = new MealySimulatorOracle<>(mealy);

        // Empty list of suffixes => minimal compliant set
        List<Word<Input>> initSuffixes = Collections.emptyList();

        EquivalenceOracle<? super MealyMachine<?, Input, ?, Output>, Input, Word<Output>> mealyEqOracle =
                new SimulatorEQOracle<>(mealy);

        for (ObservationTableCEXHandler<? super Input, ? super Word<Output>> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super Input, ? super Word<Output>> strategy : LearningTest.CLOSING_STRATEGIES) {
                LearningAlgorithm<MealyMachine<?, Input, ?, Output>, Input, Word<Output>> learner =
                        new ExtensibleLStarMealy<>(alphabet, oracle, initSuffixes, handler, strategy);

                testLearnModel(mealy, alphabet, learner, mealyEqOracle);
            }
        }
    }

}
