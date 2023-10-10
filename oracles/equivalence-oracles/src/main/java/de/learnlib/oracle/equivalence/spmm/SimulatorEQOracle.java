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
package de.learnlib.oracle.equivalence.spmm;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimulatorEQOracle<I, O> implements EquivalenceOracle<SPMM<?, I, ?, O>, I, Word<O>> {

    private final SPMM<?, I, ?, O> spmm;

    public SimulatorEQOracle(SPMM<?, I, ?, O> spmm) {
        this.spmm = spmm;
    }

    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(SPMM<?, I, ?, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        if (!(inputs instanceof ProceduralInputAlphabet)) {
            throw new IllegalArgumentException("Inputs are not an SPA alphabet");
        }

        @SuppressWarnings("unchecked")
        final ProceduralInputAlphabet<I> alphabet = (ProceduralInputAlphabet<I>) inputs;

        final Word<I> sep = Automata.findSeparatingWord(spmm, hypothesis, alphabet);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, spmm.computeOutput(sep));
    }
}
