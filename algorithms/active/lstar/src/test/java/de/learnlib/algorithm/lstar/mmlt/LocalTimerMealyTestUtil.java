package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import net.automatalib.automaton.time.impl.mmlt.StringSymbolCombiner;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.serialization.dot.LocalTimerMealyGraphvizParser;
import net.automatalib.util.automaton.mmlt.LocalTimerMealyUtil;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for loading MMLTs from resources and printing them.
 */
class LocalTimerMealyTestUtil {

    record Model<S, I, O>(LocalTimerMealy<S, I, O> automaton, LocalTimerMealyModelParams<O> params) {

    }

    static <S, I, O> void printModel(LocalTimerMealy<S, I, O> model) {
        try {
            GraphDOT.write(model.transitionGraphView(true, true), System.out);
        } catch (IOException ignored) {
        }
    }


    static Model<Integer, String, String> automatonFromFile(String name) {
        return automatonFromFile(name, -1);
    }

    /**
     * Loads the automaton model with the provided resource name.
     *
     * @param name                 Resource name
     * @param maxTimerQueryWaiting Maximum timer query waiting time. If set to -1, the maximum initial timer value is used.
     * @return The automaton model.
     */
    static Model<Integer, String, String> automatonFromFile(String name, int maxTimerQueryWaiting) {
        var modelResource = LocalTimerMealyTestUtil.class.getResource("/mmlt/" + name);
        var automaton = LocalTimerMealyGraphvizParser.parseLocalTimerMealy(new File(modelResource.getFile()), "void", StringSymbolCombiner.getInstance());

        long maxTimeoutDelay = LocalTimerMealyUtil.getMaximumTimeoutDelay(automaton);
        long maxTimerQueryWaitingFinal = (maxTimerQueryWaiting > 0) ? maxTimerQueryWaiting : LocalTimerMealyUtil.getMaximumInitialTimerValue(automaton) * 2;

        return new Model<>(automaton, new LocalTimerMealyModelParams<>("void", maxTimeoutDelay, maxTimerQueryWaitingFinal, StringSymbolCombiner.getInstance()));
    }

}
