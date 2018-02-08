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
package de.learnlib.algorithms.lstar;

import java.util.Collections;
import java.util.List;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.mealy.ClassicLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.examples.mealy.ExampleStack;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.equivalence.mealy.SymbolEQOracleWrapper;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.Test;

@Test
public class LStarMealyTest extends LearningTest {

    @Test
    public void testClassicLStarMealy() {
        ExampleStack stackExample = ExampleStack.createExample();

        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> mealy = stackExample.getReferenceAutomaton();
        Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();

        MealyMembershipOracle<ExampleStack.Input, ExampleStack.Output> oracle = new MealySimulatorOracle<>(mealy);

        EquivalenceOracle<? super MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>>
                mealyEqOracle = new SimulatorEQOracle<>(mealy);

        EquivalenceOracle<? super MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, ExampleStack.Output>
                mealySymEqOracle = new SymbolEQOracleWrapper<>(mealyEqOracle);

        for (ObservationTableCEXHandler<? super ExampleStack.Input, ? super ExampleStack.Output> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super ExampleStack.Input, ? super ExampleStack.Output> strategy : LearningTest.CLOSING_STRATEGIES) {
                LearningAlgorithm<MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, ExampleStack.Output>
                        learner = ClassicLStarMealy.createForWordOracle(alphabet, oracle, handler, strategy);

                testLearnModel(mealy, alphabet, learner, MealyUtil.wrapWordOracle(oracle), mealySymEqOracle);
            }
        }
    }

    @Test
    public void testOptimizedLStarMealy() {
        ExampleStack stackExample = ExampleStack.createExample();
        MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output> mealy = stackExample.getReferenceAutomaton();
        Alphabet<ExampleStack.Input> alphabet = stackExample.getAlphabet();

        MembershipOracle<ExampleStack.Input, Word<ExampleStack.Output>> oracle = new SimulatorOracle<>(mealy);

        // Empty list of suffixes => minimal compliant set
        List<Word<ExampleStack.Input>> initSuffixes = Collections.emptyList();

        EquivalenceOracle<? super MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>>
                mealyEqOracle = new SimulatorEQOracle<>(mealy);

        for (ObservationTableCEXHandler<? super ExampleStack.Input, ? super Word<ExampleStack.Output>> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super ExampleStack.Input, ? super Word<ExampleStack.Output>> strategy : LearningTest.CLOSING_STRATEGIES) {
                LearningAlgorithm<MealyMachine<?, ExampleStack.Input, ?, ExampleStack.Output>, ExampleStack.Input, Word<ExampleStack.Output>>
                        learner = new ExtensibleLStarMealy<>(alphabet, oracle, initSuffixes, handler, strategy);

                testLearnModel(mealy, alphabet, learner, oracle, mealyEqOracle);
            }
        }
    }

}
