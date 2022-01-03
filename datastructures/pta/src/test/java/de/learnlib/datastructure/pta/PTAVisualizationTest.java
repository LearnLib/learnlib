/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.datastructure.pta;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.CharStreams;
import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class PTAVisualizationTest {

    private final Alphabet<Character> alphabet;
    private final BlueFringePTA<Character, Void> moorePTA;

    public PTAVisualizationTest() {
        this.alphabet = Alphabets.characters('x', 'z');
        //@formatter:off
        final CompactMoore<Character, Character> moore =
                AutomatonBuilders.<Character, Character>newMoore(alphabet).withInitial(0)
                                                                          .from(0).on('z').to(1)
                                                                          .from(0).on('x', 'y').to(3)
                                                                          .from(1).on('x', 'z').to(3)
                                                                          .from(1).on('y').to(2)
                                                                          .from(2).on('y').to(3)
                                                                          .from(3).on('x').loop()
                                                                          .from(3).on('z').to(2)
                                                                          .withOutput(0, 'b')
                                                                          .withOutput(1, 'a')
                                                                          .withOutput(2, 'a')
                                                                          .withOutput(3, 'c')
                                                                          .create();
        //@formatter:on

        final List<Word<Character>> traces = new ArrayList<>();
        Covers.transitionCover(moore, alphabet, traces);

        this.moorePTA = new BlueFringePTA<>(alphabet.size());

        for (final Word<Character> trace : traces) {
            this.moorePTA.addSampleWithStateProperties(trace.toIntArray(alphabet), moore.computeOutput(trace).asList());
        }
    }

    @Test
    public void testVisualization() throws IOException {
        final StringWriter actualPTA = new StringWriter();
        GraphDOT.write(this.moorePTA.graphView(alphabet), actualPTA);

        final String expectedPTA =
                CharStreams.toString(IOUtil.asBufferedUTF8Reader(PTAVisualizationTest.class.getResourceAsStream(
                        "/pta.dot")));

        Assert.assertEquals(actualPTA.toString(), expectedPTA);
    }
}
