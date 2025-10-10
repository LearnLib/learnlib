package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.util.automaton.mmlt.LocalTimerMealyUtil;
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

    private final LocalTimerMealy<?, I, O> refModel;

    public LocalTimerMealySimulatorOracle(LocalTimerMealy<?, I, O> refModel) {
        this.refModel = refModel;
    }

    @Override
    public @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExample(LocalTimerMealy<?, I, O> hypothesis, Collection<? extends LocalTimerMealySemanticInputSymbol<I>> inputs) {
        List<LocalTimerMealySemanticInputSymbol<I>> listInputs = new ArrayList<>(inputs);

        var separatingWord = LocalTimerMealyUtil.findSeparatingWord(refModel, hypothesis, listInputs);
        if (separatingWord != null) {
            var sulOutput = refModel.getSemantics().computeSuffixOutput(Word.epsilon(), separatingWord);
            return new DefaultQuery<>(Word.epsilon(), separatingWord, sulOutput);
        } else {
            return null;
        }
    }
}
