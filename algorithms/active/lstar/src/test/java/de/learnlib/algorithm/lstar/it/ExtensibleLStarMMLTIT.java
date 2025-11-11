package de.learnlib.algorithm.lstar.it;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.algorithm.MMLTModelParams;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.equivalence.mmlt.EQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.oracle.symbol_filters.AcceptAllSymbolFilter;
import de.learnlib.oracle.symbol_filters.CachedSymbolFilter;
import de.learnlib.oracle.symbol_filters.IgnoreAllSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.MMLTPerfectSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.MMLTRandomSymbolFilter;
import de.learnlib.oracle.symbol_filters.mmlt.MMLTStatisticsSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.TimedSUL;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import de.learnlib.testsupport.example.mmlt.MMLTModel;
import de.learnlib.util.statistic.container.MapStatsContainer;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.util.automaton.mmlt.MMLTUtil;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

/**
 * Integration tests for the MMLT learner that uses several EQ oracles, symbol filters
 * and a cache to learn different models.
 */
@Test
public class ExtensibleLStarMMLTIT {

    private enum FilterMode {
        none, random, ignore_all, perfect
    }

    private static <I, O> void runExperiment(ExtensibleLStarMMLT<I, O> learner, MMLTEquivalenceOracle<I, O> tester, StatsContainer stats, int maxRounds) {
        stats.startOrResumeClock("learningRt", "Processing time");
        learner.startLearning();

        var hyp = learner.getHypothesisModel();
        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        stats.increaseCounter("roundCount", "CEX queries");

        int roundCount = 1;
        while (cex != null && roundCount < maxRounds) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
            cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
            stats.increaseCounter("roundCount", null);
            roundCount += 1;
        }
        stats.pauseClock("learningRt");

        final var finalHypothesis = learner.getHypothesisModel();

        // Add some more stats:
        stats.setCounter("result_locs", "Locations in result", finalHypothesis.getStates().size());

