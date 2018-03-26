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
package de.learnlib.algorithms.lstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.api.algorithm.feature.GlobalSuffixLearner;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.GenericObservationTable;
import de.learnlib.datastructure.observationtable.Inconsistency;
import de.learnlib.datastructure.observationtable.OTLearner;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.util.MQUtil;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * An abstract base class for L*-style algorithms.
 * <p>
 * This class implements basic management features (table, alphabet, oracle) and the main loop of alternating
 * completeness and consistency checks. It does not take care of choosing how to initialize the table and hypothesis
 * construction.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public abstract class AbstractLStar<A, I, D>
        implements OTLearner<A, I, D>, GlobalSuffixLearner<A, I, D>, SupportsGrowingAlphabet<I> {

    protected Alphabet<I> alphabet;
    protected final MembershipOracle<I, D> oracle;
    protected GenericObservationTable<I, D> table;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet.
     * @param oracle
     *         the membership oracle.
     */
    protected AbstractLStar(Alphabet<I> alphabet, MembershipOracle<I, D> oracle) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.table = new GenericObservationTable<>(alphabet);
    }

    @Override
    public void startLearning() {
        List<Word<I>> prefixes = initialPrefixes();
        List<Word<I>> suffixes = initialSuffixes();
        List<List<Row<I>>> initialUnclosed = table.initialize(prefixes, suffixes, oracle);

        completeConsistentTable(initialUnclosed, table.isInitialConsistencyCheckRequired());
    }

    @Override
    public final boolean refineHypothesis(DefaultQuery<I, D> ceQuery) {
        if (!MQUtil.isCounterexample(ceQuery, hypothesisOutput())) {
            return false;
        }
        int oldDistinctRows = table.numberOfDistinctRows();
        doRefineHypothesis(ceQuery);
        assert (table.numberOfDistinctRows() > oldDistinctRows);
        return true;
    }

    protected abstract SuffixOutput<I, D> hypothesisOutput();

    protected void doRefineHypothesis(DefaultQuery<I, D> ceQuery) {
        List<List<Row<I>>> unclosed = incorporateCounterExample(ceQuery);
        completeConsistentTable(unclosed, true);
    }

    /**
     * Incorporates the information provided by a counterexample into the observation data structure.
     *
     * @param ce
     *         the query which contradicts the hypothesis
     *
     * @return the rows (equivalence classes) which became unclosed by adding the information.
     */
    protected List<List<Row<I>>> incorporateCounterExample(DefaultQuery<I, D> ce) {
        return ObservationTableCEXHandlers.handleClassicLStar(ce, table, oracle);
    }

    protected List<Word<I>> initialPrefixes() {
        return Collections.singletonList(Word.<I>epsilon());
    }

    /**
     * Returns the list of initial suffixes which are used to initialize the table.
     *
     * @return the list of initial suffixes.
     */
    protected abstract List<Word<I>> initialSuffixes();

    /**
     * Iteratedly checks for unclosedness and inconsistencies in the table, and fixes any occurrences thereof. This
     * process is repeated until the observation table is both closed and consistent.
     *
     * @param unclosed
     *         the unclosed rows (equivalence classes) to start with.
     */
    protected boolean completeConsistentTable(List<List<Row<I>>> unclosed, boolean checkConsistency) {
        boolean refined = false;
        List<List<Row<I>>> unclosedIter = unclosed;
        do {
            while (!unclosedIter.isEmpty()) {
                List<Row<I>> closingRows = selectClosingRows(unclosedIter);
                unclosedIter = table.toShortPrefixes(closingRows, oracle);
                refined = true;
            }

            if (checkConsistency) {
                Inconsistency<I> incons;

                do {
                    incons = table.findInconsistency();
                    if (incons != null) {
                        Word<I> newSuffix = analyzeInconsistency(incons);
                        unclosedIter = table.addSuffix(newSuffix, oracle);
                    }
                } while (unclosedIter.isEmpty() && (incons != null));
            }
        } while (!unclosedIter.isEmpty());

        return refined;
    }

    /**
     * This method selects a set of rows to use for closing the table. It receives as input a list of row lists, such
     * that each (inner) list contains long prefix rows with (currently) identical contents, which have no matching
     * short prefix row. The outer list is the list of all those equivalence classes.
     *
     * @param unclosed
     *         a list of equivalence classes of unclosed rows.
     *
     * @return a list containing a representative row from each class to move to the short prefix part.
     */
    protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
        List<Row<I>> closingRows = new ArrayList<>(unclosed.size());

        for (List<Row<I>> rowList : unclosed) {
            closingRows.add(rowList.get(0));
        }

        return closingRows;
    }

    /**
     * Analyzes an inconsistency. This analysis consists in determining the column in which the two successor rows
     * differ.
     *
     * @param incons
     *         the inconsistency description
     *
     * @return the suffix to add in order to fix the inconsistency
     */
    protected Word<I> analyzeInconsistency(Inconsistency<I> incons) {
        int inputIdx = alphabet.getSymbolIndex(incons.getSymbol());

        Row<I> succRow1 = incons.getFirstRow().getSuccessor(inputIdx);
        Row<I> succRow2 = incons.getSecondRow().getSuccessor(inputIdx);

        int numSuffixes = table.getSuffixes().size();

        for (int i = 0; i < numSuffixes; i++) {
            D val1 = table.cellContents(succRow1, i), val2 = table.cellContents(succRow2, i);
            if (!Objects.equals(val1, val2)) {
                I sym = alphabet.getSymbol(inputIdx);
                Word<I> suffix = table.getSuffixes().get(i);
                return suffix.prepend(sym);
            }
        }

        throw new IllegalArgumentException("Bogus inconsistency");
    }

    @Override
    public Collection<Word<I>> getGlobalSuffixes() {
        return Collections.unmodifiableCollection(table.getSuffixes());
    }

    @Override
    public boolean addGlobalSuffixes(Collection<? extends Word<I>> newGlobalSuffixes) {
        List<List<Row<I>>> unclosed = table.addSuffixes(newGlobalSuffixes, oracle);
        if (unclosed.isEmpty()) {
            return false;
        }
        return completeConsistentTable(unclosed, false);
    }

    @Override
    public ObservationTable<I, D> getObservationTable() {
        return table;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        final List<List<Row<I>>> unclosed = this.table.addAlphabetSymbol(symbol, oracle);

        // since we share the alphabet instance with our observation table, our alphabet might have already been updated
        // (if it was already a GrowableAlphabet)
        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        }

        completeConsistentTable(unclosed, true);
    }
}
