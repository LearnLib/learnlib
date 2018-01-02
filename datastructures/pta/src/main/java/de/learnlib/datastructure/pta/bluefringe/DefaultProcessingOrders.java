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
package de.learnlib.datastructure.pta.bluefringe;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

import de.learnlib.datastructure.pta.pta.AbstractBlueFringePTAState;
import de.learnlib.datastructure.pta.pta.PTATransition;
import net.automatalib.commons.util.comparison.CmpUtil;

/**
 * Standard processing orders that can be used for the RPNI algorithm.
 *
 * @author Malte Isberner
 */
public enum DefaultProcessingOrders implements ProcessingOrder {
    /**
     * Processes blue states in ascending canonical order of their access sequences.
     *
     * @see AbstractBlueFringePTAState#compareTo(AbstractBlueFringePTAState)
     * @see CmpUtil#canonicalCompare(int[], int[])
     */
    CANONICAL_ORDER {
        @Override
        public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
            return new PriorityQueue<>((t1, t2) -> {
                int cmp = t1.getSource().compareTo(t2.getSource());
                if (cmp == 0) {
                    cmp = t1.getIndex() - t2.getIndex();
                }
                return cmp;
            });
        }
    },
    /**
     * Processes blue states in ascending lexicographical order of their access sequences.
     *
     * @see AbstractBlueFringePTAState#lexCompareTo(AbstractBlueFringePTAState)
     * @see CmpUtil#lexCompare(int[], int[])
     */
    LEX_ORDER {
        @Override
        public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
            return new PriorityQueue<>((t1, t2) -> {
                int cmp = t1.getSource().lexCompareTo(t2.getSource());
                if (cmp == 0) {
                    cmp = t1.getIndex() - t2.getIndex();
                }
                return cmp;
            });
        }
    },
    /**
     * Processes blue states in a first-in, first-out (queue-like) manner.
     */
    FIFO_ORDER {
        @Override
        public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
            return new ArrayDeque<>();
        }
    },
    /**
     * Processes blue states in a last-in, first-out (stack-like) manner.
     */
    LIFO_ORDER {
        @Override
        public <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist() {
            return Collections.asLifoQueue(new ArrayDeque<>());
        }
    }
}
