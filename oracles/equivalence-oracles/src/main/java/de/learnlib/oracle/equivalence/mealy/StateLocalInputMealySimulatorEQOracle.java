/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.oracle.equivalence.mealy;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StateLocalInputMealySimulatorEQOracle<I, O>
        implements EquivalenceOracle<StateLocalInputMealyMachine<?, I, ?, O>, I, Word<OutputAndLocalInputs<I, O>>> {

    private final MealySimulatorEQOracle<I, O> delegate;
    private final SuffixOutput<I, Word<OutputAndLocalInputs<I, O>>> mealyAsSLIMealy;

    public <S> StateLocalInputMealySimulatorEQOracle(StateLocalInputMealyMachine<S, I, ?, O> mealy) {
        this.delegate = new MealySimulatorEQOracle<>(mealy);

        // we can use 'null' as a sink, because we will never traverse this state
        this.mealyAsSLIMealy = StateLocalInputMealyUtil.partialToObservableOutput(mealy, null);
    }

    @Override
    public @Nullable DefaultQuery<I, Word<OutputAndLocalInputs<I, O>>> findCounterExample(StateLocalInputMealyMachine<?, I, ?, O> hypothesis,
                                                                                          Collection<? extends I> inputs) {
        final DefaultQuery<I, Word<O>> cex = delegate.findCounterExample(hypothesis, inputs);

        if (cex != null) {
            final Word<I> prefix = cex.getPrefix();
            final Word<I> suffix = cex.getSuffix();

            return new DefaultQuery<>(prefix, suffix, this.mealyAsSLIMealy.computeSuffixOutput(prefix, suffix));
        }

        return null;
    }

}
