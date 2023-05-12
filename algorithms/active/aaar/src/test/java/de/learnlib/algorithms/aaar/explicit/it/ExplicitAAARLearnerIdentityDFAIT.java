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
package de.learnlib.algorithms.aaar.explicit.it;

import de.learnlib.algorithms.aaar.AAARTestUtil;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.TranslatingLearnerWrapper;
import de.learnlib.algorithms.aaar.explicit.ExplicitAAARLearnerDFA;
import de.learnlib.algorithms.aaar.explicit.IdentityInitialAbstraction;
import de.learnlib.algorithms.aaar.explicit.NoopIncrementor;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public class ExplicitAAARLearnerIdentityDFAIT extends AbstractDFALearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqOracle,
                                          DFALearnerVariantList<I> variants) {

        for (Pair<String, LearnerProvider<? extends DFALearner<I>, DFA<?, I>, I, Boolean>> l : AAARTestUtil.<I>getDFALearners()) {
            final String name = l.getFirst();
            final LearnerProvider<? extends DFALearner<I>, DFA<?, I>, I, Boolean> learner = l.getSecond();

            variants.addLearnerVariant(name, new LearnerWrapper<>(learner, mqOracle, alphabet));
        }
    }

    private static class LearnerWrapper<L extends DFALearner<I> & SupportsGrowingAlphabet<I>, I>
            extends TranslatingLearnerWrapper<L, DFA<?, I>, I, Boolean> implements DFALearner<I> {

        LearnerWrapper(LearnerProvider<L, DFA<?, I>, I, Boolean> learnerProvider,
                       MembershipOracle<I, Boolean> mqo,
                       Alphabet<I> alphabet) {
            super(new ExplicitAAARLearnerDFA<>(learnerProvider,
                                               mqo,
                                               new IdentityInitialAbstraction<>(alphabet),
                                               new NoopIncrementor<>()));
        }
    }
}
