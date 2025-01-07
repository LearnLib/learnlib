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
package de.learnlib.util.mealy;

import java.util.Iterator;
import java.util.Objects;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class helping to unify various approaches to actively learning Mealy machines.
 */
public final class MealyUtil {

    public static final int NO_MISMATCH = -1;

    private MealyUtil() {
        // prevent instantiation
    }

    public static <I, O> int findMismatch(MealyMachine<?, I, ?, O> hypothesis, Word<I> input, Word<O> output) {
        return doFindMismatch(hypothesis, input, output);
    }

    public static <O> int findMismatch(Word<O> out1, Word<O> out2) {
        int len = out1.length();
        assert len == out2.length();

        for (int i = 0; i < len; i++) {
            O sym1 = out1.getSymbol(i);
            O sym2 = out2.getSymbol(i);

            if (!Objects.equals(sym1, sym2)) {
                return i;
            }
        }

        return NO_MISMATCH;
    }

    private static <S, I, T, O> int doFindMismatch(MealyMachine<S, I, T, O> hypothesis, Word<I> input, Word<O> output) {
        S state = hypothesis.getInitialState();

        if (state == null) {
            return NO_MISMATCH;
        }

        Iterator<I> inIt = input.iterator();
        Iterator<O> outIt = output.iterator();
        int i = 0;

        while (inIt.hasNext() && outIt.hasNext()) {
            final T trans = hypothesis.getTransition(state, inIt.next());
            if (trans == null) {
                return NO_MISMATCH;
            }
            final O ceOut = outIt.next();
            final O transOut = hypothesis.getTransitionOutput(trans);
            if (!Objects.equals(transOut, ceOut)) {
                return i;
            }
            state = hypothesis.getSuccessor(trans);
            i++;
        }

        return NO_MISMATCH;
    }

    public static <I, O> @Nullable DefaultQuery<I, Word<O>> shortenCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                                  DefaultQuery<I, Word<O>> ceQuery) {
        Word<I> cePrefix = ceQuery.getPrefix(), ceSuffix = ceQuery.getSuffix();
        Word<O> hypOut = hypothesis.computeSuffixOutput(cePrefix, ceSuffix);
        Word<O> ceOut = ceQuery.getOutput();
        assert ceOut.length() == hypOut.length();

        int mismatchIdx = findMismatch(hypOut, ceOut);
        if (mismatchIdx == NO_MISMATCH) {
            return null;
        }

        return new DefaultQuery<>(cePrefix, ceSuffix.prefix(mismatchIdx + 1), ceOut.prefix(mismatchIdx + 1));
    }

    public static <I, O> @Nullable DefaultQuery<I, O> reduceCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                           DefaultQuery<I, Word<O>> ceQuery) {
        Word<I> cePrefix = ceQuery.getPrefix(), ceSuffix = ceQuery.getSuffix();
        Word<O> hypOut = hypothesis.computeSuffixOutput(cePrefix, ceSuffix);
        Word<O> ceOut = ceQuery.getOutput();
        assert ceOut.length() == hypOut.length();

        int mismatchIdx = findMismatch(hypOut, ceOut);
        if (mismatchIdx == NO_MISMATCH) {
            return null;
        }

        return new DefaultQuery<>(cePrefix, ceSuffix.prefix(mismatchIdx + 1), ceOut.getSymbol(mismatchIdx));
    }

    public static <M extends MealyMachine<?, I, ?, O>, I, O> MealyLearner<I, O> wrapSymbolLearner(LearningAlgorithm<M, I, O> learner) {
        return new MealyLearnerWrapper<>(learner);
    }

    public static <I, O> MembershipOracle<I, @Nullable O> wrapWordOracle(MembershipOracle<I, Word<O>> oracle) {
        return new SymbolOracleWrapper<>(oracle);
    }

}
