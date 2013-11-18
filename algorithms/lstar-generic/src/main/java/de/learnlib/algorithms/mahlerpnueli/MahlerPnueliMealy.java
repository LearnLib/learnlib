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
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.MembershipOracle;

public class MahlerPnueliMealy<I,O> extends ExtensibleLStarMealy<I, O> {

	public MahlerPnueliMealy(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
		this(alphabet, oracle, Collections.<Word<I>>emptyList(), ClosingStrategies.CLOSE_FIRST);
	}
	
	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public MahlerPnueliMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			List<Word<I>> initialSuffixes,
			ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
		super(alphabet, oracle, initialSuffixes, ObservationTableCEXHandlers.MAHLER_PNUELI, closingStrategy);
	}

}
