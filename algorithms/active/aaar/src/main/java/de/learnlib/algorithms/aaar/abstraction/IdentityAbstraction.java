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

import net.automatalib.words.Alphabet;

public class IdentityAbstraction<I> implements InitialAbstraction<I, I> {

    private final Alphabet<I> alphabet;

    public IdentityAbstraction(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Alphabet<I> getSigmaC() {
        return alphabet;
    }

    @Override
    public Alphabet<I> getSigmaA() {
        return alphabet;
    }

    @Override
    public I getAbstractSymbol(I c) {
        return c;
    }

    @Override
    public I getRepresentative(I a) {
        return a;
    }
}
