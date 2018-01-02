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
package de.learnlib.algorithms.nlstar;

/**
 * An (RFSA) inconsistency in an {@link ObservationTable}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class Inconsistency<I> {

    private final Row<I> row1;
    private final Row<I> row2;

    private final int symbolIdx;

    private final int suffixIdx;

    /**
     * Constructor.
     *
     * @param row1
     *         the first upper prime row
     * @param row2
     *         the second upper prime row (covered by {@code row1})
     * @param symbolIdx
     *         the index of the symbol
     * @param suffixIdx
     *         the index of the suffix
     */
    public Inconsistency(Row<I> row1, Row<I> row2, int symbolIdx, int suffixIdx) {
        this.row1 = row1;
        this.row2 = row2;
        this.symbolIdx = symbolIdx;
        this.suffixIdx = suffixIdx;
    }

    public Row<I> getRow1() {
        return row1;
    }

    public Row<I> getRow2() {
        return row2;
    }

    public int getSymbolIdx() {
        return symbolIdx;
    }

    public int getSuffixIdx() {
        return suffixIdx;
    }

}
