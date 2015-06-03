/* Copyright (C) 2015 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.passive.commons.bluefringe;

import java.util.Comparator;
import java.util.Queue;

import de.learnlib.passive.commons.pta.AbstractBlueFringePTAState;
import de.learnlib.passive.commons.pta.PTATransition;

/**
 * Interface for entities that specify the order in which blue states are processed
 * (i.e., considered for merges). This class contains a single method, which creates
 * a worklist (i.e., a {@link Queue}) in which blue states are maintained. A class
 * implementing this interface can establish specific ordering constraints on the
 * returned worklist, such as LIFO or FIFO behavior, or ordering according to a certain
 * {@link Comparator}.
 * <p>
 * This interface is provided for extensibility only, but most probably does not need
 * to be implemented by a user. See {@link DefaultProcessingOrders} for a (probably)
 * sufficient set of pre-defined processing orders.
 * 
 * @author Malte Isberner
 *
 * @see DefaultProcessingOrders
 */
public interface ProcessingOrder {
	/**
	 * Creates a worklist for managing the set of blue states in the
	 * RPNI algorithm.
	 * @return a worklist with some specific ordering constraints
	 */
	public <S extends AbstractBlueFringePTAState<?, ?, S>>
	Queue<PTATransition<S>> createWorklist();
}
