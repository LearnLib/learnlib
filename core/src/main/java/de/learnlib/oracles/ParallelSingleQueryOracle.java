/* Copyright (C) 2015 TU Dortmund
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

import de.learnlib.api.Query;
import de.learnlib.api.SingleQueryOracle;

/**
 * Base interface for oracle that answer queries independently and in parallel.
 * 
 * @deprecated since 2015-05-10. Parallelization of query processing should not be determined
 * by an oracle implementation and always be configurable. Instead, implement
 * {@link SingleQueryOracle} (or the respective specialization) and use the
 * {@link MQUtil#PARALLEL_THRESHOLD} variable (or setting) to configure parallelization.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@Deprecated
public interface ParallelSingleQueryOracle<I, D> extends SingleQueryOracle<I,D> {
	@Override
	default public void processQueries(Collection<? extends Query<I,D>> queries) {
		MQUtil.answerQueriesParallel(this, queries);
	}
}
