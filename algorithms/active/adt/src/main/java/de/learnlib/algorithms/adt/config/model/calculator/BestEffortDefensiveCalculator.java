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
package de.learnlib.algorithms.adt.config.model.calculator;

import java.util.Optional;
import java.util.Set;

import de.learnlib.algorithms.adt.ads.DefensiveADS;
import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithms.adt.config.model.DefensiveADSCalculator;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public class BestEffortDefensiveCalculator implements DefensiveADSCalculator {

    @Override
    public <S, I, O> Optional<ADTNode<S, I, O>> compute(MealyMachine<S, I, ?, O> automaton,
                                                        Alphabet<I> alphabet,
                                                        Set<S> states,
                                                        PartialTransitionAnalyzer<S, I> partialTransitionAnalyzer) {
        return DefensiveADS.compute(automaton, alphabet, states, partialTransitionAnalyzer);
    }
}
