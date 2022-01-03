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
package de.learnlib.filter.statistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.api.query.Query;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public final class TestQueries {

    public static final String COUNTER_NAME = "testCounter";

    public static final Alphabet<Integer> INPUTS;
    public static final Alphabet<Character> OUTPUTS;
    public static final CompactMealy<Integer, Character> DELEGATE;

    static {
        INPUTS = Alphabets.integers(1, 3);
        OUTPUTS = Alphabets.characters('a', 'c');
        DELEGATE = RandomAutomata.randomMealy(new Random(42), 10, INPUTS, OUTPUTS);
    }

    private TestQueries() {
        // prevent instantiation
    }

    public static <I, D> Collection<Query<I, D>> createNoopQueries(int numQueries) {
        List<Query<I, D>> result = new ArrayList<>(numQueries);
        for (int i = 0; i < numQueries; i++) {
            result.add(new NoopQuery<>(Word.epsilon()));
        }
        return result;
    }

    public static <I, D> Collection<Query<I, D>> createNoopQueries(int numQueries,
                                                                   int numInputs,
                                                                   Collection<I> inputs) {

        final Random r = new Random(42);
        final List<? extends I> inputsAsList = CollectionsUtil.randomAccessList(inputs);

        List<Query<I, D>> result = new ArrayList<>(numQueries);
        for (int i = 0; i < numQueries; i++) {
            result.add(new NoopQuery<>(Word.fromList(RandomUtil.sample(inputsAsList, numInputs, r))));
        }
        return result;
    }

}
