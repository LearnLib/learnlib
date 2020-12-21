/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.algorithms.ostia;

import de.learnlib.testsupport.it.learner.AbstractSSTPassiveLearnerIT;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantList;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OSTIAIT extends AbstractSSTPassiveLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             PassiveLearnerVariantList<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> variants) {
        variants.addLearnerVariant("OSTIA", new OSTIA<>(alphabet));
    }
}
