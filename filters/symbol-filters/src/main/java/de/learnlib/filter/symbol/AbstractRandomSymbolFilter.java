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
package de.learnlib.filter.symbol;

import java.util.Random;

import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.SymbolFilter;
import net.automatalib.word.Word;

/**
 * A symbol filter that falsely answers a query with a specified probability.
 *
 * @param <U>
 *         input symbol type of the prefix
 * @param <V>
 *         input symbol type of the transition label
 */
public abstract class AbstractRandomSymbolFilter<U, V> extends AbstractTruthfulSymbolFilter<U, V> implements SymbolFilter<U, V> {

    private final double inaccurateProb;
    private final Random random;

    public AbstractRandomSymbolFilter(double inaccurateProb, Random random) {
        this(inaccurateProb, random, valideProbability(inaccurateProb));
    }

    // utility constructor to prevent finalizer attacks, see SEI CERT Rule OBJ-11
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private AbstractRandomSymbolFilter(double inaccurateProb, Random random, boolean validated) {
        this.inaccurateProb = inaccurateProb;
        this.random = random;
    }

    private static boolean valideProbability(double inaccurateProb) {
        if (inaccurateProb > 1 || inaccurateProb < 0) {
            throw new IllegalArgumentException("Ratios must be between zero and 1 (inclusive).");
        }
        return true;
    }

    @Override
    public FilterResponse query(Word<U> prefix, V symbol) {
        boolean ignorable = isIgnorable(prefix, symbol) == FilterResponse.IGNORE;

        // Randomly misclassify:
        if (this.random.nextDouble() <= this.inaccurateProb) {
            ignorable = !ignorable;
        }

        if (ignorable) {
            return FilterResponse.IGNORE;
        } else {
            return FilterResponse.ACCEPT;
        }
    }
}
