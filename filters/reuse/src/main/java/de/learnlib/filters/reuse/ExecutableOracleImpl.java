/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.filters.reuse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.drivers.api.SULException;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.api.SystemStateRef;
import de.learnlib.logging.LearnLogger;

/**
 * TODO Documentation!
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
@SuppressWarnings("serial")
public class ExecutableOracleImpl<S extends SystemStateRef<?, I, O>, I extends InjectableSystemStateRef<S,I,O>, O> implements MembershipOracle<I, Word<O>> {
	private final LearnLogger logger = LearnLogger.getLogger(ExecutableOracleImpl.class.getName());

	private S systemState;

	private int mq = 0;
	private InjectableSystemStateRef<S, I, O> reset;

	/**
	 * Default constructor.
	 * The {@link #reset} will be executed on a fresh system state
	 * each time {@link #reset()} will be called.
	 *
	 * @param reset
	 */
	public ExecutableOracleImpl(InjectableSystemStateRef<S,I,O> reset) {
		this.logger.warning("Changed logger level to INFO...");

		if (reset == null) {
			throw new IllegalArgumentException("Reset is not allowed to be null");
		}
		this.reset = reset;
	}

	/**
	 * Creates a new system state and executes the provided reset.
	 */
	public S reset() {
		systemState = reset.retrieve();
		return this.systemState;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> query : queries) {
			Word<O> output = processQuery(query.getInput());
			query.answer(output);
		}
	}


	/**
	 * Executes the given trace on the current system state.
	 * This invocation needs a previous call to {@link #reset()}.
	 *
	 * After executing the given trace the system state will
	 * hold the trace with its output in variables
	 * {@link ReuseTreeImpl#INPUT} and {@link ReuseTreeImpl#OUTPUT}.
	 */
	public Word<O> processQuery(Word<I> trace) {
		List<O> outputs = new ArrayList<>(trace.size());
		for (int i = 0; i <= trace.size() - 1; i++) {
			I s = trace.getSymbol(i);
			O output = null;
			try {
				s.inject(systemState);
				output = s.execute();
				systemState = s.retrieve();
			} catch (SULException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			outputs.add(output);
		}
		Word<O> result = Word.fromList(outputs);

		this.logger.info("Execute MQ " + (++mq) + " '" + trace + "' -> '" + result + "'");

		systemState.setPrefixInput(trace);
		systemState.setPrefixOutput(result);

		return result;
	}

	/**
	 * Executes the given trace on the current system state.
	 * This invocation needs a previous call to {@link #setSystemState(SystemState)}.
	 *
	 * After executing the given trace (the full trace, so you should only
	 * put the remaining suffix in here!) the system state will
	 * hold the full trace with its full output in variables
	 * {@link ReuseTreeImpl#INPUT} and {@link ReuseTreeImpl#OUTPUT}.
	 */
	public Word<O> processQueryWithoutReset(Word<I> trace) {
		Word<I> prefix = systemState.getPrefixInput();
		Word<O> prefixResult = systemState.getPrefixOutput();

		this.logger.info("Execute " + (++mq) + " " + trace + " without reset on " + prefix);

		List<O> outputs = new ArrayList<>(trace.size());
		for (int i = 0; i <= trace.size() - 1; i++) {
			I s = trace.getSymbol(i);
			O output = null;
			try {
				s.inject(systemState);
				output = s.execute();
				systemState = s.retrieve();
			} catch (SULException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputs.add(output);
		}

		Word<O> result = Word.fromList(outputs);

		systemState.setPrefixInput(prefix.concat(trace));
		systemState.setPrefixOutput(prefixResult.concat(result));

		return result;
	}

	/**
	 * Sets the current system state.
	 */
	public void setSystemState(S state) {
		this.systemState = state;
	}

	/**
	 * Returns the current system state.
	 */
	public S getSystemState() {
		return this.systemState;
	}

}
