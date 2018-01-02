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
package de.learnlib.filter.cache.mealy;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * An {@link EquivalenceOracle} that tests an hypothesis for consistency with the contents of a {@link
 * MealyCacheOracle}.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Malte Isberner
 */
public class MealyCacheConsistencyTest<I, O> implements MealyEquivalenceOracle<I, O> {

    private final IncrementalMealyBuilder<I, O> incMealy;
    private final Lock incMealyLock;

    /**
     * Constructor.
     *
     * @param incMealy
     *         the {@link IncrementalMealyBuilder} data structure underlying the cache.
     */
    public MealyCacheConsistencyTest(IncrementalMealyBuilder<I, O> incMealy, Lock lock) {
        this.incMealy = incMealy;
        this.incMealyLock = lock;
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
        WordBuilder<O> wb;
        Word<I> w;

        incMealyLock.lock();
        try {
            w = incMealy.findSeparatingWord(hypothesis, inputs, false);
            if (w == null) {
                return null;
            }
            wb = new WordBuilder<>(w.length());
            incMealy.lookup(w, wb);
        } finally {
            incMealyLock.unlock();
        }

        DefaultQuery<I, Word<O>> result = new DefaultQuery<>(w);
        result.answer(wb.toWord());
        return result;
    }

}
