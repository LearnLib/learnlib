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
package de.learnlib.filter.cache.mealy;

import java.util.Collection;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link EquivalenceOracle} that tests a hypothesis for consistency with the contents of a {@link MealyCacheOracle}.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
public class MealyCacheConsistencyTest<I, O> implements MealyEquivalenceOracle<I, O> {

    private final IncrementalMealyBuilder<I, O> incMealy;

    /**
     * Constructor.
     *
     * @param incMealy
     *         the {@link IncrementalMealyBuilder} data structure underlying the cache
     */
    public MealyCacheConsistencyTest(IncrementalMealyBuilder<I, O> incMealy) {
        this.incMealy = incMealy;
    }

    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        final Word<I> w = incMealy.findSeparatingWord(hypothesis, inputs, false);

        if (w == null) {
            return null;
        }

        return new DefaultQuery<>(w, incMealy.lookup(w));
    }

}
