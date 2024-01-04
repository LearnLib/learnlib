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

import java.util.Collection;

import de.learnlib.oracle.AutomatonOracle.DFAOracle;
import de.learnlib.oracle.AutomatonOracle.MealyOracle;
import de.learnlib.oracle.InclusionOracle;
import de.learnlib.oracle.InclusionOracle.DFAInclusionOracle;
import de.learnlib.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.automaton.concept.DetOutputAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link InclusionOracle} that generates words in a breadth-first manner.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @see InclusionOracle
 * @see AbstractBFOracle
 */
@GenerateRefinement(name = "DFABFInclusionOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMapping = @Mapping(from = MembershipOracle.class,
                                           to = DFAMembershipOracle.class,
                                           generics = @Generic("I")),
                    interfaces = {@Interface(clazz = DFAInclusionOracle.class, generics = @Generic("I")),
                                  @Interface(clazz = DFAOracle.class, generics = @Generic("I"))},
                    classDoc = "A {@link DFA}-specific refinement of {@link AbstractBFInclusionOracle}.\n" +
                               "@param <I> input symbol type\n")
@GenerateRefinement(name = "MealyBFInclusionOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMapping = @Mapping(from = MembershipOracle.class,
                                           to = MealyMembershipOracle.class,
                                           generics = {@Generic("I"), @Generic("O")}),
                    interfaces = {@Interface(clazz = MealyInclusionOracle.class,
                                             generics = {@Generic("I"), @Generic("O")}),
                                  @Interface(clazz = MealyOracle.class, generics = {@Generic("I"), @Generic("O")})},
                    classDoc = "A {@link MealyMachine}-specific refinement of {@link AbstractBFInclusionOracle}.\n" +
                               "@param <I> input symbol type\n" +
                               "@param <O> output symbol type\n")
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
