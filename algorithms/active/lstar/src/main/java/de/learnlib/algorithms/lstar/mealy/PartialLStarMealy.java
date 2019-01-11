package de.learnlib.algorithms.lstar.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.PartialObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;
import de.learnlib.api.oracle.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import de.learnlib.api.oracle.SLIMMUtil;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.SimpleAlphabet;

public class PartialLStarMealy<I, O>
        extends AbstractExtensibleAutomatonLStar<StateLocalInputMealyMachine<?, I, ?, O>, I, Word<OutputAndLocalInputs<I, O>>, Integer, CompactMealyTransition<O>, Void, O, CompactMealy<I, O>> {

    private final List<OutputAndLocalInputs<I, O>> outputTable = new ArrayList<>();
    private final GrowingAlphabet<I> alphabetAsGrowing;

    public PartialLStarMealy(MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super Word<OutputAndLocalInputs<I, O>>> cexHandler,
                             ClosingStrategy<? super I, ? super Word<OutputAndLocalInputs<I, O>>> closingStrategy) {
        this(oracle, Collections.singletonList(Word.epsilon()), initialSuffixes, cexHandler, closingStrategy);
    }

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public PartialLStarMealy(MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle,
                             List<Word<I>> initialPrefixes,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super Word<OutputAndLocalInputs<I, O>>> cexHandler,
                             ClosingStrategy<? super I, ? super Word<OutputAndLocalInputs<I, O>>> closingStrategy) {
        this(new SimpleAlphabet<>(),
             oracle,
             new CompactMealy<>(new SimpleAlphabet<>()),
             initialPrefixes,
             computePartialSuffixes(initialSuffixes, cexHandler),
             cexHandler,
             closingStrategy);
    }

    private PartialLStarMealy(GrowingAlphabet<I> alphabet,
                              MembershipOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle,
                              CompactMealy<I, O> internalHypothesis,
                              List<Word<I>> initialPrefixes,
                              List<Word<I>> initialSuffixes,
                              ObservationTableCEXHandler<? super I, ? super Word<OutputAndLocalInputs<I, O>>> cexHandler,
                              ClosingStrategy<? super I, ? super Word<OutputAndLocalInputs<I, O>>> closingStrategy) {
        super(alphabet,
              oracle,
              internalHypothesis,
              initialPrefixes,
              computePartialSuffixes(initialSuffixes, cexHandler),
              cexHandler,
              closingStrategy);
        this.table = new PartialObservationTable<>(this::propagateNewAlphabetSymbol);
        this.alphabetAsGrowing = alphabet;
    }

    private void propagateNewAlphabetSymbol(I i) {
        this.alphabetAsGrowing.addSymbol(i);
        this.internalHyp.addAlphabetSymbol(i);
    }

    private static <I, O> List<Word<I>> computePartialSuffixes(List<Word<I>> initialSuffixes,
                                                               ObservationTableCEXHandler<? super I, ? super Word<OutputAndLocalInputs<I, O>>> cexHandler) {
        final List<Word<I>> result = new ArrayList<>();
        result.add(Word.epsilon());
        result.addAll(LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes,
                                                            Alphabets.fromArray(),
                                                            cexHandler.needsConsistencyCheck()));

        return result;
    }

    @Override
    protected StateLocalInputMealyMachine<?, I, ?, O> exposeInternalHypothesis() {
        return internalHyp;
    }

    @Override
    protected void updateInternalHypothesis() {
        updateOutputs();
        super.updateInternalHypothesis();
    }

    @Override
    protected Void stateProperty(ObservationTable<I, Word<OutputAndLocalInputs<I, O>>> table, Row<I> stateRow) {
        return null;
    }

    @Override
    protected O transitionProperty(ObservationTable<I, Word<OutputAndLocalInputs<I, O>>> table,
                                   Row<I> stateRow,
                                   int inputIdx) {
        Row<I> transRow = stateRow.getSuccessor(inputIdx);
        return outputTable.get(transRow.getRowId() - 1).getOutput();
    }

    protected void updateOutputs() {
        int numOutputs = outputTable.size();
        int numTransRows = table.numberOfRows() - 1;

        int newOutputs = numTransRows - numOutputs;
        if (newOutputs == 0) {
            return;
        }

        List<DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>>> outputQueries = new ArrayList<>(numOutputs);

        for (int i = numOutputs + 1; i <= numTransRows; i++) {
            Row<I> row = table.getRow(i);
            Word<I> rowPrefix = row.getLabel();
            int prefixLen = rowPrefix.size();
            outputQueries.add(new DefaultQuery<>(rowPrefix.prefix(prefixLen - 1), rowPrefix.suffix(1)));
        }

        oracle.processQueries(outputQueries);

        for (int i = 0; i < newOutputs; i++) {
            DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>> query = outputQueries.get(i);
            OutputAndLocalInputs<I, O> outSym = query.getOutput().getSymbol(1);

            outputTable.add(outSym);
        }
    }

    @Override
    protected SuffixOutput<I, Word<OutputAndLocalInputs<I, O>>> hypothesisOutput() {
        return SLIMMUtil.partial2StateLocal(internalHyp);
    }
}
