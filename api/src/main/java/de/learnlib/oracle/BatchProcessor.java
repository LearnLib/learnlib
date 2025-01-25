/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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

import java.util.Collection;

import de.learnlib.exception.BatchInterruptedException;

/**
 * A markup interface for classes that can process a batch of work in a parallel environment (e.g. a {@link
 * MembershipOracle} when used by a {@link ParallelOracle}).
 *
 * @param <T>
 *         batch type
 */
public interface BatchProcessor<T> {

    /**
     * Process the batch.
     *
     * @param batch
     *         the batch to process
     *
     * @throws BatchInterruptedException
     *         if the processing thread was interrupted by an exception.
     */
    void processBatch(Collection<? extends T> batch);

}
