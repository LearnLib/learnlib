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
package de.learnlib.algorithm.ttt.dfa.it;

import de.learnlib.acex.analyzer.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.analyzer.AcexAnalyzers;
import de.learnlib.algorithm.ttt.vpa.TTTLearnerVPABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractOneSEVPALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList;
import net.automatalib.alphabet.VPAlphabet;
import org.testng.annotations.Test;

@Test
public class TTTLearnerVPAIT extends AbstractOneSEVPALearnerIT {

    @Override
    protected <I> void addLearnerVariants(VPAlphabet<I> alphabet,
                                          DFAMembershipOracle<I> mqOracle,
                                          LearnerVariantList.OneSEVPALearnerVariantList<I> variants) {

        final TTTLearnerVPABuilder<I> builder = new TTTLearnerVPABuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (AbstractNamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
            builder.setAnalyzer(analyzer);
            variants.addLearnerVariant("analyzer=" + analyzer, builder.create());
        }

    }
}
