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
import de.learnlib.algorithms.aaar.TranslatingMooreMachine;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Word;

public class ExplicitAAARLearnerMoore<L extends MooreLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractExplicitAAARLearner<L, MooreMachine<?, AI, ?, O>, MooreMachine<?, CI, ?, O>, AI, CI, Word<O>> {

    public ExplicitAAARLearnerMoore(LearnerProvider<L, MooreMachine<?, CI, ?, O>, CI, Word<O>> learnerProvider,
                                    MembershipOracle<CI, Word<O>> o,
                                    ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                    Function<AI, AI> incrementor) {
        super(learnerProvider, o, explicitInitialAbstraction, incrementor);
    }

    @Override
    public MooreMachine<?, AI, ?, O> getHypothesisModel() {
        final MooreMachine<?, CI, ?, O> concrete = super.getLearnerHypothesisModel();
        final CompactMoore<AI, O> result = new CompactMoore<>(getAbstractAlphabet());

        super.copyAbstract(concrete, result);

        return result;
    }

    @Override
    public MooreMachine<?, CI, ?, O> getTranslatingHypothesisModel() {
        return new TranslatingMooreMachine<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }
}

