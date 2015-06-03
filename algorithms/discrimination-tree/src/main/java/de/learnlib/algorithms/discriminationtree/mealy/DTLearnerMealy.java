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
package de.learnlib.algorithms.discriminationtree.mealy;

import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.discriminationtree.MultiDTree;
import de.learnlib.oracles.AbstractQuery;

/**
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class DTLearnerMealy<I, O>
		extends
		AbstractDTLearner<MealyMachine<?, I, ?, O>, I, Word<O>, Void, O>
		implements
		MealyLearner<I, O> {
	
	private final HypothesisWrapperMealy<I, O> hypWrapper;

	/**
	 * Constructor.
	 * @param alphabet the input alphabet
	 * @param oracle the membership oracle
	 * @param suffixFinder method to use for analyzing counterexamples
	 */
	@GenerateBuilder(defaults = AbstractDTLearner.BuilderDefaults.class)
	public DTLearnerMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
			boolean repeatedCounterexampleEvaluation) {
		super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new MultiDTree<I,Word<O>,HState<I,Word<O>,Void,O>>(oracle));
		this.hypWrapper = new HypothesisWrapperMealy<>(hypothesis);
	}

	@Override
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return hypWrapper;
	}

	@Override
	protected Query<I, Word<O>> spQuery(HState<I, Word<O>, Void, O> state) {
		return null;
	}

	@Override
	protected Query<I, Word<O>> tpQuery(
			final HTransition<I, Word<O>, Void, O> transition) {
		return new AbstractQuery<I,Word<O>>(transition.getSource().getAccessSequence(), Word.fromLetter(transition.getSymbol())) {
			@Override
			public void answer(Word<O> output) {
				transition.setProperty(output.firstSymbol());
			}
		};
	}
	
	@Override
	public GraphDOTHelper<HState<I, Word<O>, Void, O>, HTransition<I, Word<O>, Void, O>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<HState<I,Word<O>,Void,O>,HTransition<I,Word<O>,Void,O>>() {
			@Override
			public boolean getEdgeProperties(HState<I, Word<O>, Void, O> src,
					HTransition<I, Word<O>, Void, O> edge,
					HState<I, Word<O>, Void, O> tgt,
					Map<String, String> properties) {
				if (!super.getEdgeProperties(src, edge, tgt, properties)) {
					return false;
				}
				String label = String.valueOf(edge.getSymbol());
				label += " / ";
				if (edge.getProperty() != null) {
					label += edge.getProperty();
				}
				properties.put(EdgeAttrs.LABEL, label);
				
				return true;
			}

		};
	}
}
