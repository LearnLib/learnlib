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
package de.learnlib.testsupport.it.learner;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.examples.LearningExample.SPALearningExample;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;

public class SPALearnerITCase<I> extends AbstractLearnerVariantITCase<I, Boolean, SPA<?, I>> {

    private final SPALearningExample<I> example;

    SPALearnerITCase(LearnerVariant<SPA<?, I>, I, Boolean> variant,
                     SPALearningExample<I> example,
                     EquivalenceOracle<? super SPA<?, I>, I, Boolean> eqOracle) {
        super(variant, example, eqOracle);
        this.example = example;
    }

    @Override
    protected Word<I> checkEquivalence(SPA<?, I> hypothesis) {
        return Automata.findSeparatingWord(this.example.getReferenceAutomaton(),
                                           hypothesis,
                                           this.example.getAlphabet());
    }
}
