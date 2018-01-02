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
package de.learnlib.acex.analyzers;

import de.learnlib.acex.AbstractCounterexample;

public final class AcexAnalysisAlgorithms {

    private AcexAnalysisAlgorithms() {
        // prevent instantiation
    }

    /**
     * Scan linearly through the counterexample in ascending order.
     *
     * @param acex
     *         the abstract counterexample
     * @param low
     *         the lower bound of the search range
     * @param high
     *         the upper bound of the search range
     *
     * @return an index <code>i</code> such that <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
     */
    public static <E> int linearSearchFwd(AbstractCounterexample<E> acex, int low, int high) {
        assert !acex.testEffects(low, high);

        E effPrev = acex.effect(low);
        for (int i = low + 1; i <= high; i++) {
            E eff = acex.effect(i);
            if (!acex.checkEffects(effPrev, eff)) {
                return i - 1;
            }
            effPrev = eff;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Scan linearly through the counterexample in descending order.
     *
     * @param acex
     *         the abstract counterexample
     * @param low
     *         the lower bound of the search range
     * @param high
     *         the upper bound of the search range
     *
     * @return an index <code>i</code> such that <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
     */
    public static <E> int linearSearchBwd(AbstractCounterexample<E> acex, int low, int high) {
        assert !acex.testEffects(low, high);

        E effPrev = acex.effect(high);
        for (int i = high - 1; i >= low; i--) {
            E eff = acex.effect(i);
            if (!acex.checkEffects(eff, effPrev)) {
                return i;
            }
            effPrev = eff;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Search for a suffix index using an exponential search.
     *
     * @param acex
     *         the abstract counterexample
     * @param low
     *         the lower bound of the search range
     * @param high
     *         the upper bound of the search range
     *
     * @return an index <code>i</code> such that <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
     */
    public static <E> int exponentialSearchBwd(AbstractCounterexample<E> acex, int low, int high) {
        assert !acex.testEffects(low, high);

        int ofs = 1;
        E effHigh = acex.effect(high);

        int highIter = high;
        int lowIter = low;

        while (highIter - ofs > lowIter) {
            int next = highIter - ofs;
            E eff = acex.effect(next);
            if (!acex.checkEffects(eff, effHigh)) {
                lowIter = next;
                break;
            }
            highIter = next;
            ofs *= 2;
        }

        return binarySearchRight(acex, lowIter, highIter);
    }

    /**
     * Search for a suffix index using a binary search.
     *
     * @param acex
     *         the abstract counterexample
     * @param low
     *         the lower bound of the search range
     * @param high
     *         the upper bound of the search range
     *
     * @return an index <code>i</code> such that <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
     */
    public static <E> int binarySearchRight(AbstractCounterexample<E> acex, int low, int high) {
        E effLow = acex.effect(low);
        E effHigh = acex.effect(high);

        assert !acex.checkEffects(effLow, effHigh) :
                "compatible effects at " + low + ", " + high + ": " + effLow + ", " + effHigh;

        int highIter = high;
        int lowIter = low;

        while (highIter - lowIter > 1) {
            int mid = lowIter + (highIter - lowIter) / 2;
            E effMid = acex.effect(mid);
            if (!acex.checkEffects(effMid, effHigh)) {
                lowIter = mid;
            } else {
                highIter = mid;
                effHigh = effMid;
            }
        }

        return lowIter;
    }

    public static <E> int exponentialSearchFwd(AbstractCounterexample<E> acex, int low, int high) {
        assert !acex.testEffects(low, high);

        int ofs = 1;
        E effLow = acex.effect(low);

        int lowIter = low;

        while (lowIter + ofs < high) {
            int next = lowIter + ofs;
            E eff = acex.effect(next);
            if (!acex.checkEffects(effLow, eff)) {
                break;
            }
            lowIter = next;
            ofs *= 2;
        }

        return binarySearchLeft(acex, lowIter, high);
    }

    public static <E> int binarySearchLeft(AbstractCounterexample<E> acex, int low, int high) {
        E effLow = acex.effect(low);
        E effHigh = acex.effect(high);

        assert !acex.checkEffects(effLow, effHigh) :
                "compatible effects at " + low + ", " + high + ": " + effLow + ", " + effHigh;

        int highIter = high;
        int lowIter = low;

        while (highIter - lowIter > 1) {
            int mid = lowIter + (highIter - lowIter) / 2;
            E effMid = acex.effect(mid);
            if (!acex.checkEffects(effLow, effMid)) {
                highIter = mid;
            } else {
                lowIter = mid;
                effLow = effMid;
            }
        }

        return lowIter;
    }
}
