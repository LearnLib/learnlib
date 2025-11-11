package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.mmlt.impl.ReducedMMLTSemantics;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.cover.MMLTCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * RandomWP counterexample search for MMLT learning.
 * Key modification: samples prefix from entry prefixes instead of all state prefixes.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class RandomWpOracle<I, O> implements MMLTEquivalenceOracle<I, O>, LearnerStatsProvider {
    private static final Logger logger = LoggerFactory.getLogger(RandomWpOracle.class);
    private final TimedQueryOracle<I, O> timeOracle;

    private StatsContainer stats = new DummyStatsContainer();

    private final Random random;
    private final int minSize;
    private final int rndLen;
    private final int bound;

    public RandomWpOracle(TimedQueryOracle<I, O> timeOracle,
                          long randomSeed,
                          int minSize, int rndAddLength, int bound) {

        this.timeOracle = timeOracle;

        this.random = new Random(randomSeed);

        this.minSize = minSize;
        this.rndLen = rndAddLength;
        this.bound = bound;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        return findCounterExampleInternal(hypothesis, inputs);
    }

    private <S> DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExampleInternal(MMLT<S, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        // Make expanded form of hypothesis:
        var hypSemModel = ReducedMMLTSemantics.forLocalTimerMealy(hypothesis);

        // Create a list of symbols (for faster access):
        List<TimedInput<I>> listAlphabet = new ArrayList<>(inputs);

        // Identify global suffixes:
        var globalSuffixes = Automata.characterizingSet(hypSemModel, inputs);

        // Get list of prefixes in deterministic order (so we can reproduce experiments easily):
        var locationCover = MMLTCover.getLocalTimerMealyLocationCover(hypothesis, listAlphabet);
        var prefixList = locationCover
                .values()
                .stream()
                .sorted(Comparator.comparing(AbstractPrintable::toString))
                .toList();

        // Generate test words:
        for (int i = 0; i < this.bound; i++) {
            stats.increaseCounter("WP_TESTED_WORD", "RandomWpOracle: tested words");

            var sulAnswer = this.generateTestword(prefixList, globalSuffixes, hypothesis, hypSemModel, listAlphabet);
            Word<TimedOutput<O>> hypAnswer = hypothesis.getSemantics().computeSuffixOutput(sulAnswer.getPrefix(), sulAnswer.getSuffix());

            // Found inconsistency if outputs do no match:
            if (!sulAnswer.getOutput().equals(hypAnswer)) {
                return sulAnswer;
            }
        }

        return null;
    }

    private <S, T> DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> generateTestword(List<Word<TimedInput<I>>> prefixes,
                                                                                      List<Word<TimedInput<I>>> globalSuffixes,
                                                                                      MMLT<S, I, ?, O> hypothesis,
                                                                                      ReducedMMLTSemantics<S, I, O> hypSemModel,
                                                                                      List<TimedInput<I>> alphabet) {

        WordBuilder<TimedInput<I>> wbTestWord = new WordBuilder<>();

        // 1. Pick a random entry config prefix:
        Word<TimedInput<I>> prefix = prefixes.get(this.random.nextInt(prefixes.size()));
        wbTestWord.append(prefix);

        // 2. Add random middle part:
        int size = minSize;
        while ((size > 0) || (this.random.nextDouble() > 1 / (this.rndLen + 1.0))) {
            var nextSymbol = alphabet.get(this.random.nextInt(alphabet.size()));
            wbTestWord.append(nextSymbol);

            if (size > 0) {
                size--;
            }
        }

        // 3. Pick a random suffix for this state:
        // 50% chance for state testing, 50% chance for transition testing
        Word<TimedInput<I>> suffix = Word.epsilon();
        if (this.random.nextBoolean()) {
            if (!globalSuffixes.isEmpty()) {
                suffix = globalSuffixes.get(random.nextInt(globalSuffixes.size()));
            }
        } else {
            // Identify configuration reached by prefix:
            var currentConfig = hypothesis.getSemantics().getState(wbTestWord.toWord());
            var state = hypSemModel.getStateForConfiguration(currentConfig, true);
            var localSuffixes = Automata.stateCharacterizingSet(hypSemModel, alphabet, state);

            if (!localSuffixes.isEmpty()) {
                suffix = localSuffixes.get(random.nextInt(localSuffixes.size()));
            }
        }
        wbTestWord.append(suffix);

        // Query SUL:
        var testWord = wbTestWord.toWord();
        var sulAnswer = timeOracle.answerQuery(testWord);
        return new DefaultQuery<>(testWord, sulAnswer);
    }


    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }
}
