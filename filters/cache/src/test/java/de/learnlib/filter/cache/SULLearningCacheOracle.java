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
package de.learnlib.filter.cache;

import java.util.Collection;

import de.learnlib.Resumable;
import de.learnlib.filter.cache.LearningCache.MealyLearningCache;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.filter.cache.sul.StateLocalInputSULCache;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.query.Query;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class SULLearningCacheOracle<I, O, C extends MealyLearningCache<I, O> & SupportsGrowingAlphabet<I> & Resumable<?>>
        implements MealyLearningCacheOracle<I, O>, SupportsGrowingAlphabet<I> {

    private final C cache;
    private final MealyMembershipOracle<I, O> oracle;

    public SULLearningCacheOracle(C cache, MealyMembershipOracle<I, O> oracle) {
        this.cache = cache;
        this.oracle = oracle;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        oracle.processQueries(queries);
    }

    @Override
    public EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> createCacheConsistencyTest() {
        return cache.createCacheConsistencyTest();
    }

    public C getCache() {
        return cache;
    }

    public MealyMembershipOracle<I, O> getOracle() {
        return oracle;
    }

    public static <I, O> SULLearningCacheOracle<I, O, SULCache<I, O>> fromSULCache(SULCache<I, O> cache) {
        return new SULLearningCacheOracle<>(cache, new SULOracle<>(cache));
    }

    public static <I, O> SULLearningCacheOracle<I, O, StateLocalInputSULCache<I, O>> fromSLISULCache(
            StateLocalInputSULCache<I, O> cache,
            O undefinedInput) {
        return new SULLearningCacheOracle<>(cache, new StateLocalInputSULOracle<>(cache, undefinedInput));
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        cache.addAlphabetSymbol(symbol);
    }
}
