package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.driver.simulator.LocalTimerMealySimulatorSUL;
import de.learnlib.oracle.equivalence.mmlt.LocalTimerMealySimulatorOracle;
import de.learnlib.oracle.membership.TimedQueryOracle;
import de.learnlib.oracle.symbol_filters.AcceptAllSymbolFilter;
import de.learnlib.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyExamples;
import de.learnlib.testsupport.example.mmlt.LocalTimerMealyModel;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeStepSymbol;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.annotations.Test;


import java.util.Collections;
import java.util.List;

/**
 * Tests several different cases of counterexamples.
 */
@Test
public class LStarLocalTimerMealyCounterexampleTests {

    private static <S, I, O> void learnModel(LocalTimerMealyModel<S, I, O> model, List<Word<LocalTimerMealySemanticInputSymbol<I>>> counterexamples) {

        GrowingAlphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet = new GrowingMapAlphabet<>();
        model.automaton().getUntimedAlphabet().forEach(alphabet::addSymbol);

        LocalTimerMealySimulatorSUL<S, I, O> sul = new LocalTimerMealySimulatorSUL<>(model.automaton());
        TimedQueryOracle<I, O> timeOracle = new TimedQueryOracle<>(sul, model.params());

        var learner = new LStarLocalTimerMealy<>(alphabet, model.params(), Collections.emptyList(),
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


    private Word<LocalTimerMealySemanticInputSymbol<String>> getTimeStepSequence(int timeSteps) {
        WordBuilder<LocalTimerMealySemanticInputSymbol<String>> wbTimeStep = new WordBuilder<>();
        wbTimeStep.repeatAppend(timeSteps, new TimeStepSymbol<>());
        return wbTimeStep.toWord();
    }

    private Word<LocalTimerMealySemanticInputSymbol<String>> getTimeoutSequence(int timeouts) {
        WordBuilder<LocalTimerMealySemanticInputSymbol<String>> wbTimeouts = new WordBuilder<>();
        wbTimeouts.repeatAppend(timeouts, new TimeoutSymbol<>());
        return wbTimeouts.toWord();
    }

    @Test
    public void testOverApproxReset() {
        // Infers a missing local reset instead of a missing discriminator first.
        var model = LocalTimerMealyTestUtil.automatonFromFile("over_approx_reset.dot");

        // Missing discriminator at non-del in stable config:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                Word.fromSymbols(new TimeStepSymbol<>(), new NonDelayingInput<>("i"), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
    }

    @Test
    public void testRecursiveDecomp() {
        // Triggers recursive decomposition
        var model = LocalTimerMealyTestUtil.automatonFromFile("recursive_decomp.dot", 3);

        // Missing discriminator at non-del in stable config:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p"), new NonDelayingInput<>("f")),

                Word.fromSymbols(new NonDelayingInput<>("u"),
                        new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                        new NonDelayingInput<>("f")),

                Word.fromSymbols(new NonDelayingInput<>("u"),
                        new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                        new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
    }

    @Test
    public void testMissingDiscriminators() {
        var model = LocalTimerMealyExamples.SensorCollector();

        // Missing discriminator at non-del in stable config:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromSymbols(new NonDelayingInput<>("p2"), new TimeStepSymbol<>(), new NonDelayingInput<>("abort"), new TimeoutSymbol<>())
        );

        // Missing discriminator at one-shot:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromSymbols(new NonDelayingInput<>("p2"), new TimeoutSymbol<>(), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingResets() {
        var model = LocalTimerMealyExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing reset in stable config:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromSymbols(new NonDelayingInput<>("p1"), new TimeStepSymbol<>(), new NonDelayingInput<>("abort"), new TimeoutSymbol<>())
        );

        // Missing reset in non-stable config:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromSymbols(new NonDelayingInput<>("p1"),
                        new TimeStepSymbol<>(), new TimeStepSymbol<>(), new TimeStepSymbol<>(),
                        new NonDelayingInput<>("abort"), new TimeoutSymbol<>())
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
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromWords(Word.fromLetter(new NonDelayingInput<>("p1")), getTimeoutSequence(14)
                )
        );

        // Missing one-shot in location with single timer:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromSymbols(new NonDelayingInput<>("p2"), new TimeoutSymbol<>(), new TimeoutSymbol<>())
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }

    @Test
    public void testMissingOneShotModelA() {
        var model = LocalTimerMealyExamples.SensorCollector();
        model.params().setMaxTimerQueryWaitingTime(40);

        // Missing one-shot via bad output:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex1 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromWords(Word.fromLetter(new NonDelayingInput<>("p1")),
                        getTimeStepSequence(40),
                        Word.fromLetter(new TimeoutSymbol<>())) // alternatively: new NonDelayingInput<>("abort")
        );

        // Missing one-shot via bad target:
        List<Word<LocalTimerMealySemanticInputSymbol<String>>> cex2 = List.of(
                // Initial hyp:
                Word.fromSymbols(new NonDelayingInput<>("p1"), new NonDelayingInput<>("p1")),
                Word.fromSymbols(new NonDelayingInput<>("p2"), new NonDelayingInput<>("abort")),

                Word.fromWords(Word.fromLetter(new NonDelayingInput<>("p1")),
                        getTimeoutSequence(14),
                        Word.fromLetter(new NonDelayingInput<>("collect")),
                        Word.fromLetter(new NonDelayingInput<>("p1"))
                )
        );

        learnModel(model, cex1);
        learnModel(model, cex2);
    }


}
