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
package de.learnlib.algorithm.aaar.explicit;

import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.AbstractAAARTest;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine;
import de.learnlib.testsupport.example.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class ExplicitAAARLearnerMealyTest
        extends AbstractAAARTest<ExplicitAAARLearnerMealy<?, Input, Input, String>, Input, Word<String>, MealyMachine<?, Input, ?, String>> {

    public ExplicitAAARLearnerMealyTest() {
        super(ExampleCoffeeMachine.createExample());
    }

    @Override
    protected ExplicitAAARLearnerMealy<?, Input, Input, String> getLearner(Alphabet<Input> alphabet,
                                                                           MembershipOracle<Input, Word<String>> oracle) {
        return new ExplicitAAARLearnerMealy<>(AAARTestUtil.<Input, String>getMealyLearners().get(0).getSecond(),
                                              oracle,
                                              new IdentityInitialAbstraction<>(alphabet),
                                              new NoopIncrementor<>());
    }
}
