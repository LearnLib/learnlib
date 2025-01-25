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
package de.learnlib.algorithm.malerpnueli;

import de.learnlib.algorithm.lstar.ExtensibleLStarMooreResumableLearnerTest;
import de.learnlib.algorithm.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import net.automatalib.alphabet.Alphabet;

public class MalerPnueliMooreResumableLearnerTest extends ExtensibleLStarMooreResumableLearnerTest {

    @Override
    protected ExtensibleLStarMoore<Character, Character> getLearner(MooreMembershipOracle<Character, Character> oracle,
                                                                    Alphabet<Character> alphabet) {

        return new MalerPnueliMooreBuilder<Character, Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected int getRounds() {
        return 1;
    }
}
