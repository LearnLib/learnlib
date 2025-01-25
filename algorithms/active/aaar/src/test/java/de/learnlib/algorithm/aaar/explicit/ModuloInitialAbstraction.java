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
package de.learnlib.algorithm.aaar.explicit;

import java.util.Arrays;
import java.util.Collection;

import de.learnlib.algorithm.aaar.ExplicitInitialAbstraction;
import net.automatalib.alphabet.Alphabet;

public class ModuloInitialAbstraction<CI> implements ExplicitInitialAbstraction<String, CI> {

    private final Alphabet<CI> alphabet;

    public ModuloInitialAbstraction(Alphabet<CI> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public String getAbstractSymbol(CI c) {
        if (alphabet.getSymbolIndex(c) % 2 == 0) {
            return "even";
        } else {
            return "odd";
        }
    }

    @Override
    public CI getRepresentative(String a) {
        switch (a) {
            case "even":
                return alphabet.getSymbol(0);
            case "odd":
                return alphabet.getSymbol(1);
            default:
                throw new IllegalArgumentException("Unknown symbol: " + a);
        }
    }

    @Override
    public Collection<String> getInitialAbstracts() {
        return Arrays.asList("even", "odd");
    }
}
