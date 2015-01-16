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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;


/**
 * Hypothesis DFA for the {@link BaseTTTLearner TTT algorithm}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public abstract class TTTHypothesis<I,D,T> implements DeterministicAutomaton<TTTState<I,D>,I,T>,
		FiniteAlphabetAutomaton<TTTState<I,D>, I, T> {

	private final List<TTTState<I,D>> states = new ArrayList<>();
	
	protected final Alphabet<I> alphabet;
	private TTTState<I,D> initialState;

	/**
	 * Constructor.
	 * 
	 * @param alphabet the input alphabet
	 */
	public TTTHypothesis(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
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
		TTTTransition<I,D> trans = state.transitions[inputIdx];
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
			for (TTTTransition<I, D> trans : node.transitions) {
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
	
	public GraphView graphView() {
		return new GraphView();
	}
	
}
