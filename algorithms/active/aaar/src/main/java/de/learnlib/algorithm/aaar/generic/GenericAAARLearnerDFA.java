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
package de.learnlib.algorithm.aaar.generic;

import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.aaar.TranslatingDFA;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;

/**
 * A {@link DFA}-specific refinement of {@link AbstractGenericAAARLearner}.
 *
 * @param <L>
 *         learner type
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 */
public class GenericAAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractGenericAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

    /**
     * Constructor.
     *
     * @param learnerConstructor
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param initialConcrete
     *         the initial (concrete) input symbol used for starting the learning process
     * @param abstractor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided (concrete) counterexamples
     */
    public GenericAAARLearnerDFA(LearnerConstructor<L, CI, Boolean> learnerConstructor,
                                 MembershipOracle<CI, Boolean> oracle,
                                 CI initialConcrete,
                                 Function<CI, AI> abstractor) {
        super(learnerConstructor, oracle, initialConcrete, abstractor);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        final DFA<?, CI> concrete = super.getLearnerHypothesisModel();
        final CompactDFA<AI> result = new CompactDFA<>(getAbstractAlphabet());

        super.copyAbstract(concrete, result);

        return result;
    }

    @Override
    public DFA<?, CI> getTranslatingHypothesisModel() {
        return new TranslatingDFA<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }

}

