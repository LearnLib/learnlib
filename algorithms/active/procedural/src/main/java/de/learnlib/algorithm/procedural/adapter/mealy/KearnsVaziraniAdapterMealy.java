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
package de.learnlib.algorithm.procedural.adapter.mealy;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Adapter for using {@link KearnsVaziraniMealy} as a procedural learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class KearnsVaziraniAdapterMealy<I, O> extends KearnsVaziraniMealy<I, O>
        implements AccessSequenceTransformer<I> {

    public KearnsVaziraniAdapterMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        super(alphabet, oracle, false, AcexAnalyzers.LINEAR_FWD);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final int reachedState = getStateId(super.getHypothesisModel(), word);
        return super.stateInfos.get(reachedState).accessSequence;
    }

    private <S> int getStateId(MealyMachine<S, I, ?, O> mealyMachine, Word<I> word) {
        final S s = mealyMachine.getState(word);
        return mealyMachine.stateIDs().getStateId(s);
    }
}
