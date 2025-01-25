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
package de.learnlib.algorithm.lambda.lstar.dfa;

import de.learnlib.algorithm.lambda.lstar.LLambdaDFA;
import de.learnlib.algorithm.lambda.lstar.LLambdaState;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerDFATest;
import net.automatalib.alphabet.Alphabet;

public class LLambdaDFAResumableTest
        extends AbstractResumableLearnerDFATest<LLambdaDFA<Character>, LLambdaState<Character, Boolean>> {

    @Override
    protected LLambdaDFA<Character> getLearner(DFAMembershipOracle<Character> oracle, Alphabet<Character> alphabet) {
        return new LLambdaDFA<>(alphabet, oracle);
    }

    @Override
    protected int getRounds() {
        return 2;
    }
}
