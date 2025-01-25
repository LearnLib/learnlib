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
package de.learnlib.algorithm.procedural.spa;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.word.Word;

/**
 * A manager of access sequences, terminating sequences, and return sequences of {@link SPA}s during the learning
 * process.
 *
 * @param <I>
 *         input symbol type
 */
public interface ATRManager<I> {

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
     * Returns a return sequence for the given procedure. Note that the sequence must match the
     * {@link #getAccessSequence(Object) access sequence} in order to provide an admissible context for query
     * expansion.
     *
     * @param procedure
     *         the call symbol that identifies the procedure
     *
     * @return the return sequence for the given procedure
     */
    Word<I> getReturnSequence(I procedure);

    /**
     * Extracts from a positive counterexample (potentially new) access sequences, terminating sequences, and return
     * sequences.
     *
     * @param counterexample
     *         the counterexample
     *
     * @return a {@link Set} of procedures (identified by their respective call symbols) for which new access,
     * terminating, and return sequences could be extracted and for which previously none of the sequences were
     * available.
     */
    Set<I> scanPositiveCounterexample(Word<I> counterexample);

    /**
     * Scans a set of (hypothesis) procedures in order to potentially extract new access sequences, terminating
     * sequences, and return sequences.
     *
     * @param procedures
     *         a {@link Map} from call symbols to the respective procedural (hypothesis) models
     * @param providers
     *         a {@link Map} from call symbols to {@link AccessSequenceTransformer}s
     * @param inputs
     *         a {@link Collection} of input symbols which should be used for finding new access sequences, terminating
     *         sequences, and return sequences
     */
    void scanProcedures(Map<I, ? extends DFA<?, I>> procedures,
                        Map<I, ? extends AccessSequenceTransformer<I>> providers,
                        Collection<I> inputs);

}
