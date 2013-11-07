package de.learnlib.oracles;

import de.learnlib.api.SUL;
import de.learnlib.statistics.Counter;
import de.learnlib.statistics.StatisticSUL;

public class SymbolCounterSUL<I, O> implements StatisticSUL<I,O> {
	
	private final SUL<I,O> sul;
	private final Counter counter;

	public SymbolCounterSUL(String name, SUL<I,O> sul) {
		this.sul = sul;
		this.counter = new Counter(name, "symbols");
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#pre()
	 */
	@Override
	public void pre() {
		sul.pre();
	}


	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#post()
	 */
        @Override
	public void post() {
		sul.post();
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.SUL#step(java.lang.Object)
	 */
	@Override
	public O step(I in) {
		counter.increment();
		return sul.step(in);
	}

	/* (non-Javadoc)
	 * @see de.learnlib.statistics.StatisticSUL#getStatisticalData()
	 */
	@Override
	public Counter getStatisticalData() {
		return counter;
	}
	
	

}
