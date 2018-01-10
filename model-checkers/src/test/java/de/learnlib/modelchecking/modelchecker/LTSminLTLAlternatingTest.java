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
package de.learnlib.modelchecking.modelchecker;

import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;

/**
 * Tests whether LTSminLTLAlternating actually uses alternating edge semantics.
 *
 * @see LTSminLTLIOTest
 *
 * @author Jeroen Meijer
 */
public class LTSminLTLAlternatingTest extends AbstractLTSminLTLMealyTest<LTSminLTLAlternating<String, String>> {

    @Override
    protected LTSminLTLAlternating<String, String> createModelChecker() {
        return new LTSminLTLAlternatingBuilder<String, String>().withString2Input(s -> s).
                withString2Output(s -> s).create();
    }

    @Override
    protected MealyLasso<?, String, String> createLasso() {
        return new MealyLasso<>(createAutomaton(), getAlphabet(), 4);
    }

    @Override
    protected MealyMachine<?, String, ?, String> createAutomaton() {
        return AutomatonBuilders.forMealy(new CompactMealy<String, String>(getAlphabet())).
                withInitial("q0").
                from("q0").on("a").withOutput("1").loop().create();
    }

    @Override
    protected String createFalseProperty() {
        return "X letter == \"a\"";
    }
}