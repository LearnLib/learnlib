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
package de.learnlib.util.mealy;

import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Utility class helping to unify various approaches to actively learning Mealy machines.
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public final class MealyUtil {

    public static final int NO_MISMATCH = -1;

    // Prevent instantiation
    private MealyUtil() {
        throw new IllegalStateException("Constructor should never be invoked");
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
        int i = 0;
        S state = hypothesis.getInitialState();

        Iterator<I> inIt = input.iterator();
        Iterator<O> outIt = output.iterator();

        while (inIt.hasNext() && outIt.hasNext()) {
            T trans = hypothesis.getTransition(state, inIt.next());
            O ceOut = outIt.next();
            O transOut = hypothesis.getTransitionOutput(trans);
            if (!Objects.equals(transOut, ceOut)) {
                return i;
            }
            state = hypothesis.getSuccessor(trans);
            i++;
        }

        return NO_MISMATCH;
    }

    @Nullable
    public static <I, O> DefaultQuery<I, Word<O>> shortenCounterExample(MealyMachine<?, I, ?, O> hypothesis,
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

    @Nullable
    public static <I, O> DefaultQuery<I, O> reduceCounterExample(MealyMachine<?, I, ?, O> hypothesis,
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

    @Nonnull
    public static <M extends MealyMachine<?, I, ?, O>, I, O> LearningAlgorithm.MealyLearner<I, O> wrapSymbolLearner(
            LearningAlgorithm<M, I, O> learner) {
        return new MealyLearnerWrapper<>(learner);
    }

    @Nonnull
    public static <I, O> MembershipOracle<I, O> wrapWordOracle(MembershipOracle<I, Word<O>> oracle) {
        return new SymbolOracleWrapper<>(oracle);
    }

}
