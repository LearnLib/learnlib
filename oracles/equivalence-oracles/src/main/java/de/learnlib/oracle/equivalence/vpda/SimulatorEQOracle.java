/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.oracle.equivalence.vpda;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * An equivalence oracle based on the computation of a separating word for a given hypothesis and a previously known
 * target system.
 *
 * @author frohme
 */
public class SimulatorEQOracle<I> implements EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> {

    private final OneSEVPA<?, I> reference;
    private final VPDAlphabet<I> alphabet;

    public SimulatorEQOracle(final OneSEVPA<?, I> reference, final VPDAlphabet<I> alphabet) {
        this.reference = reference;
        this.alphabet = alphabet;
    }

    @Override
    public DefaultQuery<I, Boolean> findCounterExample(final OneSEVPA<?, I> hypothesis,
                                                       final Collection<? extends I> inputs) {

        final Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, alphabet);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, reference.computeOutput(sep));
    }
}
