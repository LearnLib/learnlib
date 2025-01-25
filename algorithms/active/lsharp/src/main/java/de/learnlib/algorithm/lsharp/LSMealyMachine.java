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
package de.learnlib.algorithm.lsharp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LSMealyMachine<I, O> implements InputAlphabetHolder<I>, MealyMachine<LSState, I, Pair<LSState, I>, O> {

    private final Set<LSState> states;
    private final LSState initialState;
    private final Alphabet<I> inputAlphabet;
    private final Map<Pair<LSState, I>, Pair<LSState, O>> transFunction;

    public LSMealyMachine(Alphabet<I> inputAlphabet,
                          Collection<LSState> states,
                          LSState initialState,
                          Map<Pair<LSState, I>, Pair<LSState, O>> transFunction) {
        this.states = new HashSet<>(states);
        this.initialState = initialState;
        this.inputAlphabet = inputAlphabet;
        this.transFunction = transFunction;
    }

    public <S extends Comparable<S>> LSMealyMachine(Alphabet<I> inputAlphabet, MealyMachine<S, I, ?, O> mealy) {
        this.inputAlphabet = inputAlphabet;
        this.transFunction = new HashMap<>();

        Map<S, LSState> toState = new HashMap<>();
        toState.put(mealy.getInitialState(), new LSState(0));
        mealy.getStates().forEach(s -> toState.computeIfAbsent(s, k -> new LSState(toState.size())));
        states = new HashSet<>(toState.values());

        for (S s : mealy.getStates()) {
            for (I i : inputAlphabet) {
                LSState p = toState.get(s);
                O o = mealy.getOutput(s, i);
                LSState q = toState.get(mealy.getSuccessor(s, i));
                transFunction.put(Pair.of(p, i), Pair.of(q, o));
            }
        }

        this.initialState = toState.get(mealy.getInitialState());
    }

    @Override
    public Collection<Pair<LSState, I>> getTransitions(LSState state, I input) {
        Pair<LSState, I> trans = getTransition(state, input);
        return trans == null ? Collections.emptySet() : Collections.singleton(trans);
    }

    @Override
    public Collection<LSState> getStates() {
        return states;
    }

    @Override
    public @Nullable LSState getInitialState() {
        return initialState;
    }

    @Override
    public @Nullable LSState getSuccessor(LSState state, I input) {
        return this.getSuccessor(Pair.of(state, input));
    }

    @Override
    public LSState getSuccessor(Pair<LSState, I> transition) {
        Pair<LSState, O> pair = transFunction.getOrDefault(transition, null);
        if (pair == null) {
            return null;
        }

        return pair.getFirst();
    }

    @Override
    public @Nullable Pair<LSState, I> getTransition(LSState state, I input) {
        return Pair.of(state, input);
    }

    @Override
    public Void getStateProperty(LSState state) {
        return null;
    }

    @Override
    public O getTransitionProperty(Pair<LSState, I> transition) {
        Pair<LSState, O> pair = this.transFunction.getOrDefault(transition, null);
        if (pair == null) {
            return null;
        }

        return pair.getSecond();
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return this.inputAlphabet;
    }

    @Override
    public O getTransitionOutput(Pair<LSState, I> transition) {
        if (transFunction.containsKey(transition)) {
            return transFunction.get(transition).getSecond();
        }

        return null;
    }

}
