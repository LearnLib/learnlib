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
package de.learnlib.algorithms.aaar;

import java.util.function.Function;

import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

public class AAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

    public AAARLearnerDFA(LearnerProvider<L, DFA<?, CI>, CI, Boolean> learnerProvider,
                          MembershipOracle<CI, Boolean> o,
                          CI initialConcrete,
                          Function<CI, AI> abstractor) {
        super(learnerProvider, o, initialConcrete, abstractor);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        final DFA<?, CI> concrete = super.getConcreteHypothesisModel();
        final CompactDFA<AI> result = new CompactDFA<>(super.abs);

        super.copyAbstract(concrete, result);

        return result;
    }
}

