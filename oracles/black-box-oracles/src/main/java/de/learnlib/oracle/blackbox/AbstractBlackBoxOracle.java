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
package de.learnlib.oracle.blackbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;

/**
 * A {@link BlackBoxOracle} that contains a set of {@link BlackBoxProperty}s.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 * @param <P> the BlackBoxProperty type
 */
public abstract class AbstractBlackBoxOracle<A, I, D, P extends BlackBoxProperty<?, A, I, D>>
        implements BlackBoxOracle<A, I, D, P> {

    /**
     * The set of properties that need to be verified.
     */
    private final Set<P> properties;

    /**
     * Constructs a new {@link AbstractBlackBoxOracle} with a set of properties.
     *
     * @param properties the set of {@link BlackBoxProperty}s that need to be verified.
     */
    protected AbstractBlackBoxOracle(Set<P> properties) {
        this.properties = new HashSet<>();
        this.properties.addAll(properties);
        for (P p : this.properties) {
            p.useCache();
        }
    }

    /**
     * Constructs a new {@link AbstractBlackBoxOracle} with a single property.
     *
     * @param property the {@link BlackBoxProperty}.
     */
    protected AbstractBlackBoxOracle(P property) {
        this(Collections.singleton(property));
    }

    /**
     * Constructs a new {@link AbstractBlackBoxOracle} with no properties.
     */
    protected AbstractBlackBoxOracle() {
        this(Collections.emptySet());
    }

    @Override
    public Set<P> getProperties() {
        return properties;
    }
}
