/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.procedural.adapter.dfa;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.ttt.base.TTTState;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

/**
 * Adapter for using {@link TTTLearnerDFA} as a procedural learner.
 *
 * @param <I>
 *         input symbol type
 */
public class TTTAdapterDFA<I> extends TTTLearnerDFA<I> implements AccessSequenceTransformer<I> {

    public TTTAdapterDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        super(alphabet, oracle, AcexAnalyzers.BINARY_SEARCH_BWD);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final TTTState<I, Boolean> s = super.getHypothesisDS().getState(word);
        // we should only query defined paths
        assert s != null;
        return s.getAccessSequence();
    }

}
