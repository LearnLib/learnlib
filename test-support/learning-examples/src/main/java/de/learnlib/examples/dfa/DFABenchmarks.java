package de.learnlib.examples.dfa;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.learnlibv2.LearnLibV2Serialization;
import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;
import de.learnlib.examples.LearningExample.DFALearningExample;

public class DFABenchmarks {
	
	public static DFALearningExample<Integer> loadLearnLibV2Benchmark(String name) {
		String resourceName = "/automata/learnlibv2/" + name + ".dfa";
		DFABenchmarks.class.getResource(resourceName);
		if (DFABenchmarks.class.getResource(resourceName) == null) {
			resourceName += ".gz"; // look for GZip compressed resource
			if (DFABenchmarks.class.getResource(resourceName) == null) {
				return null;
			}
		}
		
		try (InputStream is = DFABenchmarks.class.getResourceAsStream(resourceName)) {
			CompactDFA<Integer> dfa = LearnLibV2Serialization.getInstance().readGenericDFA(is);
			return new DefaultDFALearningExample<>(dfa);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static DFALearningExample<Integer> loadPots2() {
		return loadLearnLibV2Benchmark("pots2");
	}
	
	public static DFALearningExample<Integer> loadPots3() {
		return loadLearnLibV2Benchmark("pots3");
	}
	
	public static DFALearningExample<Integer> loadPeterson2() {
		return loadLearnLibV2Benchmark("peterson2");
	}
	
	public static DFALearningExample<Integer> loadPeterson3() {
		return loadLearnLibV2Benchmark("peterson3");
	}
}
