package de.learnlib.algorithms.discriminationtree;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Symbol;

import org.testng.annotations.Test;

import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;

@Test
public class DTLearnerDFATest extends AbstractDTLearnerTest {
	
	
	protected static <I> void testLearnDFA(Alphabet<I> alphabet, DFA<?,I> dfa) {
		DFASimulatorOracle<I> oracle = new DFASimulatorOracle<>(dfa);
		
		for(LocalSuffixFinder<Object,Object> lsf : SUFFIX_FINDERS) {
			DTLearnerDFA<I> learner = new DTLearnerDFA<>(alphabet, oracle, lsf);
		
			testLearn(learner, alphabet, dfa);
		}
	}

	@Test
	public void testExampleAngluin() {
		Alphabet<Integer> alphabet = ExampleAngluin.getInputAlphabet();
		DFA<?,Integer> dfa = ExampleAngluin.getInstance();
		
		testLearnDFA(alphabet, dfa);
	}
	
	@Test
	public void testExamplePaulMary() {
		Alphabet<Symbol> alphabet = ExamplePaulAndMary.getInputAlphabet();
		DFA<?,Symbol> dfa = ExamplePaulAndMary.getInstance();
		
		testLearnDFA(alphabet, dfa);
	}


}
