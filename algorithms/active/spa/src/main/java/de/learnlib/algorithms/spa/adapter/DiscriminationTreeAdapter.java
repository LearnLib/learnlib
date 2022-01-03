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
package de.learnlib.algorithms.spa.adapter;

import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Adapter for using {@link DTLearnerDFA} as a sub-procedural learner.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class DiscriminationTreeAdapter<I> extends DTLearnerDFA<I> implements AccessSequenceTransformer<I> {

    public DiscriminationTreeAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        super(alphabet, oracle, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        return super.getHypothesisDS().transformAccessSequence(word);
    }

}
