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
package de.learnlib.algorithm.adt.learner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.testsupport.AbstractLearnerASTTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

public class ADTASTTest
        extends AbstractLearnerASTTest<ADTLearner<Integer, Character>, MealyMachine<?, Integer, ?, Character>, AdaptiveMembershipOracle<Integer, Character>, Integer, Word<Character>> {

    @Override
    protected Alphabet<Integer> getInitialAlphabet() {
        return Alphabets.integers(1, 5);
    }

    @Override
    protected MealyMachine<?, Integer, ?, Character> getSUL(Alphabet<Integer> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 15, alphabet, Alphabets.characters('a', 'f'));
    }

    @Override
    protected AdaptiveMembershipOracle<Integer, Character> getOracle(MealyMachine<?, Integer, ?, Character> target) {
        return new SULAdaptiveOracle<>(new MealySimulatorSUL<>(target));
    }

    @Override
    protected ADTLearner<Integer, Character> getLearner(AdaptiveMembershipOracle<Integer, Character> oracle,
                                                        Alphabet<Integer> alphabet) {
        return new ADTLearnerBuilder<Integer, Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected Collection<Word<Integer>> getTrueRepresentatives() {
        final ADT<ADTState<Integer, Character>, Integer, Character> adt = learner.getADT();
        final Set<ADTNode<ADTState<Integer, Character>, Integer, Character>> leaves =
                ADTUtil.collectLeaves(adt.getRoot());

        final List<Word<Integer>> result = new ArrayList<>(leaves.size());

        for (ADTNode<ADTState<Integer, Character>, Integer, Character> l : leaves) {
            if (l instanceof ADTLeafNode<ADTState<Integer, Character>, Integer, Character> leaf) {
                result.add(leaf.getState().getAccessSequence());
            }
        }

        return result;
    }
}
