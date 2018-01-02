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
package de.learnlib.algorithms.ttt.base;

import de.learnlib.algorithms.ttt.base.AbstractTTTLearner.Splitter;
import net.automatalib.words.Word;

public interface TTTEventListener<I, D> {

    void preFinalizeDiscriminator(AbstractBaseDTNode<I, D> blockRoot, Splitter<I, D> splitter);

    void postFinalizeDiscriminator(AbstractBaseDTNode<I, D> blockRoot, Splitter<I, D> splitter);

    void ensureConsistency(TTTState<I, D> state, AbstractBaseDTNode<I, D> dtNode, D realOutcome);

    void preSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator);

    void postSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator);
}
