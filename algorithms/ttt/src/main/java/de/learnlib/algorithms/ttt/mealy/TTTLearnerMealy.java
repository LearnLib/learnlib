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
package de.learnlib.algorithms.ttt.mealy;

import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.ttt.base.BaseTTTLearner;
import de.learnlib.algorithms.ttt.base.DTNode;
import de.learnlib.algorithms.ttt.base.TTTHypothesis.TTTEdge;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;

public class TTTLearnerMealy<I, O> extends
		BaseTTTLearner<MealyMachine<?, I, ?, O>, I, Word<O>> implements LearningAlgorithm.MealyLearner<I, O> {

	@GenerateBuilder(defaults = BaseTTTLearner.BuilderDefaults.class)
	public TTTLearnerMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder) {
		super(alphabet, oracle, new TTTHypothesisMealy<I,O>(alphabet), suffixFinder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return (TTTHypothesisMealy<I, O>) hypothesis;
	}
	
	@Override
	protected TTTTransition<I,Word<O>> createTransition(TTTState<I,Word<O>> state, I sym) {
		TTTTransitionMealy<I,O> trans = new TTTTransitionMealy<>(state, sym);
		trans.output = query(state, Word.fromLetter(sym)).firstSymbol();
		return trans;
	}

	@Override
	protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans,
			DTNode<I, Word<O>> succSeparator) {
		TTTTransitionMealy<I, O> mtrans = (TTTTransitionMealy<I, O>) trans;
		if (succSeparator == null) {
			return Word.fromLetter(mtrans.output);
		}
		return succSeparator.subtreeLabel(trans.getDTTarget()).prepend(mtrans.output);
	}

	@Override
	protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state,
			Iterable<? extends I> suffix) {
		TTTState<I,Word<O>> curr = state;
		
		WordBuilder<O> wb = new WordBuilder<>();
		
		for (I sym : suffix) {
			TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I,O>) hypothesis.getInternalTransition(curr, sym);
			wb.append(trans.output);
			curr = getTarget(trans);
		}
		
		return wb.toWord();
	}

	@Override
	public GraphDOTHelper<TTTState<I,Word<O>>, TTTEdge<I, Word<O>>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<TTTState<I,Word<O>>,TTTEdge<I,Word<O>>>() {
			@Override
			public boolean getEdgeProperties(TTTState<I, Word<O>> src,
					TTTEdge<I, Word<O>> edge, TTTState<I, Word<O>> tgt,
					Map<String, String> properties) {
				if (!super.getEdgeProperties(src, edge, tgt, properties)) {
					return false;
				}
				String label = String.valueOf(edge.transition.getInput());
				label += " / ";
				TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I,O>) edge.transition;
				if (trans.output != null) {
					label += trans.output;
				}
				properties.put(EdgeAttrs.LABEL, label);
				return true;
			}
		};
	}
}
