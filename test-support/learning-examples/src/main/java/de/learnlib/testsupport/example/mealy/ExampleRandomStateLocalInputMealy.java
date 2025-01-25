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
package de.learnlib.testsupport.example.mealy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.learnlib.testsupport.example.LearningExample.StateLocalInputMealyLearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.StateLocalInputMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ExampleRandomStateLocalInputMealy<I, O> implements StateLocalInputMealyLearningExample<I, O> {

    private final Alphabet<I> alphabet;
    private final StateLocalInputMealyMachine<?, I, ?, O> referenceAutomaton;
    private final O undefinedOutput;

    @SafeVarargs
    public ExampleRandomStateLocalInputMealy(Random random,
                                             Alphabet<I> alphabet,
                                             int size,
                                             O undefinedOutput,
                                             O... outputs) {
        this(random, alphabet, size, undefinedOutput, validateOutputs(undefinedOutput, outputs));
    }

    private ExampleRandomStateLocalInputMealy(Random random,
                                              Alphabet<I> alphabet,
                                              int size,
                                              O undefinedOutput,
                                              List<O> outputs) {
        this.alphabet = alphabet;
        this.undefinedOutput = undefinedOutput;
        CompactMealy<I, O> source = RandomAutomata.randomMealy(random, size, alphabet, outputs);

        final int alphabetSize = alphabet.size();

        final Collection<Integer> oldStates = source.getStates();
        final Integer sink = source.addState();

        for (Integer s : oldStates) {
            // randomly remove (redirect to sink) transitions
            for (int idx : RandomUtil.distinctIntegers(random, random.nextInt(alphabetSize), alphabetSize)) {
                source.setTransition(s, idx, sink, undefinedOutput);
            }
        }
        // configure sink
        for (I i : alphabet) {
            source.addTransition(sink, i, sink, undefinedOutput);
        }

        final CompactMealy<I, O> minimized = HopcroftMinimizer.minimizeMealyInvasive(source, alphabet);
        final MutableMapping<Integer, Collection<I>> enabledInputs = source.createStaticStateMapping();

        for (Integer s : minimized) {
            final Collection<I> stateInputs = new HashSet<>(alphabet);

            for (I i : alphabet) {
                if (Objects.equals(undefinedOutput, minimized.getOutput(s, i))) {
                    stateInputs.remove(i);
                }
            }

            enabledInputs.put(s, stateInputs);
        }

        this.referenceAutomaton = new MockedSLIMealy<>(minimized, enabledInputs);
    }

    @SafeVarargs
    public static <I, O> ExampleRandomStateLocalInputMealy<I, O> createExample(Random random,
                                                                               Alphabet<I> alphabet,
                                                                               int size,
                                                                               O undefinedOutput,
                                                                               O... outputs) {
        return new ExampleRandomStateLocalInputMealy<>(random, alphabet, size, undefinedOutput, outputs);
    }

    @Override
    public StateLocalInputMealyMachine<?, I, ?, O> getReferenceAutomaton() {
        return referenceAutomaton;
    }

    @Override
    public Alphabet<I> getAlphabet() {
        return alphabet;
    }

    @Override
    public O getUndefinedOutput() {
        return this.undefinedOutput;
    }

    @SafeVarargs
    private static <O> List<O> validateOutputs(O undefinedOutput, O... outputs) {
        final List<O> result = Arrays.asList(outputs);

        if (result.contains(undefinedOutput)) {
            throw new IllegalArgumentException("The special undefined input should not be contained in regular outputs");
        }

        return result;
    }

    private static class MockedSLIMealy<S, I, T, O> implements StateLocalInputMealyMachine<S, I, T, O> {

        private final MealyMachine<S, I, T, O> delegate;
        private final Mapping<S, Collection<I>> localInputs;

        MockedSLIMealy(MealyMachine<S, I, T, O> delegate, Mapping<S, Collection<I>> localInputs) {
            this.delegate = delegate;
            this.localInputs = localInputs;
        }

        @Override
        public Collection<I> getLocalInputs(S state) {
            final Collection<I> collection = this.localInputs.get(state);
            return collection == null ? Collections.emptyList() : collection;
        }

        @Override
        public Collection<S> getStates() {
            return this.delegate.getStates();
        }

        @Override
        public O getTransitionOutput(T transition) {
            return this.delegate.getTransitionOutput(transition);
        }

        @Override
        public @Nullable T getTransition(S state, I input) {
            return this.delegate.getTransition(state, input);
        }

        @Override
        public S getSuccessor(T transition) {
            return this.delegate.getSuccessor(transition);
        }

        @Override
        public @Nullable S getInitialState() {
            return this.delegate.getInitialState();
        }
    }
}
