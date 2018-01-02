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
package de.learnlib.algorithms.ttt;

import de.learnlib.algorithms.ttt.base.TTTLearnerState;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFABuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerDFATest;
import net.automatalib.words.Alphabet;

/**
 * @author bainczyk
 */
public class TTTLearnerDFAResumableLearnerTest
        extends AbstractResumableLearnerDFATest<TTTLearnerDFA<Character>, TTTLearnerState<Character, Boolean>> {

    @Override
    protected TTTLearnerDFA<Character> getLearner(final MembershipOracle<Character, Boolean> oracle,
                                                  final Alphabet<Character> alphabet) {

        return new TTTLearnerDFABuilder<Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected int getRounds() {
        return 6;
    }

}
