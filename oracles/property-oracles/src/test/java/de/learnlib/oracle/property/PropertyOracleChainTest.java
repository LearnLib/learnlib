/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.oracle.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.learnlib.oracle.PropertyOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertyOracleChainTest {

    @Test
    public void testEmpty() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new PropertyOracleChain<>(Collections.emptyList()));
    }

    @Test
    public void testDifferentProperties() {
        var oracle1 = new BasePropertyOracle<>("p1", null, null, null);
        var oracle2 = new BasePropertyOracle<>("p2", null, null, null);

        Assert.assertThrows(IllegalArgumentException.class, () -> new PropertyOracleChain<>(oracle1, oracle2));
    }

    @Test
    public void testDisprove() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> dfa = new CompactDFA<>(alphabet);
        final DefaultQuery<Character, Boolean> ce1 = new DefaultQuery<>(Word.fromString("abc"), true);
        final DefaultQuery<Character, Boolean> ce2 = new DefaultQuery<>(Word.fromString("def"), true);

        // use case 1: ce found on first hit
        Oracle<Character> o1 = new Oracle<>(dfa, alphabet, ce1);
        Oracle<Character> o2 = new Oracle<>(dfa, alphabet, ce2);
        Oracle<Character> o3 = new Oracle<>(dfa, alphabet);

        var chain = new PropertyOracleChain<>(o1, o2, o3);
        DefaultQuery<Character, Boolean> ce = chain.disprove(dfa, alphabet);

        Assert.assertEquals(ce, ce1);
        Assert.assertEquals(chain.getCounterExample(), ce1);
        Assert.assertTrue(o1.disproveCalled);
        Assert.assertFalse(o2.disproveCalled);
        Assert.assertFalse(o3.disproveCalled);

        // use case 2: ce found on last element
        o1 = new Oracle<>(dfa, alphabet);
        o2 = new Oracle<>(dfa, alphabet);
        o3 = new Oracle<>(dfa, alphabet, ce2);

        chain = new PropertyOracleChain<>(o1, o2, o3);
        ce = chain.disprove(dfa, alphabet);

        Assert.assertEquals(ce, ce2);
        Assert.assertEquals(chain.getCounterExample(), ce2);
        Assert.assertTrue(o1.disproveCalled);
        Assert.assertTrue(o2.disproveCalled);
        Assert.assertTrue(o3.disproveCalled);

        // use case 3: no ce found
        o1 = new Oracle<>(dfa, alphabet);
        o2 = new Oracle<>(dfa, alphabet);
        o3 = new Oracle<>(dfa, alphabet);

        chain = new PropertyOracleChain<>(o1, o2, o3);
        ce = chain.disprove(dfa, alphabet);

        Assert.assertNull(ce);
        Assert.assertNull(chain.getCounterExample());
        Assert.assertTrue(o1.disproveCalled);
        Assert.assertTrue(o2.disproveCalled);
        Assert.assertTrue(o3.disproveCalled);
    }

    @Test
    public void testFindCounterExample() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> dfa = new CompactDFA<>(alphabet);
        final DefaultQuery<Character, Boolean> ce1 = new DefaultQuery<>(Word.fromString("abc"), true);
        final DefaultQuery<Character, Boolean> ce2 = new DefaultQuery<>(Word.fromString("def"), true);

        // use case 1: ce found on first hit
        Oracle<Character> o1 = new Oracle<>(dfa, alphabet, ce1);
        Oracle<Character> o2 = new Oracle<>(dfa, alphabet, ce2);
        Oracle<Character> o3 = new Oracle<>(dfa, alphabet);

        var chain = new PropertyOracleChain<>(Arrays.asList(o1, o2, o3));
        DefaultQuery<Character, Boolean> ce = chain.doFindCounterExample(dfa, alphabet);

        Assert.assertEquals(ce, ce1);
        Assert.assertNull(chain.getCounterExample());
        Assert.assertTrue(o1.doFindCounterExampleCalled);
        Assert.assertFalse(o2.doFindCounterExampleCalled);
        Assert.assertFalse(o3.doFindCounterExampleCalled);

        // use case 2: ce found on last element
        o1 = new Oracle<>(dfa, alphabet);
        o2 = new Oracle<>(dfa, alphabet);
        o3 = new Oracle<>(dfa, alphabet, ce2);

        chain = new PropertyOracleChain<>(Arrays.asList(o1, o2, o3));
        ce = chain.doFindCounterExample(dfa, alphabet);

        Assert.assertEquals(ce, ce2);
        Assert.assertNull(chain.getCounterExample());
        Assert.assertTrue(o1.doFindCounterExampleCalled);
        Assert.assertTrue(o2.doFindCounterExampleCalled);
        Assert.assertTrue(o3.doFindCounterExampleCalled);

        // use case 3: no ce found
        o1 = new Oracle<>(dfa, alphabet);
        o2 = new Oracle<>(dfa, alphabet);
        o3 = new Oracle<>(dfa, alphabet);

        chain = new PropertyOracleChain<>(Arrays.asList(o1, o2, o3));
        ce = chain.doFindCounterExample(dfa, alphabet);

        Assert.assertNull(ce);
        Assert.assertNull(chain.getCounterExample());
        Assert.assertTrue(o1.doFindCounterExampleCalled);
        Assert.assertTrue(o2.doFindCounterExampleCalled);
        Assert.assertTrue(o3.doFindCounterExampleCalled);
    }

    private static class Oracle<I> implements PropertyOracle<I, DFA<?, I>, String, Boolean> {

        private boolean disproveCalled;
        private boolean doFindCounterExampleCalled;

        private final DFA<?, I> hypothesis;
        private final Collection<? extends I> inputs;
        private final @Nullable DefaultQuery<I, Boolean> ce;

        Oracle(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
            this(hypothesis, inputs, null);
        }

        Oracle(DFA<?, I> hypothesis, Collection<? extends I> inputs, @Nullable DefaultQuery<I, Boolean> ce) {
            this.hypothesis = hypothesis;
            this.inputs = inputs;
            this.ce = ce;
        }

        @Override
        public String getProperty() {
            return "";
        }

        @Override
        public @Nullable DefaultQuery<I, Boolean> getCounterExample() {
            return ce;
        }

        @Override
        public @Nullable DefaultQuery<I, Boolean> disprove(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
            Assert.assertEquals(hypothesis, this.hypothesis);
            Assert.assertEquals(inputs, this.inputs);
            this.disproveCalled = true;
            return ce;
        }

        @Override
        public @Nullable DefaultQuery<I, Boolean> doFindCounterExample(DFA<?, I> hypothesis,
                                                                       Collection<? extends I> inputs) {
            Assert.assertEquals(hypothesis, this.hypothesis);
            Assert.assertEquals(inputs, this.inputs);
            this.doFindCounterExampleCalled = true;
            return ce;
        }
    }
}
