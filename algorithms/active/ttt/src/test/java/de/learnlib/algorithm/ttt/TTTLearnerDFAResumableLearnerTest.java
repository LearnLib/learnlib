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
package de.learnlib.algorithm.ttt;

import de.learnlib.algorithm.ttt.base.TTTLearnerState;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFABuilder;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerDFATest;
import net.automatalib.alphabet.Alphabet;

public class TTTLearnerDFAResumableLearnerTest
        extends AbstractResumableLearnerDFATest<TTTLearnerDFA<Character>, TTTLearnerState<Character, Boolean>> {

    @Override
    protected TTTLearnerDFA<Character> getLearner(DFAMembershipOracle<Character> oracle,
                                                  Alphabet<Character> alphabet) {

        return new TTTLearnerDFABuilder<Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected int getRounds() {
        return 6;
    }

}
