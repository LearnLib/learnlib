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

import de.learnlib.example.LearningExample.UniversalDeterministicLearningExample;
import de.learnlib.oracle.EquivalenceOracle;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

public class UniversalDeterministicLearnerITCase<I, D, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>>
        extends AbstractLearnerVariantITCase<I, D, M> {

    private final UniversalDeterministicLearningExample<I, ? extends M> example;

    UniversalDeterministicLearnerITCase(LearnerVariant<M, I, D> variant,
                                        UniversalDeterministicLearningExample<I, ? extends M> example,
                                        EquivalenceOracle<? super M, I, D> eqOracle) {
        super(variant, example, eqOracle);
        this.example = example;
    }

    @Override
    protected Word<I> checkEquivalence(M hypothesis) {
        return Automata.findSeparatingWord(this.example.getReferenceAutomaton(),
                                           hypothesis,
                                           this.example.getAlphabet());
    }
}
