/* Copyright (C) 2014 TU Dortmund
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

/**
 * Abstract base class for objects that may occur in a {@link BlockList},
 * either as a value element or the head of the list (which represents the list itself,
 * but does not carry any value).
 * <p>
 * The purpose of this class is to enable managing block lists <i>intrusively</i>.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public abstract class BlockListElem<I,D> {
	
	protected DTNode<I,D> nextBlock;

}
