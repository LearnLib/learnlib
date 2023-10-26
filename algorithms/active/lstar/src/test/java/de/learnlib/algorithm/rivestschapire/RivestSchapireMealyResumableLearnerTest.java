/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithm.rivestschapire;

import de.learnlib.algorithm.lstar.ExtensibleLStarMealyResumableLearnerTest;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import net.automatalib.alphabet.Alphabet;

public class RivestSchapireMealyResumableLearnerTest extends ExtensibleLStarMealyResumableLearnerTest {

    @Override
    protected ExtensibleLStarMealy<Character, Character> getLearner(MealyMembershipOracle<Character, Character> oracle,
                                                                    Alphabet<Character> alphabet) {

        return new RivestSchapireMealyBuilder<Character, Character>().withAlphabet(alphabet)
                                                                     .withOracle(oracle)
                                                                     .create();
    }

    @Override
    protected int getRounds() {
        return 3;
    }

}
