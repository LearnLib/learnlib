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
package de.learnlib.testsupport.it.learner;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;

public class PassiveLearnerVariant<M, I, D> {

    private final String name;
    private final PassiveLearningAlgorithm<? extends M, I, D> learner;

    PassiveLearnerVariant(String name, PassiveLearningAlgorithm<? extends M, I, D> learner) {
        this.name = name;
        this.learner = learner;
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

    public PassiveLearningAlgorithm<? extends M, I, D> getLearner() {
        return learner;
    }

}
