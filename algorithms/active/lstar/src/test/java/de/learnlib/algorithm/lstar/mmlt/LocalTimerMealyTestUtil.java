package de.learnlib.algorithm.lstar.mmlt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyModel;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.mmlt.MMLTUtil;

/**
 * Utility class for loading MMLTs from resources and printing them.
 */
public class LocalTimerMealyTestUtil {

    /**
     * Prints the provided MMLT to stdout.
     */
    static <S, I, T, O> void printModel(MMLT<S, I, T, O> model) {
        try {
            GraphDOT.write(model.transitionGraphView(model.getInputAlphabet()), System.out, new MMLTVisualizationHelper<>(model, true, true));
        } catch (IOException ignored) {
        }
    }

    /**
     * Lists all MMLT models in the resources directory.
     */
    static List<String> listModelFiles() {
        var models = new ArrayList<String>();
        try {
            var modelFiles = LocalTimerMealyTestUtil.class.getResource("/mmlt");
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

    static LocalTimerMealyModel<?, String, ?, String> automatonFromFile(String name)
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
    static LocalTimerMealyModel<?, String, ?, String> automatonFromFile(String name, int maxTimerQueryWaiting)
            throws IOException, FormatException {

        var silentOutput = "void";
        var outputCombiner = StringSymbolCombiner.getInstance();
        var parser = DOTParsers.mmlt(silentOutput, outputCombiner);

        try (InputStream is = LocalTimerMealyTestUtil.class.getResourceAsStream("/mmlt/" + name)) {
            var model = parser.readModel(is);
            var automaton = model.model;

            long maxTimeoutDelay = MMLTUtil.getMaximumTimeoutDelay(automaton);
            long maxTimerQueryWaitingFinal = (maxTimerQueryWaiting > 0) ?
                    maxTimerQueryWaiting :
                    MMLTUtil.getMaximumInitialTimerValue(automaton) * 2;

            return new LocalTimerMealyModel<>(name,
                                              automaton,
                                              new LocalTimerMealyModelParams<>(silentOutput,
                                                                               maxTimeoutDelay,
                                                                               maxTimerQueryWaitingFinal,
                                                                               outputCombiner));
        }
    }

}
