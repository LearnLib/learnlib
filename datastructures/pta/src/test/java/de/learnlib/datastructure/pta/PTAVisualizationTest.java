/* Copyright (C) 2013-2023 TU Dortmund
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
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.transducer.CompactMoore;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PTAVisualizationTest {

    private final BlueFringePTA<Character, Void> pta;

    public PTAVisualizationTest() {
        final Alphabet<Character> alphabet = Alphabets.characters('x', 'z');
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

        this.pta = new BlueFringePTA<>(alphabet.size());

        for (Word<Character> trace : traces) {
            this.pta.addSampleWithStateProperties(trace.asIntSeq(alphabet), moore.computeOutput(trace).asList());
        }
    }

    @Test
    public void testVisualization() throws IOException {
        final StringWriter actualPTA = new StringWriter();
        GraphDOT.write(this.pta, actualPTA);

        final String expectedPTA =
                CharStreams.toString(IOUtil.asBufferedUTF8Reader(PTAVisualizationTest.class.getResourceAsStream(
                        "/pta.dot")));

        Assert.assertEquals(actualPTA.toString(), expectedPTA);
    }
}
