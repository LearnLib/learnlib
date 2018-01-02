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

import java.util.Collection;
import java.util.stream.Collectors;

import de.learnlib.algorithms.rpni.BlueFringeMDLDFA;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.testsupport.it.learner.AbstractDFAPassiveLearnerIT;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantList;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public class MdlDfaIT extends AbstractDFAPassiveLearnerIT {

    @Override
    protected <I> Collection<DefaultQuery<I, Boolean>> generateSamplesInternal(Alphabet<I> alphabet,
                                                                               DFA<?, I> reference) {
        final Collection<DefaultQuery<I, Boolean>> samples = super.generateSamples(alphabet, reference);
        // filter out negative examples
        return samples.stream().filter(DefaultQuery::getOutput).collect(Collectors.toList());
    }

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          PassiveLearnerVariantList<DFA<?, I>, I, Boolean> variants) {

        final boolean[] determinism = {true, false};
        final boolean[] parallelism = {true, false};

        for (final boolean d : determinism) {
            for (final boolean p : parallelism) {
                final BlueFringeMDLDFA<I> learner = new BlueFringeMDLDFA<>(alphabet);
                learner.setParallel(p);
                learner.setDeterministic(d);
                variants.addLearnerVariant(String.format("BlueFringeMDLDFA, det=%b, par=%b", d, p), learner);
            }
        }
    }
}