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
package de.learnlib.algorithm.observationpack;

import de.learnlib.algorithm.observationpack.dfa.OPLearnerDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import net.automatalib.words.Alphabet;
import org.testng.annotations.Test;

@Test
public class OPLearnerDFAIT extends AbstractDFALearnerIT {

    private static final boolean[] BOOLEAN_VALUES = {false, true};

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqOracle,
                                          DFALearnerVariantList<I> variants) {
        OPLearnerDFABuilder<I> builder = new OPLearnerDFABuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (boolean epsilonRoot : BOOLEAN_VALUES) {
            builder.setEpsilonRoot(epsilonRoot);
            for (LocalSuffixFinder<? super I, ? super Boolean> suffixFinder : LocalSuffixFinders.values()) {
                builder.setSuffixFinder(suffixFinder);

                String name = "epsilonRoot=" + epsilonRoot + ",suffixFinder=" + suffixFinder.toString();
                variants.addLearnerVariant(name, builder.create());
            }
        }
    }
}
