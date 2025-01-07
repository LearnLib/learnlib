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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.oracle.BlackBoxOracle;
import de.learnlib.oracle.BlackBoxOracle.DFABlackBoxOracle;
import de.learnlib.oracle.BlackBoxOracle.MealyBlackBoxOracle;
import de.learnlib.oracle.PropertyOracle;
import de.learnlib.oracle.PropertyOracle.DFAPropertyOracle;
import de.learnlib.oracle.PropertyOracle.MealyPropertyOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The strategy of this black-box oracle is to first try to disprove all properties before finding a counter example to
 * the given hypothesis.
 * <p>
 * One may favor this implementation if refining a hypothesis is expensive compared to trying to disprove properties.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @see CExFirstOracle
 */
@GenerateRefinement(name = "DFADisproveFirstOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = PropertyOracle.class,
                                            to = DFAPropertyOracle.class,
                                            generics = {@Generic("I"), @Generic("?")}),
                    interfaces = @Interface(clazz = DFABlackBoxOracle.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyDisproveFirstOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = PropertyOracle.class,
                                            to = MealyPropertyOracle.class,
                                            generics = {@Generic("I"), @Generic("O"), @Generic("?")}),
                    interfaces = @Interface(clazz = MealyBlackBoxOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class DisproveFirstOracle<A extends Output<I, D>, I, D> implements BlackBoxOracle<A, I, D> {

    private final List<PropertyOracle<I, ? super A, ?, D>> propertyOracles;

    public DisproveFirstOracle() {
        this(Collections.emptyList());
    }

    public DisproveFirstOracle(PropertyOracle<I, ? super A, ?, D> propertyOracle) {
        this(Collections.singleton(propertyOracle));
    }

    public DisproveFirstOracle(Collection<? extends PropertyOracle<I, ? super A, ?, D>> propertyOracles) {
        this.propertyOracles = new ArrayList<>(propertyOracles);
    }

    @Override
    public List<PropertyOracle<I, ? super A, ?, D>> getPropertyOracles() {
        return propertyOracles;
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, ? super A, ?, D> po : propertyOracles) {
            if (!po.isDisproved()) {
                po.disprove(hypothesis, inputs);
            }
        }

        for (PropertyOracle<I, ? super A, ?, D> po : propertyOracles) {
            if (!po.isDisproved()) {
                final DefaultQuery<I, D> ce = po.doFindCounterExample(hypothesis, inputs);
                if (ce != null) {
                    assert isCounterExample(hypothesis, ce.getInput(), ce.getOutput());
                    return ce;
                }
            }
        }

        return null;
    }
}
