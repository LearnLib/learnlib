package de.learnlib.algorithm.lstar;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.lstar.it.ExtensibleLStarMMLTIT;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.oracle.symbol_filters.AcceptAllSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import de.learnlib.testsupport.example.mmlt.MMLTModel;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.exception.FormatException;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

/**
 * Tests several different cases of counterexamples.
 */
@Test
public class ExtensibleLStarMMLTCounterexampleTests {

    private static <S, I, T, O> void learnModel(MMLTModel<S, I, T, O> model, List<Word<TimedInput<I>>> counterexamples) {

        var sul = new MMLTSimulatorSUL<>(model.automaton().getSemantics());
        var timeOracle = new TimedSULOracle<>(sul, model.params());

        var learner = new ExtensibleLStarMMLT<>(model.automaton().getInputAlphabet(), model.params(), Collections.emptyList(),
                                                timeOracle, new AcceptAllSymbolFilter<>());

        learner.startLearning();

        for (var cex : counterexamples) {
            var output = timeOracle.answerQuery(cex);
            learner.refineHypothesis(new DefaultQuery<>(cex, output));
        }

        // Now continue until arriving at an accurate model:
        SimulatorEQOracle<I, O> simOracle = new SimulatorEQOracle<>(model.automaton());

        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> cex;
        MMLT<Integer, I, ?, O> hyp = learner.getHypothesisModel();

        while ((cex = simOracle.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet())) != null) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
        }
    }

    @Test
    public void testOverApproxReset() throws IOException, FormatException {
        // Infers a missing local reset instead of a missing discriminator first.
        var model = ExtensibleLStarMMLTIT.automatonFromFile("over_approx_reset.dot");

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.fromSymbols(TimedInput.step(), new InputSymbol<>("i"), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
    }

    @Test
    public void testRecursiveDecomp() throws IOException, FormatException {
        // Triggers recursive decomposition
        var model = ExtensibleLStarMMLTIT.automatonFromFile("recursive_decomp.dot", 3);

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.upcast(TimedInput.inputs("p", "f")),
                Word.fromWords(TimedInput.inputs("u"), TimedInput.timeouts(4), TimedInput.inputs("f")),
                Word.fromWords(TimedInput.inputs("u"), TimedInput.timeouts(5))
        );

        learnModel(model, cex1);
    }

    @Test
    public void testMissingDiscriminators() {
        var model = MMLTExamples.SensorCollector();

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromSymbols(TimedInput.input("p2"), TimedInput.step(), TimedInput.input("abort"), TimedInput.timeout())
        );

        // Missing discriminator at one-shot:
        List<Word<TimedInput<String>>> cex2 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p2"), TimedInput.timeouts(2))
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingResets() {
        var model = MMLTExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing reset in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromSymbols(TimedInput.input("p1"), TimedInput.step(), TimedInput.input("abort"), TimedInput.timeout())
        );

        // Missing reset in non-stable config:
        List<Word<TimedInput<String>>> cex2 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.steps(3), TimedInput.inputs("abort"), TimedInput.timeouts(1))
        );

        learnModel(model, cex1);
        learnModel(model, cex2);

    }

    @Test
    public void testMissingOneShotModelB() {
        // Setting max waiting = 6 -> all inferred timers are periodic:
        var model = MMLTExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(6);

        // Missing one-shot via bad return to entry:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(14))
        );

        // Missing one-shot in location with single timer:
        List<Word<TimedInput<String>>> cex2 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p2"), TimedInput.timeouts(2))
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingOneShotModelA() {
        var model = MMLTExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing one-shot via bad output:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.steps(40), TimedInput.timeouts(1)) // alternatively: new InputSymbol<>("abort")
        );

        // Missing one-shot via bad target:
        List<Word<TimedInput<String>>> cex2 = List.of(
                Word.upcast(TimedInput.inputs("p1", "p1")),
                Word.upcast(TimedInput.inputs("p2", "abort")),
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(14), TimedInput.inputs("collect", "p1"))
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }


}
