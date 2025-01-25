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
package de.learnlib.algorithm.procedural;

/**
 * A utility class to annotate an input symbol with a (boolean) <i>continuable</i> flag.
 *
 * @param <I>
 *         input symbol type
 */
public class SymbolWrapper<I> {

    private final I delegate;
    private final boolean continuable;

    public SymbolWrapper(I delegate, boolean continuable) {
        this.delegate = delegate;
        this.continuable = continuable;
    }

    public I getDelegate() {
        return delegate;
    }

    public boolean isContinuable() {
        return continuable;
    }

    @Override
    public String toString() {
        return delegate + " (" + continuable + ')';
    }
}
