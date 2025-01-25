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
package de.learnlib.oracle.membership;

import java.util.Collection;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.sul.SUL;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A wrapper around a system under learning (SUL).
 * <p>
 * This membership oracle is <b>not</b> thread-safe.
 */
public class SULOracle<I, O> implements MealyMembershipOracle<I, O> {

    private final SUL<I, O> sul;

    public SULOracle(SUL<I, O> sul) {
        this.sul = sul;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        for (Query<I, Word<O>> q : queries) {
            Word<O> output = answerQuery(q.getPrefix(), q.getSuffix());
            q.answer(output);
        }
    }

    @Override
    public Word<O> answerQuery(Word<I> prefix, Word<I> suffix) {
        sul.pre();
        try {
            // Prefix: Execute symbols, don't record output
            for (I sym : prefix) {
                sul.step(sym);
            }

            // Suffix: Execute symbols, outputs constitute output word
            WordBuilder<O> wb = new WordBuilder<>(suffix.length());
            for (I sym : suffix) {
                wb.add(sul.step(sym));
            }

            return wb.toWord();
        } finally {
            sul.post();
        }
    }

}
