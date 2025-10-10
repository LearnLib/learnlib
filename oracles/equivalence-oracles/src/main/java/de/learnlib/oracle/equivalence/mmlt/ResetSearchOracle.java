package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.AbstractTimedQueryOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.util.automaton.cover.LocalTimerMealyCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    private <S> List<LocalTimerMealySemanticInputSymbol<I>> getLoopingSymbols(S sourceLoc, List<LocalTimerMealySemanticInputSymbol<I>> alphabet, LocalTimerMealy<S, I, O> hypothesis) {

        List<LocalTimerMealySemanticInputSymbol<I>> loopingInputs = new ArrayList<>();
        for (var sym : alphabet) {
            if (!(sym instanceof NonDelayingInput<I> ndi)) {
                continue; // only consider non-delaying inputs, as only these can perform local resets
            }
            var trans = hypothesis.getTransition(sourceLoc, ndi);

            // Collect self-loops:
            if (trans == null || (trans.successor().equals(sourceLoc))) {
                loopingInputs.add(sym);
            }
        }

        return loopingInputs;
    }

    @Override
    public @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExample(LocalTimerMealy<?, I, O> hypothesis, Collection<? extends LocalTimerMealySemanticInputSymbol<I>> inputs) {
        if (loopInsertPerc == 0) {
            return null; // oracle is disabled
        }
        List<LocalTimerMealySemanticInputSymbol<I>> listInputs = new ArrayList<>(inputs);
        return this.findCexInternal(hypothesis, listInputs);
    }

    private <S> @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCexInternal
            (LocalTimerMealy<S, I, O> hypothesis, List<LocalTimerMealySemanticInputSymbol<I>> inputs) {

        // Retrieve prefixes from state cover, to establish some separation between learner and teacher:
        var stateCover = LocalTimerMealyCover.getLocalTimerMealyLocationCover(hypothesis, inputs);

        // Only keep locations that have at least two stable configs (only these can have local resets):
        List<Word<LocalTimerMealySemanticInputSymbol<I>>> prefixes = new ArrayList<>();
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

        List<Word<LocalTimerMealySemanticInputSymbol<I>>> chosenPrefixes = RandomUtil.sampleUnique(locPrefixRandom, prefixes, randPrefixes);


        for (var prefix : chosenPrefixes) {
            // Retrieve looping symbols:
            var sourceLoc = hypothesis.getSemantics().traceInputs(prefix).getLocation();
            var loopingInputs = getLoopingSymbols(sourceLoc, inputs, hypothesis);
            if (loopingInputs.isEmpty()) {
                continue; // no loops
            }

            // Determine number of looping symbols we want to append:
            int randElements = (int) Math.round(loopInsertPerc * loopingInputs.size());
            randElements = Math.min(loopingInputs.size(), randElements);

            List<LocalTimerMealySemanticInputSymbol<I>> chosenLoopingInputs = RandomUtil.sampleUnique(new Random(loopingInputSelectionSeed), loopingInputs, randElements);


            // Create test word:
            WordBuilder<LocalTimerMealySemanticInputSymbol<I>> wbTestWord = new WordBuilder<>();
            wbTestWord.append(prefix);
            wbTestWord.append(new TimeStepSymbol<>());
            wbTestWord.append(Word.fromList(chosenLoopingInputs));
            wbTestWord.append(new TimeoutSymbol<>());

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
