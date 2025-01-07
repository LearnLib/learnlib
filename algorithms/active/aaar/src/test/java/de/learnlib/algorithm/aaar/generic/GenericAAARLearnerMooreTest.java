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
package de.learnlib.algorithm.aaar.generic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;
import java.util.function.Function;

import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.AbstractAAARTest;
import de.learnlib.algorithm.aaar.ComboConstructor;
import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.example.moore.ExampleRandomMoore;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GenericAAARLearnerMooreTest
        extends AbstractAAARTest<GenericAAARLearnerMoore<?, Character, Character, String>, Character, Word<String>, MooreMachine<?, Character, ?, String>> {

    private static final Alphabet<Character> RANDOM_ALPHABET = Alphabets.characters('a', 'c');
    private static final int RANDOM_SIZE = 40;
    private static final String[] RANDOM_OUTPUTS = {"o1", "o2", "o3"};
    private static final long RANDOM_SEED = 42L;

    public GenericAAARLearnerMooreTest() {
        super(ExampleRandomMoore.createExample(new Random(RANDOM_SEED), RANDOM_ALPHABET, RANDOM_SIZE, RANDOM_OUTPUTS));
    }

    @Override
    protected GenericAAARLearnerMoore<?, Character, Character, String> getLearner(Alphabet<Character> alphabet,
                                                                                  MembershipOracle<Character, Word<String>> oracle) {
        final Pair<String, ComboConstructor<? extends MooreLearner<Character, String>, Character, Word<String>>>
                provider = AAARTestUtil.<Character, String>getMooreLearners().get(3);

        // The WpMethod EQ discovered an error in the TTTMoore implementation
        Assert.assertEquals(provider.getFirst(), "TTT");

        return new GenericAAARLearnerMoore<>(AAARTestUtil.<Character, String>getMooreLearners().get(0).getSecond(),
                                             oracle,
                                             alphabet.getSymbol(0),
                                             Function.identity());
    }

    @Test(dependsOnMethods = "testAbstractHypothesisEquivalence")
    public void testTreeSerialization() throws IOException {
        final AbstractAbstractionTree<Character, Character, Word<String>> tree = super.aaarLearner.getAbstractionTree();

        try (Reader r = IOUtil.asBufferedUTF8Reader(GenericAAARLearnerMooreTest.class.getResourceAsStream(
                "/tree_moore.dot"));
             Writer w = new StringWriter()) {

            final String expected = IOUtil.toString(r);
            GraphDOT.write((GraphViewable) tree, w);

            Assert.assertEquals(w.toString(), expected);
        }
    }
}
