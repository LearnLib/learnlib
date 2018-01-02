/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * @author Maik Merten
 */
public class RandomWordsEQOracle<A extends Output<I, D>, I, D> extends AbstractTestWordEQOracle<A, I, D> {

    private final Random random;
    private final int maxTests;
    private final int minLength;
    private final int maxLength;

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle,
                               int minLength,
                               int maxLength,
                               int maxTests) {
        this(mqOracle, minLength, maxLength, maxTests, new Random(), 1);
    }

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle,
                               int minLength,
                               int maxLength,
                               int maxTests,
                               Random random) {
        this(mqOracle, minLength, maxLength, maxTests, random, 1);
    }

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle,
                               int minLength,
                               int maxLength,
                               int maxTests,
                               Random random,
                               int batchSize) {
        super(mqOracle, batchSize);
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.random = random;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {

        final List<? extends I> symbolList;
        if (inputs instanceof List) {
            symbolList = (List<? extends I>) inputs;
        } else {
            symbolList = new ArrayList<>(inputs);
        }

        return Stream.generate(() -> generateTestWord(symbolList, symbolList.size())).limit(maxTests);
    }

    private Word<I> generateTestWord(List<? extends I> symbolList, int numSyms) {

        final int length = minLength + random.nextInt((maxLength - minLength) + 1);
        final WordBuilder<I> result = new WordBuilder<>(length);

        for (int j = 0; j < length; ++j) {
            int symidx = random.nextInt(numSyms);
            I sym = symbolList.get(symidx);
            result.append(sym);
        }

        return result.toWord();
    }

    public static class DFARandomWordsEQOracle<I> extends RandomWordsEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFARandomWordsEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                      int minLength,
                                      int maxLength,
                                      int maxTests,
                                      Random random) {
            super(mqOracle, minLength, maxLength, maxTests, random);
        }

        public DFARandomWordsEQOracle(MembershipOracle<I, Boolean> mqOracle,
                                      int minLength,
                                      int maxLength,
                                      int maxTests,
                                      Random random,
                                      int batchSize) {
            super(mqOracle, minLength, maxLength, maxTests, random, batchSize);
        }
    }

    public static class MealyRandomWordsEQOracle<I, O> extends RandomWordsEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        public MealyRandomWordsEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                        int minLength,
                                        int maxLength,
                                        int maxTests,
                                        Random random) {
            super(mqOracle, minLength, maxLength, maxTests, random);
        }

        public MealyRandomWordsEQOracle(MembershipOracle<I, Word<O>> mqOracle,
                                        int minLength,
                                        int maxLength,
                                        int maxTests,
                                        Random random,
                                        int batchSize) {
            super(mqOracle, minLength, maxLength, maxTests, random, batchSize);
        }
    }
}
