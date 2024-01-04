/* Copyright (C) 2013-2024 TU Dortmund University
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

import de.learnlib.oracle.BlackBoxOracle;
import de.learnlib.oracle.BlackBoxOracle.DFABlackBoxOracle;
import de.learnlib.oracle.BlackBoxOracle.MealyBlackBoxOracle;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.PropertyOracle;
import de.learnlib.oracle.PropertyOracle.DFAPropertyOracle;
import de.learnlib.oracle.PropertyOracle.MealyPropertyOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Map;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The strategy of this black-box oracle is to first try out a property, to see if it can be disproved. If it can not be
 * disproved it tries the same property to find a counter example to the hypothesis, before continuing with the next
 * property.
 * <p>
 * This implementation may be used when refining a hypothesis is inexpensive compared to disproving propertyOracles.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @see DisproveFirstOracle
 */
@GenerateRefinement(name = "DFACExFirstOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = DFAPropertyOracle.class,
                                            withGenerics = {"I", "?"}),
                    interfaces = {@Interface(clazz = DFABlackBoxOracle.class, generics = "I"),
                                  @Interface(clazz = DFAEquivalenceOracle.class, generics = "I")})
@GenerateRefinement(name = "MealyCExFirstOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = MealyPropertyOracle.class,
                                            withGenerics = {"I", "O", "?"}),
                    interfaces = {@Interface(clazz = MealyBlackBoxOracle.class, generics = {"I", "O"}),
                                  @Interface(clazz = MealyEquivalenceOracle.class, generics = {"I", "O"})})
public class CExFirstOracle<A extends Output<I, D>, I, D> implements BlackBoxOracle<A, I, D> {

    private final List<PropertyOracle<I, ? super A, ?, D>> propertyOracles;

    public CExFirstOracle() {
        this(Collections.emptySet());
    }

    public CExFirstOracle(PropertyOracle<I, ? super A, ?, D> propertyOracle) {
        this(Collections.singleton(propertyOracle));
    }

    public CExFirstOracle(Collection<? extends PropertyOracle<I, ? super A, ?, D>> propertyOracles) {
        this.propertyOracles = new ArrayList<>(propertyOracles);
    }

    @Override
    public List<PropertyOracle<I, ? super A, ?, D>> getPropertyOracles() {
        return propertyOracles;
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, ? super A, ?, D> propertyOracle : propertyOracles) {
            final DefaultQuery<I, D> result = propertyOracle.findCounterExample(hypothesis, inputs);
            if (result != null) {
                assert isCounterExample(hypothesis, result.getInput(), result.getOutput());
                return result;
            }
        }

        return null;
    }
}
