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
package de.learnlib.filter.cache.dfa;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.words.Word;

/**
 * An {@link EquivalenceOracle} that tests an hypothesis for consistency with the contents of a {@link DFACacheOracle}.
 *
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
public final class DFACacheConsistencyTest<I> implements DFAEquivalenceOracle<I> {

    private final IncrementalDFABuilder<I> incDfa;
    private final Lock incDfaLock;

    /**
     * Constructor.
     *
     * @param incDfa
     *         the {@link IncrementalDFABuilder} data structure of the cache
     */
    public DFACacheConsistencyTest(IncrementalDFABuilder<I> incDfa, Lock lock) {
        this.incDfa = incDfa;
        this.incDfaLock = lock;
    }

    @Override
    public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
        Word<I> w;
        Acceptance acc;
        incDfaLock.lock();
        try {
            w = incDfa.findSeparatingWord(hypothesis, inputs, false);
            if (w == null) {
                return null;
            }
            acc = incDfa.lookup(w);
        } finally {
            incDfaLock.unlock();
        }
        assert (acc != Acceptance.DONT_KNOW);

        Boolean out = acc == Acceptance.TRUE;
        DefaultQuery<I, Boolean> result = new DefaultQuery<>(w);
        result.answer(out);
        return result;
    }

}

