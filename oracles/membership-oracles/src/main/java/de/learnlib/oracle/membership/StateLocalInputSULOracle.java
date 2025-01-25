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
import java.util.Collections;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A wrapper around a system under learning (SUL) with state local inputs.
 * <p>
 * This membership oracle is <b>not</b> thread-safe.
 */
public class StateLocalInputSULOracle<I, O> implements MealyMembershipOracle<I, O> {

    private final StateLocalInputSUL<I, O> sul;
    private final O undefinedOutput;

    public StateLocalInputSULOracle(StateLocalInputSUL<I, O> sul, O undefinedOutput) {
        this.sul = sul;
        this.undefinedOutput = undefinedOutput;
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
        try {
            sul.pre();
            Collection<I> enabledInputs = sul.currentlyEnabledInputs();

            for (I sym : prefix) {
                if (enabledInputs.contains(sym)) {
                    sul.step(sym);
                    enabledInputs = sul.currentlyEnabledInputs();
                } else {
                    enabledInputs = Collections.emptySet();
                }
            }

            final WordBuilder<O> wb = new WordBuilder<>(suffix.length());

            for (I sym : suffix) {
                if (enabledInputs.contains(sym)) {
                    final O out = sul.step(sym);
                    enabledInputs = sul.currentlyEnabledInputs();
                    wb.add(out);
                } else {
                    enabledInputs = Collections.emptySet();
                    wb.add(this.undefinedOutput);
                }
            }

            return wb.toWord();
        } finally {
            sul.post();
        }
    }
}

