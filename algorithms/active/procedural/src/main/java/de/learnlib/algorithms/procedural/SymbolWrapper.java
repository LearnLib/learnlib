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

import java.util.Objects;

import net.automatalib.words.VPDAlphabet.SymbolType;

public class SymbolWrapper<I> {

    private final I delegate;
    private final boolean isTerminating;
    private final SymbolType type;

    public SymbolWrapper(I delegate, boolean isTerminating, SymbolType type) {
        this.delegate = delegate;
        this.isTerminating = isTerminating;
        this.type = type;
    }

    public I getDelegate() {
        return delegate;
    }

    public boolean isTerminating() {
        return isTerminating;
    }

    public SymbolType getType() {
        return type;
    }

    @Override
    public String toString() {
        if (type == SymbolType.CALL) {
            return String.valueOf(delegate) + '(' + isTerminating + ')';
        }
        return Objects.toString(delegate);
    }
}
