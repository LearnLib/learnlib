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
package de.learnlib.testsupport;

import java.util.Random;

import de.learnlib.api.Resumable;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.QueryAnswerer;
import de.learnlib.oracle.equivalence.MooreSimulatorEQOracle;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public abstract class AbstractResumableLearnerMooreTest<L extends Resumable<T> & LearningAlgorithm<MooreMachine<?, Character, ?, Character>, Character, Word<Character>>, T>
        extends AbstractResumableLearnerTest<L, MooreMachine<?, Character, ?, Character>, MembershipOracle<Character, Word<Character>>, Character, Word<Character>, T> {

    private static final int AUTOMATON_SIZE = 20;

    @Override
    protected Alphabet<Character> getInitialAlphabet() {
        return Alphabets.characters('1', '4');
    }

    @Override
    protected MooreMachine<?, Character, ?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomMoore(new Random(RANDOM_SEED),
                                          AUTOMATON_SIZE,
                                          alphabet,
                                          Alphabets.characters('a', 'd'));
    }

    @Override
    protected MembershipOracle<Character, Word<Character>> getOracle(MooreMachine<?, Character, ?, Character> target) {
        return ((QueryAnswerer<Character, Word<Character>>) target::computeSuffixOutput).asOracle();
    }

    @Override
    protected MooreEquivalenceOracle<Character, Character> getEquivalenceOracle(MooreMachine<?, Character, ?, Character> target) {
        return new MooreSimulatorEQOracle<>(target);
    }
}
