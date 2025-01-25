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

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

@GenerateRefinement(name = "DFARandomWordsEQOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyRandomWordsEQOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreRandomWordsEQOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class RandomWordsEQOracle<A extends Output<I, D>, I, D> extends AbstractTestWordEQOracle<A, I, D> {

    private final Random random;
    private final int maxTests;
    private final int minLength;
    private final int maxLength;

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle, int minLength, int maxLength, int maxTests) {
        this(mqOracle, minLength, maxLength, maxTests, new Random());
    }

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle,
                               int minLength,
                               int maxLength,
                               int maxTests,
                               Random random) {
        this(mqOracle, minLength, maxLength, maxTests, random, 1);
    }

    public RandomWordsEQOracle(MembershipOracle<I, D> mqOracle,
                               int minLength,
                               int maxLength,
                               int maxTests,
                               Random random,
                               int batchSize) {
        super(mqOracle, batchSize);
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.random = random;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {

        final List<? extends I> symbolList = CollectionUtil.randomAccessList(inputs);

        return Stream.generate(() -> generateTestWord(symbolList, symbolList.size())).limit(maxTests);
    }

    private Word<I> generateTestWord(List<? extends I> symbolList, int numSyms) {

        final int length = minLength + random.nextInt(maxLength - minLength + 1);
        final WordBuilder<I> result = new WordBuilder<>(length);

        for (int j = 0; j < length; ++j) {
            int symidx = random.nextInt(numSyms);
            I sym = symbolList.get(symidx);
            result.append(sym);
        }

        return result.toWord();
    }
}
