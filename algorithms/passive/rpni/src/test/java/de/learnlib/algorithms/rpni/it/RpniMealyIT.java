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
package de.learnlib.algorithms.rpni.it;

import de.learnlib.algorithms.rpni.BlueFringeRPNIMealy;
import de.learnlib.testsupport.it.learner.AbstractMealyPassiveLearnerIT;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantList;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class RpniMealyIT extends AbstractMealyPassiveLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             PassiveLearnerVariantList<MealyMachine<?, I, ?, O>, I, Word<O>> variants) {

        final boolean[] determinism = {true, false};
        final boolean[] parallelism = {true, false};

        for (final boolean d : determinism) {
            for (final boolean p : parallelism) {
                final BlueFringeRPNIMealy<I, O> learner = new BlueFringeRPNIMealy<>(alphabet);
                learner.setParallel(p);
                learner.setDeterministic(d);
                variants.addLearnerVariant(String.format("BlueFringeRPNIDFA, det=%b, par=%b", d, p), learner);
            }
        }
    }
}
