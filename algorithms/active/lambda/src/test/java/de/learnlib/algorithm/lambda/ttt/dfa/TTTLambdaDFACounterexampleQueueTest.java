/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.lambda.ttt.dfa;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.lambda.AbstractCounterexampleQueueTest;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import net.automatalib.alphabet.Alphabet;

public class TTTLambdaDFACounterexampleQueueTest extends AbstractCounterexampleQueueTest {

    @Override
    protected <I> DFALearner<I> getLearner(Alphabet<I> alphabet, DFAMembershipOracle<I> oracle) {
        return new TTTLambdaDFA<>(alphabet, oracle);
    }
}
