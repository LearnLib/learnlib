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
package de.learnlib.oracle.equivalence.mmlt;

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.util.automaton.mmlt.MMLTs;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simulator oracle for {@link MMLT}s.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class SimulatorEQOracle<I, O> implements MMLTEquivalenceOracle<I, O> {

    private final MMLT<?, I, ?, O> refModel;

    public SimulatorEQOracle(MMLT<?, I, ?, O> refModel) {
        this.refModel = refModel;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis,
                                                                                          Collection<? extends TimedInput<I>> inputs) {
        final Word<TimedInput<I>> separatingWord = MMLTs.findSeparatingWord(refModel, hypothesis, inputs);

        if (separatingWord != null) {
            final Word<TimedOutput<O>> sulOutput = refModel.getSemantics().computeOutput(separatingWord);
            return new DefaultQuery<>(separatingWord, sulOutput);
        } else {
            return null;
        }
    }
}
