/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.filter.reuse.tree;

/**
 * A implementation of this interface that is set to the {@link ReuseTree} (see {@link
 * de.learnlib.filter.reuse.ReuseOracle.ReuseOracleBuilder#withSystemStateHandler(SystemStateHandler)}) will be informed
 * about all removed system states whenever {@link ReuseTree#disposeSystemstates()} gets called.
 * <p>
 * The objective of this handler is that clearing system states from the reuse tree may also be resulting in cleaning up
 * the SUL by e.g. perform tasks like removing persisted entities from a database.
 * <p>
 * Please note that the normal removal of system states (by sifting them down in the reuse tree by executing only
 * suffixes of a query) is not be seen as a disposing.
 *
 * @param <S>
 *         system state class
 *
 * @author Oliver Bauer
 */
public interface SystemStateHandler<S> {

    /**
     * The system state S will be removed from the {@link ReuseTree}.
     *
     * @param state
     *         The state to remove.
     */
    void dispose(S state);
}
