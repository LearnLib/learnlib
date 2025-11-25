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
package de.learnlib.driver.simulator;

import de.learnlib.sul.TimedSUL;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Simulates the semantics of an {@link MMLT}.
 *
 * @param <S>
 *         location type.
 * @param <I>
 *         input symbol type (of non-delaying inputs).
 * @param <O>
 *         output symbol type.
 */
public class MMLTSimulatorSUL<S, I, T, O> implements TimedSUL<I, O> {

    private final MMLTSemantics<S, I, T, O> semantics;

    private @Nullable State<S, O> currentConfiguration;

    public MMLTSimulatorSUL(MMLTSemantics<S, I, T, O> semantics) {
        this.semantics = semantics;
        this.currentConfiguration = null;
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        T trans = this.semantics.getTransition(this.currentConfiguration, input);
        this.currentConfiguration = this.semantics.getSuccessor(trans);
        return this.semantics.getTransitionOutput(trans);
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        if (this.currentConfiguration == null) {
            throw new IllegalStateException("Not initialized!");
        }

        T trans = this.semantics.getTransition(this.currentConfiguration, new TimeoutSymbol<>(), maxTime);
        this.currentConfiguration = this.semantics.getSuccessor(trans);
        TimedOutput<O> output = this.semantics.getTransitionOutput(trans);

        if (output.equals(semantics.getSilentOutput())) {
            // No timeout observed:
            return null;
        } else {
            return output;
        }
    }

    @Override
    public void pre() {
        this.currentConfiguration = semantics.getInitialState();
    }

    @Override
    public void post() {
        this.currentConfiguration = null;
    }

}
