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

import java.util.Collection;

import de.learnlib.api.oracle.AutomatonOracle.DFAOracle;
import de.learnlib.api.oracle.AutomatonOracle.MealyOracle;
import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.api.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link InclusionOracle} that generates words in a breadth-first manner.
 *
 * @author Jeroen Meijer
 * @see InclusionOracle
 * @see AbstractBFOracle
 */
@GenerateRefinement(name = "DFABFInclusionOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = {@Interface(clazz = DFAInclusionOracle.class, generics = "I"),
                                  @Interface(clazz = DFAOracle.class, generics = "I")})
@GenerateRefinement(name = "MealyBFInclusionOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = {@Interface(clazz = MealyInclusionOracle.class, generics = {"I", "O"}),
                                  @Interface(clazz = MealyOracle.class, generics = {"I", "O"})})
public abstract class AbstractBFInclusionOracle<A extends DetOutputAutomaton<?, I, ?, D>, I, D>
        extends AbstractBFOracle<A, I, D> implements InclusionOracle<A, I, D> {

    public AbstractBFInclusionOracle(MembershipOracle<I, D> membershipOracle, double multiplier) {
        super(membershipOracle, multiplier);
    }

    @Override
    public boolean isCounterExample(A hypothesis, Iterable<? extends I> inputs, D output) {
        return InclusionOracle.super.isCounterExample(hypothesis, inputs, output);
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        return super.findCounterExample(hypothesis, inputs);
    }
}
