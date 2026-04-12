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
package de.learnlib.algorithm.sparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.AbstractLearnerASTMealyTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

public class SparseASTTest extends AbstractLearnerASTMealyTest<SparseLearner<Character, Character>> {

    @Override
    protected SparseLearner<Character, Character> getLearner(MealyMembershipOracle<Character, Character> oracle,
                                                             Alphabet<Character> alphabet) {
        return new SparseLearner<>(alphabet, oracle);
    }

    @Override
    protected Collection<Word<Character>> getTrueRepresentatives() {
        final List<CoreRow<Integer, Character, Character>> cRows = learner.getCRows();

        final List<Word<Character>> result = new ArrayList<>(cRows.size());
        for (CoreRow<Integer, Character, Character> r : cRows) {
            result.add(r.prefix);
        }

        return result;
    }
}
