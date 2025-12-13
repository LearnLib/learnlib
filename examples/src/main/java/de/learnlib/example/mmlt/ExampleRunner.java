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
package de.learnlib.example.mmlt;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.visualization.MMLTVisualizationHelper;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.visualization.Visualization;

@SuppressWarnings({"PMD.SystemPrintln", "PMD.UseExplicitTypes"}) // allow for sysouts and vars in examples
final class ExampleRunner {

    private ExampleRunner() {
        // prevent instantiation
    }

    static MMLT<Integer, String, ?, String> runExperiment(ExtensibleLStarMMLT<String, String> learner,
                                                          EquivalenceOracle.MMLTEquivalenceOracle<String, String> tester,
                                                          Alphabet<TimedInput<String>> alphabet) {
        // Start learning:
        final var experiment = new Experiment<>(learner, tester, alphabet);
        experiment.run();

        final var finalHypothesis = experiment.getFinalHypothesis();

        // Print final result + statistics:
        System.out.println(Statistics.getService().print());

        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        System.out.println("Final hypothesis:");
        Visualization.visualize(finalHypothesis.graphView(),
                                new MMLTVisualizationHelper<>(finalHypothesis, true, true));

        return finalHypothesis;
    }
}
