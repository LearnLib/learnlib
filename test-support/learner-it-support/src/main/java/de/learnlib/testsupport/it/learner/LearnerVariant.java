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
package de.learnlib.testsupport.it.learner;

import de.learnlib.algorithm.LearningAlgorithm;

public class LearnerVariant<M, I, D> {

    private final String name;
    private final LearningAlgorithm<? extends M, I, D> learner;
    private final int maxRounds;

    LearnerVariant(String name, LearningAlgorithm<? extends M, I, D> learner, int maxRounds) {
        this.name = name;
        this.learner = learner;
        this.maxRounds = maxRounds;
    }

    public String getLearnerName() {
        String learnerName = learner.toString();
        int atPos = learnerName.lastIndexOf('@');
        if (atPos != -1) {
            int simpleNameStart = learnerName.lastIndexOf('.', atPos - 1) + 1;
            learnerName = learnerName.substring(simpleNameStart, atPos);
        }

        return learnerName;
    }

    public String getName() {
        return name;
    }

    public LearningAlgorithm<? extends M, I, D> getLearner() {
        return learner;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

}
