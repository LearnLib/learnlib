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
package de.learnlib.oracle.equivalence;

import java.util.Collection;

import javax.annotation.Nullable;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleEQOracle<A extends InputAlphabetHolder<I>, I, D> implements EquivalenceOracle<A, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEQOracle.class);

    private final EquivalenceOracle<A, I, D> eqOracle;

    public SimpleEQOracle(EquivalenceOracle<A, I, D> eqOracle) {
        this.eqOracle = eqOracle;
    }

    public static <A extends InputAlphabetHolder<I>, I, D> SimpleEQOracle<A, I, D> create(EquivalenceOracle<A, I, D> eqOracle) {
        return new SimpleEQOracle<>(eqOracle);
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        LOGGER.debug("Ignoring the set of inputs '{}', because I always use the complete hypothesis' input alphabet",
                     inputs);
        return eqOracle.findCounterExample(hypothesis, hypothesis.getInputAlphabet());
    }
}
