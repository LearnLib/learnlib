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
package de.learnlib.algorithms.ttt.vpda;

import java.util.Collections;
import java.util.Set;

import net.automatalib.automata.vpda.State;

/**
 * @param <L>
 *         location type
 *
 * @author Malte Isberner
 */
final class NonDetState<L> {

    private final NondetStackContents stack;
    private final Set<L> locations;

    NonDetState(Set<L> locations, NondetStackContents stack) {
        this.locations = locations;
        this.stack = stack;
    }

    public static <L> NonDetState<L> fromDet(State<L> state) {
        return new NonDetState<>(Collections.singleton(state.getLocation()),
                                 NondetStackContents.fromDet(state.getStackContents()));
    }

    public State<L> determinize() {
        assert !isNonDet();
        return new State<>(locations.iterator().next(), NondetStackContents.toDet(stack));
    }

    public boolean isNonDet() {
        return locations.size() > 1 || (stack != null && stack.isTrueNondet());
    }

    public NondetStackContents getStack() {
        return stack;
    }

    public Set<L> getLocations() {
        return locations;
    }

}