        // Print stats:
        stats.printStats();
    }

    private static <S, I, T, O> void learnModel(String name, MMLT<S, I, T, O> automaton, MMLTModelParams<O> params,
                                             FilterMode symbolFilterMode, long seed) {

        // Add some stats:
        var stats = new MapStatsContainer();
        stats.addTextInfo("LocalTimerMealyModel", null, name);
        stats.setCounter("original_locs", "Locations in original", automaton.getStates().size());
        stats.setCounter("original_inputs", "Untimed alphabet size in original", automaton.getInputAlphabet().size());

        // Set up a pipeline:
        // Query oracle -> TimeoutReducer -> Cache -> Query stats -> SUL
        MMLTSimulatorSUL<?, I, ?, O> sul = new MMLTSimulatorSUL<>(automaton.getSemantics());
        CounterTimedSUL<I, O> statsAfterCache = new CounterTimedSUL<>(sul, stats);
        TimedSULTreeCache<I, O> cacheSUL = new TimedSULTreeCache<>(statsAfterCache, params);
        cacheSUL.setStatsContainer(stats);
        TimedSUL<I, O> toReducerSul = new TimeoutReducerSUL<>(cacheSUL, params.maxTimeoutWaitingTime(), stats);

        TimedSULOracle<I, O> timeOracle = new TimedSULOracle<>(toReducerSul, params);

        // Prepare cex oracle chain:

        EQOracleChain<I, O> chainOracle = new EQOracleChain<>();
        chainOracle.addOracle(cacheSUL.createCacheConsistencyTest());
        chainOracle.addOracle(new ResetSearchOracle<>(timeOracle, seed, 1.0, 1.0));
        chainOracle.addOracle(new RandomWpOracle<>(timeOracle, seed, 16, 0, 100));
        chainOracle.addOracle(new SimulatorEQOracle<>(automaton)); // ensure that we eventually find an accurate model
        chainOracle.setStatsContainer(stats);

        // Create learner:
        List<Word<TimedInput<I>>> suffixes = new ArrayList<>();
        automaton.getInputAlphabet().forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // Configure symbol filter:
        SymbolFilter<TimedInput<I>, InputSymbol<I>> filter = new AcceptAllSymbolFilter<>(); // pass-through
        switch (symbolFilterMode) {
            case perfect -> filter = new MMLTPerfectSymbolFilter<>(automaton);
            case random -> filter = new MMLTRandomSymbolFilter<>(automaton, 0.1, new Random(seed));
            case ignore_all -> filter = new IgnoreAllSymbolFilter<>();
        }

        filter = new MMLTStatisticsSymbolFilter<>(automaton, filter, stats);
        filter = new CachedSymbolFilter<>(filter); // need to wrap to enable updates to responses

        var learner = new ExtensibleLStarMMLT<>(automaton.getInputAlphabet(), params, suffixes, timeOracle, filter);
        learner.setStatsContainer(stats);

        // Start learning:
        runExperiment(learner, chainOracle, stats, 100);
    }


    @Test
    public void learnExamplesNoFilter() throws IOException, FormatException {
        for (String modelFile : listModelFiles()) {
            var model = automatonFromFile(modelFile);
            learnModel(model.name(), model.automaton(), model.params(), FilterMode.none, 100);
        }
        MMLTExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.none, 100));
    }

    @Test
    public void learnExamplesIgnoreAllFilter() throws IOException, FormatException {
        for (String modelFile : listModelFiles()) {
            var model = automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.ignore_all, 100);
        }
        MMLTExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.ignore_all, 100));
    }

    @Test
    public void learnExamplesPerfectFilter() throws IOException, FormatException {
        for (String modelFile : listModelFiles()) {
            var model = automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.perfect, 100);
        }
        MMLTExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.perfect, 100));
    }

    @Test
    public void learnExamplesRandomFilter() throws IOException, FormatException {
        for (String modelFile : listModelFiles()) {
            var model = automatonFromFile(modelFile);
            learnModel(modelFile, model.automaton(), model.params(), FilterMode.random, 100);
        }
        MMLTExamples.getAll().forEach(m ->
                learnModel(m.name(), m.automaton(), m.params(), FilterMode.random, 100));
    }

    /**
     * Lists all MMLT models in the resources directory.
     */
    static List<String> listModelFiles() {
        var models = new ArrayList<String>();
        try {
            var modelFiles = ExtensibleLStarMMLTIT.class.getResource("/mmlt");
            if (modelFiles != null) {
                try (Stream<Path> paths = Files.list(Paths.get(modelFiles.toURI()))) {
                    paths.filter(p -> p.toString().endsWith(".dot"))
                         .map(p -> p.getFileName().toString())
                         .forEach(models::add);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to list model files", e);
        }
        return models;
    }

    public static MMLTModel<?, String, ?, String> automatonFromFile(String name)
            throws IOException, FormatException {
        return automatonFromFile(name, -1);
    }

    /**
     * Loads the automaton model with the provided resource name.
     *
     * @param name                 Resource name
     * @param maxTimerQueryWaiting Maximum timer query waiting time. If set to -1, the maximum initial timer value is used.
     * @return The automaton model.
     */
    public static MMLTModel<?, String, ?, String> automatonFromFile(String name, int maxTimerQueryWaiting)
            throws IOException, FormatException {

        var silentOutput = "void";
        var outputCombiner = StringSymbolCombiner.getInstance();
        var parser = DOTParsers.mmlt(silentOutput, outputCombiner);

        try (InputStream is = ExtensibleLStarMMLTIT.class.getResourceAsStream("/mmlt/" + name)) {
            var model = parser.readModel(is);
            var automaton = model.model;

            long maxTimeoutDelay = MMLTUtil.getMaximumTimeoutDelay(automaton);
            long maxTimerQueryWaitingFinal = (maxTimerQueryWaiting > 0) ?
                    maxTimerQueryWaiting :
                    MMLTUtil.getMaximumInitialTimerValue(automaton) * 2;

            return new MMLTModel<>(name,
                                   automaton,
                                   new MMLTModelParams<>(silentOutput,
                                                         maxTimeoutDelay,
                                                         maxTimerQueryWaitingFinal,
                                                         outputCombiner));
        }
    }

}
