/* Copyright (C) 2013-2021 TU Dortmund
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

import java.util.Comparator;

import de.learnlib.api.Resumable;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.mealy.MealyCacheOracle.MealyCacheOracleState;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.incremental.mealy.tree.dynamic.DynamicIncrementalMealyTreeBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MealyCacheOracle<I, O> extends InternalMealyCacheOracle<I, O>
        implements Resumable<MealyCacheOracleState<I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MealyCacheOracle.class);

    MealyCacheOracle(IncrementalMealyBuilder<I, O> incrementalBuilder,
                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate) {
        super(incrementalBuilder, errorSyms, delegate);
    }

    MealyCacheOracle(IncrementalMealyBuilder<I, O> incrementalBuilder,
                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate,
                     Comparator<I> comparator) {
        super(incrementalBuilder, errorSyms, delegate, comparator);
    }

    public static <I, O> MealyCacheOracle<I, O> createDAGCacheOracle(Alphabet<I> inputAlphabet,
                                                                     MembershipOracle<I, Word<O>> delegate) {
        return createDAGCacheOracle(inputAlphabet, null, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createDAGCacheOracle(Alphabet<I> inputAlphabet,
                                                                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                     MembershipOracle<I, Word<O>> delegate) {
        IncrementalMealyBuilder<I, O> incrementalBuilder = new IncrementalMealyDAGBuilder<>(inputAlphabet);
        return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate, inputAlphabet);
    }

    public static <I, O> MealyCacheOracle<I, O> createTreeCacheOracle(Alphabet<I> inputAlphabet,
                                                                      MembershipOracle<I, Word<O>> delegate) {
        return createTreeCacheOracle(inputAlphabet, null, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createTreeCacheOracle(Alphabet<I> inputAlphabet,
                                                                      @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                      MembershipOracle<I, Word<O>> delegate) {
        IncrementalMealyBuilder<I, O> incrementalBuilder = new IncrementalMealyTreeBuilder<>(inputAlphabet);
        return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate, inputAlphabet);
    }

    public static <I, O> MealyCacheOracle<I, O> createDynamicTreeCacheOracle(MembershipOracle<I, Word<O>> delegate) {
        return createDynamicTreeCacheOracle(null, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createDynamicTreeCacheOracle(@Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                             MembershipOracle<I, Word<O>> delegate) {
        IncrementalMealyBuilder<I, O> incrementalBuilder = new DynamicIncrementalMealyTreeBuilder<>();
        return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
    }

    @Override
    public MealyCacheOracleState<I, O> suspend() {
        return new MealyCacheOracleState<>(incMealy);
    }

    @Override
    public void resume(MealyCacheOracleState<I, O> state) {
        final Class<?> thisClass = this.incMealy.getClass();
        final Class<?> stateClass = state.getBuilder().getClass();

        if (!thisClass.equals(stateClass)) {
            LOGGER.warn(
                    "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                    thisClass,
                    stateClass);
        }

        this.incMealy = state.getBuilder();
    }

    public static class MealyCacheOracleState<I, O> {

        private final IncrementalMealyBuilder<I, O> builder;

        MealyCacheOracleState(IncrementalMealyBuilder<I, O> builder) {
            this.builder = builder;
        }

        IncrementalMealyBuilder<I, O> getBuilder() {
            return builder;
        }
    }
}
