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
package de.learnlib.oracle.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
 * A chain of property oracles. Useful when combining multiple model checking strategies to disprove a property, or when
 * finding counter examples to hypotheses.
 * <p>
 * For example you may want to construct a chain that first uses a model checker for monitors, and next, one that uses
 * a model checker for full LTL. This strategy tends to give shorter counter examples for properties, and these counter
 * examples can be found more quickly (as in smaller hypothesis size and less learning queries).
 *
 * @param <I> the input type.
 * @param <A> the automaton type.
 * @param <P> the property type.
 * @param <D> the output type.
 *
 * @author Jeroen Meijer
 */
@GenerateRefinement(name = "DFAPropertyOracleChain",
                    generics = {"I", "P"},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("P"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = DFAPropertyOracle.class,
                                            withGenerics = {"I", "P"}),
                    interfaces = @Interface(clazz = DFAPropertyOracle.class, generics = {"I", "P"}))
@GenerateRefinement(name = "MealyPropertyOracleChain",
                    generics = {"I", "O", "P"},
                    parentGenerics = {@Generic("I"),
                                      @Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("P"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = PropertyOracle.class,
                                            to = MealyPropertyOracle.class,
                                            withGenerics = {"I", "O", "P"}),
                    interfaces = @Interface(clazz = MealyPropertyOracle.class, generics = {"I", "O", "P"}))
public class PropertyOracleChain<I, A extends Output<I, D>, @Nullable P, D> implements PropertyOracle<I, A, P, D> {

    private P property;

    private @Nullable DefaultQuery<I, D> counterExample;

    private final List<PropertyOracle<I, ? super A, P, D>> oracles;

    @SafeVarargs
    public PropertyOracleChain(PropertyOracle<I, ? super A, P, D>... oracles) {
        this(Arrays.asList(oracles));
    }

    public PropertyOracleChain(Collection<? extends PropertyOracle<I, ? super A, P, D>> oracles) {
        this.oracles = new ArrayList<>(oracles);
        if (!this.oracles.isEmpty()) {
            property = this.oracles.get(0).getProperty();
        } else {
            property = null;
        }
    }

    public void addOracle(PropertyOracle<I, ? super A, P, D> oracle) {
        assert oracle.getProperty() == null || oracle.getProperty().equals(property);
        oracle.setProperty(property);
        oracles.add(oracle);
    }

    @Override
    public @Nullable DefaultQuery<I, D> doFindCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, ? super A, P, D> oracle : oracles) {
            DefaultQuery<I, D> ceQry = oracle.findCounterExample(hypothesis, inputs);
            if (ceQry != null) {
                return ceQry;
            }
        }

        return null;
    }

    @Override
    public @Nullable DefaultQuery<I, D> disprove(A hypothesis, Collection<? extends I> inputs) {
        for (PropertyOracle<I, ? super A, P, D> oracle : oracles) {
            DefaultQuery<I, D> ceQry = oracle.disprove(hypothesis, inputs);
            if (ceQry != null) {
                counterExample = ceQry;
                return ceQry;
            }
        }

        return null;
    }

    @Override
    public void setProperty(P property) {
        oracles.forEach(o -> o.setProperty(property));
        this.property = property;
    }

    @Override
    public P getProperty() {
        return property;
    }

    @Override
    public @Nullable DefaultQuery<I, D> getCounterExample() {
        return counterExample;
    }
}
