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
package de.learnlib.algorithm.aaar.explicit.it;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.ComboConstructor;
import de.learnlib.algorithm.aaar.TranslatingLearnerWrapper;
import de.learnlib.algorithm.aaar.explicit.ExplicitAAARLearnerDFA;
import de.learnlib.algorithm.aaar.explicit.IdentityInitialAbstraction;
import de.learnlib.algorithm.aaar.explicit.NoopIncrementor;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.Pair;

public class ExplicitAAARLearnerIdentityDFAIT extends AbstractDFALearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqo,
                                          DFALearnerVariantList<I> variants) {

        for (Pair<String, ComboConstructor<? extends DFALearner<I>, I, Boolean>> l : AAARTestUtil.<I>getDFALearners()) {
            final String name = l.getFirst();
            final ComboConstructor<? extends DFALearner<I>, I, Boolean> learner = l.getSecond();

            variants.addLearnerVariant(name,
                                       new TranslatingLearnerWrapper<>(new ExplicitAAARLearnerDFA<>(learner,
                                                                                                    mqo,
                                                                                                    new IdentityInitialAbstraction<>(
                                                                                                            alphabet),
                                                                                                    new NoopIncrementor<>())));
        }
    }
}
