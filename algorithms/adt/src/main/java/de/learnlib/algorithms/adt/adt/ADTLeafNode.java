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
import java.util.Collections;
import java.util.Map;

/**
 * Leaf node implementation.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ADTLeafNode<S, I, O> implements ADTNode<S, I, O> {

	private ADTNode<S, I, O> parent;

	private S hypothesisState;

	public ADTLeafNode(final ADTNode<S, I, O> parent, final S hypothesisState) {
		this.parent = parent;
		this.hypothesisState = hypothesisState;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.LEAF_NODE;
	}

	@Override
	public S getHypothesisState() {
		return hypothesisState;
	}

	@Override
	public void setHypothesisState(S state) {
		this.hypothesisState = state;
	}

	@Override
	public I getSymbol() {
		return null;
	}

	@Override
	public void setSymbol(I symbol) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Final nodes do not have a symbol");
	}

	@Override
	public ADTNode<S, I, O> getParent() {
		return this.parent;
	}

	@Override
	public void setParent(final ADTNode<S, I, O> parent) {
		this.parent = parent;
	}

	@Override
	public Map<O, ADTNode<S, I, O>> getChildren() {
		return Collections.emptyMap();
	}

	@Override
	public ADTNode<S, I, O> sift(SymbolQueryOracle<I, O> oracle, final Word<I> prefix) {
		throw new UnsupportedOperationException("Final nodes cannot sift words");
	}

	@Override
	public String toString() {
		return this.getHypothesisState() == null ? "<null>" : this.getHypothesisState().toString();
	}
}
