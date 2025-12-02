/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.lstar;

import java.util.List;

import de.learnlib.algorithm.lstar.it.ExtensibleLStarMMLTIT.Example;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests several different cases of counterexamples.
 */
@Test
public class ExtensibleLStarMMLTCounterexampleTests {

    private static <I, O> void learnModel(MMLT<?, I, ?, O> example,
                                          MMLTModelParams<O> params,
                                          List<Word<TimedInput<I>>> counterexamples) {

        var sul = new MMLTSimulatorSUL<>(example);
        var timeOracle = new TimedSULOracle<>(sul, params);

        var learner = new ExtensibleLStarMMLT<>(example.getInputAlphabet(), params, timeOracle);

        learner.startLearning();

        for (var cex : counterexamples) {
            var output = timeOracle.answerQuery(cex);
            learner.refineHypothesis(new DefaultQuery<>(cex, output));
        }

        // Now continue until arriving at an accurate model:
        SimulatorEQOracle<I, O> simOracle = new SimulatorEQOracle<>(example);

        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> cex;
        MMLT<Integer, I, ?, O> hyp = learner.getHypothesisModel();

        while ((cex = simOracle.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet())) != null) {
            learner.refineHypothesis(cex);
            hyp = learner.getHypothesisModel();
        }

        Assert.assertEquals(learner.getObservationTable().numberOfDistinctRows(), hyp.size());
    }

    @Test
    public void testOverApproxReset() {
        // Infers a missing local reset instead of a missing discriminator first.
        var model = new Example("over_approx_reset.dot");

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 =
                List.of(Word.fromSymbols(TimedInput.step(), new InputSymbol<>("i"), new TimeoutSymbol<>()));

        learnModel(model.getReferenceAutomaton(), model.getParams(), cex1);
    }

    @Test
    public void testRecursiveDecomp() {
        // Triggers recursive decomposition
        var model = new Example("recursive_decomp.dot", 3);

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(Word.upcast(TimedInput.inputs("p", "f")),
                                                      Word.fromWords(TimedInput.inputs("u"),
                                                                     TimedInput.timeouts(4),
                                                                     TimedInput.inputs("f")),
                                                      Word.fromWords(TimedInput.inputs("u"), TimedInput.timeouts(5)));

        learnModel(model.getReferenceAutomaton(), model.getParams(), cex1);
    }

    @Test
    public void testMissingDiscriminators() {
        var model = MMLTExamples.sensorCollector();

        // Missing discriminator at non-del in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromSymbols(TimedInput.input("p2"),
                                                                       TimedInput.step(),
                                                                       TimedInput.input("abort"),
                                                                       TimedInput.timeout()));

        // Missing discriminator at one-shot:
        List<Word<TimedInput<String>>> cex2 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p2"), TimedInput.timeouts(2)));

        learnModel(model.getReferenceAutomaton(), model.getParams(), cex1);
        learnModel(model.getReferenceAutomaton(), model.getParams(), cex2);
    }

    @Test
    public void testMissingResets() {
        var model = MMLTExamples.sensorCollector();
        var p = model.getParams();
        var params = new MMLTModelParams<>(p.silentOutput(), p.outputCombiner(), p.maxTimeoutWaitingTime(), 40);

        // Missing reset in stable config:
        List<Word<TimedInput<String>>> cex1 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromSymbols(TimedInput.input("p1"),
                                                                       TimedInput.step(),
                                                                       TimedInput.input("abort"),
                                                                       TimedInput.timeout()));

        // Missing reset in non-stable config:
        List<Word<TimedInput<String>>> cex2 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p1"),
                                                                     TimedInput.steps(3),
                                                                     TimedInput.inputs("abort"),
                                                                     TimedInput.timeouts(1)));

        learnModel(model.getReferenceAutomaton(), params, cex1);
        learnModel(model.getReferenceAutomaton(), params, cex2);

    }

    @Test
    public void testMissingOneShotModelB() {
        // Setting max waiting = 6 -> all inferred timers are periodic:
        var model = MMLTExamples.sensorCollector();
        var p = model.getParams();
        var params = new MMLTModelParams<>(p.silentOutput(), p.outputCombiner(), p.maxTimeoutWaitingTime(), 6);

        // Missing one-shot via bad return to entry:
        List<Word<TimedInput<String>>> cex1 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(14)));

        // Missing one-shot in location with single timer:
        List<Word<TimedInput<String>>> cex2 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p2"), TimedInput.timeouts(2)));

        learnModel(model.getReferenceAutomaton(), params, cex1);
        learnModel(model.getReferenceAutomaton(), params, cex2);
    }

    @Test
    public void testMissingOneShotModelA() {
        var model = MMLTExamples.sensorCollector();
        var p = model.getParams();
        var params = new MMLTModelParams<>(p.silentOutput(), p.outputCombiner(), p.maxTimeoutWaitingTime(), 40);

        // Missing one-shot via bad output:
        List<Word<TimedInput<String>>> cex1 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p1"),
                                                                     TimedInput.steps(40),
                                                                     TimedInput.timeouts(1))
        );

        // Missing one-shot via bad target:
        List<Word<TimedInput<String>>> cex2 = List.of(Word.upcast(TimedInput.inputs("p1", "p1")),
                                                      Word.upcast(TimedInput.inputs("p2", "abort")),
                                                      Word.fromWords(TimedInput.inputs("p1"),
                                                                     TimedInput.timeouts(14),
                                                                     TimedInput.inputs("collect", "p1")));

        learnModel(model.getReferenceAutomaton(), params, cex1);
        learnModel(model.getReferenceAutomaton(), params, cex2);
    }

}
