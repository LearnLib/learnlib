/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.datastructure.observationtable;

/**
 * A description of an inconsistency in an {@link GenericObservationTable}. An inconsistency consists of two short
 * prefixes {@code u}, {@code u'} with identical contents, and an input symbol {@code a} such that the rows for
 * {@code ua} and {@code u'a} have different contents.
 *
 * @param <I>
 *         input symbol type
 */
public class Inconsistency<I> {

    private final Row<I> firstRow;
    private final Row<I> secondRow;
    private final I symbol;

    public Inconsistency(Row<I> firstRow, Row<I> secondRow, I symbol) {
        this.firstRow = firstRow;
        this.secondRow = secondRow;
        this.symbol = symbol;
    }

    /**
     * Retrieves the first (short prefix) row constituting the inconsistency.
     *
     * @return the first row
     */
    public Row<I> getFirstRow() {
        return firstRow;
    }

    /**
     * Retrieves the second (short prefix) row constituting the inconsistency.
     *
     * @return the second row
     */
    public Row<I> getSecondRow() {
        return secondRow;
    }

    /**
     * Retrieves the symbol for which's one-letter extensions the corresponding rows have different contents.
     *
     * @return the symbol
     */
    public I getSymbol() {
        return symbol;
    }
}
