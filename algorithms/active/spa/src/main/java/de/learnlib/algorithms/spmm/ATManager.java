/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.spmm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.learnlib.algorithms.sba.SymbolWrapper;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public interface ATManager<I, O> {

    Word<I> getAccessSequence(I procedure);

    Word<I> getTerminatingSequence(I procedure);

    Pair<Set<I>, Set<I>> scanCounterexample(DefaultQuery<I, Word<O>> counterexample);

    Set<I> scanRefinedProcedures(Map<I, ? extends MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures,
                                 Map<I, ? extends AccessSequenceTransformer<SymbolWrapper<I>>> providers,
                                 Collection<SymbolWrapper<I>> inputs);

}
