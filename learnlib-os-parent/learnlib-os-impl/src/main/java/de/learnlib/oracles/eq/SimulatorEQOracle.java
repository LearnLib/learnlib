package de.learnlib.oracles.eq;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.Query;



public class SimulatorEQOracle<A extends UniversalDeterministicAutomaton<?,I,?,?,?>,I,O>
	implements EquivalenceOracle<A, I, O> {
	
	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
	
	public SimulatorEQOracle(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference) {
		this.reference = reference;
	}

	@Override
	public Query<I, O> findCounterExample(A hypothesis, Alphabet<I> alphabet) {
		Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, alphabet);
		if(sep == null)
			return null;
		return new Query<>(sep); // FIXME: Output missing!
	}
	
}