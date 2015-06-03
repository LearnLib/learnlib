/* Copyright (C) 2015 TU Dortmund
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
package de.learnlib.simulator.sul;

import net.automatalib.automata.transout.MealyMachine;
import de.learnlib.api.SUL;
import de.learnlib.api.SULException;


/**
 * A {@link SUL} that implements steps by stepping through a {@link MealyMachine}.
 * <p>
 * Note: this SUL is {@link SUL#fork() forkable}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class MealySimulatorSUL<I, O> implements SUL<I,O> {

	/**
	 * Implementation class, used to hide {@code S} and {@code T} type parameters.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <S> Mealy machine state type
	 * @param <I> input symbol type
	 * @param <T> Mealy machine transition type
	 * @param <O> output symbol type
	 */
	private static final class MealySimulatorSULImpl<S,I,T,O> implements SUL<I,O> {
		private final MealyMachine<S, I, T, O> mealy;
		private final O noTransOut;
		private S curr;
		public MealySimulatorSULImpl(MealyMachine<S,I,T,O> mealy, O noTransOut) {
			this.mealy = mealy;
			this.noTransOut = noTransOut;
		}
		@Override
		public void pre() {
			this.curr = mealy.getInitialState();
		}
		@Override
		public void post() {
			this.curr = null;
		}
		@Override
		public O step(I in) throws SULException {
			O out = noTransOut;
			if (curr != null) {
				T trans = mealy.getTransition(curr, in);
				if (trans != null) {
					out = mealy.getTransitionOutput(trans);
					curr = mealy.getSuccessor(trans);
				}
				else {
					curr = null;
				}
			}
			return out;
		}
		@Override
		public boolean canFork() {
			return true;
		}
		@Override
		public MealySimulatorSULImpl<S,I,T,O> fork() {
			return new MealySimulatorSULImpl<>(mealy, noTransOut);
		}
	}
	
	
	private final MealySimulatorSULImpl<?, I, ?, O> impl;
	
	private MealySimulatorSUL(MealySimulatorSULImpl<?, I, ?, O> impl) {
		this.impl = impl;
	}
	
	/**
	 * Constructor, using {@code null} as the output for undefined transitions.
	 * <p>
	 * This constructor is provided for convenience. It is equivalent to calling
	 * {@link #MealySimulatorSUL(MealyMachine, Object)} with {@code null} as the second
	 * argument.
	 * 
	 * @param mealy Mealy machine
	 */
	public MealySimulatorSUL(MealyMachine<?, I, ?, O> mealy) {
		this(mealy, null);
	}
	
	
	/**
	 * Constructor.
	 * <p>
	 * If the given Mealy machine has no undefined transitions, the second parameter has no
	 * effect. Otherwise, if the Mealy machine is partial and sequences of {@link #step(Object)}
	 * invocations reach an undefined transition, subsequent invocations of {@link #step(Object)}
	 * will simply return the specified {@code noTransOut} symbol.
	 * 
	 * @param mealy the Mealy machine
	 * @param noTransOut the output symbol to use when encountering undefined transitions
	 */
	public MealySimulatorSUL(MealyMachine<?,I,?,O> mealy, O noTransOut) {
		this(new MealySimulatorSULImpl<>(mealy,noTransOut));
	}

	@Override
	public void pre() {
		impl.pre();
	}

	@Override
	public void post() {
		impl.post();
	}

	@Override
	public O step(I in) throws SULException {
		return impl.step(in);
	}
	
	@Override
	public boolean canFork() {
		return impl.canFork();
	}
	
	@Override
	public SUL<I,O> fork() {
		return new MealySimulatorSUL<>(impl.fork());
	}
	
}
