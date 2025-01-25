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
package de.learnlib.datastructure.pta;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMoore;
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
    public void testBasePTA() throws IOException {
        try (Reader in = IOUtil.asBufferedUTF8Reader(PTAVisualizationTest.class.getResourceAsStream("/pta.dot"));
             Writer out = new StringWriter()) {

            GraphDOT.write(this.pta, out);
            Assert.assertEquals(out.toString(), IOUtil.toString(in));
        }
    }

    @Test(dependsOnMethods = "testBasePTA") // we don't really depend on it, but we should get invoked after it
    public void testPromotedPTA() throws IOException {
        final List<PTATransition<BlueFringePTAState<Character, Void>>> promotions = new ArrayList<>();
        this.pta.init(promotions::add);
        this.pta.promote(promotions.get(0).getTarget(), b -> {});

        try (Reader in = IOUtil.asBufferedUTF8Reader(PTAVisualizationTest.class.getResourceAsStream("/promoted.dot"));
             Writer out = new StringWriter()) {

            GraphDOT.write(this.pta, out);
            Assert.assertEquals(out.toString(), IOUtil.toString(in));
        }
    }
}
