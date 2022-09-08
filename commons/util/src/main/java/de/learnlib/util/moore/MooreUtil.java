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
package de.learnlib.util.moore;

import java.util.Objects;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class helping to unify various approaches to actively learning Moore machines.
 *
 * @author frohme
 */
public final class MooreUtil {

    public static final int NO_MISMATCH = -1;

    private MooreUtil() {
        // prevent instantiation
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

    public static <I, O> @Nullable DefaultQuery<I, O> reduceCounterExample(MooreMachine<?, I, ?, O> hypothesis,
                                                                           DefaultQuery<I, Word<O>> ceQuery) {
        Word<I> cePrefix = ceQuery.getPrefix(), ceSuffix = ceQuery.getSuffix();
        Word<O> hypOut = hypothesis.computeSuffixOutput(cePrefix, ceSuffix);
        Word<O> ceOut = ceQuery.getOutput();
        assert ceOut.length() == hypOut.length();

        int mismatchIdx = findMismatch(hypOut, ceOut);
        if (mismatchIdx == NO_MISMATCH) {
            return null;
        }

        return new DefaultQuery<>(cePrefix, ceSuffix.prefix(mismatchIdx), ceOut.getSymbol(mismatchIdx));
    }

    public static <M extends MooreMachine<?, I, ?, O>, I, O> MooreLearner<I, O> wrapSymbolLearner(LearningAlgorithm<M, I, O> learner) {
        return new MooreLearnerWrapper<>(learner);
    }

    public static <I, O> MembershipOracle<I, @Nullable O> wrapWordOracle(MembershipOracle<I, Word<O>> oracle) {
        return new SymbolOracleWrapper<>(oracle);
    }

}
