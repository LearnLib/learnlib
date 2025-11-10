package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A chain of MMLT equivalence oracles. The oracles are queried in the given order until either a counterexample is found
 * or nor example is found.
 * <p>
 * This operates similarly to {@link de.learnlib.oracle.equivalence.EQOracleChain},
 * but also stores statistics about the queries in a {@link StatsContainer}.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyEQOracleChain<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O>, LearnerStatsProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalTimerMealyEQOracleChain.class);

    private final List<LocalTimerMealyEquivalenceOracle<I, O>> oracles = new ArrayList<>();
    private StatsContainer stats = new DummyStatsContainer();

    /**
     * Stores names for the equivalence oracles that are used in statistics.
     */
    private List<String> oracleNames;

    public void addOracle(LocalTimerMealyEquivalenceOracle<I, O> oracle) {
        this.oracles.add(oracle);

        // Update names:
        // Names follow the convention typeName + # + index of this oracle among all oracles of same type.
        this.oracleNames = new ArrayList<>(oracles.size());

        Map<String, Integer> typeCounter = new HashMap<>();
        for (var eqOracle : this.oracles) {
            String typeName = eqOracle.getClass().getSimpleName();

            int currentCount = typeCounter.getOrDefault(typeName, -1);
            int newCount = currentCount + 1;
            typeCounter.put(typeName, newCount);

            this.oracleNames.add(typeName + "#" + newCount);
        }
    }


    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;

        // Propagate to all oracles:
        for (var oracle : this.oracles) {
            if (oracle instanceof LearnerStatsProvider provider) {
                provider.setStatsContainer(stats);
            }
        }
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        if (this.oracles.isEmpty()) throw new IllegalStateException("Must specify at least one cex oracle in chain.");

        int oracleIdx = 0;
        for (var oracle : this.oracles) {
            var cex = oracle.findCounterExample(hypothesis, inputs);
            if (cex != null) {
                String oracleName = this.oracleNames.get(oracleIdx);
                stats.increaseCounter("cnt_cex_" + oracleName, "CEX from " + oracleName);

                logger.debug("{} found counterexample: {}", "cnt_cex_" + oracleName, cex);

                return cex;
            }
            oracleIdx++;
        }

        return null;
    }
}
