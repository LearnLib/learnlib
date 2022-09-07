package de.learnlib.algorithms.lstar.moore;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.dfa.LStarDFAUtil;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMoore;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtensibleLStarMoore <I, O>
        extends AbstractExtensibleAutomatonLStar<MooreMachine<?, I, ?, O>, I, Word<O>, Integer, Integer, O, Void, CompactMoore<I, O>>
        implements OTLearnerMoore<I, O> {


    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    protected ExtensibleLStarMoore(Alphabet<I> alphabet,
                                   MembershipOracle<I, Word<O>> oracle,
                                   List<Word<I>> initialPrefixes,
                                   List<Word<I>> initialSuffixes,
                                   ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
                                   ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        super(alphabet, oracle, new CompactMoore<>(alphabet),
                initialPrefixes,
                LStarMooreUtil.ensureSuffixCompliancy(initialSuffixes),
                cexHandler, closingStrategy);
    }


    @Override
    public CompactMoore<I, O> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected MooreMachine<?, I, ?, O> exposeInternalHypothesis() {
        return internalHyp;
    }


    @Override
    protected O stateProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow) {
        Word<O> word = table.cellContents(stateRow, 0);
        return  word.getSymbol(0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, Word<O>> hypothesisOutput() {
        return internalHyp;
    }
}