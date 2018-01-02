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

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.automaton.ADTHypothesis;
import de.learnlib.algorithms.adt.automaton.ADTState;
import de.learnlib.algorithms.adt.model.ExtensionResult;

/**
 * Interface for configuration objects that specify how to finalize the temporary splitter given by regular
 * counterexample decomposition.
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public interface ADTExtender {

    /**
     * Compute the ADT whose root node should replace the root of the temporary splitter in the current ADT.
     *
     * @param hypothesis
     *         the current hypothesis (with potentially undefined transitions/outputs)
     * @param pta
     *         the {@link PartialTransitionAnalyzer} for managing partial transitions
     * @param temporarySplitter
     *         the current temporary ADT based on the decomposed counterexample
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return the extension result
     */
    <I, O> ExtensionResult<ADTState<I, O>, I, O> computeExtension(ADTHypothesis<I, O> hypothesis,
                                                                  PartialTransitionAnalyzer<ADTState<I, O>, I> pta,
                                                                  ADTNode<ADTState<I, O>, I, O> temporarySplitter);

}
