/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.util;

import java.util.Collection;
import java.util.Objects;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.OmegaQueryAnswerer;
import de.learnlib.oracle.QueryAnswerer;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.OmegaQuery;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MQUtil {

    private MQUtil() {
        // prevent instantiation
    }

    public static <I, D> DefaultQuery<I, D> normalize(MembershipOracle<I, D> oracle, DefaultQuery<I, D> query) {
        if (query.isNormalized()) {
            return query;
        }
        return query(oracle, Word.epsilon(), query.getInput());
    }

    public static <I, D> DefaultQuery<I, D> query(MembershipOracle<I, D> oracle, Word<I> prefix, Word<I> suffix) {
        DefaultQuery<I, D> qry = new DefaultQuery<>(prefix, suffix);
        oracle.processQuery(qry);
        return qry;
    }

    public static <I, D> DefaultQuery<I, D> query(MembershipOracle<I, D> oracle, Word<I> queryWord) {
        return query(oracle, Word.epsilon(), queryWord);
    }

    public static <I, D> void answerQueries(QueryAnswerer<I, D> answerer, Collection<? extends Query<I, D>> queries) {
        for (Query<I, D> query : queries) {
            Word<I> prefix = query.getPrefix();
            Word<I> suffix = query.getSuffix();
            D answer = answerer.answerQuery(prefix, suffix);
            query.answer(answer);
        }
    }

    public static <S, I, D> void answerOmegaQueries(OmegaQueryAnswerer<S, I, D> answerer,
                                                    Collection<? extends OmegaQuery<I, D>> queries) {
        for (OmegaQuery<I, D> query : queries) {
            final Word<I> prefix = query.getPrefix();
            final Word<I> loop = query.getLoop();
            final int repeat = query.getRepeat();
            Pair<@Nullable D, Integer> answer = answerer.answerQuery(prefix, loop, repeat);
            query.answer(answer.getFirst(), answer.getSecond());
        }
    }

    public static <I, D> boolean isCounterexample(DefaultQuery<I, D> query, SuffixOutput<I, D> hyp) {
        D qryOut = query.getOutput();
        D hypOut = hyp.computeSuffixOutput(query.getPrefix(), query.getSuffix());
        return !Objects.equals(qryOut, hypOut);
    }
}
