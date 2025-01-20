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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.learnlib.filter.cache.CacheCreator.DFACacheCreator;
import de.learnlib.filter.cache.CacheCreator.MealyCacheCreator;
import de.learnlib.filter.cache.CacheCreator.MooreCacheCreator;
import de.learnlib.filter.cache.CacheCreator.SLISULCacheCreator;
import de.learnlib.filter.cache.CacheCreator.SULCacheCreator;
import de.learnlib.filter.cache.LearningCache.MealyLearningCache;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MooreLearningCacheOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelOracle;
import de.learnlib.oracle.parallelism.BatchProcessorDefaults;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

public interface CacheConfig<I, D, C extends LearningCache<?, I, D>> {

    C getRepresentative();

    ParallelOracle<I, D> getParallelOracle();

    /**
     * Supplier-based parallel cache construction. <b>Requires a thread-safe oracle upon construction.</b>
     */
    class SupplierConfig<I, D, M, C extends LearningCacheOracle<?, I, D>> implements CacheCreator<I, D, M, C> {

        private final BiFunction<Alphabet<I>, Supplier<? extends M>, Supplier<C>> provider;

        public SupplierConfig(BiFunction<Alphabet<I>, Supplier<? extends M>, Supplier<C>> provider) {
            this.provider = provider;
        }

        @Override
        public CacheConfig<I, D, C> apply(Alphabet<I> alphabet, M oracle) {

            return new CacheConfig<I, D, C>() {

                final Supplier<C> transformedSupplier;

                {
                    this.transformedSupplier = provider.apply(alphabet, () -> oracle);
                }

                @Override
                public C getRepresentative() {
                    return transformedSupplier.get();
                }

                @Override
                public ParallelOracle<I, D> getParallelOracle() {
                    return ParallelOracleBuilders.newStaticParallelOracle(this.transformedSupplier).create();
                }
            };
        }
    }

    class DFASupplierConfig<I, C extends DFALearningCacheOracle<I>>
            extends SupplierConfig<I, Boolean, MembershipOracle<I, Boolean>, C> implements DFACacheCreator<I, C> {

        public DFASupplierConfig(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Boolean>>, Supplier<C>> provider) {
            super(provider);
        }
    }

    class MealySupplierConfig<I, O, C extends MealyLearningCacheOracle<I, O>>
            extends SupplierConfig<I, Word<O>, MembershipOracle<I, Word<O>>, C> implements MealyCacheCreator<I, O, C> {

        public MealySupplierConfig(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Word<O>>>, Supplier<C>> provider) {
            super(provider);
        }
    }

    class MooreSupplierConfig<I, O, C extends MooreLearningCacheOracle<I, O>>
            extends SupplierConfig<I, Word<O>, MembershipOracle<I, Word<O>>, C> implements MooreCacheCreator<I, O, C> {

        public MooreSupplierConfig(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Word<O>>>, Supplier<C>> provider) {
            super(provider);
        }
    }

    class SULConfig<I, O, C extends SUL<I, O> & MealyLearningCache<I, O>> implements SULCacheCreator<I, O, C> {

        private final BiFunction<Alphabet<I>, SUL<I, O>, C> provider;

        public SULConfig(BiFunction<Alphabet<I>, SUL<I, O>, C> provider) {
            this.provider = provider;
        }

        @Override
        public CacheConfig<I, Word<O>, C> apply(Alphabet<I> alphabet, SUL<I, O> sul) {

            return new CacheConfig<I, Word<O>, C>() {

                final C cache;

                {
                    this.cache = provider.apply(alphabet, sul);
                }

                @Override
                public C getRepresentative() {
                    return this.cache;
                }

                @Override
                public ParallelOracle<I, Word<O>> getParallelOracle() {
                    return ParallelOracleBuilders.newStaticParallelOracle(this.cache).create();
                }
            };
        }
    }

    class SLISULConfig<I, O, C extends StateLocalInputSUL<I, O> & MealyLearningCache<I, O>>
            implements SLISULCacheCreator<I, O, C> {

        private final BiFunction<Alphabet<I>, StateLocalInputSUL<I, O>, C> provider;

        public SLISULConfig(BiFunction<Alphabet<I>, StateLocalInputSUL<I, O>, C> provider) {
            this.provider = provider;
        }

        @Override
        public CacheConfig<I, Word<O>, C> apply(Alphabet<I> alphabet, StateLocalInputSUL<I, O> sul) {

            return new CacheConfig<I, Word<O>, C>() {

                final C cache;

                {
                    this.cache = provider.apply(alphabet, sul);
                }

                @Override
                public C getRepresentative() {
                    return this.cache;
                }

                @Override
                public ParallelOracle<I, Word<O>> getParallelOracle() {
                    return ParallelOracleBuilders.newStaticParallelOracle(this.cache, null).create();
                }
            };
        }
    }

    /**
     * Collection-based parallel cache construction. <b>Requires a thread-safe oracle upon construction.</b>
     */
    class CollectionConfig<I, D, M, C extends LearningCacheOracle<?, I, D>> implements CacheCreator<I, D, M, C> {

        final BiFunction<Alphabet<I>, Collection<? extends M>, Collection<C>> provider;

        public CollectionConfig(BiFunction<Alphabet<I>, Collection<? extends M>, Collection<C>> provider) {
            this.provider = provider;
        }

        @Override
        public CacheConfig<I, D, C> apply(Alphabet<I> alphabet, M oracle) {

            return new CacheConfig<I, D, C>() {

                final List<C> oracles;

                {
                    List<M> suls = new ArrayList<>(BatchProcessorDefaults.POOL_SIZE);

                    for (int i = 0; i < BatchProcessorDefaults.POOL_SIZE; i++) {
                        suls.add(oracle);
                    }
                    this.oracles = new ArrayList<>(provider.apply(alphabet, suls));
                }

                @Override
                public C getRepresentative() {
                    return this.oracles.get(0);
                }

                @Override
                public ParallelOracle<I, D> getParallelOracle() {
                    return ParallelOracleBuilders.newStaticParallelOracle(this.oracles).create();
                }
            };
        }
    }

    class DFACollectionConfig<I, C extends DFALearningCacheOracle<I>>
            extends CollectionConfig<I, Boolean, MembershipOracle<I, Boolean>, C> implements DFACacheCreator<I, C> {

        public DFACollectionConfig(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Boolean>>, Collection<C>> provider) {
            super(provider);
        }
    }

    class MealyCollectionConfig<I, O, C extends MealyLearningCacheOracle<I, O>>
            extends CollectionConfig<I, Word<O>, MembershipOracle<I, Word<O>>, C>
            implements MealyCacheCreator<I, O, C> {

        public MealyCollectionConfig(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Word<O>>>, Collection<C>> provider) {
            super(provider);
        }
    }

    class MooreCollectionConfig<I, O, C extends MooreLearningCacheOracle<I, O>>
            extends CollectionConfig<I, Word<O>, MembershipOracle<I, Word<O>>, C>
            implements MooreCacheCreator<I, O, C> {

        public MooreCollectionConfig(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Word<O>>>, Collection<C>> provider) {
            super(provider);
        }
    }

}
