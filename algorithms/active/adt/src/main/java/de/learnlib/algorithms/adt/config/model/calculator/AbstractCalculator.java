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
package de.learnlib.algorithms.adt.config.model.calculator;

import java.util.Optional;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.config.model.ADSCalculator;
import de.learnlib.algorithms.adt.util.ADTUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
@ParametersAreNonnullByDefault
public abstract class AbstractCalculator implements ADSCalculator {

    @Override
    public <S, I, O> Optional<ADTNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> hypothesis,
                                                        final Alphabet<I> alphabet,
                                                        final Set<S> targets) {

        final Optional<ADSNode<S, I, O>> result = computeInternal(hypothesis, alphabet, targets);

        return result.map(ADTUtil::buildFromADS);
    }

    protected abstract <S, I, O> Optional<ADSNode<S, I, O>> computeInternal(MealyMachine<S, I, ?, O> hypothesis,
                                                                            Alphabet<I> alphabet,
                                                                            Set<S> targets);

}
