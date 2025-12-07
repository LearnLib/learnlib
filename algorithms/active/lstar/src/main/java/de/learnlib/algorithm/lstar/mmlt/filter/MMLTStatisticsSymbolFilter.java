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
package de.learnlib.algorithm.lstar.mmlt.filter;

import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.symbol.AbstractStatisticsSymbolFilter;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;

/**
 * A symbol filter that logs statistics about false accepts and false ignores.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 */
public class MMLTStatisticsSymbolFilter<I> extends AbstractStatisticsSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, ?> automaton;

    public MMLTStatisticsSymbolFilter(MMLT<?, I, ?, ?> automaton,
                                      SymbolFilter<TimedInput<I>, InputSymbol<I>> delegate) {
        super(delegate);
        this.automaton = automaton;
    }

    @Override
    protected FilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return MMLTSymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
