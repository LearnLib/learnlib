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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.filter.cache.CacheConfig.DFACollectionConfig;
import de.learnlib.filter.cache.CacheConfig.DFASupplierConfig;
import de.learnlib.filter.cache.CacheConfig.MealyCollectionConfig;
import de.learnlib.filter.cache.CacheConfig.MealySupplierConfig;
import de.learnlib.filter.cache.CacheConfig.MooreCollectionConfig;
import de.learnlib.filter.cache.CacheConfig.MooreSupplierConfig;
import de.learnlib.filter.cache.CacheConfig.SLISULConfig;
import de.learnlib.filter.cache.CacheConfig.SULConfig;
import de.learnlib.filter.cache.LearningCache.DFALearningCache;
import de.learnlib.filter.cache.LearningCache.MealyLearningCache;
import de.learnlib.filter.cache.LearningCache.MooreLearningCache;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MooreLearningCacheOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

public interface CacheCreator<I, D, M, C extends LearningCache<?, I, D>>
        extends BiFunction<Alphabet<I>, M, CacheConfig<I, D, C>> {

    interface DFACacheCreator<I, C extends DFALearningCache<I>>
            extends CacheCreator<I, Boolean, MembershipOracle<I, Boolean>, C> {

        static <I, C extends DFALearningCacheOracle<I>> DFACacheCreator<I, C> forSupplier(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Boolean>>, Supplier<C>> provider) {
            return new DFASupplierConfig<>(provider);
        }

        static <I, C extends DFALearningCacheOracle<I>> DFACacheCreator<I, C> forCollection(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Boolean>>, Collection<C>> provider) {
            return new DFACollectionConfig<>(provider);
        }
    }

    interface MealyCacheCreator<I, O, C extends MealyLearningCache<I, O>>
            extends CacheCreator<I, Word<O>, MembershipOracle<I, Word<O>>, C> {

        static <I, O, C extends MealyLearningCacheOracle<I, O>> MealyCacheCreator<I, O, C> forSupplier(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Word<O>>>, Supplier<C>> provider) {
            return new MealySupplierConfig<>(provider);
        }

        static <I, O, C extends MealyLearningCacheOracle<I, O>> MealyCacheCreator<I, O, C> forSupplier(Function<Supplier<? extends MembershipOracle<I, Word<O>>>, Supplier<C>> provider) {
            return new MealySupplierConfig<>((alphabet, supplier) -> provider.apply(supplier));
        }

        static <I, O, C extends MealyLearningCacheOracle<I, O>> MealyCacheCreator<I, O, C> forCollection(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Word<O>>>, Collection<C>> provider) {
            return new MealyCollectionConfig<>(provider);
        }

        static <I, O, C extends MealyLearningCacheOracle<I, O>> MealyCacheCreator<I, O, C> forCollection(Function<Collection<? extends MembershipOracle<I, Word<O>>>, Collection<C>> provider) {
            return new MealyCollectionConfig<>((alphabet, supplier) -> provider.apply(supplier));
        }
    }

    interface MooreCacheCreator<I, O, C extends MooreLearningCache<I, O>>
            extends CacheCreator<I, Word<O>, MembershipOracle<I, Word<O>>, C> {

        static <I, O, C extends MooreLearningCacheOracle<I, O>> MooreCacheCreator<I, O, C> forSupplier(BiFunction<Alphabet<I>, Supplier<? extends MembershipOracle<I, Word<O>>>, Supplier<C>> provider) {
            return new MooreSupplierConfig<>(provider);
        }

        static <I, O, C extends MooreLearningCacheOracle<I, O>> MooreCacheCreator<I, O, C> forCollection(BiFunction<Alphabet<I>, Collection<? extends MembershipOracle<I, Word<O>>>, Collection<C>> provider) {
            return new MooreCollectionConfig<>(provider);
        }
    }

    interface SULCacheCreator<I, O, C extends SUL<I, O> & MealyLearningCache<I, O>>
            extends CacheCreator<I, Word<O>, SUL<I, O>, C> {

        static <I, O, C extends SUL<I, O> & MealyLearningCache<I, O>> SULCacheCreator<I, O, C> forSupplier(BiFunction<Alphabet<I>, SUL<I, O>, C> provider) {
            return new SULConfig<>(provider);
        }
    }

    interface SLISULCacheCreator<I, O, C extends StateLocalInputSUL<I, O> & MealyLearningCache<I, O>>
            extends CacheCreator<I, Word<O>, StateLocalInputSUL<I, O>, C> {

        static <I, O, C extends StateLocalInputSUL<I, O> & MealyLearningCache<I, O>> SLISULCacheCreator<I, O, C> forSupplier(
                BiFunction<Alphabet<I>, StateLocalInputSUL<I, O>, C> provider) {
            return new SLISULConfig<>(provider);
        }
    }

}
