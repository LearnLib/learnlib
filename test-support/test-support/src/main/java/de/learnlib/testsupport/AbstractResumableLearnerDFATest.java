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
package de.learnlib.testsupport;

import java.util.Random;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.util.automaton.random.RandomAutomata;

public abstract class AbstractResumableLearnerDFATest<L extends Resumable<T> & LearningAlgorithm<DFA<?, Character>, Character, Boolean>, T>
        extends AbstractResumableLearnerTest<L, DFA<?, Character>, DFAMembershipOracle<Character>, Character, Boolean, T> {

    private static final int AUTOMATON_SIZE = 50;

    @Override
    protected Alphabet<Character> getInitialAlphabet() {
        return Alphabets.characters('1', '6');
    }

    @Override
    protected DFA<?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomDFA(new Random(RANDOM_SEED), AUTOMATON_SIZE, alphabet);
    }

    @Override
    protected DFAMembershipOracle<Character> getOracle(DFA<?, Character> target) {
        return new DFASimulatorOracle<>(target);
    }
}
