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
package de.learnlib.algorithms.aaar.abstraction;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.words.Alphabet;

/**
 * @author fhowar
 * @author frohme
 */
public class ExplicitInitialAbstraction<AI, CI> implements InitialAbstraction<AI, CI> {

    private final Alphabet<CI> sigmaC;
    private final Alphabet<AI> sigmaA;

    private final Map<CI, AI> alpha = new HashMap<>();
    private final Map<AI, CI> gamma = new HashMap<>();

    public ExplicitInitialAbstraction(Alphabet<CI> sigmaC, Alphabet<AI> sigmaA) {
        assert sigmaC.size() == sigmaA.size();

        this.sigmaC = sigmaC;
        this.sigmaA = sigmaA;

        for (int i = 0; i < sigmaC.size(); i++) {
            final AI ai = sigmaA.getSymbol(i);
            final CI ci = sigmaC.getSymbol(i);

            alpha.put(ci, ai);
            gamma.put(ai, ci);
        }
    }

    @Override
    public Alphabet<CI> getSigmaC() {
        return sigmaC;
    }

    @Override
    public Alphabet<AI> getSigmaA() {
        return sigmaA;
    }

    @Override
    public AI getAbstractSymbol(CI c) {
        return alpha.get(c);
    }

    @Override
    public CI getRepresentative(AI a) {
        return gamma.get(a);
    }

}
