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
package de.learnlib.datastructure.discriminationtree;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;

/**
 * Generic n-ary discrimination tree node specialization.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         node data type
 *
 * @author Malte Isberner
 */
public class MultiDTNode<I, O, D> extends AbstractWordBasedDTNode<I, O, D> {

    public MultiDTNode(D data) {
        super(data);
    }

    public MultiDTNode(MultiDTNode<I, O, D> parent, O parentOutcome, D data) {
        super(parent, parentOutcome, data);
    }

    @Override
    protected Map<O, AbstractWordBasedDTNode<I, O, D>> createChildMap() {
        return new HashMap<>();
    }

    @Override
    protected MultiDTNode<I, O, D> createChild(O outcome, D data) {
        return new MultiDTNode<>(this, outcome, data);
    }
}
