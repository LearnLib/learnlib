/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.filter.cache.mealy;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class StateLocalInputCacheConsistencyTest<I, O>
        implements EquivalenceOracle<StateLocalInputMealyMachine<?, I, ?, O>, I, Word<OutputAndLocalInputs<I, O>>> {

    private final IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> incMealy;
    private final ReadWriteLock incMealyLock;

    /**
     * Constructor.
     *
     * @param incMealy
     *         the {@link IncrementalMealyBuilder} data structure underlying the cache
     * @param lock
     *         the read-write lock for accessing the cache concurrently
     */
    StateLocalInputCacheConsistencyTest(IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> incMealy,
                                        ReadWriteLock lock) {
        this.incMealy = incMealy;
        this.incMealyLock = lock;
    }

    @Override
    public DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>> findCounterExample(StateLocalInputMealyMachine<?, I, ?, O> hypothesis,
                                                                                Collection<? extends I> inputs) {
        final WordBuilder<OutputAndLocalInputs<I, O>> wb;
        Word<I> w;

        // using null here as sink is fine, because the cache will never traverse undefined transitions of the
        // hypothesis (discrepancies will be observed beforehand on the enabled inputs)
        final StateLocalInputMealyMachine<?, I, ?, OutputAndLocalInputs<I, O>> wrapped =
                StateLocalInputMealyUtil.partialToObservableOutput(hypothesis, null);

        incMealyLock.readLock().lock();
        try {
            w = incMealy.findSeparatingWord(wrapped, inputs, false);
            if (w == null) {
                return null;
            }
            wb = new WordBuilder<>(w.length());
            incMealy.lookup(w, wb);
        } finally {
            incMealyLock.readLock().unlock();
        }

        DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>> result = new DefaultQuery<>(w);
        result.answer(wb.toWord());
        return result;
    }
}
