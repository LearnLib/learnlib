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
package de.learnlib.exception;

import java.util.Collection;

import de.learnlib.oracle.BatchProcessor;

/**
 * Exception that is thrown if a parallel batch is interrupted during processing. Note that we cannot rethrow the {@link
 * InterruptedException} since the {@code throws} specification of {@link BatchProcessor#processBatch(Collection)} does
 * not allow doing so.
 */
public class BatchInterruptedException extends RuntimeException {

    public BatchInterruptedException(Throwable cause) {
        super(cause);
    }

}
