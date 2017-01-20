/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.adt.adt;

import de.learnlib.api.SymbolQueryOracle;
import net.automatalib.words.Word;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

/**
 * Symbol node implementation.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ADTSymbolNode<S, I, O> implements ADTNode<S, I, O> {

	private ADTNode<S, I, O> parent;

	private I symbol;

	private final Map<O, ADTNode<S, I, O>> successors;

	public ADTSymbolNode(final ADTNode<S, I, O> parent, final I symbol) {
		this.successors = new HashMap<>();
		this.parent = parent;
		this.symbol = symbol;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.SYMBOL_NODE;
	}

	@Override
	public S getHypothesisState() {
		return null;
	}

	@Override
	public void setHypothesisState(final S state) {
		throw new UnsupportedOperationException("Symbol nodes cannot reference a hypothesis state");
	}

	@Override
	public I getSymbol() {
		return this.symbol;
	}

	@Override
	public void setSymbol(I symbol) throws UnsupportedOperationException {
		this.symbol = symbol;
	}

	@Override
	public ADTNode<S, I, O> getParent() {
		return this.parent;
	}

	@Override
	public void setParent(ADTNode<S, I, O> parent) {
		this.parent = parent;
	}

	@Override
	public Map<O, ADTNode<S, I, O>> getChildren() {
		return this.successors;
	}

	@Override
	public ADTNode<S, I, O> sift(final SymbolQueryOracle<I, O> oracle, final Word<I> prefix) {
		final O o = oracle.query(this.symbol);

		final ADTNode<S, I, O> successor = this.successors.get(o);

		if (successor == null) {
			final ADTNode<S, I, O> result = new ADTLeafNode<>(this, null);
			successors.put(o, result);
			return result;
		}

		return successor;
	}

	@Override
	public String toString() {
		return this.symbol.toString();
	}

}
