/* Copyright (C) 2014 TU Dortmund
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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.ttt.base;

import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.base.BaseTTTLearner.Splitter;

public interface TTTEventListener<I, D> {
	
	public void preFinalizeDiscriminator(DTNode<I,D> blockRoot, Splitter<I,D> splitter);
	public void postFinalizeDiscriminator(DTNode<I,D> blockRoot, Splitter<I,D> splitter);
	
	public void ensureConsistency(TTTState<I,D> state, DTNode<I,D> dtNode, D realOutcome);
	
	
	public void preSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator);
	public void postSplit(TTTTransition<I, D> transition, Word<I> tempDiscriminator);
}
