/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.oracle;

/**
 * Basic interface for {@link MembershipOracle}s that can process queries in parallel.
 * <p>
 * Parallel oracles usually use one or more dedicated worker threads in which the processing of queries is performed.
 * Since these do not have a defined life span, they must be terminated explicitly using {@link #shutdown()} or {@link
 * #shutdownNow()}.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public interface ParallelOracle<I, D> extends ThreadPool, MembershipOracle<I, D> {}
