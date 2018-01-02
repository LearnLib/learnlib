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
package de.learnlib.algorithms.lstar.ce;

import java.util.List;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;

public interface ObservationTableCEXHandler<I, D> {

    <RI extends I, RD extends D> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                          MutableObservationTable<RI, RD> table,
                                                                          SuffixOutput<RI, RD> hypOutput,
                                                                          MembershipOracle<RI, RD> oracle);

    boolean needsConsistencyCheck();
}
