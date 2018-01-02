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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Word;

/**
 * A single row in the {@link ObservationTable} for {@link NLStarLearner NL*}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class Row<I> {

    private final Word<I> prefix;
    private final BitSet contents = new BitSet();
    private int upperId = -1;
    private Row<I>[] successorRows;
    // If this is a row in the upper part of the table,
    // this is the lists of all rows in the upper part that
    // are covered by this row.
    // Otherwise, this is the list of all rows in the whole
    // table that are covered by this row.
    private List<Row<I>> coveredRows;

    // Indicates if this row is prime. A row is prime if the join over
    // all rows in the coveredRows list
    private boolean prime;

    public Row(Word<I> prefix) {
        this.prefix = prefix;
    }

    public boolean getContent(int index) {
        return contents.get(index);
    }

    public boolean isNew() {
        return coveredRows == null;
    }

    public boolean isPrime() {
        return prime;
    }

    public int getUpperId() {
        return upperId;
    }

    public Word<I> getPrefix() {
        return prefix;
    }

    public BitSet getContents() {
        return contents;
    }

    @SuppressWarnings("unchecked")
    void makeShort(int id, int alphabetSize) {
        this.upperId = id;
        this.successorRows = new Row[alphabetSize];
    }

    Row<I> getSuccessorRow(int succIdx) {
        return successorRows[succIdx];
    }

    void setSuccessorRow(int succIdx, Row<I> row) {
        successorRows[succIdx] = row;
    }

    void updateCovered(List<Row<I>> newRows) {
        List<Row<I>> oldCovered = coveredRows;

        this.coveredRows = new ArrayList<>();
        if (oldCovered != null) {
            checkAndAddCovered(oldCovered);
        }
        checkAndAddCovered(newRows);
    }

    private void checkAndAddCovered(List<Row<I>> rowList) {
        for (Row<I> row : rowList) {
            if (row != this) {
                if (isShortPrefixRow()) {
                    if (row.isShortPrefixRow() && covers(row)) {
                        coveredRows.add(row);
                    }
                } else if (covers(row)) {
                    coveredRows.add(row);
                }
            }
        }
    }

    public boolean isShortPrefixRow() {
        return (successorRows != null);
    }

    boolean covers(Row<I> other) {
        BitSet c = (BitSet) contents.clone();
        c.or(other.contents);
        return contents.equals(c);
    }

    public List<Row<I>> getCoveredRows() {
        return coveredRows;
    }

    boolean checkPrime() {
        if (coveredRows.isEmpty()) {
            prime = true;
        } else {
            BitSet aggContents = new BitSet();

            for (Row<I> covered : coveredRows) {
                if (covered.isShortPrefixRow() || !contents.equals(covered.contents)) {
                    aggContents.or(covered.contents);
                }
            }

            prime = !contents.equals(aggContents);
        }

        return prime;
    }

    void fetchContents(Iterator<? extends DefaultQuery<I, Boolean>> queryIt, int offset, int num) {
        int idx = offset;

        for (int i = 0; i < num; i++) {
            assert queryIt.hasNext();

            boolean value = queryIt.next().getOutput();
            if (value) {
                contents.set(idx);
            }
            idx++;
        }
    }
}
