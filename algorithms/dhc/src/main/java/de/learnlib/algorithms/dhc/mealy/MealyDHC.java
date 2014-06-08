/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.dhc.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import de.learnlib.algorithms.features.globalsuffixes.GlobalSuffixLearner.GlobalSuffixLearnerMealy;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.GlobalSuffixFinder;
import de.learnlib.counterexamples.GlobalSuffixFinders;
import de.learnlib.oracles.DefaultQuery;

/**
 *
 * @author Maik Merten 
 */
public class MealyDHC<I, O> implements MealyLearner<I,O>,
		AccessSequenceTransformer<I>, GlobalSuffixLearnerMealy<I, O> {
	
	
	public static class BuilderDefaults {
		public static <I,O>
		GlobalSuffixFinder<? super I,? super Word<O>> suffixFinder() {
			return GlobalSuffixFinders.RIVEST_SCHAPIRE; 
		}
		public static <I,O>
		Collection<? extends Word<I>> initialSplitters() {
			return null;
		}
	}

	private static final Logger log = Logger.getLogger( MealyDHC.class.getName() );
	
	private final Alphabet<I> alphabet;
	private final MembershipOracle<I, Word<O>> oracle;
	private LinkedHashSet<Word<I>> splitters = new LinkedHashSet<>();
	private CompactMealy<I, O> hypothesis;
	private MutableMapping<Integer, QueueElement<I,O>> accessSequences;
	private GlobalSuffixFinder<? super I,? super Word<O>> suffixFinder;


	private static class QueueElement<I,O> {
		private Integer parentState;
		private QueueElement<I,O> parentElement;
		private I transIn;
		private O transOut;
		private int depth;

		private QueueElement(Integer parentState, QueueElement<I,O> parentElement, I transIn, O transOut) {
			this.parentState = parentState;
			this.parentElement = parentElement;
			this.transIn = transIn;
			this.transOut = transOut;
			this.depth = (parentElement != null) ? parentElement.depth+1 : 0;
		}
	}
	
	/**
	 * Constructor, provided for backwards compatibility reasons.
	 *  
	 * @param alphabet the learning alphabet
	 * @param oracle the learning membership oracle
	 */
	public MealyDHC(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> oracle) {
		this(alphabet, oracle, GlobalSuffixFinders.RIVEST_SCHAPIRE, null);
	}

	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the learning membership oracle
	 * @param suffixFinder the {@link GlobalSuffixFinder suffix finder} to use for analyzing counterexamples
	 * @param initialSplitters the initial set of splitters, {@code null} or an empty collection will result
	 * in the set of splitters being initialized as the set of alphabet symbols (interpreted as {@link Word}s) 
	 */
	@GenerateBuilder(defaults = BuilderDefaults.class, builderFinal = false)
	public MealyDHC(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			GlobalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
			Collection<? extends Word<I>> initialSplitters) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.suffixFinder = suffixFinder;
		if(initialSplitters == null || initialSplitters.isEmpty()) {
			for(I symbol : alphabet) {
				splitters.add(Word.fromLetter(symbol));
			}
		}
		else {
			splitters.addAll(initialSplitters);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#startLearning()
	 */
	@Override
	public void startLearning() {
		// initialize structure to store state output signatures
		Map<List<Word<O>>, Integer> signatures = new HashMap<>();

		// set up new hypothesis machine
		hypothesis = new CompactMealy<>(alphabet);

		// initialize exploration queue
		Queue<QueueElement<I,O>> queue = new ArrayDeque<>();
		
		// initialize storage for access sequences
		accessSequences = hypothesis.createDynamicStateMapping();

		// first element to be explored represents the initial state with no predecessor
		queue.add(new QueueElement<I,O>(null, null, null, null));

		Interner<Word<O>> deduplicator = Interners.newStrongInterner();
		
		while (!queue.isEmpty()) {
			// get element to be explored from queue
			QueueElement<I,O> elem = queue.poll();

			// determine access sequence for state
			Word<I> access = assembleAccessSequence(elem);

			// assemble queries
			ArrayList<DefaultQuery<I, Word<O>>> queries = new ArrayList<>(splitters.size());
			for (Word<I> suffix : splitters) {
				queries.add(new DefaultQuery<I, Word<O>>(access, suffix));
			}

			// retrieve answers
			oracle.processQueries(queries);

			// assemble output signature
			List<Word<O>> sig = new ArrayList<>(splitters.size());
			for (DefaultQuery<I, Word<O>> query : queries) {
				sig.add(deduplicator.intern(query.getOutput()));
			}

			Integer sibling = signatures.get(sig);

			if (sibling != null) {
				// this element does not possess a new output signature
				// create a transition from parent state to sibling
				hypothesis.addTransition(elem.parentState, elem.transIn, sibling, elem.transOut);
			} else {
				// this is actually an observably distinct state! Progress!
				// Create state and connect via transition to parent
				Integer state = elem.parentElement == null ? hypothesis.addInitialState() : hypothesis.addState();
				if (elem.parentElement != null) {
					hypothesis.addTransition(elem.parentState, elem.transIn, state, elem.transOut);
				}
				signatures.put(sig, state);
				accessSequences.put(state, elem);

				scheduleSuccessors(elem, state, queue, sig);
			}
		}
	}

	private Word<I> assembleAccessSequence(QueueElement<I,O> elem) {
		List<I> word = new ArrayList<>(elem.depth);
		
		QueueElement<I,O> pre = elem.parentElement;
		I sym = elem.transIn;
		while(pre != null && sym != null) {
			word.add(sym);
			sym = pre.transIn;
			pre = pre.parentElement;
		}
		
		Collections.reverse(word);
		return Word.fromList(word);
	}

	private void scheduleSuccessors(QueueElement<I,O> elem, Integer state, Queue<QueueElement<I,O>> queue, List<Word<O>> sig) throws IllegalArgumentException {
		for (int i = 0; i < alphabet.size(); ++i) {
			// retrieve I/O for transition
			I input = alphabet.getSymbol(i);
			O output = sig.get(i).getSymbol(0);

			// create successor element and schedule for exploration
			queue.add(new QueueElement<>(state, elem, input, output));
		}
	}
	
	private void checkInternalState() {
		if (hypothesis == null) {
			throw new IllegalStateException("No hypothesis learned yet");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
		checkInternalState();

		Collection<? extends Word<I>> ceSuffixes
				= suffixFinder.findSuffixes(ceQuery, this, hypothesis, oracle);
		
		return addSuffixesUnchecked(ceSuffixes);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#getHypothesisModel()
	 */
	@Override
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		checkInternalState();
		return hypothesis;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#transformAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		checkInternalState();
		Integer state = hypothesis.getSuccessor(hypothesis.getInitialState(), word);
		return assembleAccessSequence(accessSequences.get(state));
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#isAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public boolean isAccessSequence(Word<I> word) {
		checkInternalState();
		Word<I> canonical = transformAccessSequence(word);
		return canonical.equals(word);
	}

	@Override
	public Collection<? extends Word<I>> getGlobalSuffixes() {
		return Collections.unmodifiableCollection(splitters);
	}

	@Override
	public boolean addGlobalSuffixes(Collection<? extends Word<I>> newGlobalSuffixes) {
		checkInternalState();
		
		return addSuffixesUnchecked(newGlobalSuffixes);
	}
	
	protected boolean addSuffixesUnchecked(Collection<? extends Word<I>> newSuffixes) {
		int oldSize = hypothesis.size();
		
		for(Word<I> suf : newSuffixes) {
			if(!splitters.contains(suf)) {
				splitters.add(suf);
				log.log(Level.FINE, "added suffix: {0}", suf);
			}
		}
		
		startLearning();
		
		return (hypothesis.size() != oldSize);
	}

}
