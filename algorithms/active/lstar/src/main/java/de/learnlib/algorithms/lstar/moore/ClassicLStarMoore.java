package de.learnlib.algorithms.lstar.moore;


import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;

public class ClassicLStarMoore<I,O>
        extends AbstractExtensibleAutomatonLStar<MooreMachine<?, I, ?, O>, I,
        @Nullable O, Integer, Integer,
        O, Void, CompactMoore<I, O>> {


    @GenerateBuilder
    public ClassicLStarMoore(Alphabet<I> alphabet,
                             MembershipOracle<I, @Nullable O> oracle,
                             List<Word<I>> initialPrefixes,
                             List<Word<I>> initialSuffixes,
                             ObservationTableCEXHandler<? super I, ? super @Nullable O> cexHandler,
                             ClosingStrategy<? super I, ? super @Nullable O> closingStrategy) {
        super(alphabet, oracle, new CompactMoore<>(alphabet), initialPrefixes, initialSuffixes, cexHandler, closingStrategy);
    }

    @Override
    protected MooreMachine<?, I, Integer, O> exposeInternalHypothesis() {
        return internalHyp;
    }

    @Override
    protected O stateProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow) {

        return table.cellContents(stateRow, 0);
    }

    @Override
    protected Void transitionProperty(ObservationTable<I, @Nullable O> table, Row<I> stateRow, int inputIdx) {
        return null;
    }

    @Override
    protected SuffixOutput<I, @Nullable O> hypothesisOutput() {

        return new SuffixOutput<I, @Nullable O>() {

            @Override
            public @Nullable O computeOutput(Iterable<? extends I> input) {
                return computeSuffixOutput(Collections.emptyList(), input);
            }

            @Override
            public @Nullable O computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
                Word<O> wordOut = internalHyp.computeSuffixOutput(prefix, suffix);
                if (wordOut.isEmpty()) {
                    return null;
                }
                return wordOut.lastSymbol();
            }
        };
    }
}