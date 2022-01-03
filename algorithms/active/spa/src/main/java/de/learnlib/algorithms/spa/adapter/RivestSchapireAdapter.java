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

import java.util.Objects;

import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Adapter for using {@link RivestSchapireDFA} as a sub-procedural learner.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class RivestSchapireAdapter<I> extends RivestSchapireDFA<I>
        implements AccessSequenceTransformer<I>, DFALearner<I> {

    public RivestSchapireAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        super(alphabet, oracle);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final DFA<?, I> hypothesis = super.getHypothesisModel();
        final ObservationTable<I, Boolean> observationTable = super.getObservationTable();

        final Object reachedState = hypothesis.getState(word);

        for (final Word<I> shortPrefix : observationTable.getShortPrefixes()) {
            final Object reachedSPState = hypothesis.getState(shortPrefix);

            if (Objects.equals(reachedState, reachedSPState)) {
                return shortPrefix;
            }
        }

        throw new IllegalStateException("This should not have happened");
    }

}
