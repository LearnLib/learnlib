/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree;

import de.learnlib.algorithms.discriminationtree.moore.DTLearnerMooreBuilder;
import de.learnlib.api.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractMooreLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreLearnerVariantList;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.Test;

@Test
public class DTLearnerMooreIT extends AbstractMooreLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MooreMembershipOracle<I, O> mqOracle,
                                             MooreLearnerVariantList<I, O> variants) {
        DTLearnerMooreBuilder<I, O> builder = new DTLearnerMooreBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder : LocalSuffixFinders.values()) {
            builder.setSuffixFinder(suffixFinder);

            String name = "suffixFinder=" + suffixFinder.toString();
            variants.addLearnerVariant(name, builder.create());
        }
    }

}