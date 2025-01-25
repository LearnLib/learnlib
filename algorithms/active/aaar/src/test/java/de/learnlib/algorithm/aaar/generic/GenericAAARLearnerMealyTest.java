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
import java.util.function.Function;

import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.AbstractAAARTest;
import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.example.mealy.ExampleGrid;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GenericAAARLearnerMealyTest
        extends AbstractAAARTest<GenericAAARLearnerMealy<?, Character, Character, Integer>, Character, Word<Integer>, MealyMachine<?, Character, ?, Integer>> {

    private static final int SIZE = 4;

    public GenericAAARLearnerMealyTest() {
        super(ExampleGrid.createExample(SIZE, SIZE));
    }

    @Override
    protected GenericAAARLearnerMealy<?, Character, Character, Integer> getLearner(Alphabet<Character> alphabet,
                                                                                   MembershipOracle<Character, Word<Integer>> oracle) {
        return new GenericAAARLearnerMealy<>(AAARTestUtil.<Character, Integer>getMealyLearners().get(0).getSecond(),
                                             oracle,
                                             alphabet.getSymbol(0),
                                             Function.identity());
    }

    @Test(dependsOnMethods = "testAbstractHypothesisEquivalence")
    public void testTreeSerialization() throws IOException {
        final AbstractAbstractionTree<Character, Character, Word<Integer>> tree =
                super.aaarLearner.getAbstractionTree();

        try (Reader r = IOUtil.asBufferedUTF8Reader(GenericAAARLearnerMealyTest.class.getResourceAsStream(
                "/tree_mealy.dot"));
             Writer w = new StringWriter()) {

            final String expected = IOUtil.toString(r);
            GraphDOT.write((GraphViewable) tree, w);

            Assert.assertEquals(w.toString(), expected);
        }
    }
}
