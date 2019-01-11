//package de.learnlib.algorithms.lstar.mealy;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import com.github.misberner.buildergen.annotations.GenerateBuilder;
//import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
//import de.learnlib.algorithms.lstar.AbstractLStar;
//import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
//import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
//import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
//import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
//import de.learnlib.api.oracle.MembershipOracle;
//import de.learnlib.api.query.DefaultQuery;
//import de.learnlib.datastructure.observationtable.ObservationTable;
//import de.learnlib.datastructure.observationtable.PartialObservationTable;
//import de.learnlib.datastructure.observationtable.Row;
//import net.automatalib.automata.concepts.SuffixOutput;
//import net.automatalib.automata.transducers.MealyMachine;
//import net.automatalib.automata.transducers.OutputAndEnabledInputs;
//import net.automatalib.automata.transducers.SLAMealyMachine;
//import net.automatalib.automata.transducers.impl.compact.CompactMealy;
//import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
//import net.automatalib.commons.util.collections.CollectionsUtil;
//import net.automatalib.words.Alphabet;
//import net.automatalib.words.Word;
//import net.automatalib.words.impl.SimpleAlphabet;
//
//// TODO maybe insert resumable
//public class PartialOTLearningAlgorithm<I, O>
////        extends AbstractExtensibleAutomatonLStar<MealyMachine<?, I, ?, O>, I, Word<OutputAndEnabledInputs<I, O>, Integer, CompactMealyTransition<O>, Void, O, CompactMealy<I, O>>> //implements ResumableLearner<AutomatonLStarState<I, Word<OutputAndEnabledInputs<I,O>>, CompactMealy<I, O>, Integer>>,
//
//        extends AbstractLStar<MealyMachine<?, I, ?, O>, I, Word<OutputAndEnabledInputs<I, O>>> //implements ResumableLearner<AutomatonLStarState<I, Word<OutputAndEnabledInputs<I,O>>, CompactMealy<I, O>, Integer>>,
//        {
//
//    private final List<OutputAndEnabledInputs<I,O>> outputTable = new ArrayList<>();
//    protected final ObservationTableCEXHandler<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> cexHandler;
//    protected final ClosingStrategy<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> closingStrategy;
//    protected final List<Word<I>> initialPrefixes;
//    protected final List<Word<I>> initialSuffixes;
//    protected MealyMachine<?, I, ?, O> internalHyp;
//    protected List<StateInfo<Integer, I>> stateInfos = new ArrayList<>();
//
//    public PartialOTLearningAlgorithm(
//                                MembershipOracle<I, Word<OutputAndEnabledInputs<I,O>>> oracle,
//                                List<Word<I>> initialSuffixes,
//                                ObservationTableCEXHandler<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> cexHandler,
//                                ClosingStrategy<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> closingStrategy) {
//        this(oracle, Collections.singletonList(Word.epsilon()), initialSuffixes, cexHandler, closingStrategy);
//    }
//
//    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
//    public PartialOTLearningAlgorithm(
//                                MembershipOracle<I, Word<OutputAndEnabledInputs<I,O>>> oracle,
//                                List<Word<I>> initialPrefixes,
//                                List<Word<I>> initialSuffixes,
//                                ObservationTableCEXHandler<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> cexHandler,
//                                ClosingStrategy<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> closingStrategy) {
//        super(new SimpleAlphabet<>(), oracle);
//        this.table = new PartialObservationTable<I, O>(alphabet, sym -> addSymbol(sym));
//        this.internalHyp = new CompactMealy<>(alphabet);
//        this.initialPrefixes = initialPrefixes;
//        this.initialSuffixes = new ArrayList<>();
//        this.initialSuffixes.add(Word.epsilon());
//        this.initialSuffixes.addAll(LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes, alphabet, cexHandler.needsConsistencyCheck()));
//        this.cexHandler = cexHandler;
//        this.closingStrategy = closingStrategy;
//    }
//
//    protected Alphabet<I> addSymbol(I sym){
//        internalHyp.addAlphabetSymbol(sym);
//        this.alphabet = internalHyp.getInputAlphabet();
//
//        return this.alphabet;
//    }
//
//    @Override
//    public MealyMachine<?, I, ?, O> getHypothesisModel() {
//        return internalHyp;
//    }
//
//    protected final void doRefineHypothesis(DefaultQuery<I, Word<OutputAndEnabledInputs<I,O>>> ceQuery) {
//        refineHypothesisInternal(ceQuery);
//        updateInternalHypothesis();
//    }
//
//    /**
//     * Performs the L*-style hypothesis construction. For creating states and transitions, the {@link
//     * #stateProperty(ObservationTable, Row)} and {@link #transitionProperty(ObservationTable, Row, int)} methods are
//     * used to derive the respective properties.
//     */
//    protected void updateInternalHypothesis() {
//        updateOutputs();
//        if (!table.isInitialized()) {
//            throw new IllegalStateException("Cannot update internal hypothesis: not initialized");
//        }
//
//        int oldStates = internalHyp.size();
//        int numDistinct = table.numberOfDistinctRows();
//
//        int newStates = numDistinct - oldStates;
//
//        stateInfos.addAll(CollectionsUtil.nullList(newStates));
//
//        // FIRST PASS: Create new hypothesis states
//        for (Row<I> sp : table.getShortPrefixRows()) {
//            int id = sp.getRowContentId();
//            StateInfo<Integer, I> info = stateInfos.get(id);
//            if (info != null) {
//                // State from previous hypothesis, property might have changed
//                if (info.getRow() == sp) {
//                    internalHyp.setStateProperty(info.getState(), stateProperty(table, sp));
//                }
//                continue;
//            }
//
//            Integer state = createState((id == 0), sp);
//
//            stateInfos.set(id, new StateInfo<>(sp, state));
//        }
//
//        // SECOND PASS: Create hypothesis transitions
//        for (StateInfo<Integer, I> info : stateInfos) {
//            Row<I> sp = info.getRow();
//            Integer state = info.getState();
//
//            for (int i = 0; i < alphabet.size(); i++) {
//                I input = alphabet.getSymbol(i);
//
//                Row<I> succ = sp.getSuccessor(i);
//
//                if(succ == null)
//                    continue;
//
//                int succId = succ.getRowContentId();
//
//                Integer succState = stateInfos.get(succId).getState();
//
//                setTransition(state, input, succState, sp, i);
//            }
//        }
//    }
//
//    /**
//     * Derives a state property from the corresponding row.
//     *
//     * @param table
//     *         the current observation table
//     * @param stateRow
//     *         the row for which the state is created
//     *
//     * @return the state property of the corresponding state
//     */
//    protected Void stateProperty(ObservationTable<I, Word<OutputAndEnabledInputs<I,O>>> table, Row<I> stateRow) {
//        return null;
//    }
//
//    /**
//     * Derives a transition property from the corresponding transition.
//     * <p>
//     * N.B.: Not the transition row is passed to this method, but the row for the outgoing state. The transition row can
//     * be retrieved using {@link Row#getSuccessor(int)}.
//     *
//     * @param stateRow
//     *         the row for the source state
//     * @param inputIdx
//     *         the index of the input symbol to consider
//     *
//     * @return the transition property of the corresponding transition
//     */
//    protected OutputAndEnabledInputs<I, O> transitionProperty(ObservationTable<I, Word<OutputAndEnabledInputs<I,O>>> table, Row<I> stateRow, int inputIdx) {
//        Row<I> transRow = stateRow.getSuccessor(inputIdx);
//        return outputTable.get(transRow.getRowId() - 1);
//    }
//
//    protected void updateOutputs() {
//        int numOutputs = outputTable.size();
//        int numTransRows = table.numberOfRows() - 1;
//
//        int newOutputs = numTransRows - numOutputs;
//        if (newOutputs == 0) {
//            return;
//        }
//
//        List<DefaultQuery<I, Word<OutputAndEnabledInputs<I,O>>>> outputQueries = new ArrayList<>(numOutputs);
//
//        for (int i = numOutputs + 1; i <= numTransRows; i++) {
//            Row<I> row = table.getRow(i);
//            Word<I> rowPrefix = row.getLabel();
//            int prefixLen = rowPrefix.size();
//            outputQueries.add(new DefaultQuery<>(rowPrefix.prefix(prefixLen - 1), rowPrefix.suffix(1)));
//        }
//
//        oracle.processQueries(outputQueries);
//
//        for (int i = 0; i < newOutputs; i++) {
//            DefaultQuery<I, Word<OutputAndEnabledInputs<I,O>>> query = outputQueries.get(i);
//            OutputAndEnabledInputs<I,O> outSym = query.getOutput().getSymbol(1);
//
//            outputTable.add(outSym);
//        }
//    }
//
//    @Override
//    protected SuffixOutput<I, Word<OutputAndEnabledInputs<I,O>>> hypothesisOutput() {
//        return new SimulatorMQOracleWithEnabledInputs<>(internalHyp);
//    }
//
//    protected void refineHypothesisInternal(DefaultQuery<I, Word<OutputAndEnabledInputs<I,O>>> ceQuery) {
//        List<List<Row<I>>> unclosed = cexHandler.handleCounterexample(ceQuery, table, hypothesisOutput(), oracle);
//        completeConsistentTable(unclosed, cexHandler.needsConsistencyCheck());
//    }
//
//    @Override
//    protected List<Word<I>> initialPrefixes() {
//        return initialPrefixes;
//    }
//
//    @Override
//    protected List<Word<I>> initialSuffixes() {
//        return initialSuffixes;
//    }
//
//    @Override
//    protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
//        return closingStrategy.selectClosingRows(unclosed, table, oracle);
//    }
//
//    public static final class BuilderDefaults {
//
//        public static <I> List<Word<I>> initialPrefixes() {
//            return Collections.singletonList(Word.<I>epsilon());
//        }
//
//        public static <I> List<Word<I>> initialSuffixes() {
//            return Collections.emptyList();
//        }
//
//        public static <I,O> ObservationTableCEXHandler<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> cexHandler() {
//            return ObservationTableCEXHandlers.CLASSIC_LSTAR;
//        }
//
//        public static <I,O> ClosingStrategy<? super I, ? super Word<OutputAndEnabledInputs<I,O>>> closingStrategy() {
//            return ClosingStrategies.CLOSE_FIRST;
//        }
//
//    }
//
//    @Override
//    public final void startLearning() {
//        super.startLearning();
//        updateInternalHypothesis();
//    }
//
//    protected Integer createState(boolean initial, Row<I> row) {
//        Void prop = stateProperty(table, row);
//        if (initial) {
//            return internalHyp.addInitialState(prop);
//        }
//        return internalHyp.addState(prop);
//    }
//
//    protected void setTransition(Integer from, I input, Integer to, Row<I> fromRow, int inputIdx) {
//        OutputAndEnabledInputs<I,O> prop = transitionProperty(table, fromRow, inputIdx);
//        internalHyp.setTransition(from, input, to, prop.getOutput());
//    }
//
////    @Override
////    public AutomatonLStarState<I, Word<OutputAndEnabledInputs<I,O>>, CompactMealy<I, OutputAndEnabledInputs<I,O>>, Integer> suspend() {
////        return new AutomatonLStarState<>(table, internalHyp, stateInfos);
////    }
//
////    @Override
////    public void resume(final AutomatonLStarState<I, Word<OutputAndEnabledInputs<I,O>>, CompactMealy<I, O>, Integer> state) {
////        this.table = (PartialObservationTable<I, O>) state.getObservationTable();
////        this.table.setInputAlphabet(alphabet);
////        this.internalHyp = state.getHypothesis();
////        this.stateInfos = state.getStateInfos();
////    }
//
//    static final class StateInfo<Integer, I> implements Serializable {
//
//        private final Row<I> row;
//        private final Integer state;
//
//        StateInfo(Row<I> row, Integer state) {
//            this.row = row;
//            this.state = state;
//        }
//
//        public Row<I> getRow() {
//            return row;
//        }
//
//        public Integer getState() {
//            return state;
//        }
//    }
//}
