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
import de.learnlib.algorithms.aaar.explicit.ExplicitAAARLearnerMoore;
import de.learnlib.algorithms.aaar.explicit.IdentityInitialAbstraction;
import de.learnlib.algorithms.aaar.explicit.NoopIncrementor;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMooreLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreLearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public class ExplicitAAARLearnerIdentityMooreIT extends AbstractMooreLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MooreMembershipOracle<I, O> mqOracle,
                                             MooreLearnerVariantList<I, O> variants) {

        for (Pair<String, LearnerProvider<? extends MooreLearner<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>>> l : AAARTestUtil.<I, O>getMooreLearners()) {
            final String name = l.getFirst();
            final LearnerProvider<? extends MooreLearner<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>> learner =
                    l.getSecond();

            variants.addLearnerVariant(name, new LearnerWrapper<>(learner, mqOracle, alphabet));
        }
    }

    private static class LearnerWrapper<L extends MooreLearner<I, O> & SupportsGrowingAlphabet<I>, I, O>
            extends TranslatingLearnerWrapper<L, MooreMachine<?, I, ?, O>, I, Word<O>> implements MooreLearner<I, O> {

        LearnerWrapper(LearnerProvider<L, MooreMachine<?, I, ?, O>, I, Word<O>> learnerProvider,
                       MembershipOracle<I, Word<O>> mqo,
                       Alphabet<I> alphabet) {
            super(new ExplicitAAARLearnerMoore<>(learnerProvider,
                                                 mqo,
                                                 new IdentityInitialAbstraction<>(alphabet),
                                                 new NoopIncrementor<>()));
        }
    }
}
