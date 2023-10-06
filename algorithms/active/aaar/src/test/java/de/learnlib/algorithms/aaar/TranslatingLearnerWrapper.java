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

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.SupportsGrowingAlphabet;

public class TranslatingLearnerWrapper<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, CM, CI, D>
        implements LearningAlgorithm<CM, CI, D> {

    private final AbstractAAARLearner<L, ?, CM, ?, CI, D> delegate;

    public TranslatingLearnerWrapper(AbstractAAARLearner<L, ?, CM, ?, CI, D> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void startLearning() {
        this.delegate.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<CI, D> ceQuery) {
        return this.delegate.refineHypothesis(ceQuery);
    }

    @Override
    public CM getHypothesisModel() {
        return this.delegate.getTranslatingHypothesisModel();
    }
}
