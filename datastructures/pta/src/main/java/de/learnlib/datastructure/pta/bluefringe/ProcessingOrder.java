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

import java.util.Comparator;
import java.util.Queue;

import de.learnlib.datastructure.pta.pta.AbstractBlueFringePTAState;
import de.learnlib.datastructure.pta.pta.PTATransition;

/**
 * Interface for entities that specify the order in which blue states are processed (i.e., considered for merges). This
 * class contains a single method, which creates a worklist (i.e., a {@link Queue}) in which blue states are maintained.
 * A class implementing this interface can establish specific ordering constraints on the returned worklist, such as
 * LIFO or FIFO behavior, or ordering according to a certain {@link Comparator}.
 * <p>
 * This interface is provided for extensibility only, but most probably does not need to be implemented by a user. See
 * {@link DefaultProcessingOrders} for a (probably) sufficient set of pre-defined processing orders.
 *
 * @author Malte Isberner
 * @see DefaultProcessingOrders
 */
public interface ProcessingOrder {

    /**
     * Creates a worklist for managing the set of blue states in the RPNI algorithm.
     *
     * @return a worklist with some specific ordering constraints
     */
    <S extends AbstractBlueFringePTAState<?, ?, S>> Queue<PTATransition<S>> createWorklist();
}
