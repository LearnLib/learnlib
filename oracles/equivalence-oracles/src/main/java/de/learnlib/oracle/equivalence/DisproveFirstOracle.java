/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.oracle.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.BlackBoxOracle.DFABlackBoxOracle;
import de.learnlib.api.oracle.BlackBoxOracle.MealyBlackBoxOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.oracle.PropertyOracle.DFAPropertyOracle;
import de.learnlib.api.oracle.PropertyOracle.MealyPropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The strategy of this black-box oracle is to first try to disprove all properties before finding a counter example to
 * the given hypothesis.
 * <p>
 * One may favor this implementation if refining a hypothesis is expensive compared to trying to disprove properties.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @author Jeroen Meijer
 * @see CExFirstOracle
 */
@GenerateRefinement(name = "DFADisproveFirstOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = DFAPropertyOracle.class,
                                            withGenerics = {"I", "?"}),
                    interfaces = @Interface(clazz = DFABlackBoxOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyDisproveFirstOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = MealyPropertyOracle.class,
                                            withGenerics = {"I", "O", "?"}),
                    interfaces = @Interface(clazz = MealyBlackBoxOracle.class, generics = {"I", "O"}))
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
