package de.learnlib.algorithms.rivestschapire;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.lstargeneric.ExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.api.MembershipOracle;

public class RivestSchapireDFA<I> extends ExtensibleLStarDFA<I> {

	public RivestSchapireDFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle) {
		this(alphabet, oracle, Collections.<Word<I>>emptyList(), ClosingStrategies.CLOSE_FIRST);
	}
	
	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public RivestSchapireDFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle,
			List<Word<I>> initialSuffixes,
			ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
		super(alphabet, oracle, initialSuffixes, ObservationTableCEXHandlers.RIVEST_SCHAPIRE, closingStrategy);
	}


}
