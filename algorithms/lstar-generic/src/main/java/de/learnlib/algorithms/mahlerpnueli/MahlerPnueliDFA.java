package de.learnlib.algorithms.mahlerpnueli;

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

public class MahlerPnueliDFA<I> extends ExtensibleLStarDFA<I> {

	public MahlerPnueliDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
		this(alphabet, oracle, Collections.<Word<I>>emptyList(), ClosingStrategies.CLOSE_FIRST);
	}
	
	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public MahlerPnueliDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle, List<Word<I>> initialSuffixes, ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
		super(alphabet, oracle, initialSuffixes, ObservationTableCEXHandlers.MAHLER_PNUELI, closingStrategy);
	}

}
