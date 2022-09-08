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
package de.learnlib.algorithms.ttt.moore;

import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;

/**
 * A {@link MooreMachine}-specific state of the {@link TTTHypothesisMoore} class.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbols type
 *
 * @author bayram
 * @author frohme
 */
public class TTTStateMoore<I, O> extends TTTState<I, Word<O>> {

    O output;

    public TTTStateMoore(int initialAlphabetSize, TTTTransition<I, Word<O>> parentTransition, int id) {
        super(initialAlphabetSize, parentTransition, id);
    }

    public O getOutput() {
        return output;
    }

    public void setOutput(O output) {
        this.output = output;
    }
}