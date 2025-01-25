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
package de.learnlib.algorithm.lstar.ce;

import java.util.List;

import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.SuffixOutput;

public interface ObservationTableCEXHandler<I, D> {

    <RI extends I, RD extends D> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                          MutableObservationTable<RI, RD> table,
                                                                          SuffixOutput<RI, RD> hypOutput,
                                                                          MembershipOracle<RI, RD> oracle);

    boolean needsConsistencyCheck();
}
