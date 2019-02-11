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
package de.learnlib.filter.cache.mealy;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.learnlib.api.Resumable;
import de.learnlib.api.oracle.StateLocalInputOracle;
import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.filter.cache.mealy.StateLocalInputMealyCacheOracle.StateLocalInputMealyCacheOracleState;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.tree.dynamic.StateLocalInputIncrementalMealyTreeBuilder;
import net.automatalib.words.Word;

public class StateLocalInputMealyCacheOracle<I, O> extends InternalMealyCacheOracle<I, OutputAndLocalInputs<I, O>>
        implements StateLocalInputMealyOracle<I, OutputAndLocalInputs<I, O>>,
                   Resumable<StateLocalInputMealyCacheOracleState<I, O>> {

    private final StateLocalInputOracle<I, Word<OutputAndLocalInputs<I, O>>> oracle;
    private Map<Word<? extends I>, Set<I>> definedInputsCache;

    StateLocalInputMealyCacheOracle(IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> incrementalBuilder,
                                    Mapping<? super OutputAndLocalInputs<I, O>, ? extends OutputAndLocalInputs<I, O>> errorSyms,
                                    StateLocalInputOracle<I, Word<OutputAndLocalInputs<I, O>>> delegate) {
        super(incrementalBuilder, errorSyms, delegate);
        this.oracle = delegate;
        this.definedInputsCache = new HashMap<>();
    }

    public static <I, O> StateLocalInputMealyCacheOracle<I, O> createStateLocalInputTreeCacheOracle(Collection<I> initialLocalInputs,
                                                                                                    StateLocalInputOracle<I, Word<OutputAndLocalInputs<I, O>>> delegate) {
        return createStateLocalInputTreeCacheOracle(initialLocalInputs, null, delegate);
    }

    public static <I, O> StateLocalInputMealyCacheOracle<I, O> createStateLocalInputTreeCacheOracle(Collection<I> initialLocalInputs,
                                                                                                    Mapping<? super OutputAndLocalInputs<I, O>, ? extends OutputAndLocalInputs<I, O>> errorSyms,
                                                                                                    StateLocalInputOracle<I, Word<OutputAndLocalInputs<I, O>>> delegate) {
        StateLocalInputIncrementalMealyTreeBuilder<I, O> incrementalBuilder =
                new StateLocalInputIncrementalMealyTreeBuilder<>(initialLocalInputs);
        return new StateLocalInputMealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
    }

    public StateLocalInputCacheConsistencyTest<I, O> createStateLocalInputCacheConsistencyTest() {
        return new StateLocalInputCacheConsistencyTest<>(incMealy, incMealyLock);
    }

    @Override
    public Set<I> definedInputs(Word<? extends I> input) {
        if (!definedInputsCache.containsKey(input)) {
            definedInputsCache.put(input, oracle.definedInputs(input));
        }

        return definedInputsCache.get(input);
    }

    @Override
    public StateLocalInputMealyCacheOracleState<I, O> suspend() {
        return new StateLocalInputMealyCacheOracleState<>(incMealy, definedInputsCache);
    }

    @Override
    public void resume(StateLocalInputMealyCacheOracleState<I, O> state) {
        this.incMealy = state.getBuilder();
        this.definedInputsCache = state.getEnabledInputs();
    }

    public static class StateLocalInputMealyCacheOracleState<I, O> implements Serializable {

        private final IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> builder;
        private final Map<Word<? extends I>, Set<I>> enabledInputs;

        StateLocalInputMealyCacheOracleState(IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> builder,
                                                    Map<Word<? extends I>, Set<I>> enabledInputs) {
            this.builder = builder;
            this.enabledInputs = enabledInputs;
        }

        IncrementalMealyBuilder<I, OutputAndLocalInputs<I, O>> getBuilder() {
            return builder;
        }

        Map<Word<? extends I>, Set<I>> getEnabledInputs() {
            return enabledInputs;
        }
    }
}
