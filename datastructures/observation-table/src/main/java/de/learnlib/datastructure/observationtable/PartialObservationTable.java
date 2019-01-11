package de.learnlib.datastructure.observationtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.oracle.OutputAndLocalInputs;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

public class PartialObservationTable<I, O> extends GenericObservationTable<I, Word<OutputAndLocalInputs<I, O>>> {

    private final transient GrowingAlphabet<I> alphabet;
    private Consumer<I> newAlphabetNotifier;

    public PartialObservationTable(Consumer<I> newAlphabetNotifier) {
        this(new SimpleAlphabet<>(), newAlphabetNotifier);
    }

    private PartialObservationTable(GrowingAlphabet<I> alphabet, Consumer<I> newAlphabetNotifier) {
        super(alphabet);
        this.alphabet = alphabet;
        this.newAlphabetNotifier = newAlphabetNotifier;
    }

    public List<List<Row<I>>> initialize(List<Word<I>> initialShortPrefixes,
                                         List<Word<I>> initialSuffixes,
                                         MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {
        if (allRows.size() > 0) {
            throw new IllegalStateException("Called initialize, but there are already rows present");
        }

        if (!checkPrefixClosed(initialShortPrefixes)) {
            throw new IllegalArgumentException("Initial short prefixes are not prefix-closed");
        }

        if (!initialShortPrefixes.get(0).isEmpty()) {
            throw new IllegalArgumentException("First initial short prefix MUST be the empty word!");
        }

        int numSuffixes = initialSuffixes.size();
        for (Word<I> suffix : initialSuffixes) {
            if (suffixSet.add(suffix)) {
                suffixes.add(suffix);
            }
        }

        int numPrefixes = alphabet.size() * initialShortPrefixes.size() + 1;

        List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queries = new ArrayList<>(numPrefixes * numSuffixes);

        // PASS 1: Add short prefix rows
        for (Word<I> sp : initialShortPrefixes) {
            createSpRow(sp);
            buildQueries(queries, sp, suffixes);
        }

        oracle.processQueries(queries);
        // get enabled inputs for initial state
        Iterator<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queryIt = queries.iterator();
        for (RowImpl<I> spRow : shortPrefixRows) {
            List<Word<OutputAndLocalInputs<I, O>>> rowContents = new ArrayList<>(numSuffixes);
            fetchResults(queryIt, rowContents, numSuffixes);
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
                RowImpl<I> succRow = rowMap.get(lp);
                if (succRow == null) {
                    succRow = createLpRow(lp);
                    buildQueries(queries, lp, suffixes);
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
                List<Word<OutputAndLocalInputs<I, O>>> rowContents = new ArrayList<>(numSuffixes);
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

    public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows,
                                              MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {
        List<RowImpl<I>> freshSpRows = new ArrayList<>();
        List<RowImpl<I>> freshLpRows = new ArrayList<>();

        for (Row<I> r : lpRows) {
            final RowImpl<I> row = allRows.get(r.getRowId());
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

            for (I sym : getEnabledInputs(row)) {
                checkForNewAlphabetSymbol(sym);

                Word<I> lp = prefix.append(sym);
                RowImpl<I> lpRow = rowMap.get(lp);
                if (lpRow == null) {
                    lpRow = createLpRow(lp);
                    freshLpRows.add(lpRow);
                }
                
                int i = alphabet.getSymbolIndex(sym);
                row.setSuccessor(i, lpRow);
            }
        }

        int numSuffixes = suffixes.size();

        int numFreshRows = freshSpRows.size() + freshLpRows.size();
        List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queries = new ArrayList<>(numFreshRows * numSuffixes);
        buildRowQueries(queries, freshSpRows, suffixes);
        buildRowQueries(queries, freshLpRows, suffixes);

        oracle.processQueries(queries);
        Iterator<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> queryIt = queries.iterator();

        for (RowImpl<I> row : freshSpRows) {
            List<Word<OutputAndLocalInputs<I, O>>> contents = new ArrayList<>(numSuffixes);
            fetchResults(queryIt, contents, numSuffixes);
            processContents(row, contents, true);
        }

        int numSpRows = numberOfDistinctRows();
        List<List<Row<I>>> unclosed = new ArrayList<>();

        for (RowImpl<I> row : freshLpRows) {
            List<Word<OutputAndLocalInputs<I, O>>> contents = new ArrayList<>(numSuffixes);
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
    public List<List<Row<I>>> addAlphabetSymbol(I symbol,
                                                final MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle) {
        throw new UnsupportedOperationException("remove via inheritance");
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
