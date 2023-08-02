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
package de.learnlib.algorithms.spmm.adapter;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Adapter for using {@link TTTLearnerMealy} as a sub-procedural learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class TTTMealyAdapter<I, O> extends TTTLearnerMealy<I, O> implements AccessSequenceTransformer<I> {

    public TTTMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        super(alphabet, oracle, AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final TTTState<I, Word<O>> s = super.getHypothesisDS().getState(word);
        // we should only query defined paths
        assert s != null;
        return s.getAccessSequence();
    }

}
