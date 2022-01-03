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
package de.learnlib.oracle.equivalence.spa;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimulatorEQOracle<I> implements EquivalenceOracle<SPA<?, I>, I, Boolean> {

    private final SPA<?, I> spa;

    public SimulatorEQOracle(SPA<?, I> spa) {
        this.spa = spa;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(SPA<?, I> hypothesis, Collection<? extends I> inputs) {
        if (!(inputs instanceof SPAAlphabet)) {
            throw new IllegalArgumentException("Inputs are not an SPA alphabet");
        }

        @SuppressWarnings("unchecked")
        final SPAAlphabet<I> alphabet = (SPAAlphabet<I>) inputs;

        final Word<I> sep = Automata.findSeparatingWord(spa, hypothesis, alphabet);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, spa.computeOutput(sep));
    }
}

