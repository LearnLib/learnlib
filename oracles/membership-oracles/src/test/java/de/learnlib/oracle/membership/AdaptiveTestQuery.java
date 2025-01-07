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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.learnlib.query.AdaptiveQuery;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class AdaptiveTestQuery<I, O> implements AdaptiveQuery<I, O> {

    private final Deque<Deque<I>> inputs;
    private final List<WordBuilder<O>> outputs;

    @SafeVarargs
    public AdaptiveTestQuery(Word<I>... inputs) {
        this.inputs = new ArrayDeque<>(inputs.length);
        this.outputs = new ArrayList<>(inputs.length);

        for (Word<I> input : inputs) {
            this.inputs.add(new ArrayDeque<>(input.asList()));
        }

        this.outputs.add(new WordBuilder<>());
    }

    @Override
    public I getInput() {
        final Deque<I> peek = inputs.peek();
        assert peek != null;
        final I result = peek.peek();
        assert result != null;
        return result;
    }

    @Override
    public Response processOutput(O out) {
        final Deque<I> input = this.inputs.peekFirst();
        final WordBuilder<O> output = this.outputs.get(this.outputs.size() - 1);

        assert input != null;
        input.removeFirst();
        output.add(out);

        if (input.isEmpty()) {
            this.inputs.removeFirst();

            if (this.inputs.isEmpty()) {
                return Response.FINISHED;
            } else {
                final Deque<I> peek = this.inputs.peek();
                assert peek != null;
                this.outputs.add(new WordBuilder<>(peek.size()));
                return Response.RESET;
            }
        }

        return Response.SYMBOL;
    }

    public List<WordBuilder<O>> getOutputs() {
        return outputs;
    }
}
