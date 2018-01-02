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
package de.learnlib.datastructure.discriminationtree.model;

import net.automatalib.words.Word;

/**
 * Convenient class for word-based discrimination tree nodes that already binds certain generics.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         node data type
 *
 * @author frohme
 */
public abstract class AbstractWordBasedDTNode<I, O, D>
        extends AbstractDTNode<Word<I>, O, D, AbstractWordBasedDTNode<I, O, D>> {

    public AbstractWordBasedDTNode(D data) {
        super(data);
    }

    public AbstractWordBasedDTNode(AbstractWordBasedDTNode<I, O, D> parent, O parentOutcome, D data) {
        super(parent, parentOutcome, data);
    }
}
