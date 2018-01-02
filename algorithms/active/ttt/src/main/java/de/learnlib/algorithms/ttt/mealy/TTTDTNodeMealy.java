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
package de.learnlib.algorithms.ttt.mealy;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.algorithms.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithms.ttt.base.TTTState;

/**
 * Generic n-ary discrimination tree node specialization.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output symbol type
 *
 * @author frohme
 */
public class TTTDTNodeMealy<I, D> extends AbstractBaseDTNode<I, D> {

    public TTTDTNodeMealy() {
        this(null, null);
    }

    public TTTDTNodeMealy(AbstractBaseDTNode<I, D> parent, D parentEdgeLabel) {
        super(parent, parentEdgeLabel);
    }

    @Override
    protected Map<D, AbstractBaseDTNode<I, D>> createChildMap() {
        return new HashMap<>();
    }

    @Override
    protected AbstractBaseDTNode<I, D> createChild(D outcome, TTTState<I, D> data) {
        return new TTTDTNodeMealy<>(this, outcome);
    }
}
