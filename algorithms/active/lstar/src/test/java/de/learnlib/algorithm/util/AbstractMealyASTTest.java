/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.testsupport.AbstractLearnerASTMealyTest;
import net.automatalib.word.Word;

public abstract class AbstractMealyASTTest extends AbstractLearnerASTMealyTest<ExtensibleLStarMealy<Character, Character>> {

    @Override
    protected Collection<Word<Character>> getTrueRepresentatives() {
        final ObservationTable<Character, Word<Character>> ot = learner.getObservationTable();
        final Collection<Word<Character>> shortPrefixes = ot.getShortPrefixes();
        final List<Word<Character>> result = new ArrayList<>(shortPrefixes.size());

        for (Word<Character> sp : shortPrefixes) {
            if (ot.isAccessSequence(sp)) {
                result.add(sp);
            }
        }

        return result;
    }
}
