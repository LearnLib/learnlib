/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.util.moore;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;

final class MooreLearnerWrapper<M extends MooreMachine<?, I, ?, O>, I, O> implements MooreLearner<I, O> {

    private final LearningAlgorithm<M, I, O> learner;

    MooreLearnerWrapper(LearningAlgorithm<M, I, O> learner) {
        this.learner = learner;
    }

    @Override
    public void startLearning() {
        learner.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
        M hyp = learner.getHypothesisModel();
        DefaultQuery<I, O> reducedQry = MooreUtil.reduceCounterExample(hyp, ceQuery);

        return reducedQry != null && learner.refineHypothesis(reducedQry);
    }

    @Override
    public M getHypothesisModel() {
        return learner.getHypothesisModel();
    }

}
