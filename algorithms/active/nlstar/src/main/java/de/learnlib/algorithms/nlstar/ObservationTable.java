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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * The observation table implementation for the {@link NLStarLearner NL* algorithm}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class ObservationTable<I> {

    private final Alphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> oracle;

    private final List<Row<I>> upperRows = new ArrayList<>();
    private final List<Row<I>> allRows = new ArrayList<>();

    private final List<Row<I>> newUppers = new ArrayList<>();
    private final List<Row<I>> newRows = new ArrayList<>();

    private final List<Word<I>> suffixes = new ArrayList<>();
    private final Set<Word<I>> suffixSet = new HashSet<>();

    private final List<Row<I>> upperPrimes = new ArrayList<>();

    public ObservationTable(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
        this.alphabet = alphabet;
        this.oracle = oracle;
    }

    public List<List<Row<I>>> initialize() {
        if (!suffixes.isEmpty()) {
            throw new IllegalStateException();
        }

        Row<I> row = createRow(Word.epsilon());

        makeUpper(row);

        return addSuffix(Word.epsilon());
    }

    private Row<I> createRow(Word<I> prefix) {
        Row<I> row = new Row<>(prefix);
        allRows.add(row);
        newRows.add(row);
        return row;
    }

    public List<List<Row<I>>> addSuffix(Word<I> suffixToAdd) {
        return addSuffixes(Collections.singletonList(suffixToAdd));
    }

    private List<List<Row<I>>> makeUpper(Row<I> row) {
        return makeUpper(Collections.singletonList(row));
    }

    public List<List<Row<I>>> makeUpper(List<Row<I>> rows) {
        List<Row<I>> newRows = new ArrayList<>(rows.size() * alphabet.size());
        for (Row<I> row : rows) {
            makeShort(row);
            Word<I> prefix = row.getPrefix();

            for (int i = 0; i < alphabet.size(); i++) {
                I sym = alphabet.getSymbol(i);
                Word<I> newPrefix = prefix.append(sym);
                Row<I> newRow = createRow(newPrefix);
                row.setSuccessorRow(i, newRow);
                newRows.add(newRow);
            }
        }

        if (suffixes.isEmpty()) {
            return Collections.emptyList();
        }

        int numSuffixes = suffixes.size();

        List<DefaultQuery<I, Boolean>> queries = new ArrayList<>(newRows.size() * numSuffixes);

        for (Row<I> newRow : newRows) {
            for (Word<I> suffix : suffixes) {
                queries.add(new DefaultQuery<>(newRow.getPrefix(), suffix));
            }
        }

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, Boolean>> queryIt = queries.iterator();

        for (Row<I> newRow : newRows) {
            newRow.fetchContents(queryIt, 0, numSuffixes);
        }

        return updateMetadata();

    }

    public List<List<Row<I>>> addSuffixes(List<? extends Word<I>> suffixesToAdd) {
        List<Word<I>> newSuffixes = new ArrayList<>();

        int oldNumSuffixes = suffixes.size();

        for (Word<I> suffix : suffixesToAdd) {
            if (suffixSet.add(suffix)) {
                suffixes.add(suffix);
                newSuffixes.add(suffix);
            }
        }

        if (newSuffixes.isEmpty()) {
            return Collections.emptyList();
        }

        int numNewSuffixes = newSuffixes.size();

        List<DefaultQuery<I, Boolean>> queries = new ArrayList<>(allRows.size() * numNewSuffixes);

        for (Row<I> row : allRows) {
            Word<I> prefix = row.getPrefix();
            for (Word<I> suffix : newSuffixes) {
                queries.add(new DefaultQuery<>(prefix, suffix));
            }
        }

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, Boolean>> queryIt = queries.iterator();

        for (Row<I> row : allRows) {
            row.fetchContents(queryIt, oldNumSuffixes, numNewSuffixes);
        }

        return updateMetadata();
    }

    private void makeShort(Row<I> row) {
        row.makeShort(upperRows.size(), alphabet.size());
        upperRows.add(row);
        newUppers.add(row);
    }

    private List<List<Row<I>>> updateMetadata() {

        // Update coverage information
        for (Row<I> row : allRows) {
            if (row.isShortPrefixRow()) {
                if (row.isNew()) {
                    row.updateCovered(upperRows);
                } else {
                    row.updateCovered(newUppers);
                }
            } else {
                if (row.isNew()) {
                    row.updateCovered(allRows);
                } else {
                    row.updateCovered(newRows);
                }
            }
        }

        newRows.clear();
        newUppers.clear();

        upperPrimes.clear();

        Map<BitSet, List<Row<I>>> primeContents = new HashMap<>();
        List<List<Row<I>>> allUnclosed = new ArrayList<>();

        for (Row<I> row : allRows) {
            boolean prime = row.checkPrime();

            if (prime) {
                if (row.isShortPrefixRow()) {
                    upperPrimes.add(row);
                } else {
                    List<Row<I>> unclosedClass = primeContents.get(row.getContents());
                    if (unclosedClass == null) {
                        unclosedClass = new ArrayList<>();
                        allUnclosed.add(unclosedClass);
                        primeContents.put(row.getContents(), unclosedClass);
                    }
                    unclosedClass.add(row);
                }
            }
        }

        return allUnclosed;
    }

    public Word<I> getSuffix(int suffixIdx) {
        return suffixes.get(suffixIdx);
    }

    public List<Row<I>> getCoveredRows(Row<I> coveringRow) {
        return coveringRow.getCoveredRows();
    }

    public Row<I> getUpperRow(int index) {
        return upperRows.get(index);
    }

    public List<Row<I>> getUpperRows() {
        return upperRows;
    }

    public List<Row<I>> getUpperPrimes() {
        return upperPrimes;
    }

    public int getNumUpperRows() {
        return upperRows.size();
    }

    public Inconsistency<I> findInconsistency() {
        for (Row<I> row1 : upperRows) {
            for (Row<I> row2 : row1.getCoveredRows()) {
                assert row2.isShortPrefixRow();

                for (int i = 0; i < alphabet.size(); i++) {
                    Row<I> row1succ = row1.getSuccessorRow(i);
                    Row<I> row2succ = row2.getSuccessorRow(i);

                    for (int j = 0; j < suffixes.size(); j++) {
                        if (!row1succ.getContent(j) && row2succ.getContent(j)) {
                            return new Inconsistency<>(row1, row2, i, j);
                        }
                    }
                }
            }
        }

        return null;
    }
}
