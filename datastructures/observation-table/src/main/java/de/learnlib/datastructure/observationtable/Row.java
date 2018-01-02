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
package de.learnlib.datastructure.observationtable;

import javax.annotation.Nonnull;

import net.automatalib.words.Word;

/**
 * A row in an observation table. Minimally, a row consists of a prefix (the row label) and a unique identifier in its
 * observation table which remains constant throughout the whole process.
 * <p>
 * Apart from that, a row is also associated with contents (via an integer id). The prefix of a row may be either a
 * short or long prefix. In the former case, the row will also have successor rows (one-step futures) associated with
 * it.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public interface Row<I> {

    /**
     * Retrieves the unique row identifier associated with this row.
     *
     * @return the row identifier
     *
     * @see ObservationTable#numberOfRows()
     */
    int getRowId();

    /**
     * Retrieves the unique identifier associated with the content of this row (may be {@code -1} if this row has not
     * yet been initialized).
     *
     * @return the row content identifier
     *
     * @see ObservationTable#numberOfDistinctRows()
     */
    int getRowContentId();

    /**
     * Retrieves the label of this row.
     *
     * @return the label of this row
     */
    @Nonnull
    Word<I> getLabel();

    /**
     * Retrieves whether this row is a short or a long prefix row.
     *
     * @return {@code true} if this row is a short prefix row, {@code false} otherwise.
     */
    boolean isShortPrefixRow();

    /**
     * Retrieves the successor row for this short label row and the given alphabet symbol (by index). If this is no
     * short label row, an exception might occur.
     *
     * @param pos
     *         the index of the alphabet symbol.
     *
     * @return the successor row (may be <code>null</code>)
     */
    Row<I> getSuccessor(int pos);
}
