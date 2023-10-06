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
package de.learnlib.oracle.equivalence.vpa;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpa.OneSEVPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.VPAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An equivalence oracle based on the computation of a separating word for a given hypothesis and a previously known
 * target system.
 */
public class SimulatorEQOracle<I> implements EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> {

    private final OneSEVPA<?, I> reference;

    public SimulatorEQOracle(OneSEVPA<?, I> reference) {
        this.reference = reference;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(OneSEVPA<?, I> hypothesis,
                                                                 Collection<? extends I> inputs) {
        if (!(inputs instanceof VPAlphabet)) {
            throw new IllegalArgumentException("Inputs are not a visibly push-down alphabet");
        }

        @SuppressWarnings("unchecked")
        final VPAlphabet<I> alphabet = (VPAlphabet<I>) inputs;

        final Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, alphabet);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, reference.computeOutput(sep));
    }
}
