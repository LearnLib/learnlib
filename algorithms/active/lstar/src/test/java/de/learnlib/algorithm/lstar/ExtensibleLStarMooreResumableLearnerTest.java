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
package de.learnlib.algorithm.lstar;

import java.util.Random;

import de.learnlib.algorithm.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.algorithm.lstar.moore.ExtensibleLStarMooreBuilder;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerMooreTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

public class ExtensibleLStarMooreResumableLearnerTest
        extends AbstractResumableLearnerMooreTest<ExtensibleLStarMoore<Character, Character>, AutomatonLStarState<Character, Word<Character>, CompactMoore<Character, Character>, Integer>> {

    @Override
    protected ExtensibleLStarMoore<Character, Character> getLearner(MooreMembershipOracle<Character, Character> oracle,
                                                                    Alphabet<Character> alphabet) {
        return new ExtensibleLStarMooreBuilder<Character, Character>().withAlphabet(alphabet)
                                                                      .withOracle(oracle)
                                                                      .create();
    }

    @Override
    protected int getRounds() {
        return 2;
    }

    @Override
    protected MooreMachine<?, Character, ?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomMoore(new Random(42), 100, alphabet, Alphabets.characters('a', 'd'));
    }
}
