package de.learnlib.oracles;

import de.learnlib.api.SUL;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.StatisticSUL;

public class ResetCounterSUL<I, O> implements StatisticSUL<I, O> {
	
	private final SUL<I,O> sul;
	private final Counter counter;

	public ResetCounterSUL(String name, SUL<I,O> sul) {
		this.sul = sul;
		this.counter = new Counter(name, "resets");
	}

	@Override
	public void reset() {
		counter.increment();
		sul.reset();
	}

	@Override
	public O step(I in) {
		return sul.step(in);
	}

	@Override
	public Counter getStatisticalData() {
		return counter;
	}

}
