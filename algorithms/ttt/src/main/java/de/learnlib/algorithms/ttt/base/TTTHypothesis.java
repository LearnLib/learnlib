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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 * Hypothesis DFA for the {@link BaseTTTLearner TTT algorithm}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public abstract class TTTHypothesis<I,D,T> implements DeterministicAutomaton<TTTState<I,D>,I,T>,
		FiniteAlphabetAutomaton<TTTState<I,D>, I, T>,
		DeterministicAutomaton.FullIntAbstraction<T>,
		GrowableAlphabetAutomaton<I> {

	protected final List<TTTState<I,D>> states = new ArrayList<>();
	
	protected final GrowingAlphabet<I> alphabet;
	private TTTState<I,D> initialState;

	/**
	 * Constructor.
	 * 
	 * @param alphabet the input alphabet
	 */
	public TTTHypothesis(Alphabet<I> alphabet) {
		this.alphabet = new SimpleAlphabet<>(alphabet);
	}
	
	@Override
	public int size() {
		return states.size();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.simple.SimpleAutomaton#getStates()
	 */
	@Override
	public Collection<TTTState<I,D>> getStates() {
		return Collections.unmodifiableList(states);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleDTS#getInitialState()
	 */
	@Override
	public TTTState<I,D> getInitialState() {
		return initialState;
	}

	@Override
	public T getTransition(TTTState<I,D> state, I input) {
		TTTTransition<I,D> trans = getInternalTransition(state, input);
		return mapTransition(trans);
	}
	
	protected abstract T mapTransition(TTTTransition<I, D> internalTransition);
	
	/**
	 * Checks whether this automaton was initialized (i.e.,
	 * {@link #initialize()} has been called).
	 * 
	 * @return {@code true} if this automaton was initialized, {@code false}
	 * otherwise.
	 */
	public boolean isInitialized() {
		return (initialState != null);
	}
	
	/**
	 * Initializes the automaton, adding an initial state. Whether or not the
	 * initial state is accepting needs to be known at this point.
	 * 
	 * @return the initial state of this newly initialized automaton
	 */
	public TTTState<I,D> initialize() {
		assert !isInitialized();
		
		initialState = createState(null);
		return initialState;
	}
	
	/**
	 * Retrieves the <i>internal</i> transition (i.e., the {@link TTTTransition} object)
	 * for a given state and input. This method is required since the {@link DFA} interface
	 * requires the return value of {@link #getTransition(TTTState, Object)} to
	 * refer to the successor state directly.
	 * 
	 * @param state the source state
	 * @param input the input symbol triggering the transition
	 * @return the transition object
	 */
	public TTTTransition<I,D> getInternalTransition(TTTState<I,D> state, I input) {
		int inputIdx = alphabet.getSymbolIndex(input);
		return getInternalTransition(state, inputIdx);
	}
	
	public TTTTransition<I,D> getInternalTransition(TTTState<I,D> state, int input) {
		TTTTransition<I,D> trans = state.getTransition(input);
		return trans;
	}
	
	
	public TTTState<I,D> createState(TTTTransition<I,D> parent) {
		TTTState<I,D> state = newState(alphabet.size(), parent, states.size());
		states.add(state);
		if(parent != null) {
			parent.makeTree(state);
		}
		return state;
	}
	
	protected TTTState<I,D> newState(int alphabetSize, TTTTransition<I, D> parent, int id) {
		return new TTTState<>(alphabetSize, parent, id);
	}

	@Override
	public Alphabet<I> getInputAlphabet() {
		return alphabet;
	}
	
	public static final class TTTEdge<I,D> {
		public final TTTTransition<I, D> transition;
		public final TTTState<I, D> target;
		public TTTEdge(TTTTransition<I, D> transition, TTTState<I,D> target) {
			this.transition = transition;
			this.target = target;
		}
	}
	
	public class GraphView implements Graph<TTTState<I,D>,TTTEdge<I,D>> {

		@Override
		public Collection<? extends TTTState<I,D>> getNodes() {
			return states;
		}

		@Override
		public Collection<? extends TTTEdge<I,D>> getOutgoingEdges(
				TTTState<I,D> node) {
			List<TTTEdge<I,D>> result = new ArrayList<>();
			for (TTTTransition<I, D> trans : node.getTransitions()) {
				for (TTTState<I, D> target : trans.getDTTarget().subtreeStates()) {
					result.add(new TTTEdge<>(trans, target));
				}
			}
			return result;
		}

		@Override
		public TTTState<I,D> getTarget(TTTEdge<I,D> edge) {
			return edge.target;
		}

		@Override
		public GraphDOTHelper<TTTState<I,D>, TTTEdge<I,D>> getGraphDOTHelper() {
			return new DefaultDOTHelper<TTTState<I,D>,TTTEdge<I,D>>() {

				@Override
				public boolean getEdgeProperties(TTTState<I,D> src,
						TTTEdge<I,D> edge, TTTState<I,D> tgt,
						Map<String, String> properties) {
					properties.put(EdgeAttrs.LABEL, String.valueOf(edge.transition.getInput()));
					if(edge.transition.isTree()) {
						properties.put(EdgeAttrs.STYLE, "bold");
					}
					else if (edge.transition.getDTTarget().isInner()) {
						properties.put(EdgeAttrs.STYLE, "dotted");
					}
					return true;
				}
				
			};
		}
		
		
	}
	
	@Override
	public GraphView graphView() {
		return new GraphView();
	}
	
	@Override
	public T getTransition(int stateId, int symIdx) {
		TTTState<I,D> state = states.get(stateId);
		TTTTransition<I,D> trans = getInternalTransition(state, symIdx);
		return mapTransition(trans);
	}
	
	@Override
	public int getIntInitialState() {
		return 0;
	}
	
	@Override
	public int numInputs() {
		return alphabet.size();
	}
	
	@Override
	public int getIntSuccessor(T trans) {
		return getSuccessor(trans).id;
	}
	
	
	@Override
	public DeterministicAutomaton.FullIntAbstraction<T> fullIntAbstraction(Alphabet<I> alphabet) {
		if (alphabet == this.alphabet) {
			return this;
		}
		return DeterministicAutomaton.super.fullIntAbstraction(alphabet);
	}

	@Override
	public void addAlphabetSymbol(I symbol) {
		this.alphabet.addSymbol(symbol);
		final int alphabetSize = this.alphabet.size();

		for (final TTTState<I, D> s : this.getStates()) {
			s.ensureInputCapacity(alphabetSize);
		}
	}
	
//
//	
//	- The type TTTHypothesisMealy<I,O> must implement the inherited abstract method 
//	 DeterministicAutomaton.FullIntAbstraction<TTTTransitionMealy<I,O>>.getTransition(int, int)
//	- The type TTTHypothesisMealy<I,O> must implement the inherited abstract method 
//	 SimpleDeterministicAutomaton.IntAbstraction.getIntInitialState()
//	- The type TTTHypothesisMealy<I,O> must implement the inherited abstract method 
//	 SimpleDeterministicAutomaton.FullIntAbstraction.numInputs()
//	- The type TTTHypothesisMealy<I,O> must implement the inherited abstract method 
//	 DeterministicAutomaton.IntAbstraction<TTTTransitionMealy<I,O>>.getIntSuccessor(TTTTransitionMealy<I,O
}
