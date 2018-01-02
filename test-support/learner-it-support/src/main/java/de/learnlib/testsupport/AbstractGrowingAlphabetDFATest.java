/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.Collection;
import java.util.Random;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public abstract class AbstractGrowingAlphabetDFATest<L extends SupportsGrowingAlphabet<Character> & LearningAlgorithm<DFA<?, Character>, Character, Boolean>>
        extends AbstractGrowingAlphabetTest<L, DFA<?, Character>, MembershipOracle<Character, Boolean>, Character, Boolean> {

    @Override
    protected Alphabet<Character> getInitialAlphabet() {
        return Alphabets.characters('0', '4');
    }

    @Override
    protected Collection<Character> getAlphabetExtensions() {
        return Alphabets.characters('5', '9');
    }

    @Override
    protected DFA<?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomDFA(new Random(RANDOM_SEED), DEFAULT_AUTOMATON_SIZE, alphabet);
    }

    @Override
    protected MembershipOracle<Character, Boolean> getOracle(DFA<?, Character> target) {
        return new SimulatorOracle<>(target);
    }

}
