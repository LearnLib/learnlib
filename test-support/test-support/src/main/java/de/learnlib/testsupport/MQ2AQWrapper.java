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
package de.learnlib.testsupport;

import java.util.Collection;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Utility class that wraps a given {@link MembershipOracle} into an {@link AdaptiveMembershipOracle} by translating
 * each step of an {@link AdaptiveQuery} into a separate {@link DefaultQuery}. Not very performant, but good enough to
 * test adaptive learners in preset testing scenarios.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class MQ2AQWrapper<I, O> implements AdaptiveMembershipOracle<I, O> {

    final WordBuilder<I> wb;
    final MembershipOracle<I, Word<O>> oracle;

    public MQ2AQWrapper(MembershipOracle<I, Word<O>> oracle) {
        this.oracle = oracle;
        this.wb = new WordBuilder<>();
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> adaptiveQueries) {
        for (AdaptiveQuery<I, O> q : adaptiveQueries) {
            processQuery(q);
        }
    }

    @Override
    public void processQuery(AdaptiveQuery<I, O> query) {
        wb.clear();
        Response response;

        do {
            wb.append(query.getInput());
            final O out = this.oracle.answerQuery(wb.toWord()).lastSymbol();

            response = query.processOutput(out);

            if (response == Response.RESET) {
                wb.clear();
            }
        } while (response != Response.FINISHED);
    }
}
