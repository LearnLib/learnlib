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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

@GenerateRefinement(name = "DFAEQOracleChain",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = EquivalenceOracle.class,
                                            to = DFAEquivalenceOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyEQOracleChain",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = EquivalenceOracle.class,
                                            to = MealyEquivalenceOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class, generics = {"I", "O"}))
public class EQOracleChain<A, I, D> implements EquivalenceOracle<A, I, D> {

    private final List<EquivalenceOracle<? super A, I, D>> oracles;

    @SafeVarargs
    public EQOracleChain(EquivalenceOracle<? super A, I, D>... oracles) {
        this(Arrays.asList(oracles));
    }

    public EQOracleChain(List<? extends EquivalenceOracle<? super A, I, D>> oracles) {
        this.oracles = new ArrayList<>(oracles);
    }

    public void addOracle(EquivalenceOracle<? super A, I, D> oracle) {
        oracles.add(oracle);
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        for (EquivalenceOracle<? super A, I, D> eqOracle : oracles) {
            DefaultQuery<I, D> ceQry = eqOracle.findCounterExample(hypothesis, inputs);
            if (ceQry != null) {
                return ceQry;
            }
        }
        return null;
    }

}
