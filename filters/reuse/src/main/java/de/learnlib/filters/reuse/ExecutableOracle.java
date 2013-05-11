package de.learnlib.filters.reuse;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.filters.reuse.api.ExecutableSymbol;
import de.learnlib.filters.reuse.api.ReuseTree;
import de.learnlib.filters.reuse.api.SystemState;
import de.learnlib.filters.reuse.api.SystemStateImpl;
import de.learnlib.logging.LearnLogger;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
@SuppressWarnings("serial")
public class ExecutableOracle<S extends ExecutableSymbol<V>, V> implements MembershipOracle<S, Word<V>> {
	private final LearnLogger logger = LearnLogger.getLogger(ExecutableOracle.class.getName());

	private SystemState systemState;

	private int mq = 0;
	private ExecutableSymbol reset;

	/**
	 * Default constructor.
	 * The {@link #reset} will be executed on a fresh system state
	 * each time {@link #reset()} will be called.
	 *
	 * @param reset
	 */
	public ExecutableOracle(ExecutableSymbol reset) {
		this.logger.warning("Changed logger level to INFO...");

		if (reset == null) {
			throw new IllegalArgumentException("Reset is not allowed to be null");
		}
		this.reset = reset;
	}

	/**
	 * Creates a new system state and executes the provided reset.
	 */
	public SystemState reset() {
		this.systemState = new SystemStateImpl();
		this.reset.execute(systemState);
		return this.systemState;
	}

	@Override
	public void processQueries(Collection<? extends Query<S, Word<V>>> queries) {
		for (Query<S, Word<V>> query : queries) {
			Word<V> output = processQuery(query.getInput());
			query.answer(output);
		}
	}


	/**
	 * Executes the given trace on the current system state.
	 * This invocation needs a previous call to {@link #reset()}.
	 *
	 * After executing the given trace the system state will
	 * hold the trace with its output in variables
	 * {@link ReuseTree#INPUT} and {@link ReuseTree#OUTPUT}.
	 */
	public Word<V> processQuery(Word<S> trace) {
		List<V> outputs = new ArrayList<>(trace.size());
		for (int i = 0; i <= trace.size() - 1; i++) {
			ExecutableSymbol<V> e = trace.getSymbol(i);
			V output = e.execute(systemState);

			outputs.add(output);
		}
		Word<V> result = Word.fromList(outputs);

		this.logger.info("Execute MQ " + (++mq) + " '" + trace + "' -> '" + result + "'");

		this.systemState.put(ReuseTree.INPUT, trace);
		this.systemState.put(ReuseTree.OUTPUT, result);

		return result;
	}

	/**
	 * Executes the given trace on the current system state.
	 * This invocation needs a previous call to {@link #setSystemState(SystemState)}.
	 *
	 * After executing the given trace (the full trace, so you should only
	 * put the remaining suffix in here!) the system state will
	 * hold the full trace with its full output in variables
	 * {@link ReuseTree#INPUT} and {@link ReuseTree#OUTPUT}.
	 */
	public Word<V> processQueryWithoutReset(Word<S> trace) {
		Word<S> prefix = (Word) systemState.get(ReuseTree.INPUT);
		Word<V> prefixResult = (Word) systemState.get(ReuseTree.OUTPUT);

		this.logger.info("Execute " + (++mq) + " " + trace + " without reset on " + prefix);

		List<V> outputs = new ArrayList<>(trace.size());
		for (int i = 0; i <= trace.size() - 1; i++) {
			ExecutableSymbol<V> e = trace.getSymbol(i);
			V output = e.execute(systemState);
			outputs.add(output);
		}

		Word<V> result = Word.fromList(outputs);

		this.systemState.put(ReuseTree.INPUT, prefix.concat(trace));
		this.systemState.put(ReuseTree.OUTPUT, prefixResult.concat(result));
		return result;
	}

	/**
	 * Sets the current system state.
	 */
	public void setSystemState(SystemState state) {
		this.systemState = state;
	}

	/**
	 * Returns the current system state.
	 */
	public SystemState getSystemState() {
		return this.systemState;
	}

}
