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
package de.learnlib.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.automata.concepts.SuffixOutput;

import java.util.Collection;

/**
 * Utility oracle that delegates all queries to a given automaton (in fact we only need a {@link SuffixOutput}).
 *
 * @param <M> automaton type
 * @param <I> (query) input type
 * @param <D> (query) output type
 *
 * @author frohme
 */
public class AutomatonDelegationOracle<M extends SuffixOutput<I, D>, I, D> implements MembershipOracle<I, D> {

	private final M delegate;

	public AutomatonDelegationOracle(M delegate) {
		this.delegate = delegate;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, D>> queries) {
		for (final Query<I, D> q : queries) {
			q.answer(delegate.computeSuffixOutput(q.getPrefix(), q.getSuffix()));
		}
	}
}
