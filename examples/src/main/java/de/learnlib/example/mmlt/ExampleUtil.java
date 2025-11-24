package de.learnlib.example.mmlt;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.StatisticsCollector;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;

public class ExampleUtil {
    static MMLT<Integer, String, ?, String> runExperiment(ExtensibleLStarMMLT<String, String> learner,
                                                          EquivalenceOracle.MMLTEquivalenceOracle<String, String> tester,
                                                          StatisticsCollector statisticsCollector, int maxRounds) {
        statisticsCollector.startOrResumeClock("learningRt", "Processing time");
        learner.startLearning();

        // Our experiment follows the usual learn-loop:
        // We retrieve a hypothesis and ask for a counterexample. If a counterexample is found, we
        // provide it to the learner to refine the hypothesis. This process repeats until no
        // counterexample is found.
        var hyp = learner.getHypothesisModel();
        DefaultQuery<TimedInput<String>, Word<TimedOutput<String>>> cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        statisticsCollector.increaseCounter("roundCount", "CEX queries");

        int roundCount = 1;
        while (cex != null && roundCount < maxRounds) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
            cex = tester.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
            statisticsCollector.increaseCounter("roundCount", null);
            roundCount += 1;
        }
        statisticsCollector.pauseClock("learningRt");

        final var finalHypothesis = learner.getHypothesisModel();

        // Add some more stats:
        statisticsCollector.setCounter("result_locs", "Locations in result", finalHypothesis.getStates().size());

        // Print final result + statistics:
        System.out.println(statisticsCollector.printStats());

        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        System.out.println("Final hypothesis:");
        //Visualization.visualize(finalHypothesis.graphView(), new MMLTVisualizationHelper<>(finalHypothesis, true, true));

        return finalHypothesis;
    }
}
