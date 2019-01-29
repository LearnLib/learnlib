/* Copyright (C) 2013-2019 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * A generic observation table, that implements the classical OT semantics for fully specified systems.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public final class GenericObservationTable<I, D> extends AbstractObservationTable<I, D> {

    private transient int alphabetSize;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet.
     */
    public GenericObservationTable(Alphabet<I> alphabet) {
        super(alphabet);
        this.alphabetSize = alphabet.size();
    }

    @Override
    public List<List<Row<I>>> initialize(List<Word<I>> initialShortPrefixes,
                                         List<Word<I>> initialSuffixes,
                                         MembershipOracle<I, D> oracle) {

        checkInitialShortPrefixes(initialShortPrefixes);

        final List<Word<I>> suffixes = initializeSuffixes(initialSuffixes);

        final Alphabet<I> alphabet = getInputAlphabet();
        int numPrefixes = alphabet.size() * initialShortPrefixes.size() + 1;
        int numSuffixes = suffixes.size();

        List<DefaultQuery<I, D>> queries = new ArrayList<>(numPrefixes * numSuffixes);

        // PASS 1: Add short prefix rows
        for (Word<I> sp : initialShortPrefixes) {
            createSpRow(sp);
            buildQueries(queries, sp, suffixes);
        }

        // PASS 2: Add missing long prefix rows
        for (RowImpl<I> spRow : shortPrefixRows) {
            Word<I> sp = spRow.getLabel();
            for (int i = 0; i < alphabet.size(); i++) {
                I sym = alphabet.getSymbol(i);
                Word<I> lp = sp.append(sym);
                RowImpl<I> succRow = getRow(lp);
                if (succRow == null) {
                    succRow = createLpRow(lp);
                    buildQueries(queries, lp, suffixes);
                }
                spRow.setSuccessor(i, succRow);
            }
        }

        oracle.processQueries(queries);

        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();

        for (RowImpl<I> spRow : shortPrefixRows) {
            List<D> rowContents = new ArrayList<>(numSuffixes);
            fetchResults(queryIt, rowContents, numSuffixes);
            if (!processContents(spRow, rowContents, true)) {
                initialConsistencyCheckRequired = true;
            }
        }

        int distinctSpRows = numberOfDistinctRows();

        List<List<Row<I>>> unclosed = new ArrayList<>();

        for (RowImpl<I> spRow : shortPrefixRows) {
            for (int i = 0; i < alphabet.size(); i++) {
                RowImpl<I> succRow = spRow.getSuccessor(i);
                if (succRow.isShortPrefixRow()) {
                    continue;
                }
                List<D> rowContents = new ArrayList<>(numSuffixes);
                fetchResults(queryIt, rowContents, numSuffixes);
                if (processContents(succRow, rowContents, false)) {
                    unclosed.add(new ArrayList<>());
                }

                int id = succRow.getRowContentId();

                if (id >= distinctSpRows) {
                    unclosed.get(id - distinctSpRows).add(succRow);
                }
            }
        }

        return unclosed;
    }

    @Override
    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows, MembershipOracle<I, D> oracle) {
        List<RowImpl<I>> freshSpRows = new ArrayList<>();
        List<RowImpl<I>> freshLpRows = new ArrayList<>();
        Alphabet<I> alphabet = getInputAlphabet();

        for (Row<I> r : lpRows) {
            final RowImpl<I> row = getRow(r.getRowId());
            if (row.isShortPrefixRow()) {
                if (row.hasContents()) {
                    continue;
                }
                freshSpRows.add(row);
            } else {
                makeShort(row);
                if (!row.hasContents()) {
                    freshSpRows.add(row);
                }
            }

            Word<I> prefix = row.getLabel();

            for (int i = 0; i < alphabet.size(); i++) {
                I sym = alphabet.getSymbol(i);
                Word<I> lp = prefix.append(sym);
                RowImpl<I> lpRow = getRow(lp);
                if (lpRow == null) {
                    lpRow = createLpRow(lp);
                    freshLpRows.add(lpRow);
                }
                row.setSuccessor(i, lpRow);
            }
        }

        List<Word<I>> suffixes = getSuffixes();
        int numSuffixes = suffixes.size();

        int numFreshRows = freshSpRows.size() + freshLpRows.size();
        List<DefaultQuery<I, D>> queries = new ArrayList<>(numFreshRows * numSuffixes);
        buildRowQueries(queries, freshSpRows, suffixes);
        buildRowQueries(queries, freshLpRows, suffixes);

        oracle.processQueries(queries);
        Iterator<DefaultQuery<I, D>> queryIt = queries.iterator();

        for (RowImpl<I> row : freshSpRows) {
            List<D> contents = new ArrayList<>(numSuffixes);
            fetchResults(queryIt, contents, numSuffixes);
            processContents(row, contents, true);
        }

        int numSpRows = numberOfDistinctRows();
        List<List<Row<I>>> unclosed = new ArrayList<>();

        for (RowImpl<I> row : freshLpRows) {
            List<D> contents = new ArrayList<>(numSuffixes);
            fetchResults(queryIt, contents, numSuffixes);
            if (processContents(row, contents, false)) {
                unclosed.add(new ArrayList<>());
            }

            int id = row.getRowContentId();
            if (id >= numSpRows) {
                unclosed.get(id - numSpRows).add(row);
            }
        }

        return unclosed;
    }

    @Override
    public List<List<Row<I>>> addAlphabetSymbol(I symbol, final MembershipOracle<I, D> oracle) {

        final Alphabet<I> alphabet = getInputAlphabet();

        if (!alphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(alphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = alphabet.size();

        if (this.isInitialized() && this.alphabetSize < newAlphabetSize) {
            this.alphabetSize = newAlphabetSize;
            final int newSymbolIdx = alphabet.getSymbolIndex(symbol);

            final List<RowImpl<I>> shortPrefixes = shortPrefixRows;
            final List<RowImpl<I>> newLongPrefixes = new ArrayList<>(shortPrefixes.size());

            for (RowImpl<I> prefix : shortPrefixes) {
                prefix.ensureInputCapacity(newAlphabetSize);

                final Word<I> newLongPrefix = prefix.getLabel().append(symbol);
                final RowImpl<I> longPrefixRow = createLpRow(newLongPrefix);

                newLongPrefixes.add(longPrefixRow);
                prefix.setSuccessor(newSymbolIdx, longPrefixRow);
            }

            final int numLongPrefixes = newLongPrefixes.size();
            final int numSuffixes = this.numberOfSuffixes();
            final List<DefaultQuery<I, D>> queries = new ArrayList<>(numLongPrefixes * numSuffixes);

            buildRowQueries(queries, newLongPrefixes, getSuffixes());
            oracle.processQueries(queries);

            final Iterator<DefaultQuery<I, D>> queryIterator = queries.iterator();
            final List<List<Row<I>>> result = new ArrayList<>(numLongPrefixes);

            for (RowImpl<I> row : newLongPrefixes) {
                final List<D> contents = new ArrayList<>(numSuffixes);

                fetchResults(queryIterator, contents, numSuffixes);

                if (processContents(row, contents, false)) {
                    result.add(Collections.singletonList(row));
                }
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
