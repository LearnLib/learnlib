/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.algorithms.lstargeneric.dfa;

import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.features.observationtable.OTLearner.OTLearnerDFA;
import de.learnlib.algorithms.lstargeneric.ExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;


/**
 * An implementation of Angluin's L* algorithm for learning DFAs, as described in the paper
 * "Learning Regular Sets from Queries and Counterexamples".
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 */
public class ExtensibleLStarDFA<I>
	extends ExtensibleAutomatonLStar<DFA<?,I>, I, Boolean, Integer, Integer, Boolean, Void, CompactDFA<I>>
	implements OTLearnerDFA<I> {

	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet.
	 * @param oracle the DFA oracle.
	 */
	public ExtensibleLStarDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super Boolean> cexHandler,
			ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
		this(alphabet, oracle, Collections.singletonList(Word.<I>epsilon()), initialSuffixes, cexHandler, closingStrategy);
	}
	
	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public ExtensibleLStarDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle,
			List<Word<I>> initialPrefixes,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super Boolean> cexHandler,
			ClosingStrategy<? super I, ? super Boolean> closingStrategy) {
		super(alphabet, oracle, new CompactDFA<I>(alphabet),
				initialPrefixes,
				LStarDFAUtil.ensureSuffixCompliancy(initialSuffixes),
				cexHandler, closingStrategy);
	}


	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractLStar#initialSuffixes()
	 */
	@Override
	protected List<Word<I>> initialSuffixes() {
		return Collections.singletonList(Word.<I>epsilon());
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#stateProperty(de.learnlib.lstar.Row)
	 */
	@Override
	protected Boolean stateProperty(Row<I> stateRow) {
		return table.cellContents(stateRow, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#transitionProperty(de.learnlib.lstar.Row, int)
	 */
	@Override
	protected Void transitionProperty(Row<I> stateRow, int inputIdx) {
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.AbstractAutomatonLStar#exposeInternalHypothesis()
	 */
	@Override
	protected DFA<?, I> exposeInternalHypothesis() {
		return internalHyp;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.ExtensibleAutomatonLStar#hypothesisOutput()
	 */
	@Override
	protected SuffixOutput<I, Boolean> hypothesisOutput() {
		return internalHyp;
	}


}