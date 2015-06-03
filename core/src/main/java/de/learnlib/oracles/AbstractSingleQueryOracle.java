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

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.QueryAnswerer;
import de.learnlib.api.SingleQueryOracle;

/**
 * Base class for oracles whose semantic is defined in terms of directly answering single queries
 * (like a {@link QueryAnswerer}, and that cannot profit from batch processing of queries.
 * <p>
 * Subclassing this class instead of directly implementing {@link MembershipOracle} means that
 * the {@link #answerQuery(Word, Word)} instead of the {@link #processQueries(Collection)} method
 * needs to be implemented.
 * 
 * @deprecated since 2015-05-10. This class is no longer necessary due to the introduction
 * of default methods. Instead, implement {@link SingleQueryOracle} (or the respective specialization)
 * directly.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@Deprecated
@ParametersAreNonnullByDefault
public abstract class AbstractSingleQueryOracle<I, D> implements SingleQueryOracle<I, D> {
	
	@Deprecated
	public static abstract class AbstractSingleQueryOracleDFA<I>
			extends AbstractSingleQueryOracle<I,Boolean> implements SingleQueryOracleDFA<I> {}
	
	@Deprecated
	public static abstract class AbstractSingleQueryOracleMealy<I,O>
			extends AbstractSingleQueryOracle<I,Word<O>> implements SingleQueryOracleMealy<I,O> {}
}
