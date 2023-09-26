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
package de.learnlib.testsupport.it.learner;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.examples.LearningExample.SPMMLearningExample;
import net.automatalib.automata.procedural.SPMM;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;

public class SPMMLearnerITCase<I, O> extends AbstractLearnerVariantITCase<I, Word<O>, SPMM<?, I, ?, O>> {

    private final SPMMLearningExample<I, O> example;

    SPMMLearnerITCase(LearnerVariant<SPMM<?, I, ?, O>, I, Word<O>> variant,
                      SPMMLearningExample<I, O> example,
                      EquivalenceOracle<? super SPMM<?, I, ?, O>, I, Word<O>> eqOracle) {
        super(variant, example, eqOracle);
        this.example = example;
    }

    @Override
    protected Word<I> checkEquivalence(SPMM<?, I, ?, O> hypothesis) {
        return Automata.findSeparatingWord(this.example.getReferenceAutomaton(),
                                           hypothesis,
                                           this.example.getAlphabet());
    }
}
