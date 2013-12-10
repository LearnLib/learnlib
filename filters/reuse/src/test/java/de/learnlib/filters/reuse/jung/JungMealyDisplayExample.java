package de.learnlib.filters.reuse.jung;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseOracle;

public class JungMealyDisplayExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JungMealyDisplayExample t = new JungMealyDisplayExample();
		
		ReuseOracle<Integer, Integer, String> reuseOracle;
		Alphabet<Integer> sigma;
		
		int size = 10;
		ReuseCapableOracle<Integer, Integer, String> reuseCapableOracle = t.new TestOracle(size);
		sigma = Alphabets.integers(0, size);
		reuseOracle = new ReuseOracle<>(sigma, reuseCapableOracle);
		
		
		MealyLearner<Integer, String> learner = new ExtensibleLStarMealyBuilder<Integer, String>()
				.withAlphabet(sigma).withOracle(reuseOracle).create();

		learner.startLearning();
		
		JungMealyDisplay.render(learner.getHypothesisModel(), sigma);
	}
	
	class TestOracle implements ReuseCapableOracle<Integer, Integer, String> {
		private int threshold;

		public TestOracle(int threshold) {
			this.threshold = threshold;
		}

		@Override
		public QueryResult<Integer, String> continueQuery(Word<Integer> trace,
				Integer s) {

			Integer integer = s;

			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
				}
			}

			QueryResult<Integer, String> result;
			result = new QueryResult<Integer, String>(output.toWord(), integer,
					true);

			return result;
		}

		@Override
		public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
			Integer integer = new Integer(0);
			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
				}
			}

			QueryResult<Integer, String> result;
			result = new QueryResult<Integer, String>(output.toWord(), integer,
					true);

			return result;
		}
	}

}
