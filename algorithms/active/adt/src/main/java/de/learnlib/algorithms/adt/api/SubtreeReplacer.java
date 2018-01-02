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
package de.learnlib.algorithms.adt.api;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.model.ReplacementResult;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

/**
 * Interface for configuration objects that specify how nodes of the current ADT should be replaced.
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public interface SubtreeReplacer {

    /**
     * Compute how certain nodes of the ADT should be replaced. It is assumed, the replacements are well-defined (i.e.
     * each replaced node belongs to a distinct subtree).
     * <p>
     * Currently only replacements in the form of an ADS (i.e. no reset nodes) are supported.
     *
     * @param hypothesis
     *         the current hypothesis (without any undefined transitions)
     * @param inputs
     *         the input alphabet
     * @param adt
     *         the current adaptive discrimination tree
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return A {@link Set} of proposed replacements
     */
    <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(MealyMachine<S, I, ?, O> hypothesis,
                                                                  Alphabet<I> inputs,
                                                                  ADT<S, I, O> adt);

}
