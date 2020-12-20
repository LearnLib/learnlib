/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.automata.transducers;

import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SubsequentialTransducersTest {

    private static final Alphabet<Character> INPUTS = Alphabets.characters('a', 'c');

    @Test
    public void testAlreadyOnwardModel() {

        final CompactSST<Character, Character> sst = new CompactSST<>(INPUTS);

        final int s1 = sst.addInitialState(Word.fromCharSequence("x"));
        final int s2 = sst.addState(Word.fromCharSequence("x"));
        final int s3 = sst.addState(Word.fromCharSequence("x"));

        sst.setTransition(s1, (Character) 'a', s2, Word.fromCharSequence("x"));
        sst.setTransition(s1, (Character) 'b', s2, Word.fromCharSequence("y"));
        sst.setTransition(s1, (Character) 'c', s2, Word.fromCharSequence("z"));

        sst.setTransition(s2, (Character) 'a', s3, Word.fromCharSequence("x"));
        sst.setTransition(s2, (Character) 'b', s3, Word.fromCharSequence("y"));
        sst.setTransition(s2, (Character) 'c', s3, Word.fromCharSequence("z"));

        sst.setTransition(s3, (Character) 'a', s3, Word.fromCharSequence("x"));
        sst.setTransition(s3, (Character) 'b', s3, Word.fromCharSequence("y"));
        sst.setTransition(s3, (Character) 'c', s3, Word.fromCharSequence("z"));

        final CompactSST<Character, Character> osst =
                SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS), false);

        Assert.assertTrue(SubsequentialTransducers.isOnwardSST(osst, INPUTS));
        Assert.assertTrue(Automata.testEquivalence(sst, osst, INPUTS));
    }

    @Test
    public void testPartialModel() {

        final CompactSST<Character, Character> sst = new CompactSST<>(INPUTS);

        final int s1 = sst.addInitialState(Word.fromCharSequence("x"));
        final int s2 = sst.addState(Word.fromCharSequence("x"));
        final int s3 = sst.addState(Word.fromCharSequence("x"));

        sst.setTransition(s1, (Character) 'a', s2, Word.fromCharSequence("x"));
        sst.setTransition(s1, (Character) 'b', s2, Word.fromCharSequence("y"));
        sst.setTransition(s1, (Character) 'c', s2, Word.fromCharSequence("z"));

        sst.setTransition(s2, (Character) 'a', s3, Word.fromCharSequence("x"));
        sst.setTransition(s2, (Character) 'b', s3, Word.fromCharSequence("y"));
        sst.setTransition(s2, (Character) 'c', s3, Word.fromCharSequence("z"));

        final CompactSST<Character, Character> osst =
                SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS), false);

        Assert.assertTrue(SubsequentialTransducers.isOnwardSST(osst, INPUTS));

        final CompactSST<Character, Character> expected = new CompactSST<>(INPUTS);

        final int e1 = expected.addInitialState(Word.fromCharSequence("x"));
        final int e2 = expected.addState(Word.fromCharSequence("x"));
        final int e3 = expected.addState(Word.epsilon());

        expected.setTransition(e1, (Character) 'a', e2, Word.fromCharSequence("x"));
        expected.setTransition(e1, (Character) 'b', e2, Word.fromCharSequence("y"));
        expected.setTransition(e1, (Character) 'c', e2, Word.fromCharSequence("z"));

        expected.setTransition(e2, (Character) 'a', e3, Word.fromCharSequence("xx"));
        expected.setTransition(e2, (Character) 'b', e3, Word.fromCharSequence("yx"));
        expected.setTransition(e2, (Character) 'c', e3, Word.fromCharSequence("zx"));

        Assert.assertTrue(Automata.testEquivalence(expected, osst, INPUTS));
    }

    @Test
    public void testMultiplePropagations() {

        final CompactSST<Character, Character> sst = new CompactSST<>(INPUTS);

        final int s1 = sst.addInitialState(Word.fromCharSequence("x"));
        final int s2 = sst.addState(Word.fromCharSequence("xx"));
        final int s3 = sst.addState(Word.fromCharSequence("x"));

        sst.setTransition(s1, (Character) 'a', s2, Word.fromCharSequence("x"));
        sst.setTransition(s1, (Character) 'b', s2, Word.fromCharSequence("y"));
        sst.setTransition(s1, (Character) 'c', s2, Word.fromCharSequence("z"));

        sst.setTransition(s2, (Character) 'a', s3, Word.fromCharSequence("x"));
        sst.setTransition(s2, (Character) 'b', s3, Word.fromCharSequence("x"));
        sst.setTransition(s2, (Character) 'c', s3, Word.fromCharSequence("x"));

        sst.setTransition(s3, (Character) 'a', s3, Word.fromCharSequence("xx"));
        sst.setTransition(s3, (Character) 'b', s3, Word.fromCharSequence("xy"));
        sst.setTransition(s3, (Character) 'c', s3, Word.fromCharSequence("xz"));

        final CompactSST<Character, Character> osst =
                SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS), false);

        Assert.assertTrue(SubsequentialTransducers.isOnwardSST(osst, INPUTS));

        final CompactSST<Character, Character> expected = new CompactSST<>(INPUTS);

        final int e1 = expected.addInitialState(Word.fromCharSequence("x"));
        final int e2 = expected.addState(Word.epsilon());
        final int e3 = expected.addState(Word.epsilon());

        expected.setTransition(e1, (Character) 'a', e2, Word.fromCharSequence("xxx"));
        expected.setTransition(e1, (Character) 'b', e2, Word.fromCharSequence("yxx"));
        expected.setTransition(e1, (Character) 'c', e2, Word.fromCharSequence("zxx"));

        expected.setTransition(e2, (Character) 'a', e3, Word.epsilon());
        expected.setTransition(e2, (Character) 'b', e3, Word.epsilon());
        expected.setTransition(e2, (Character) 'c', e3, Word.epsilon());

        expected.setTransition(e3, (Character) 'a', e3, Word.fromCharSequence("xx"));
        expected.setTransition(e3, (Character) 'b', e3, Word.fromCharSequence("yx"));
        expected.setTransition(e3, (Character) 'c', e3, Word.fromCharSequence("zx"));

        Assert.assertTrue(Automata.testEquivalence(expected, osst, INPUTS));
    }

    @Test
    public void testLoopOnInitial() {

        final CompactSST<Character, Character> sst = new CompactSST<>(INPUTS);

        final int s1 = sst.addInitialState(Word.fromCharSequence("x"));
        final int s2 = sst.addState(Word.fromCharSequence("x"));
        final int s3 = sst.addState(Word.fromCharSequence("x"));

        sst.setTransition(s1, (Character) 'a', s2, Word.epsilon());
        sst.setTransition(s1, (Character) 'b', s2, Word.epsilon());
        sst.setTransition(s1, (Character) 'c', s2, Word.epsilon());

        sst.setTransition(s2, (Character) 'a', s3, Word.fromCharSequence("xx"));
        sst.setTransition(s2, (Character) 'b', s3, Word.fromCharSequence("xy"));
        sst.setTransition(s2, (Character) 'c', s3, Word.fromCharSequence("xz"));

        sst.setTransition(s3, (Character) 'a', s1, Word.fromCharSequence("xx"));
        sst.setTransition(s3, (Character) 'b', s1, Word.fromCharSequence("xy"));
        sst.setTransition(s3, (Character) 'c', s1, Word.fromCharSequence("xz"));

        final CompactSST<Character, Character> osst =
                SubsequentialTransducers.toOnwardSST(sst, INPUTS, new CompactSST<>(INPUTS), false);

        Assert.assertTrue(SubsequentialTransducers.isOnwardSST(osst, INPUTS));

        final CompactSST<Character, Character> expected = new CompactSST<>(INPUTS);

        final int e1 = expected.addInitialState(Word.fromCharSequence("x"));
        final int e2 = expected.addState(Word.epsilon());
        final int e3 = expected.addState(Word.epsilon());
        final int e4 = expected.addState(Word.epsilon());

        expected.setTransition(e1, (Character) 'a', e2, Word.fromCharSequence("x"));
        expected.setTransition(e1, (Character) 'b', e2, Word.fromCharSequence("x"));
        expected.setTransition(e1, (Character) 'c', e2, Word.fromCharSequence("x"));

        expected.setTransition(e2, (Character) 'a', e3, Word.fromCharSequence("xx"));
        expected.setTransition(e2, (Character) 'b', e3, Word.fromCharSequence("yx"));
        expected.setTransition(e2, (Character) 'c', e3, Word.fromCharSequence("zx"));

        expected.setTransition(e3, (Character) 'a', e4, Word.fromCharSequence("xx"));
        expected.setTransition(e3, (Character) 'b', e4, Word.fromCharSequence("yx"));
        expected.setTransition(e3, (Character) 'c', e4, Word.fromCharSequence("zx"));

        expected.setTransition(e4, (Character) 'a', e2, Word.epsilon());
        expected.setTransition(e4, (Character) 'b', e2, Word.epsilon());
        expected.setTransition(e4, (Character) 'c', e2, Word.epsilon());

        Assert.assertTrue(Automata.testEquivalence(expected, osst, INPUTS));
    }
}
