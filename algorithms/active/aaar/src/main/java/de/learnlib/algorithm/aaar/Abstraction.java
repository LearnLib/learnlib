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
package de.learnlib.algorithm.aaar;

/**
 * An interface for mapping concrete symbols to their abstraction, and abstractions to their representatives,
 * respectively.
 *
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 */
public interface Abstraction<AI, CI> {

    /**
     * Returns the abstract symbol for a given concrete one.
     *
     * @param c
     *         the concrete symbol
     *
     * @return the abstraction of {@code c}
     */
    AI getAbstractSymbol(CI c);

    /**
     * Returns the (concrete) representative for a given abstract symbol.
     *
     * @param a
     *         the abstract symbol
     *
     * @return the concrete representative of {@code a}
     */
    CI getRepresentative(AI a);

}
