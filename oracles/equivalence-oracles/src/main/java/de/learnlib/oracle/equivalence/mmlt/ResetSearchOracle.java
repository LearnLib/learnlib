package de.learnlib.oracle.equivalence.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.learnlib.oracle.AbstractTimedQueryOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.util.automaton.cover.MMLTCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for counterexamples that reveal local resets.
 * <p>
 * - Takes any prefix from a known location
 * - Appends a single time step.
 * - Appends inputs of all non-delaying inputs that self-loop in that location.
 * - Appends timeout.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class ResetSearchOracle<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O> {

    private final static Logger logger = LoggerFactory.getLogger(ResetSearchOracle.class);

    private final AbstractTimedQueryOracle<I, O> timeOracle;
    private final Random locPrefixRandom;

    private final double loopInsertPerc;
    private final double testedLocPerc;

    private final long loopingInputSelectionSeed;

    public ResetSearchOracle(AbstractTimedQueryOracle<I, O> timeOracle, long seed, double loopInsertPerc, double testedLocPerc) {
        this.timeOracle = timeOracle;
        this.locPrefixRandom = new Random(seed);
        this.loopInsertPerc = loopInsertPerc;
        this.testedLocPerc = testedLocPerc;

        this.loopingInputSelectionSeed = seed;
    }

    private <S, T> List<TimedInput<I>> getLoopingSymbols(S sourceLoc, List<TimedInput<I>> alphabet, MMLT<S, I, T, O> hypothesis) {

        List<TimedInput<I>> loopingInputs = new ArrayList<>();
        for (var sym : alphabet) {
            if (!(sym instanceof InputSymbol<I> ndi)) {
                continue; // only consider non-delaying inputs, as only these can perform local resets
            }
            var trans = hypothesis.getTransition(sourceLoc, ndi);

            // Collect self-loops:
            if (trans == null || Objects.equals(hypothesis.getSuccessor(trans), sourceLoc)) {
                loopingInputs.add(sym);
            }
        }

        return loopingInputs;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        if (loopInsertPerc == 0) {
            return null; // oracle is disabled
        }
        List<TimedInput<I>> listInputs = new ArrayList<>(inputs);
        if (listInputs.stream().noneMatch(s -> s instanceof TimeStepSequence<I>) ||
            listInputs.stream().noneMatch(s -> s instanceof TimeoutSymbol<I>)) {
            logger.warn("ResetSearchOracle requires inputs to contain TimeoutSymbol and TimeStepSymbol. Will not find counterexample.");
            return null;
        }
        return this.findCexInternal(hypothesis, listInputs);
    }

    private <S, T> @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCexInternal
            (MMLT<S, I, T, O> hypothesis, List<TimedInput<I>> inputs) {

        // Retrieve prefixes from state cover, to establish some separation between learner and teacher:
        var stateCover = MMLTCover.getLocalTimerMealyLocationCover(hypothesis, inputs);

        // Only keep locations that have at least two stable configs (only these can have local resets):
        List<Word<TimedInput<I>>> prefixes = new ArrayList<>();
        for (var loc : stateCover.keySet()) {
            if (!hypothesis.getSortedTimers(loc).isEmpty() &&
                    hypothesis.getSortedTimers(loc).get(0).initial() > 1) {
                prefixes.add(stateCover.get(loc));
            }
        }

        // Sort alphabetically, so that experiments are easily reproducible:
        prefixes.sort(Comparator.comparing(AbstractPrintable::toString));

        // Determine number of tested locations:
        int randPrefixes = (int) Math.round(testedLocPerc * prefixes.size());
        if (randPrefixes == 0) {
            logger.warn("No prefixes tested. Need higher percentage?");
            return null;
        }

        List<Word<TimedInput<I>>> chosenPrefixes = RandomUtil.sampleUnique(locPrefixRandom, prefixes, randPrefixes);


        for (var prefix : chosenPrefixes) {
            // Retrieve looping symbols:
            var sourceLoc = hypothesis.getSemantics().getState(prefix).getLocation();
            var loopingInputs = getLoopingSymbols(sourceLoc, inputs, hypothesis);
            if (loopingInputs.isEmpty()) {
                continue; // no loops
            }

            // Determine number of looping symbols we want to append:
            int randElements = (int) Math.round(loopInsertPerc * loopingInputs.size());
            randElements = Math.min(loopingInputs.size(), randElements);

            List<TimedInput<I>> chosenLoopingInputs = RandomUtil.sampleUnique(new Random(loopingInputSelectionSeed), loopingInputs, randElements);


            // Create test word:
            WordBuilder<TimedInput<I>> wbTestWord = new WordBuilder<>();
            wbTestWord.append(prefix);
            wbTestWord.append(TimedInput.step());
            wbTestWord.append(Word.fromList(chosenLoopingInputs));
            wbTestWord.append(TimedInput.timeout());

            // Check if counterexample:
            var testWord = wbTestWord.toWord();

            var hypOutput = hypothesis.getSemantics().computeSuffixOutput(Word.epsilon(), testWord);
            var sulOutput = timeOracle.querySuffixOutput(Word.epsilon(), testWord);
            if (!hypOutput.equals(sulOutput)) {
                return new DefaultQuery<>(testWord, sulOutput);
            }

        }
        return null;
    }


}
