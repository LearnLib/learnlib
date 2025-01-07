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
package de.learnlib.algorithm.lstar.closing;

import java.util.List;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;

/**
 * A closing strategy, determining how to proceed when an observation table needs to be closed.
 *
 * @param <I>
 *         type variable for input symbol upper bound.
 * @param <D>
 *         type variable for output symbol upper bound.
 */
public interface ClosingStrategy<I, D> {

    /**
     * Given a list of row equivalence classes, this method selects for each of the classes one (representative) row
     * which is being closed. This corresponds to selecting one of several long prefixes (i.e., transitions reaching an
     * unknown state) to be an access sequence.
     * <p>
     * By contract, the size of the returned list <b>must</b> equal the size of the {@code unclosedClasses} argument.
     *
     * @param unclosedClasses
     *         the list of row equivalence classes
     * @param table
     *         the observation table
     * @param oracle
     *         the membership oracle
     * @param <RI>
     *         the (concrete) row input type
     * @param <RO>
     *         the (concrete) row output type
     *
     * @return a selection of representative rows to be closed.
     */
    <RI extends I, RO extends D> List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses,
                                                                 ObservationTable<RI, RO> table,
                                                                 MembershipOracle<RI, RO> oracle);
}
