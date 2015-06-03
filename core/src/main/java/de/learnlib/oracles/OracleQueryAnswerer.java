/* Copyright (C) 2013-2014 TU Dortmund
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.QueryAnswerer;


/**
 * A {@link QueryAnswerer} that queries a {@link MembershipOracle} for implementing
 * the {@link #answerQuery(Word,Word)} method.
 * 
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 * 
 * @deprecated since 2015-05-10. {@link MembershipOracle} now extends {@link QueryAnswerer},
 * rendering this class obsolete.
 */
@Deprecated
@ParametersAreNonnullByDefault
public final class OracleQueryAnswerer<I, D> implements QueryAnswerer<I,D> {
	
	private final MembershipOracle<I,D> oracle;
	
	public OracleQueryAnswerer(MembershipOracle<I,D> oracle) {
		this.oracle = oracle;
	}

	@Override
	@Nullable
	public D answerQuery(Word<I> prefix, Word<I> suffix) {
		return oracle.answerQuery(prefix, suffix);
	}

}
