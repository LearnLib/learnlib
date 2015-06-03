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
package de.learnlib.cache.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.Query;
import de.learnlib.oracles.AbstractQuery;


/**
 * A "master" query. This query corresponds to a maximal input word in the batch,
 * and all queries that constitute prefixes of this input word are slaves of this query.
 * Upon answering the master query, all slave queries are also answered.
 *  
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
final class MasterQuery<I,O> extends AbstractQuery<I,Word<O>> {
	
	private Word<O> answer;
	private final Mapping<? super O,? extends O> errorSyms;
	
	private final List<Query<I,Word<O>>> slaves;
		
	public MasterQuery(Word<I> word) {
		this(word, (Mapping<? super O,? extends O>)null);
	}
	
	public MasterQuery(Word<I> word, Word<O> output) {
		super(word);
		this.answer = output;
		this.errorSyms = null;
		this.slaves = null;
	}
	
	public MasterQuery(Word<I> word, Mapping<? super O, ? extends O> errorSyms) {
		super(word);
		this.errorSyms = errorSyms;
		this.slaves = new ArrayList<>();
	}

	public Word<O> getAnswer() {
		return answer;
	}
	
	public boolean isAnswered() {
		return (answer != null);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.Query#answer(java.lang.Object)
	 */
	@Override
	public void answer(Word<O> output) {
		output = truncateOutput(output);
		this.answer = output;
		for(Query<I,Word<O>> slave : slaves) {
			answerSlave(slave);
		}
	}
	
	
	public void addSlave(Query<I,Word<O>> slave) {
		if(slaves == null) {
			answerSlave(slave);
		}
		else {
			slaves.add(slave);
		}
	}
	
	private void answerSlave(Query<I,Word<O>> slave) {
		int start = slave.getPrefix().length();
		int end = start + slave.getSuffix().length();
		slave.answer(answer.subWord(start, end));
	}
	
	private Word<O> truncateOutput(Word<O> output) {
		if(errorSyms == null) {
			return output;
		}
		
		int maxLen = output.length() - 1;
		int i = 0;
		O repSym = null;
		
		while(i < maxLen && repSym == null) {
			O sym = output.getSymbol(i++);
			repSym = errorSyms.get(sym);
		}
		
		if(repSym == null) {
			return output;
		}
		
		WordBuilder<O> wb = new WordBuilder<>(maxLen + 1);
		wb.append(output.prefix(i));
		wb.repeatAppend(1 + maxLen - i, repSym);
		
		return wb.toWord();
	}

	/**
	 * @see de.learnlib.oracles.AbstractQuery#toStringWithAnswer(Object)
	 */
	@Override
	public String toString() {
		return toStringWithAnswer(answer);
	}

}