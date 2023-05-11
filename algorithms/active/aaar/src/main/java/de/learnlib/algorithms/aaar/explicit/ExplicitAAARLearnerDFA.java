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
package de.learnlib.algorithms.aaar.explicit;

import java.util.function.Function;

import de.learnlib.algorithms.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.TranslatingDFA;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

public class ExplicitAAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractExplicitAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

    public ExplicitAAARLearnerDFA(LearnerProvider<L, DFA<?, CI>, CI, Boolean> learnerProvider,
                                  MembershipOracle<CI, Boolean> o,
                                  ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                  Function<AI, AI> incrementor) {
        super(learnerProvider, o, explicitInitialAbstraction, incrementor);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        final DFA<?, CI> hyp = super.getLearnerHypothesisModel();
        final CompactDFA<AI> result = new CompactDFA<>(getAbstractAlphabet());

        super.copyAbstract(hyp, result);

        return result;
    }

    @Override
    public DFA<?, CI> getTranslatingHypothesisModel() {
        return new TranslatingDFA<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }

}

