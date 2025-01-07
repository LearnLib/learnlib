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
package de.learnlib.algorithm.observationpack.vpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Random;

import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.vpa.SimulatorEQOracle;
import de.learnlib.oracle.membership.SEVPASimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.automaton.vpa.impl.DefaultOneSEVPA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DTVisualizationTest {

    private final OPLearnerVPA<Character> learner;

    public DTVisualizationTest() {
        final DefaultVPAlphabet<Character> alphabet = new DefaultVPAlphabet<>(Alphabets.characters('a', 'c'),
                                                                              Alphabets.characters('1', '3'),
                                                                              Alphabets.characters('x', 'z'));
        final DefaultOneSEVPA<Character> vpa =
                RandomAutomata.randomOneSEVPA(new Random(42), 10, alphabet, 0.5, 0.5, true);

        final DFAMembershipOracle<Character> mqo = new SEVPASimulatorOracle<>(vpa);
        final SimulatorEQOracle<Character> eqo = new SimulatorEQOracle<>(vpa);
        this.learner = new OPLearnerVPA<>(alphabet, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);

        final Experiment<OneSEVPA<?, Character>> exp = new Experiment<>(learner, eqo, alphabet);
        exp.run();
    }

    @Test
    public void testVisualizeHyp() throws IOException {
        final String expectedHyp = resourceAsString("/hyp.dot");

        final StringWriter actualHyp = new StringWriter();
        GraphDOT.write(this.learner.getHypothesisModel(), actualHyp);

        Assert.assertEquals(actualHyp.toString(), expectedHyp);
    }

    @Test
    public void testVisualizeDT() throws IOException {
        final String expectedDT = resourceAsString("/dt.dot");

        final StringWriter actualDT = new StringWriter();
        GraphDOT.write(this.learner.getDiscriminationTree(), actualDT);

        Assert.assertEquals(actualDT.toString(), expectedDT);
    }

    private String resourceAsString(String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            assert is != null;
            return IOUtil.toString(IOUtil.asBufferedUTF8Reader(is));
        }
    }
}
