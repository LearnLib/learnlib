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
package de.learnlib.testsupport.it.learner;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.testsupport.example.LearningExample.OneSEVPALearningExample;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.util.automaton.vpa.OneSEVPAs;
import net.automatalib.word.Word;

public class OneSEVPALearnerITCase<I> extends AbstractLearnerVariantITCase<I, Boolean, OneSEVPA<?, I>> {

    private final OneSEVPALearningExample<I> example;

    OneSEVPALearnerITCase(LearnerVariant<OneSEVPA<?, I>, I, Boolean> variant,
                          OneSEVPALearningExample<I> example,
                          EquivalenceOracle<? super OneSEVPA<?, I>, I, Boolean> eqOracle) {
        super(variant, example, eqOracle);
        this.example = example;
    }

    @Override
    protected Word<I> checkEquivalence(OneSEVPA<?, I> hypothesis) {
        return OneSEVPAs.findSeparatingWord(this.example.getReferenceAutomaton(),
                                            hypothesis,
                                            this.example.getAlphabet());
    }
}
