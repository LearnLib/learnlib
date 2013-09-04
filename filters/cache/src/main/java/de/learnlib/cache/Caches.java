package de.learnlib.cache;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.cache.sul.SULCache;

public abstract class Caches {
	
	
	public static <I> DFACacheOracle<I> createDFACache(Alphabet<I> alphabet, MembershipOracle<I,Boolean> mqOracle) {
		return new DFACacheOracle<I>(alphabet, mqOracle);
	}
	
	public static <I,O> MealyCacheOracle<I, O> createMealyCache(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> mqOracle) {
		return new MealyCacheOracle<>(alphabet, mqOracle);
	}
	
	public static <I,O> SULCache<I,O> createSULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return new SULCache<>(alphabet, sul);
	}

	// prevent inheritance
	private Caches() {
	}

}
