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
package de.learnlib.api.algorithm.feature;

import java.util.Collection;

import de.learnlib.api.algorithm.LearningAlgorithm;
import net.automatalib.words.Word;

public interface GlobalSuffixFeature<I> {

    /**
     * Retrieves the global suffixes of this learner. Calling this method before {@link
     * LearningAlgorithm#startLearning()} should return an empty collection.
     * <p>
     * The return value should not be modified; attempting to do so may result in an {@link
     * UnsupportedOperationException}. It is the implementation's responsibility to ensure attempted modifications do
     * not corrupt the learner's internal state.
     *
     * @return the global suffixes used by this algorithm
     */
    Collection<Word<I>> getGlobalSuffixes();

    /**
     * Add the provided suffixes to the collection of global suffixes. As this method is designed to possibly trigger a
     * <em>refinement</em>, it is illegal to invoke it before {@link LearningAlgorithm#startLearning()} has been
     * called.
     * <p>
     * The implementation may choose to (but is not required to) omit suffixes which are already present (that is,
     * manage the global suffixes as a proper set).
     *
     * @param globalSuffixes
     *         the global suffixes to add
     *
     * @return {@code true} if a refinement was triggered by adding the global suffixes, {@code false otherwise}.
     */
    boolean addGlobalSuffixes(Collection<? extends Word<I>> globalSuffixes);
}
