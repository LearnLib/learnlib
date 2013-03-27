package de.learnlib.algorithms.lstargeneric.components;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ClassicLStarCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.CloseFirstStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;

@LLComponent(name = "ExtensibleLStarMealy", type = LearningAlgorithm.class)
public class ExtensibleLStarMealyFactory<I, O> implements
		LLComponentFactory<ExtensibleLStarMealy<I, O>> {
	
	private Alphabet<I> alphabet;
	private MembershipOracle<I, Word<O>> oracle;
	private List<Word<I>> initialSuffixes = Collections.emptyList();
	private ObservationTableCEXHandler<I, Word<O>> cexHandler = ClassicLStarCEXHandler.getInstance();
	private ClosingStrategy<I, Word<O>> closingStrategy = CloseFirstStrategy.getInstance();

	
	@LLComponentParameter(name = "alphabet", required = true)
	public void setAlphabet(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	@LLComponentParameter(name = "oracle", required = true)
	public void setOracle(MembershipOracle<I,Word<O>> oracle) {
		this.oracle = oracle;
	}
	
	@LLComponentParameter(name = "initialSuffixes")
	public void setInitialSuffix(List<Word<I>> initialSuffixes) {
		this.initialSuffixes = initialSuffixes;
	}
	
	@LLComponentParameter(name = "cexHandler")
	public void setCEXHandler(ObservationTableCEXHandler<I, Word<O>> cexHandler) {
		this.cexHandler = cexHandler;
	}
	
	@LLComponentParameter(name = "closingStrategy")
	public void setClosingStrategy(ClosingStrategy<I,Word<O>> closingStrategy) {
		this.closingStrategy = closingStrategy;
	}
	@Override
	public ExtensibleLStarMealy<I, O> instantiate() {
		return new ExtensibleLStarMealy<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}
	
}
