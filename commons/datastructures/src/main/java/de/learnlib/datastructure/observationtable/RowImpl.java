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
package de.learnlib.datastructure.observationtable;

import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.word.Word;

final class RowImpl<I> implements Row<I> {

    private final Word<I> label;
    private final int rowId;

    private int rowContentId = -1;
    private int lpIndex;
    private ArrayStorage<RowImpl<I>> successors;

    /**
     * Constructor for short label rows.
     *
     * @param label
     *         the label (label) of this row
     * @param rowId
     *         the unique row identifier
     * @param alphabetSize
     *         the size of the alphabet, used for initializing the successor array
     */
    RowImpl(Word<I> label, int rowId, int alphabetSize) {
        this(label, rowId);

        makeShort(alphabetSize);
    }

    /**
     * Constructor.
     *
     * @param label
     *         the label (label) of this row
     * @param rowId
     *         the unique row identifier
     */
    RowImpl(Word<I> label, int rowId) {
        this.label = label;
        this.rowId = rowId;
    }

    /**
     * Makes this row a short label row. This leads to a successor array being created. If this row already is a short
     * label row, nothing happens.
     *
     * @param initialAlphabetSize
     *         the size of the input alphabet.
     */
    void makeShort(int initialAlphabetSize) {
        if (lpIndex == -1) {
            return;
        }
        lpIndex = -1;
        this.successors = new ArrayStorage<>(initialAlphabetSize);
    }

    @Override
    public RowImpl<I> getSuccessor(int inputIdx) {
        return successors.get(inputIdx);
    }

    /**
     * Sets the successor row for this short label row and the given alphabet symbol (by index). If this is no short
     * label row, an exception might occur.
     *
     * @param inputIdx
     *         the index of the alphabet symbol.
     * @param succ
     *         the successor row
     */
    void setSuccessor(int inputIdx, RowImpl<I> succ) {
        successors.set(inputIdx, succ);
    }

    @Override
    public Word<I> getLabel() {
        return label;
    }

    @Override
    public int getRowId() {
        return rowId;
    }

    @Override
    public int getRowContentId() {
        return rowContentId;
    }

    /**
     * Sets the ID of the row contents.
     *
     * @param id
     *         the contents id
     */
    void setRowContentId(int id) {
        this.rowContentId = id;
    }

    @Override
    public boolean isShortPrefixRow() {
        return lpIndex == -1;
    }

    boolean hasContents() {
        return rowContentId != -1;
    }

    int getLpIndex() {
        return lpIndex;
    }

    void setLpIndex(int lpIndex) {
        this.lpIndex = lpIndex;
    }

    void ensureInputCapacity(int capacity) {
        this.successors.ensureCapacity(capacity);
    }
}
