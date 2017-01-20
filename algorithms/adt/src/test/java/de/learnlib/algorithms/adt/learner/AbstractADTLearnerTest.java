/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.adt.learner;

import de.learnlib.algorithms.adt.api.ADTExtender;
import de.learnlib.algorithms.adt.api.LeafSplitter;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;
import de.learnlib.algorithms.adt.util.CompactMealyWrapperSUL;
import de.learnlib.algorithms.adt.config.ADTExtenders;
import de.learnlib.algorithms.adt.config.LeafSplitters;
import de.learnlib.algorithms.adt.config.SubtreeReplacers;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.SymbolQueryOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SULSymbolQueryOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test for {@link ADTLearner}.
 *
 * @author frohme.
 */
public abstract class AbstractADTLearnerTest<I, O> {

	protected final static Integer BIG_AUTOMATON_SIZE = 50;

	protected final static Integer SMALL_AUTOMATON_SIZE = 10;

	@Test
	public void runDefaultSplitter_NeverAnalyze_NeverReplace() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.NOP,
							 SubtreeReplacers.NEVER_REPLACE);
	}

	@Test
	public void runDefaultSplitter_NeverAnalyze_GreedyReplace() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.NOP,
							 SubtreeReplacers.LEVELED_BEST_EFFORT);
	}

	@Test
	public void runDefaultSplitter_NeverAnalyze_GreedyReplace2() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.NOP,
							 SubtreeReplacers.EXHAUSTIVE_MIN_LENGTH);
	}

	@Test
	public void runDefaultSplitter_NeverAnalyze_GreedyReplace3() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.NOP,
							 SubtreeReplacers.SINGLE_MIN_SIZE);
	}

	@Test
	public void runDefaultSplitter_ContinueAnalyze_NeverReplace() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.EXTEND_BEST_EFFORT,
							 SubtreeReplacers.NEVER_REPLACE);
	}

	@Test
	public void runDefaultSplitter_ContinueAnalyze_GreedyReplace() {
		this.runWithReplacer(LeafSplitters.DEFAULT_SPLITTER,
							 ADTExtenders.EXTEND_BEST_EFFORT,
							 SubtreeReplacers.LEVELED_BEST_EFFORT);
	}

	@Test
	public void runParentSplitter_NeverAnalyze_NeverReplace() {
		this.runWithReplacer(LeafSplitters.EXTEND_PARENT, ADTExtenders.NOP, SubtreeReplacers.NEVER_REPLACE);
	}

	@Test
	public void runParentSplitter_NeverAnalyze_GreedyReplace() {
		this.runWithReplacer(LeafSplitters.EXTEND_PARENT,
							 ADTExtenders.NOP,
							 SubtreeReplacers.LEVELED_BEST_EFFORT);
	}

	@Test
	public void runParentSplitter_ContinueAnalyze_NeverReplace() {
		this.runWithReplacer(LeafSplitters.EXTEND_PARENT,
							 ADTExtenders.EXTEND_BEST_EFFORT,
							 SubtreeReplacers.NEVER_REPLACE);
	}

	@Test
	public void runParentSplitter_ContinueAnalyze_GreedyReplace() {
		this.runWithReplacer(LeafSplitters.EXTEND_PARENT,
							 ADTExtenders.EXTEND_BEST_EFFORT,
							 SubtreeReplacers.LEVELED_BEST_EFFORT);
	}

	private void runWithReplacer(final LeafSplitter leafSplitter,
								 final ADTExtender analyzer,
								 final SubtreeReplacer replacer) {
		final CompactMealy<I, O> target = this.getTarget();
		final Alphabet<I> alphabet = target.getInputAlphabet();
		final SymbolQueryOracle<I, O> statOracle = new SULSymbolQueryOracle<>(new CompactMealyWrapperSUL<>(target));
		final LearningAlgorithm.MealyLearner<I, O> learner =
				new ADTLearner<>(alphabet, statOracle, leafSplitter, analyzer, replacer);

		learner.startLearning();
		final MealyMachine<?, I, ?, O> hyp = learner.getHypothesisModel();

		while (Automata.findSeparatingWord(target, hyp, alphabet) != null) {
			final Word<I> sepWord = Automata.findSeparatingWord(target, hyp, alphabet);
			final DefaultQuery<I, Word<O>> ce =
					new DefaultQuery<>(Word.epsilon(), sepWord, target.computeOutput(sepWord));

			if (!learner.refineHypothesis(ce)) {
				break;
			}
		}

		Assert.assertEquals(target.size(), hyp.size());
		Assert.assertTrue(Automata.testEquivalence(target, hyp, alphabet));
	}

	protected abstract CompactMealy<I, O> getTarget();

	protected static Iterator<Object[]> generateSizes(final int size) {

		final List<Object[]> sizes = new ArrayList<>(size);

		for (int i = 1; i <= size; i++) {
			sizes.add(new Integer[] { i });
		}

		return sizes.iterator();
	}
}
