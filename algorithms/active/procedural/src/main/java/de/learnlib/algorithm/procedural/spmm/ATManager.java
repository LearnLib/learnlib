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
package de.learnlib.algorithm.procedural.spmm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

/**
 * A manager of access sequences and terminating sequences of {@link SPMM}s during the learning process.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public interface ATManager<I, O> {

    /**
     * Returns an access sequence for the given procedure.
     *
     * @param procedure
     *         the call symbol that identifies the procedure
     *
     * @return the access sequence for the given procedure
     */
    Word<I> getAccessSequence(I procedure);

    /**
     * Returns a terminating sequence for the given procedure.
     *
     * @param procedure
     *         the call symbol that identifies the procedure
     *
     * @return the terminating sequence for the given procedure
     */
    Word<I> getTerminatingSequence(I procedure);

    /**
     * Extracts from a positive counterexample (potentially new) access sequences and terminating sequences.
     *
     * @param counterexample
     *         the counterexample
     *
     * @return a {@link Pair} of {@link Set}s of procedures (identified by their respective call symbols) for which new
     * access and terminating sequences could be extracted and for which previously none of the sequences were
     * available.
     */
    Pair<Set<I>, Set<I>> scanCounterexample(DefaultQuery<I, Word<O>> counterexample);

    /**
     * Scans a set of (hypothesis) procedures in order to potentially extract new access sequences and terminating
     * sequences.
     *
     * @param procedures
     *         a {@link Map} from call symbols to the respective procedural (hypothesis) models
     * @param providers
     *         a {@link Map} from call symbols to {@link AccessSequenceTransformer}s
     * @param inputs
     *         a {@link Collection} of input symbols which should be used for finding new access sequences, terminating
     *         sequences, and return sequences
     *
     * @return a {@link Set} of procedures (identified by their respective call symbols) for which terminating sequences
     * could be extracted and for which previously no such sequences were available.
     */
    Set<I> scanProcedures(Map<I, ? extends MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures,
                          Map<I, ? extends AccessSequenceTransformer<SymbolWrapper<I>>> providers,
                          Collection<SymbolWrapper<I>> inputs);

}
