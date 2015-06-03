/* Copyright (C) 2013-2015 TU Dortmund
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
package de.learnlib.oracles;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.SUL;
import de.learnlib.api.SULException;

/**
 * A wrapper around a system under learning (SUL).
 * <p>
 * This membership oracle is thread-safe. Thread-safety is obtained in either of the following
 * ways:
 * <ul>
 * <li>if the {@link SUL} can be {@link SUL#fork() forked}, each thread from which
 * {@link #processQueries(Collection)} will maintain a {@link ThreadLocal thread-local} fork
 * of the SUL, which is used for processing queries.</li>
 * <li>otherwise, if the SUL is not forkable, accesses to the SUL in {@link #processQueries(Collection)}
 * will be synchronized explicitly.</li>
 * </ul>
 * 
 * @author Falk Howar
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class SULOracle<I, O> implements MealyMembershipOracle<I,O> {

	private final SUL<I, O> sul;
	private final ThreadLocal<SUL<I,O>> localSul;

	public SULOracle(SUL<I, O> sul) {
		this.sul = sul;
		if (sul.canFork()) {
			this.localSul = new ThreadLocal<SUL<I,O>>() {
				@Override
				protected SUL<I,O> initialValue() {
					return sul.fork();
				}
			};
		}
		else {
			this.localSul = null;
		}
	}
	
	@Override
	public void processQueries(Collection<? extends Query<I,Word<O>>> queries) {
		if (localSul != null) {
			processQueries(localSul.get(), queries);
		}
		else {
			synchronized(sul) {
				processQueries(sul, queries);
			}
		}
	}

	private static <I,O> void processQueries(SUL<I,O> sul, Collection<? extends Query<I,Word<O>>> queries) {
		for (Query<I,Word<O>> q : queries) {
			Word<O> output = answerQuery(sul, q.getPrefix(), q.getSuffix());
			q.answer(output);
		}
	}
	
	@Nonnull
	private static <I,O> Word<O> answerQuery(SUL<I,O> sul, Word<I> prefix, Word<I> suffix) throws SULException {
		sul.pre();
		try {
			// Prefix: Execute symbols, don't record output
			for(I sym : prefix) {
				sul.step(sym);
			}
			
			// Suffix: Execute symbols, outputs constitute output word
			WordBuilder<O> wb = new WordBuilder<>(suffix.length());
			for(I sym : suffix) {
				wb.add(sul.step(sym));
			}
			
			return wb.toWord();
		}
		finally {
			sul.post();
		}
	}

}
