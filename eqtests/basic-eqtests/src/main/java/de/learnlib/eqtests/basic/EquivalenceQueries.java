package de.learnlib.eqtests.basic;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import de.learnlib.api.MembershipOracle;

public abstract class EquivalenceQueries {

	private EquivalenceQueries() {
	}

	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	WMethodEQOracle<A, I, O> wMethod(int maxDepth, MembershipOracle<I, O> sulOracle) {
		return new WMethodEQOracle<>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	WpMethodEQOracle<A, I, O> wpMethod(int maxDepth, MembershipOracle<I,O> sulOracle) {
		return new WpMethodEQOracle<>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	SimulatorEQOracle<I, O> simulator(A target) {
		A automaton = target;
		return new SimulatorEQOracle<I,O>(automaton);
	}

}
