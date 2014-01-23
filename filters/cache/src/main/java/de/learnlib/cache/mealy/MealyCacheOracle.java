/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.cache.mealy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;

/**
 * Mealy cache. This cache is implemented as a membership oracle: upon construction, it is
 * provided with a delegate oracle. Queries that can be answered from the cache are answered
 * directly, others are forwarded to the delegate oracle. When the delegate oracle has finished
 * processing these remaining queries, the results are incorporated into the cache.
 * 
 * This oracle additionally enables the user to define a Mealy-style prefix-closure filter:
 * a {@link Mapping} from output symbols to output symbols may be provided, with the following
 * semantics: If in an output word a symbol for which the given mapping has a non-null value
 * is encountered, all symbols <i>after</i> this symbol are replaced by the respective value.
 * The rationale behind this is that the concrete error message (key in the mapping) is still
 * reflected in the learned model, it is forced to result in a sink state with only a single
 * repeating output symbol (value in the mapping).
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class MealyCacheOracle<I, O> implements MealyMembershipOracle<I,O> {
	
	private static final class ReverseLexCmp<I> implements Comparator<Query<I,?>> {
		private final Alphabet<I> alphabet;
		
		public ReverseLexCmp(Alphabet<I> alphabet) {
			this.alphabet = alphabet;
		}
		
		@Override
		public int compare(Query<I, ?> o1, Query<I, ?> o2) {
			return -CmpUtil.lexCompare(o1.getInput(), o2.getInput(), alphabet);
		}
	}
	
	public static <I,O>
	MealyCacheOracle<I,O> createDAGCacheOracle(Alphabet<I> inputAlphabet, MembershipOracle<I,Word<O>> delegate) {
		return createDAGCacheOracle(inputAlphabet, null, delegate);
	}
	
	public static <I,O>
	MealyCacheOracle<I,O> createDAGCacheOracle(
			Alphabet<I> inputAlphabet, Mapping<? super O,? extends O> errorSyms, MembershipOracle<I,Word<O>> delegate) {
		IncrementalMealyBuilder<I,O> incrementalBuilder = new IncrementalMealyDAGBuilder<>(inputAlphabet);
		return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
	}
	
	public static <I,O>
	MealyCacheOracle<I,O> createTreeCacheOracle(Alphabet<I> inputAlphabet, MembershipOracle<I,Word<O>> delegate) {
		return createTreeCacheOracle(inputAlphabet, null, delegate);
	}
	
	public static <I,O>
	MealyCacheOracle<I,O> createTreeCacheOracle(Alphabet<I> inputAlphabet, Mapping<? super O,? extends O> errorSyms, MembershipOracle<I,Word<O>> delegate) {
		IncrementalMealyBuilder<I,O> incrementalBuilder = new IncrementalMealyTreeBuilder<>(inputAlphabet);
		return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
	}
	
	private final MembershipOracle<I,Word<O>> delegate;
	private final IncrementalMealyBuilder<I, O> incMealy;
	private final Comparator<? super Query<I,?>> queryCmp;
	private final Mapping<? super O,? extends O> errorSyms;
	
	
	public MealyCacheOracle(IncrementalMealyBuilder<I, O> incrementalBuilder, Mapping<? super O,? extends O> errorSyms, MembershipOracle<I,Word<O>> delegate) {
		this.incMealy = incrementalBuilder;
		this.queryCmp = new ReverseLexCmp<>(incrementalBuilder.getInputAlphabet());
		this.errorSyms = errorSyms;
		this.delegate = delegate;
	}
	/**
	 * Constructor.
	 * @param alphabet the input alphabet for the cache
	 * @param delegate the delegate Mealy oracle
	 * @deprecated since 2014-01-23. Use {@link #createDAGCacheOracle(Alphabet, MembershipOracle)} to reproduce old behavior.
	 */
	@Deprecated
	public MealyCacheOracle(Alphabet<I> alphabet, MembershipOracle<I,Word<O>> delegate) {
		this(alphabet, null, delegate);
	}
	
	/**
	 * Constructor.
	 * @param alphabet the input alphabet for the cache
	 * @param errorSyms the error symbol mapping (see class description)
	 * @param delegate the delegate Mealy oracle
	 * @deprecated since 2014-01-23. Use {@link #createDAGCacheOracle(Alphabet, Mapping, MembershipOracle)} to reproduce old
	 * behavior.
	 */
	@Deprecated
	public MealyCacheOracle(Alphabet<I> alphabet, Mapping<? super O, ? extends O> errorSyms, MembershipOracle<I,Word<O>> delegate) {
		this(new IncrementalMealyDAGBuilder<I,O>(alphabet), errorSyms, delegate);
	}
	
	public int getCacheSize() {
		return incMealy.size();
	}
	
	/**
	 * Creates an equivalence oracle that checks an hypothesis for consistency with the
	 * contents of this cache. Note that the returned oracle is backed by the cache data structure,
	 * i.e., it is sufficient to call this method once after creation of the cache.
	 * @return the cache consistency test backed by the contents of this cache.
	 */
	public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
		return new MealyCacheConsistencyTest<>(incMealy);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		if(queries.isEmpty()) {
			return;
		}
		
		List<Query<I,Word<O>>> qrys = new ArrayList<Query<I,Word<O>>>(queries);
		Collections.sort(qrys, queryCmp);
		
		List<MasterQuery<I,O>> masterQueries = new ArrayList<>();
		
		Iterator<Query<I,Word<O>>> it = qrys.iterator();
		Query<I,Word<O>> q = it.next();
		Word<I> ref = q.getInput();
		MasterQuery<I,O> master = createMasterQuery(ref);
		if(master.getAnswer() == null)
			masterQueries.add(master);
		master.addSlave(q);
		
		while(it.hasNext()) {
			q = it.next();
			Word<I> curr = q.getInput();
			if(!curr.isPrefixOf(ref)) {
				master = createMasterQuery(curr);
				if(master.getAnswer() == null)
					masterQueries.add(master);
			}
			
			master.addSlave(q);
			ref = curr;
		}
		
		delegate.processQueries(masterQueries);
		
		for(MasterQuery<I,O> m : masterQueries)
			postProcess(m);
	}
	
	private void postProcess(MasterQuery<I,O> master) {
		Word<I> word = master.getSuffix();
		Word<O> answer = master.getAnswer();
		
		if(errorSyms == null) {
			incMealy.insert(word, answer);
			return;
		}
		
		int answLen = answer.length();
		int i = 0;
		while(i < answLen) {
			O sym = answer.getSymbol(i++);
			if(errorSyms.get(sym) != null)
				break;
		}
		
		if(i == answLen)
			incMealy.insert(word, answer);
		else
			incMealy.insert(word.prefix(i), answer.prefix(i));
	}
	
	private MasterQuery<I,O> createMasterQuery(Word<I> word) {
		WordBuilder<O> wb = new WordBuilder<O>();
		if(incMealy.lookup(word, wb)) {
			return new MasterQuery<>(word, wb.toWord());
		}
		
		if(errorSyms == null) {
			return new MasterQuery<>(word);
		}
		int wbSize = wb.size();
		O repSym;
		if(wbSize == 0 || (repSym = errorSyms.get(wb.getSymbol(wbSize - 1))) == null) {
			return new MasterQuery<>(word, errorSyms);
		}
		
		wb.repeatAppend(word.length() - wbSize, repSym);
		return new MasterQuery<>(word, wb.toWord());
	}

}
