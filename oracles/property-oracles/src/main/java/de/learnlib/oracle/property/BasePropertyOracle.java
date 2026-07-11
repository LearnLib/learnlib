/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.oracle.property;

import java.util.Collection;

import de.learnlib.oracle.EmptinessOracle;
import de.learnlib.oracle.EmptinessOracle.DFAEmptinessOracle;
import de.learnlib.oracle.EmptinessOracle.MealyEmptinessOracle;
import de.learnlib.oracle.InclusionOracle;
import de.learnlib.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.oracle.LassoEmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.oracle.LassoEmptinessOracle.MealyLassoEmptinessOracle;
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
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.modelchecking.ModelChecker.DFAModelChecker;
import net.automatalib.modelchecking.ModelChecker.MealyModelChecker;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link PropertyOracle} that uses {@link InclusionOracle}s and {@link EmptinessOracle}s to evaluate potential
 * counterexamples produced by a given {@link ModelChecker}.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <D>
 *         the output type
 * @param <R>
 *         the result type of model checker
 */
@GenerateRefinement(name = "DFAFinitePropertyOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "P", desc = "property type")},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("P"),
                                      @Generic(clazz = Boolean.class),
                                      @Generic(clazz = DFA.class, generics = {"?", "I"})},
                    typeMappings = {@Mapping(from = InclusionOracle.class,
                                             to = DFAInclusionOracle.class,
                                             generics = @Generic("I")),
                                    @Mapping(from = EmptinessOracle.class,
                                             to = DFAEmptinessOracle.class,
                                             generics = @Generic("I")),
                                    @Mapping(from = ModelChecker.class,
                                             to = DFAModelChecker.class,
                                             generics = {@Generic("I"),
                                                         @Generic("P"),
                                                         @Generic(clazz = DFA.class, generics = {"?", "I"})})},
                    interfaces = @Interface(clazz = DFAPropertyOracle.class, generics = {@Generic("I"), @Generic("P")}))
@GenerateRefinement(name = "DFALassoPropertyOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "P", desc = "property type")},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("P"),
                                      @Generic(clazz = Boolean.class),
                                      @Generic(clazz = DFALasso.class, generics = "I")},
                    typeMappings = {@Mapping(from = InclusionOracle.class,
                                             to = DFAInclusionOracle.class,
                                             generics = @Generic("I")),
                                    @Mapping(from = EmptinessOracle.class,
                                             to = DFALassoEmptinessOracle.class,
                                             generics = @Generic("I")),
                                    @Mapping(from = ModelChecker.class,
                                             to = DFAModelCheckerLasso.class,
                                             generics = {@Generic("I"), @Generic("P")})},
                    interfaces = @Interface(clazz = DFAPropertyOracle.class, generics = {@Generic("I"), @Generic("P")}))
@GenerateRefinement(name = "MealyFinitePropertyOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type"),
                                @Generic(value = "P", desc = "property type")},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("P"),
                                      @Generic(clazz = Word.class, generics = "O"),
                                      @Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"})},
                    typeMappings = {@Mapping(from = InclusionOracle.class,
                                             to = MealyInclusionOracle.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = EmptinessOracle.class,
                                             to = MealyEmptinessOracle.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = ModelChecker.class,
                                             to = MealyModelChecker.class,
                                             generics = {@Generic("I"),
                                                         @Generic("O"),
                                                         @Generic("P"),
                                                         @Generic(clazz = MealyMachine.class,
                                                                  generics = {"?", "I", "?", "O"})})},
                    interfaces = @Interface(clazz = MealyPropertyOracle.class,
                                            generics = {@Generic("I"), @Generic("O"), @Generic("P")}))
@GenerateRefinement(name = "MealyLassoPropertyOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type"),
                                @Generic(value = "P", desc = "property type")},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("P"),
                                      @Generic(clazz = Word.class, generics = "O"),
                                      @Generic(clazz = MealyLasso.class, generics = {"I", "O"})},
                    typeMappings = {@Mapping(from = InclusionOracle.class,
                                             to = MealyInclusionOracle.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = EmptinessOracle.class,
                                             to = MealyLassoEmptinessOracle.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                    @Mapping(from = ModelChecker.class,
                                             to = MealyModelCheckerLasso.class,
                                             generics = {@Generic("I"), @Generic("O"), @Generic("P")})},
                    interfaces = @Interface(clazz = MealyPropertyOracle.class,
                                            generics = {@Generic("I"), @Generic("O"), @Generic("P")}))
public class BasePropertyOracle<I, A extends Output<I, D>, P, D, R extends A> implements PropertyOracle<I, A, P, D> {

    private final P property;
    private final InclusionOracle<A, I, D> inclusionOracle;
    private final EmptinessOracle<R, I, D> emptinessOracle;
    private final ModelChecker<I, A, P, R> modelChecker;
    private @Nullable DefaultQuery<I, D> counterExample;

    public BasePropertyOracle(P property,
                              InclusionOracle<A, I, D> inclusionOracle,
                              EmptinessOracle<R, I, D> emptinessOracle,
                              ModelChecker<I, A, P, R> modelChecker) {
        this.property = property;
        this.inclusionOracle = inclusionOracle;
        this.emptinessOracle = emptinessOracle;
        this.modelChecker = modelChecker;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Override
    public @Nullable DefaultQuery<I, D> getCounterExample() {
        return counterExample;
    }

    @Override
    public @Nullable DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final A result = modelCheck(hypothesis, inputs);
        return result != null ? inclusionOracle.findCounterExample(result, inputs) : null;
    }

    @Override
    public @Nullable DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) {
        final R ce = modelCheck(hypothesis, inputs);

        if (ce == null) {
            return null;
        } else {
            counterExample = emptinessOracle.findCounterExample(ce, inputs);
            return counterExample;
        }
    }

    private @Nullable R modelCheck(A hypothesis, Collection<? extends I> inputs) {
        return modelChecker.findCounterExample(hypothesis, inputs, getProperty());
    }
}
