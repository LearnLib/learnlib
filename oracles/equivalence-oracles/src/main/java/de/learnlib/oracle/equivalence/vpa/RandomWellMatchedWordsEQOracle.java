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
package de.learnlib.oracle.equivalence.vpa;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.AbstractTestWordEQOracle;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * An equivalence oracle based on the generation of random (well-matched) words.
 *
 * @param <I>
 *         input symbol type
 */
public final class RandomWellMatchedWordsEQOracle<I> extends AbstractTestWordEQOracle<Output<I, Boolean>, I, Boolean> {

    private final Random random;

    private final double callProb;

    private final int maxTests, minLength, maxLength;

    public RandomWellMatchedWordsEQOracle(Random random,
                                          MembershipOracle<I, Boolean> oracle,
                                          double callProb,
                                          int maxTests,
                                          int minLength,
                                          int maxLength) {
        this(random, oracle, callProb, maxTests, minLength, maxLength, 1);
    }

    public RandomWellMatchedWordsEQOracle(Random random,
                                          MembershipOracle<I, Boolean> oracle,
                                          double callProb,
                                          int maxTests,
                                          int minLength,
                                          int maxLength,
                                          int batchSize) {
        super(oracle, batchSize);

        if (minLength > maxLength) {
            throw new IllegalArgumentException("minLength is smaller than maxLength");
        }

        this.random = random;
        this.callProb = callProb;
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(Output<I, Boolean> hypothesis, Collection<? extends I> inputs) {

        if (!(inputs instanceof VPAlphabet)) {
            throw new IllegalArgumentException(
                    "In order to generate well-matched words, a structured alphabet is required");
        }

        @SuppressWarnings("unchecked")
        final VPAlphabet<I> alphabet = (VPAlphabet<I>) inputs;

        final int lengthRange = maxLength - minLength + 1;
        return Stream.generate(() -> generateWellMatched(alphabet, minLength + random.nextInt(lengthRange)))
                     .limit(maxTests);
    }

    private Word<I> generateWellMatched(VPAlphabet<I> alphabet, int len) {
        WordBuilder<I> wb = new WordBuilder<>(len);
        generateWellMatched(wb, alphabet, len);
        return wb.toWord();
    }

    private void generateWellMatched(WordBuilder<I> wb, VPAlphabet<I> alphabet, int length) {
        if (length == 0) {
            return;
        }
        if (length == 1) {
            wb.append(alphabet.getInternalSymbol(random.nextInt(alphabet.getNumInternals())));
            return;
        }
        double act = random.nextDouble();
        if (act < callProb) {
            boolean dir = random.nextBoolean();
            if (dir) {
                final int cpos = random.nextInt(length - 1);
                generateWellMatched(wb, alphabet, cpos);
                wb.append(alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())));
                final int rpos = cpos + 1 + random.nextInt(length - cpos - 1);
                generateWellMatched(wb, alphabet, rpos - cpos - 1);
                wb.append(alphabet.getReturnSymbol(random.nextInt(alphabet.getNumReturns())));
                generateWellMatched(wb, alphabet, length - rpos - 1);
            } else {
                final int rpos = 1 + random.nextInt(length - 1);
                final int cpos = random.nextInt(rpos);
                generateWellMatched(wb, alphabet, cpos);
                wb.append(alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())));
                generateWellMatched(wb, alphabet, rpos - cpos - 1);
                wb.append(alphabet.getReturnSymbol(random.nextInt(alphabet.getNumReturns())));
                generateWellMatched(wb, alphabet, length - rpos - 1);
            }
        } else {
            final int sep = 1 + random.nextInt(length - 1);
            generateWellMatched(wb, alphabet, sep);
            generateWellMatched(wb, alphabet, length - sep);
        }
    }

}
