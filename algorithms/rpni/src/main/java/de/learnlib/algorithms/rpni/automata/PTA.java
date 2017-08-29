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
package de.learnlib.algorithms.rpni.automata;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The prefix tree acceptor implementation. Besides inheriting from {@link CompactDFA} this class additionally allows
 * to store information about parent states (which state is the parent, which parent input leads to a state) which is
 * used during the processing of the PTA.
 *
 * This class also offers the possibility to remove states, which allows state-merging and -folding operations to occur
 * in-place.
 *
 * @param <I> Type of input symbols
 *
 * @author frohme
 *
 * TODO: although we support removing states, data structures are currenty not shrunk. This potentially leads to a lot
 * of unused (and thus wasted) memory
 */
public class PTA<I> extends CompactDFA<I> {

	private int[] nodeBackRef, nodeBackRefInput;
	private final BitSet states;

	public PTA(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);

		this.states = new BitSet();
		this.nodeBackRef = new int[super.stateCapacity];
		this.nodeBackRefInput = new int[super.stateCapacity];

		Arrays.fill(this.nodeBackRef, INVALID_STATE);
		Arrays.fill(this.nodeBackRefInput, INVALID_STATE);
	}

	public PTA(PTA<I> that) {
		super(that);

		this.states = (BitSet) that.states.clone();
		this.nodeBackRef = Arrays.copyOf(that.nodeBackRef, that.nodeBackRef.length);
		this.nodeBackRefInput = Arrays.copyOf(that.nodeBackRefInput, that.nodeBackRefInput.length);
	}

	public int getParentNode(final int state) {
		return this.nodeBackRef[state];
	}

	public I getParentNodeInput(final int state) {
		return super.getInputAlphabet().getSymbol(this.nodeBackRefInput[state]);
	}

	public void setParentNode(final int state, final int parent) {
		this.nodeBackRef[state] = parent;
	}

	public void setParentNodeInput(final int state, final I input) {
		this.nodeBackRefInput[state] = super.getInputAlphabet().getSymbolIndex(input);
	}

	@Override
	public Collection<Integer> getStates() {
		return IntStream.range(0, super.numStates).filter(states::get).boxed().collect(Collectors.toList());
	}

	@Override
	public void initState(int stateId, Boolean property) {
		this.states.set(stateId);
		super.initState(stateId, property);
	}

	@Override
	public void clear() {
		Arrays.fill(nodeBackRef, 0, super.numStates, INVALID_STATE);
		Arrays.fill(nodeBackRefInput, 0, super.numStates, INVALID_STATE);
		this.states.clear();

		super.clear();
	}

	@Override
	public int size() {
		return this.states.cardinality();
	}

	@Override
	public void ensureCapacity(final int newCapacity) {

		final int oldCap = super.stateCapacity;
		super.ensureCapacity(newCapacity);
		final int newCap = super.stateCapacity;

		if (newCap > oldCap) {
			final int[] newNodeBackRef = new int[newCap];
			final int[] newNodeBackRefInput = new int[newCap];

			System.arraycopy(nodeBackRef, 0, newNodeBackRef, 0, nodeBackRef.length);
			System.arraycopy(nodeBackRefInput, 0, newNodeBackRefInput, 0, nodeBackRefInput.length);

			Arrays.fill(newNodeBackRef, oldCap, newNodeBackRef.length, INVALID_STATE);
			Arrays.fill(newNodeBackRefInput, oldCap, newNodeBackRefInput.length, INVALID_STATE);

			this.nodeBackRef = newNodeBackRef;
			this.nodeBackRefInput = newNodeBackRefInput;
		}
	}

	public void removeState(final Integer state) {
		removeAllTransitions(state);
		this.states.clear(state);

		this.nodeBackRef[state] = INVALID_STATE;
		this.nodeBackRefInput[state] = INVALID_STATE;
	}
}
