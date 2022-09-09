/* Copyright (C) 2013-2022 TU Dortmund
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

import de.learnlib.algorithms.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.algorithms.rivestschapire.RivestSchapireMooreBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public class RivestSchapireMooreResumableLearnerTest extends ExtensibleLStarMooreResumableLearnerTest {

    @Override
    protected ExtensibleLStarMoore<Character, Character> getLearner(final MembershipOracle<Character, Word<Character>> oracle,
                                                                    final Alphabet<Character> alphabet) {

        return new RivestSchapireMooreBuilder<Character, Character>().withAlphabet(alphabet)
                                                                     .withOracle(oracle)
                                                                     .create();
    }

    @Override
    protected int getRounds() {
        return 3;
    }

}
