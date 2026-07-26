/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.factory;

import java.util.Objects;

import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Symbols.ContextFreeSymbols;
import de.learnlib.cli.option.Symbols.RegularSymbols;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;

public final class AlphabetFactory {

    private AlphabetFactory() {
        // prevent instantiation
    }

    public static Alphabet<String> getRegularAlphabet(Options options) {
        final RegularSymbols inputs = validateRegularSymbols(options);
        return Alphabets.fromList(inputs.symbols);
    }

    public static ProceduralInputAlphabet<String> getProceduralAlphabet(Options options) {
        final ContextFreeSymbols inputs = validateContextFreeSymbols(options);
        if (inputs.returnSymbols.size() != 1) {
            throw new IllegalArgumentException("Procedural systems require exactly one return symbol");
        }
        return new DefaultProceduralInputAlphabet<>(Alphabets.fromList(inputs.internalSymbols),
                                                    Alphabets.fromList(inputs.callSymbols),
                                                    inputs.returnSymbols.get(0));
    }

    public static VPAlphabet<String> getVPAlphabet(Options options) {
        final ContextFreeSymbols inputs = validateContextFreeSymbols(options);
        return new DefaultVPAlphabet<>(Alphabets.fromList(inputs.internalSymbols),
                                       Alphabets.fromList(inputs.callSymbols),
                                       Alphabets.fromList(inputs.returnSymbols));
    }

    private static RegularSymbols validateRegularSymbols(Options options) {
        return Objects.requireNonNull(options.symbols.regularSymbols,
                                      String.format("Type '%s' requires a regular alphabet definition", options.type));
    }

    private static ContextFreeSymbols validateContextFreeSymbols(Options options) {
        return Objects.requireNonNull(options.symbols.contextFreeSymbols,
                                      String.format("Type '%s' requires a context-free alphabet definition",
                                                    options.type));
    }
}
