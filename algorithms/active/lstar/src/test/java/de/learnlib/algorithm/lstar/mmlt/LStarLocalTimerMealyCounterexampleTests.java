package de.learnlib.algorithm.lstar.mmlt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.simulator.LocalTimerMealySimulatorSUL;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealySimulatorOracle;
import de.learnlib.oracle.membership.TimedQueryOracle;
import de.learnlib.oracle.symbol_filters.AcceptAllSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyExamples;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyModel;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.exception.FormatException;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

/**
 * Tests several different cases of counterexamples.
 */
@Test
public class LStarLocalTimerMealyCounterexampleTests {

    private static <S, I, T, O> void learnModel(LocalTimerMealyModel<S, I, T, O> model, List<Word<TimedInput<I>>> counterexamples) {

        var sul = new LocalTimerMealySimulatorSUL<>(model.automaton().getSemantics());
        TimedQueryOracle<I, O> timeOracle = new TimedQueryOracle<>(sul, model.params());

        var learner = new LStarLocalTimerMealy<>(model.automaton().getInputAlphabet(), model.params(), Collections.emptyList(),
                timeOracle, new AcceptAllSymbolFilter<>());

        learner.startLearning();

        System.out.println("Initial hypothesis:");
        LocalTimerMealyTestUtil.printModel(learner.getHypothesisModel());

        // Trigger the expected cases with the provided counterexamples:
        for (var cex : counterexamples) {
            System.out.println();
            new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);
            System.out.println();

            var output = timeOracle.querySuffixOutput(Word.epsilon(), cex);
            learner.refineHypothesis(new DefaultQuery<>(cex, output));

            System.out.println("Current hypothesis:");
            LocalTimerMealyTestUtil.printModel(learner.getHypothesisModel());
        }

        // Now continue until arriving at an accurate model:
        System.out.println("Running to completion");
        LocalTimerMealySimulatorOracle<I, O> simOracle = new LocalTimerMealySimulatorOracle<>(model.automaton());
        int round = 0;
        while (round < 100) {
            var hyp = learner.getHypothesisModel();
            var cex = simOracle.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
            if (cex != null) {
                learner.refineHypothesis(cex);
            } else {
                break;
            }
            round++;
        }

        System.out.println("Took " + (round + 1) + " additional rounds.");
        System.out.println("Final hypothesis:");
        LocalTimerMealyTestUtil.printModel(learner.getHypothesisModel());
        System.out.println();
        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);
        System.out.println();

    }

    @Test
    public void testOverApproxReset() throws IOException, FormatException {
        // Infers a missing local reset instead of a missing discriminator first.
        var model = LocalTimerMealyTestUtil.automatonFromFile("over_approx_reset.dot");

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                Word.fromSymbols(TimedInput.step(), new InputSymbol<>("i"), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
    }

    @Test
    public void testRecursiveDecomp() throws IOException, FormatException {
        // Triggers recursive decomposition
        var model = LocalTimerMealyTestUtil.automatonFromFile("recursive_decomp.dot", 3);

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p"), new InputSymbol<>("f")),

                Word.fromSymbols(new InputSymbol<>("u"),
                        new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                        new InputSymbol<>("f")),

                Word.fromSymbols(new InputSymbol<>("u"),
                        new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                        new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
    }

    @Test
    public void testMissingDiscriminators() {
        var model = LocalTimerMealyExamples.SensorCollector();

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromSymbols(new InputSymbol<>("p2"), TimedInput.step(), new InputSymbol<>("abort"), new TimeoutSymbol<>())
        );

        // Missing discriminator at one-shot:
        List<Word<TimedInput<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromSymbols(new InputSymbol<>("p2"), new TimeoutSymbol<>(), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingResets() {
        var model = LocalTimerMealyExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing reset in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromSymbols(new InputSymbol<>("p1"), TimedInput.step(), new InputSymbol<>("abort"), new TimeoutSymbol<>())
        );

        // Missing reset in non-stable config:
        List<Word<TimedInput<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromSymbols(new InputSymbol<>("p1"),
                                 TimedInput.step(), TimedInput.step(), TimedInput.step(),
                                 new InputSymbol<>("abort"), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
        learnModel(model, cex2);

    }

    @Test
    public void testMissingOneShotModelB() {
        // Setting max waiting = 6 -> all inferred timers are periodic:
        var model = LocalTimerMealyExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(6);

        // Missing one-shot via bad return to entry:
        List<Word<TimedInput<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromWords(Word.fromLetter(new InputSymbol<>("p1")), TimedInput.timeouts(14))
        );

        // Missing one-shot in location with single timer:
        List<Word<TimedInput<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromSymbols(new InputSymbol<>("p2"), new TimeoutSymbol<>(), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingOneShotModelA() {
        var model = LocalTimerMealyExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing one-shot via bad output:
        List<Word<TimedInput<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),

                Word.fromWords(Word.fromLetter(new InputSymbol<>("p1")),
                        TimedInput.steps(40),
                        Word.fromLetter(new TimeoutSymbol<>())) // alternatively: new NonDelayingInput<>("abort")
        );

        // Missing one-shot via bad target:
        List<Word<TimedInput<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new InputSymbol<>("p1"), new InputSymbol<>("p1")),
                Word.fromSymbols(new InputSymbol<>("p2"), new InputSymbol<>("abort")),
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(14), TimedInput.inputs("collect", "p1"))
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }


}
