/* Copyright (C) 2015 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
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
