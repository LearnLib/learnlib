package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.util.automaton.mmlt.MMLTUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simulator oracle for MMLTs.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealySimulatorOracle<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O> {

    private final MMLT<?, I, ?, O> refModel;

    public LocalTimerMealySimulatorOracle(MMLT<?, I, ?, O> refModel) {
        this.refModel = refModel;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        List<TimedInput<I>> listInputs = new ArrayList<>(inputs);

        var separatingWord = MMLTUtil.findSeparatingWord(refModel, hypothesis, listInputs);
        if (separatingWord != null) {
            var sulOutput = refModel.getSemantics().computeOutput(separatingWord);
            return new DefaultQuery<>(separatingWord, sulOutput);
        } else {
            return null;
        }
    }
}
