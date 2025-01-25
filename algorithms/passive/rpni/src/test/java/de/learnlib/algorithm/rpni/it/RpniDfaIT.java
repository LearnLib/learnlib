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
package de.learnlib.algorithm.rpni.it;

import de.learnlib.algorithm.rpni.BlueFringeRPNIDFA;
import de.learnlib.datastructure.pta.config.DefaultProcessingOrders;
import de.learnlib.datastructure.pta.config.ProcessingOrder;
import de.learnlib.testsupport.it.learner.AbstractDFAPassiveLearnerIT;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;

public class RpniDfaIT extends AbstractDFAPassiveLearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          PassiveLearnerVariantList<DFA<?, I>, I, Boolean> variants) {

        final boolean[] determinism = {true, false};
        final boolean[] parallelism = {true, false};
        final ProcessingOrder[] orders = DefaultProcessingOrders.values();

        for (boolean d : determinism) {
            for (boolean p : parallelism) {
                for (ProcessingOrder o : orders) {
                    final BlueFringeRPNIDFA<I> learner = new BlueFringeRPNIDFA<>(alphabet);
                    learner.setParallel(p);
                    learner.setDeterministic(d);
                    learner.setProcessingOrder(o);
                    variants.addLearnerVariant(String.format("BlueFringeRPNIDFA, det=%b, par=%b, ord=%s", d, p, o),
                                               learner);
                }
            }
        }
    }
}
