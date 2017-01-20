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
package de.learnlib.mealy;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.SymbolQueryOracle;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

/**
 * A wrapper that allows to use a {@link SymbolQueryOracle} where a
 * {@link de.learnlib.api.MembershipOracle.MealyMembershipOracle} is expected.
 *
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class SQOToMQOWrapper<I, O> implements MembershipOracle<I, Word<O>> {

	private final SymbolQueryOracle<I, O> delegate;

	public SQOToMQOWrapper(final SymbolQueryOracle<I, O> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> collection) {

		for (final Query<I, Word<O>> q : collection) {
			this.delegate.reset();

			final WordBuilder<O> wb = new WordBuilder<>(q.getSuffix().size() + 1);
			wb.append(Word.epsilon());

			for (final I i : q.getPrefix()) {
				this.delegate.query(i);
			}

			for (final I i : q.getSuffix()) {
				wb.append(this.delegate.query(i));
			}

			q.answer(wb.toWord());
		}

	}
}
