/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.util.nfa;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.words.Alphabet;

@ParametersAreNonnullByDefault
public class NFALearnerWrapper<I> implements LearningAlgorithm.DFALearner<I> {

    @Nonnull
    private final Alphabet<I> alphabet;
    @Nonnull
    private final LearningAlgorithm<? extends NFA<?, I>, I, Boolean> nfaLearner;

    public NFALearnerWrapper(Alphabet<I> alphabet, LearningAlgorithm<? extends NFA<?, I>, I, Boolean> nfaLearner) {
        this.alphabet = alphabet;
        this.nfaLearner = nfaLearner;
    }

    @Override
    public void startLearning() {
        nfaLearner.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        return nfaLearner.refineHypothesis(ceQuery);
    }

    @Override
    public CompactDFA<I> getHypothesisModel() {
        NFA<?, I> nfaHyp = nfaLearner.getHypothesisModel();
        return NFAs.determinize(nfaHyp, alphabet);
    }

    @Override
    public String toString() {
        return nfaLearner.toString();
    }

}
