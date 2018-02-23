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
package de.learnlib.filter.statistic.oracle;

import java.util.concurrent.atomic.AtomicLong;

import de.learnlib.api.oracle.SymbolQueryOracle;

/**
 * A simple wrapper for counting the number of {@link SymbolQueryOracle#reset() resets} and
 * {@link SymbolQueryOracle#query(Object) symbol queries} of a {@link SymbolQueryOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class CounterSymbolQueryOracle<I, O> implements SymbolQueryOracle<I, O> {

    private final SymbolQueryOracle<I, O> delegate;
    private final AtomicLong resetCounter = new AtomicLong();
    private final AtomicLong symbolCounter = new AtomicLong();

    public CounterSymbolQueryOracle(SymbolQueryOracle<I, O> delegate) {
        this.delegate = delegate;
    }

    @Override
    public O query(I i) {
        symbolCounter.incrementAndGet();
        return delegate.query(i);
    }

    @Override
    public void reset() {
        resetCounter.incrementAndGet();
        delegate.reset();
    }

    public long getResetCount() {
        return resetCounter.get();
    }

    public long getSymbolCount() {
        return symbolCounter.get();
    }
}
