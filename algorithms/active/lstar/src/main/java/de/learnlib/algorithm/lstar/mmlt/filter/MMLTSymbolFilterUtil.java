/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lstar.mmlt.filter;

import java.util.Objects;

import de.learnlib.filter.FilterResponse;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

final class MMLTSymbolFilterUtil {

    private MMLTSymbolFilterUtil() {
        // prevent instantiation
    }

    static <I, O> FilterResponse isIgnorable(MMLT<?, I, ?, O> automaton,
                                             Word<TimedInput<I>> prefix,
                                             InputSymbol<I> symbol) {
        return isIgnorable(automaton.getSemantics(), prefix, symbol);
    }

    static <S, I, T, O> FilterResponse isIgnorable(MMLTSemantics<S, I, T, O> semantics,
                                                   Word<TimedInput<I>> prefix,
                                                   InputSymbol<I> symbol) {
        State<S, O> targetConfig = semantics.getState(prefix);
        assert targetConfig != null;
        T trans = semantics.getTransition(targetConfig, symbol);
        assert trans != null;
        State<S, O> target = semantics.getSuccessor(trans);
        TimedOutput<O> output = semantics.getTransitionOutput(trans);

        boolean ignorable = Objects.equals(output, semantics.getSilentOutput()) && Objects.equals(targetConfig, target);

        return ignorable ? FilterResponse.IGNORE : FilterResponse.ACCEPT;
    }
}
