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
package de.learnlib.oracle.equivalence.sba;

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.util.automaton.procedural.SBAs;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimulatorEQOracle<I> implements EquivalenceOracle<SBA<?, I>, I, Boolean> {

    private final SBA<?, I> sba;

    public SimulatorEQOracle(SBA<?, I> sba) {
        this.sba = sba;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(SBA<?, I> hypothesis, Collection<? extends I> inputs) {
        if (!(inputs instanceof ProceduralInputAlphabet)) {
            throw new IllegalArgumentException("Inputs are not a procedural alphabet");
        }

        @SuppressWarnings("unchecked")
        final ProceduralInputAlphabet<I> alphabet = (ProceduralInputAlphabet<I>) inputs;

        final Word<I> sep = SBAs.findSeparatingWord(sba, hypothesis, alphabet);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, sba.computeOutput(sep));
    }
}
