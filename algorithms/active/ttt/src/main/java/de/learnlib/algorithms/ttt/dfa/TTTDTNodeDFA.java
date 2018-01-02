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
package de.learnlib.algorithms.ttt.dfa;

import java.util.Map;

import de.learnlib.algorithms.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.datastructure.discriminationtree.model.BooleanMap;

/**
 * Binary discrimination tree node specialization.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class TTTDTNodeDFA<I> extends AbstractBaseDTNode<I, Boolean> {

    public TTTDTNodeDFA() {
        this(null, null);
    }

    public TTTDTNodeDFA(AbstractBaseDTNode<I, Boolean> parent, Boolean parentEdgeLabel) {
        super(parent, parentEdgeLabel);
    }

    @Override
    protected Map<Boolean, AbstractBaseDTNode<I, Boolean>> createChildMap() {
        return new BooleanMap<>();
    }

    @Override
    protected AbstractBaseDTNode<I, Boolean> createChild(Boolean outcome, TTTState<I, Boolean> data) {
        return new TTTDTNodeDFA<>(this, outcome);
    }
}
