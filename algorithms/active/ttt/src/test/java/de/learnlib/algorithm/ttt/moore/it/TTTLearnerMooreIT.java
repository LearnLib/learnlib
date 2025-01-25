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
package de.learnlib.algorithm.ttt.moore.it;

import de.learnlib.acex.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.ttt.moore.TTTLearnerMooreBuilder;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMooreLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import org.testng.annotations.Test;

@Test
public class TTTLearnerMooreIT extends AbstractMooreLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MooreMembershipOracle<I, O> mqOracle,
                                             MooreLearnerVariantList<I, O> variants) {

        TTTLearnerMooreBuilder<I, O> builder = new TTTLearnerMooreBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (AbstractNamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
            builder.setAnalyzer(analyzer);
            variants.addLearnerVariant("analyzer=" + analyzer, builder.create());
        }
    }

}
