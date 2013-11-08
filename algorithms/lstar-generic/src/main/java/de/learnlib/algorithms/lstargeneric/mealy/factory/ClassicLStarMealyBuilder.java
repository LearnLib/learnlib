package de.learnlib.algorithms.lstargeneric.mealy.factory;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.factory.ExtensibleAutomatonLStarBuilder;
import de.learnlib.algorithms.lstargeneric.mealy.ClassicLStarMealy;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.mealy.MealyUtil;

public class ClassicLStarMealyBuilder<I, O> extends ExtensibleAutomatonLStarBuilder<MealyMachine<?,I,?,O>,I,O,ClassicLStarMealyBuilder<I,O>> {

	@Override
	protected ClassicLStarMealyBuilder<I, O> _this() {
		return this;
	}
	
	public void setWordOracle(MembershipOracle<I,Word<O>> wordOracle) {
		setOracle(MealyUtil.wrapWordOracle(wordOracle));
	}
	
	public ClassicLStarMealyBuilder<I,O> withWordOracle(MembershipOracle<I, Word<O>> wordOracle) {
		setWordOracle(wordOracle);
		return this;
	}
	
	@Override
	public ClassicLStarMealy<I, O> create() {
		return new ClassicLStarMealy<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}
	
	public LearningAlgorithm<MealyMachine<?,I,?,O>, I, Word<O>> createWordLearner() {
		return MealyUtil.wrapSymbolLearner(create());
	}


}
