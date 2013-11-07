package de.learnlib.statistics;

import de.learnlib.api.SUL;

public interface StatisticSUL<I, O> extends SUL<I,O> {
	
	public StatisticData getStatisticalData();
	
}
