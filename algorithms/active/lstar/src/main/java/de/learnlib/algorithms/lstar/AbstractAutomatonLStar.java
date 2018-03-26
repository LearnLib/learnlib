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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.algorithm.feature.ResumableLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SymbolHidingAlphabet;

/**
 * Abstract base class for algorithms that produce (subclasses of) {@link MutableDeterministic} automata.
 * <p>
 * This class provides the L*-style hypothesis construction. Implementing classes solely have to specify how state and
 * transition properties should be derived.
 *
 * @param <A>
 *         automaton type, must be a subclass of {@link MutableDeterministic}
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @author Malte Isberner
 */
public abstract class AbstractAutomatonLStar<A, I, D, S, T, SP, TP, AI extends MutableDeterministic<S, I, T, SP, TP> & GrowableAlphabetAutomaton<I>>
        extends AbstractLStar<A, I, D> implements ResumableLearner<AutomatonLStarState<I, D, AI, S>> {

    protected AI internalHyp;
    protected List<StateInfo<S, I>> stateInfos = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the learning oracle
     */
    protected AbstractAutomatonLStar(Alphabet<I> alphabet, MembershipOracle<I, D> oracle, AI internalHyp) {
        super(SymbolHidingAlphabet.wrapIfMutable(alphabet), oracle);
        this.internalHyp = internalHyp;
        internalHyp.clear();
    }

    @Override
    public A getHypothesisModel() {
        return exposeInternalHypothesis();
    }

    protected abstract A exposeInternalHypothesis();

    @Override
    public final void startLearning() {
        super.startLearning();
        updateInternalHypothesis();
    }

    /**
     * Performs the L*-style hypothesis construction. For creating states and transitions, the {@link
     * #stateProperty(ObservationTable, Row)} and {@link #transitionProperty(ObservationTable, Row, int)} methods are
     * used to derive the respective properties.
     */
    protected void updateInternalHypothesis() {
        if (!table.isInitialized()) {
            throw new IllegalStateException("Cannot update internal hypothesis: not initialized");
        }

        int oldStates = internalHyp.size();
        int numDistinct = table.numberOfDistinctRows();

        int newStates = numDistinct - oldStates;

        stateInfos.addAll(CollectionsUtil.nullList(newStates));

        // TODO: Is there a quicker way than iterating over *all* rows?
        // FIRST PASS: Create new hypothesis states
        for (Row<I> sp : table.getShortPrefixRows()) {
            int id = sp.getRowContentId();
            StateInfo<S, I> info = stateInfos.get(id);
            if (info != null) {
                // State from previous hypothesis, property might have changed
                if (info.getRow() == sp) {
                    internalHyp.setStateProperty(info.getState(), stateProperty(table, sp));
                }
                continue;
            }

            S state = createState((id == 0), sp);

            stateInfos.set(id, new StateInfo<>(sp, state));
        }

        // SECOND PASS: Create hypothesis transitions
        for (StateInfo<S, I> info : stateInfos) {
            Row<I> sp = info.getRow();
            S state = info.getState();

            for (int i = 0; i < alphabet.size(); i++) {
                I input = alphabet.getSymbol(i);

                Row<I> succ = sp.getSuccessor(i);
                int succId = succ.getRowContentId();

                S succState = stateInfos.get(succId).getState();

                setTransition(state, input, succState, sp, i);
            }
        }
    }

    /**
     * Derives a state property from the corresponding row.
     *
     * @param table
     *         the current observation table
     * @param stateRow
     *         the row for which the state is created
     *
     * @return the state property of the corresponding state
     */
    protected abstract SP stateProperty(ObservationTable<I, D> table, Row<I> stateRow);

    protected S createState(boolean initial, Row<I> row) {
        SP prop = stateProperty(table, row);
        if (initial) {
            return internalHyp.addInitialState(prop);
        }
        return internalHyp.addState(prop);
    }

    protected void setTransition(S from, I input, S to, Row<I> fromRow, int inputIdx) {
        TP prop = transitionProperty(table, fromRow, inputIdx);
        internalHyp.setTransition(from, input, to, prop);
    }

    /**
     * Derives a transition property from the corresponding transition.
     * <p>
     * N.B.: Not the transition row is passed to this method, but the row for the outgoing state. The transition row can
     * be retrieved using {@link Row#getSuccessor(int)}.
     *
     * @param stateRow
     *         the row for the source state
     * @param inputIdx
     *         the index of the input symbol to consider
     *
     * @return the transition property of the corresponding transition
     */
    protected abstract TP transitionProperty(ObservationTable<I, D> table, Row<I> stateRow, int inputIdx);

    @Override
    protected final void doRefineHypothesis(DefaultQuery<I, D> ceQuery) {
        refineHypothesisInternal(ceQuery);
        updateInternalHypothesis();
    }

    protected void refineHypothesisInternal(DefaultQuery<I, D> ceQuery) {
        super.doRefineHypothesis(ceQuery);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (alphabet.containsSymbol(symbol)) {
            return;
        }

        this.internalHyp.addAlphabetSymbol(symbol);

        SymbolHidingAlphabet.runWhileHiding(alphabet, symbol, () -> super.addAlphabetSymbol(symbol));

        this.updateInternalHypothesis();
    }

    @Override
    public AutomatonLStarState<I, D, AI, S> suspend() {
        return new AutomatonLStarState<>(table, internalHyp, stateInfos);
    }

    @Override
    public void resume(final AutomatonLStarState<I, D, AI, S> state) {
        this.table = state.getObservationTable();
        this.table.setInputAlphabet(alphabet);
        this.internalHyp = state.getHypothesis();
        this.stateInfos = state.getStateInfos();
    }

    static final class StateInfo<S, I> implements Serializable {

        private final Row<I> row;
        private final S state;

        StateInfo(Row<I> row, S state) {
            this.row = row;
            this.state = state;
        }

        public Row<I> getRow() {
            return row;
        }

        public S getState() {
            return state;
        }

        // IDENTITY SEMANTICS!
    }
}
