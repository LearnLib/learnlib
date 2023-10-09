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
package de.learnlib.filter.cache.dfa;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link EquivalenceOracle} that tests a hypothesis for consistency with the contents of a {@link DFACacheOracle}.
 *
 * @param <I>
 *         input symbol class
 */
final class DFACacheConsistencyTest<I> implements DFAEquivalenceOracle<I> {

    private final IncrementalDFABuilder<I> incDfa;

    /**
     * Constructor.
     *
     * @param incDfa
     *         the {@link IncrementalDFABuilder} data structure of the cache
     */
    DFACacheConsistencyTest(IncrementalDFABuilder<I> incDfa) {
        this.incDfa = incDfa;
    }

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
        final Word<I> w = incDfa.findSeparatingWord(hypothesis, inputs, false);

        if (w == null) {
            return null;
        }

        final Acceptance acc = incDfa.lookup(w);
        assert acc != Acceptance.DONT_KNOW;

        return new DefaultQuery<>(w, acc == Acceptance.TRUE);
    }

}

