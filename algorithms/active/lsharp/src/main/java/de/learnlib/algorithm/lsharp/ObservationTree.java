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

package de.learnlib.algorithm.lsharp;

import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ObservationTree<S extends Comparable<S>, I, O> {
    S defaultState();

    S insertObservation(@Nullable S start, Word<I> input, Word<O> output);

    Word<I> getAccessSeq(S state);

    Word<I> getTransferSeq(S toState, S fromState);

    @Nullable
    Word<O> getObservation(@Nullable S start, Word<I> input);

    @Nullable
    Pair<O, S> getOutSucc(S src, I input);

    default @Nullable O getOut(S src, I input) {
        @Nullable
        Pair<O, S> out = this.getOutSucc(src, input);
        if (out == null) {
            return null;
        }

        return out.getFirst();
    }

    @Nullable
    S getSucc(S src, Word<I> input);

    List<Pair<S, I>> noSuccDefined(List<S> basis, boolean sort);

    Integer size();

    boolean treeAndHypStatesApartSink(S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth);

    Alphabet<I> getInputAlphabet();

}
