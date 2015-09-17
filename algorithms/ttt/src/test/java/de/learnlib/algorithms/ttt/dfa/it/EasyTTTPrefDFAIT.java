/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.ttt.dfa.it;

import java.io.File;
import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.taf.TAFSerialization;
import net.automatalib.serialization.taf.writer.TAFWriter;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import org.testng.annotations.Test;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.analyzers.NamedAcexAnalyzer;
import de.learnlib.algorithms.ttt.base.EasyTTTPref;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.eqtests.basic.SimulatorEQOracle.DFASimulatorEQOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.dfa.DFABenchmarks;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;

@Test
public class EasyTTTPrefDFAIT extends AbstractDFALearnerIT {

	@Override
	protected <I> void addLearnerVariants(Alphabet<I> alphabet, int targetSize,
			DFAMembershipOracle<I> mqOracle, DFALearnerVariantList<I> variants) {
		
		
		for (NamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
			EasyTTTPref<I> learner = new EasyTTTPref<I>(alphabet, mqOracle, analyzer);
			variants.addLearnerVariant("analyzer=" + analyzer, learner);
		}
	}

	
	public static void main(String[] args) throws Exception {
		DFALearningExample<Integer> ex = DFABenchmarks.loadPeterson2();
		System.err.println(ex.getReferenceAutomaton().size());
		System.err.println(ex.getAlphabet().size());
		TAFWriter.writeDFA(ex.getReferenceAutomaton(), ex.getAlphabet(), System.out);
		System.out.flush();
		
		Alphabet<Integer> alphabet = Alphabets.integers(0, 4);
		CompactDFA<Integer> target = RandomAutomata.randomICDFA(new Random(), 100, alphabet, true);
		
		DFASimulatorOracle<Integer> oracle = new DFASimulatorOracle<>(target);
		DFASimulatorEQOracle<Integer> eqOracle = new DFASimulatorEQOracle<>(target);
		
		EasyTTTPref<Integer> learner = new EasyTTTPref<>(alphabet, oracle, AcexAnalyzers.EXPONENTIAL_FWD);
		
		DefaultQuery<Integer, Boolean> ceQuery;
		
		learner.startLearning();
		
		while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
			learner.refineHypothesis(ceQuery);
		}
	}
}
