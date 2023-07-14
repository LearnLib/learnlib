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
package de.learnlib.algorithms.procedural;

import java.util.Arrays;
import java.util.Collection;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.ProceduralInputAlphabet;

public class AlphabetMapper<I> implements Mapping<I, SymbolWrapper<I>> {

    private final ProceduralInputAlphabet<I> source;
    private final SymbolWrapper<I>[] target;

    @SuppressWarnings("unchecked")
    public AlphabetMapper(ProceduralInputAlphabet<I> source) {
        this.source = source;
        this.target = new SymbolWrapper[source.size()];
    }

    public void set(I symbol, SymbolWrapper<I> representative) {
        this.target[source.getSymbolIndex(symbol)] = representative;
    }

    @Override
    public SymbolWrapper<I> get(I symbol) {
        return this.target[source.getSymbolIndex(symbol)];
    }

    public Collection<SymbolWrapper<I>> values() {
        return Arrays.asList(this.target);
    }
}
