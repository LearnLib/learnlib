/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.oracle.membership;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.api.query.Query;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StateLocalInputSULOracle<I, O> implements StateLocalInputMealyOracle<I, OutputAndLocalInputs<I, O>> {

    private final StateLocalInputSUL<I, O> sul;
    private final @Nullable ThreadLocal<StateLocalInputSUL<I, O>> localSul;

    public StateLocalInputSULOracle(StateLocalInputSUL<I, O> sul) {
        this.sul = sul;
        if (sul.canFork()) {
            this.localSul = ThreadLocal.withInitial(sul::fork);
        } else {
            this.localSul = null;
        }
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<OutputAndLocalInputs<I, O>>>> queries) {
        if (localSul != null) {
            processQueries(localSul.get(), queries);
        } else {
            synchronized (sul) {
                processQueries(sul, queries);
            }
        }
    }

    private void processQueries(StateLocalInputSUL<I, O> sul,
                                Collection<? extends Query<I, Word<OutputAndLocalInputs<I, O>>>> queries) {
        for (Query<I, Word<OutputAndLocalInputs<I, O>>> q : queries) {
            Word<OutputAndLocalInputs<I, O>> output = answerQuery(sul, q.getPrefix(), q.getSuffix());
            q.answer(output);
        }
    }

    private Word<OutputAndLocalInputs<I, O>> answerQuery(StateLocalInputSUL<I, O> sul, Word<I> prefix, Word<I> suffix) {
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

            final WordBuilder<OutputAndLocalInputs<I, O>> wb = new WordBuilder<>(suffix.length());

            for (I sym : suffix) {
                if (enabledInputs.contains(sym)) {
                    final O out = sul.step(sym);
                    enabledInputs = sul.currentlyEnabledInputs();
                    wb.add(new OutputAndLocalInputs<>(out, enabledInputs));
                } else {
                    enabledInputs = Collections.emptySet();
                    wb.add(OutputAndLocalInputs.undefined());
                }
            }

            return wb.toWord();
        } finally {
            sul.post();
        }
    }

    @Override
    public Set<I> definedInputs(Word<? extends I> input) {
        try {
            sul.pre();
            Collection<I> enabledInputs = sul.currentlyEnabledInputs();

            for (I sym : input) {
                if (enabledInputs.contains(sym)) {
                    sul.step(sym);
                    enabledInputs = sul.currentlyEnabledInputs();
                } else {
                    return Collections.emptySet();
                }
            }

            return new HashSet<>(enabledInputs);
        } finally {
            sul.post();
        }
    }
}

