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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialized observation table that creates new long-prefix rows depending on the available input symbols of the
 * corresponding short-prefix row.
 * <p>
 * The information about available inputs symbols is stored in the first (epsilon) column, despite this being an
 * observation table based on Mealy semantics (where an empty input always yields an empty output). The information
 * about available inputs are not fetched via conventional membership queries, but by querying the supplied provider.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Maren Geske
 * @author frohme
 */
public final class PartialObservationTable<I, O> extends AbstractObservationTable<I, Word<OutputAndLocalInputs<I, O>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialObservationTable.class);

    private final transient GrowingAlphabet<I> alphabet;
    private final transient Function<Word<I>, Collection<I>> enabledInputsProvider;
    private transient Consumer<I> newAlphabetNotifier;

    public PartialObservationTable(Consumer<I> newAlphabetNotifier,
                                   Function<Word<I>, Collection<I>> enabledInputsProvider) {
        this(new SimpleAlphabet<>(), newAlphabetNotifier, enabledInputsProvider);
    }

    private PartialObservationTable(GrowingAlphabet<I> alphabet,
                                    Consumer<I> newAlphabetNotifier,
                                    Function<Word<I>, Collection<I>> enabledInputsProvider) {
        super(alphabet);
        this.alphabet = alphabet;
        this.newAlphabetNotifier = newAlphabetNotifier;
        this.enabledInputsProvider = enabledInputsProvider;
    }

    public void setNewAlphabetNotifier(Consumer<I> newAlphabetNotifier) {
        this.newAlphabetNotifier = newAlphabetNotifier;
    }

    @Override
    public List<List<Row<I>>> initialize(List<Word<I>> initialShortPrefixes,
                                         List<Word<I>> initialSuffixes,
                                         MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {

        checkInitialShortPrefixes(initialShortPrefixes);

        final List<Word<I>> initialSuffixesWithEpsilon = new ArrayList<>(initialSuffixes.size() + 1);
        initialSuffixesWithEpsilon.add(Word.epsilon());
        initialSuffixesWithEpsilon.addAll(initialSuffixes);
        final List<Word<I>> suffixes = initializeSuffixes(initialSuffixesWithEpsilon);

        int numPrefixes = alphabet.size() * initialShortPrefixes.size() + 1;
        int numNonEmptySuffixes = suffixes.size() - 1;

        List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queries =
                new ArrayList<>(numPrefixes * numNonEmptySuffixes);

        // PASS 1: Add short prefix rows
        for (Word<I> sp : initialShortPrefixes) {
            createSpRow(sp);
            buildQueries(queries, sp, suffixes.subList(1, suffixes.size()));
        }

        oracle.processQueries(queries);
        // get enabled inputs for initial state
        Iterator<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queryIt = queries.iterator();
        for (RowImpl<I> spRow : shortPrefixRows) {
            List<Word<OutputAndLocalInputs<I, O>>> rowContents = new ArrayList<>(numNonEmptySuffixes + 1);

            rowContents.add(Word.fromLetter(new OutputAndLocalInputs<>(null,
                                                                       enabledInputsProvider.apply(spRow.getLabel()))));

            fetchResults(queryIt, rowContents, numNonEmptySuffixes);
            if (!processContents(spRow, rowContents, true)) {
                initialConsistencyCheckRequired = true;
            }
        }

        queries.clear();

        // PASS 2: Add missing long prefix rows
        for (RowImpl<I> spRow : shortPrefixRows) {
            Word<I> sp = spRow.getLabel();
            for (I sym : getEnabledInputs(spRow)) {

                checkForNewAlphabetSymbol(sym);

                Word<I> lp = sp.append(sym);
                RowImpl<I> succRow = getRow(lp);
                if (succRow == null) {
                    succRow = createLpRow(lp);
                    buildQueries(queries, lp, suffixes.subList(1, suffixes.size()));
                }

                int i = alphabet.getSymbolIndex(sym);
                spRow.setSuccessor(i, succRow);
            }
        }

        oracle.processQueries(queries);

        queryIt = queries.iterator();

        int distinctSpRows = numberOfDistinctRows();

        List<List<Row<I>>> unclosed = new ArrayList<>();

        for (RowImpl<I> spRow : shortPrefixRows) {
            for (int i = 0; i < alphabet.size(); i++) {
                RowImpl<I> succRow = spRow.getSuccessor(i);
                if (succRow == null || succRow.isShortPrefixRow()) {
                    continue;
                }
                List<Word<OutputAndLocalInputs<I, O>>> rowContents = new ArrayList<>(numNonEmptySuffixes + 1);
                rowContents.add(Word.fromLetter(new OutputAndLocalInputs<>(null,
                                                                           enabledInputsProvider.apply(succRow.getLabel()))));
                fetchResults(queryIt, rowContents, numNonEmptySuffixes);
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
    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows,
                                              MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {
        RowImpl<I> freshSpRow = null;
        List<RowImpl<I>> freshLpRows = new ArrayList<>();

        final List<Word<I>> suffixes = getSuffixes();
        final int numNonEmptySuffixes = suffixes.size() - 1;

        for (Row<I> r : lpRows) {
            final RowImpl<I> row = getRow(r.getRowId());
            if (row.isShortPrefixRow()) {
                if (row.hasContents()) {
                    continue;
                }
                freshSpRow = row;
            } else {
                makeShort(row);
                if (!row.hasContents()) {
                    freshSpRow = row;
                }
            }

            // we need to eagerly fetch new sp rows, because we rely on the epsilon column to detect the available
            // inputs in the next part
            if (freshSpRow != null) {
                final List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queries =
                        new ArrayList<>(numNonEmptySuffixes);
                buildRowQueries(queries, Collections.singletonList(freshSpRow), suffixes.subList(1, suffixes.size()));

                oracle.processQueries(queries);
                Iterator<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queryIt = queries.iterator();

                List<Word<OutputAndLocalInputs<I, O>>> contents = new ArrayList<>(numNonEmptySuffixes + 1);

                contents.add(Word.fromLetter(new OutputAndLocalInputs<>(null,
                                                                        enabledInputsProvider.apply(freshSpRow.getLabel()))));
                fetchResults(queryIt, contents, numNonEmptySuffixes);
                processContents(freshSpRow, contents, true);
            }

            Word<I> prefix = row.getLabel();

            for (I sym : getEnabledInputs(row)) {
                checkForNewAlphabetSymbol(sym);

                Word<I> lp = prefix.append(sym);
                RowImpl<I> lpRow = getRow(lp);
                if (lpRow == null) {
                    lpRow = createLpRow(lp);
                    freshLpRows.add(lpRow);
                }

                int i = alphabet.getSymbolIndex(sym);
                row.setSuccessor(i, lpRow);
            }
        }

        List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queries =
                new ArrayList<>(freshLpRows.size() * numNonEmptySuffixes);
        buildRowQueries(queries, freshLpRows, suffixes.subList(1, suffixes.size()));

        oracle.processQueries(queries);
        Iterator<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queryIt = queries.iterator();

        int numSpRows = numberOfDistinctRows();
        List<List<Row<I>>> unclosed = new ArrayList<>();

        for (RowImpl<I> row : freshLpRows) {
            List<Word<OutputAndLocalInputs<I, O>>> contents = new ArrayList<>(numNonEmptySuffixes + 1);

            contents.add(Word.fromLetter(new OutputAndLocalInputs<>(null,
                                                                    enabledInputsProvider.apply(row.getLabel()))));
            fetchResults(queryIt, contents, numNonEmptySuffixes);
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
    public List<List<Row<I>>> addAlphabetSymbol(I symbol,
                                                final MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {
        LOGGER.info("Adding new symbols to a system, which already exposes its available actions has no effect");
        LOGGER.info("Skipping ...");

        return Collections.emptyList();
    }

    private void checkForNewAlphabetSymbol(I i) {
        if (!alphabet.containsSymbol(i)) {

            alphabet.addSymbol(i);
            final int newAlphabetSize = alphabet.size();

            for (RowImpl<I> prefix : shortPrefixRows) {
                prefix.ensureInputCapacity(newAlphabetSize);
            }

            newAlphabetNotifier.accept(i);
        }
    }

    private Collection<I> getEnabledInputs(RowImpl<I> row) {
        Word<OutputAndLocalInputs<I, O>> enabledInputs = cellContents(row, 0);
        return enabledInputs.getSymbol(0).getLocalInputs();
    }

}
