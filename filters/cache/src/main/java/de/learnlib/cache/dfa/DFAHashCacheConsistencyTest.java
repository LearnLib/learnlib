package de.learnlib.cache.dfa;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

final class DFAHashCacheConsistencyTest<I> implements DFAEquivalenceOracle<I> {

	private final Map<Word<I>,Boolean> cache;
	private final Lock cacheLock;
	
	public DFAHashCacheConsistencyTest(Map<Word<I>,Boolean> cache, Lock cacheLock) {
		this.cache = cache;
		this.cacheLock = cacheLock;
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
			Collection<? extends I> inputs) {
		cacheLock.lock();
		try {
			for(Map.Entry<Word<I>,Boolean> cacheEntry : cache.entrySet()) {
				Word<I> input = cacheEntry.getKey();
				Boolean answer = cacheEntry.getValue();
				
				if(!hypothesis.computeOutput(input).equals(answer)) {
					return new DefaultQuery<>(input, answer);
				}
			}
			return null;
		}
		finally {
			cacheLock.unlock();
		}
	}

}
