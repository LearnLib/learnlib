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
package de.learnlib.datastructure.pta;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.common.util.WrapperUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlueFringePTADFA<I> extends BlueFringePTA<I, Boolean, Void> {

    public BlueFringePTADFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public DFA<?, I> asDFA() {
        return new AsDFA<>(this);
    }

    static final class AsDFA<I> implements DFA<BlueFringePTAState<Boolean, Void>, I> {

        private final BlueFringePTADFA<I> pta;

        AsDFA(BlueFringePTADFA<I> pta) {
            this.pta = pta;
        }

        @Override
        public Collection<BlueFringePTAState<Boolean, Void>> getStates() {
            return this.pta.getStates();
        }

        @Override
        public @Nullable BlueFringePTAState<Boolean, Void> getTransition(BlueFringePTAState<Boolean, Void> state,
                                                                         I input) {
            final PTATransition<BlueFringePTAState<Boolean, Void>> t = this.pta.getTransition(state, input);
            return t == null ? null : t.getTarget();
        }

        @Override
        public boolean isAccepting(BlueFringePTAState<Boolean, Void> state) {
            return WrapperUtil.booleanValue(this.pta.getStateProperty(state));
        }

        @Override
        public @Nullable BlueFringePTAState<Boolean, Void> getInitialState() {
            return this.pta.getInitialState();
        }
    }

}
