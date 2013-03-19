package de.learnlib.oracles;

import java.util.Collection;

import net.automatalib.automata.concepts.SODetOutputAutomaton;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public class SimulatorOracle<I, O> implements MembershipOracle<I, O> {
	
	private final SODetOutputAutomaton<?, I, ?, O> automaton;
	
	public SimulatorOracle(SODetOutputAutomaton<?,I,?,O> automaton) {
		this.automaton = automaton;
	}
	
	@Override
	public void processQueries(Collection<Query<I, O>> queries) {
		for(Query<I,O> q : queries) {
			O output = automaton.computeSuffixOutput(q.getPrefix(), q.getSuffix());
			q.setOutput(output);
		}
	}
	
}
