package de.learnlib.algorithms.lstargeneric.components;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ClassicLStarCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.CloseFirstStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;

@LLComponent(name = "ExtensibleLStarDFA", type = LearningAlgorithm.class)
public class ExtensibleLStarDFAFactory<I> implements
		LLComponentFactory<ExtensibleLStarDFA<I>> {
	
	private Alphabet<I> alphabet;
	private MembershipOracle<I, Boolean> oracle;
	private List<Word<I>> initialSuffixes = Collections.emptyList();
	private ObservationTableCEXHandler<I, Boolean> cexHandler = ClassicLStarCEXHandler.getInstance();
	private ClosingStrategy<I, Boolean> closingStrategy = CloseFirstStrategy.getInstance();

	
	@LLComponentParameter(name = "alphabet", required = true)
	public void setAlphabet(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	@LLComponentParameter(name = "oracle", required = true)
	public void setOracle(MembershipOracle<I,Boolean> oracle) {
		this.oracle = oracle;
	}
	
	@LLComponentParameter(name = "initialSuffixes")
	public void setInitialSuffix(List<Word<I>> initialSuffixes) {
		this.initialSuffixes = initialSuffixes;
	}
	
	@LLComponentParameter(name = "cexHandler")
	public void setCEXHandler(ObservationTableCEXHandler<I, Boolean> cexHandler) {
		this.cexHandler = cexHandler;
	}
	
	@LLComponentParameter(name = "closingStrategy")
	public void setClosingStrategy(ClosingStrategy<I,Boolean> closingStrategy) {
		this.closingStrategy = closingStrategy;
	}

	@Override
	public ExtensibleLStarDFA<I> instantiate() {
		return new ExtensibleLStarDFA<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}

}
