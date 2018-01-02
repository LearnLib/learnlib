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
package de.learnlib.filter.cache.sul;

import de.learnlib.api.SUL;
import net.automatalib.words.Alphabet;

public final class SULCaches {

    private SULCaches() {
        throw new IllegalStateException("Constructor should never be invoked");
    }

    public static <I, O> SULCache<I, O> createTreeCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return SULCache.createTreeCache(alphabet, sul);
    }

    public static <I, O> SULCache<I, O> createCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return createDAGCache(alphabet, sul);
    }

    public static <I, O> SULCache<I, O> createDAGCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return SULCache.createDAGCache(alphabet, sul);
    }
}
