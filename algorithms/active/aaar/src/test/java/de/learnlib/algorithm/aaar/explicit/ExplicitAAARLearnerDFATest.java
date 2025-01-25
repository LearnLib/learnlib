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
package de.learnlib.algorithm.aaar.explicit;

import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.AbstractAAARTest;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;

public class ExplicitAAARLearnerDFATest
        extends AbstractAAARTest<ExplicitAAARLearnerDFA<?, String, String>, String, Boolean, DFA<?, String>> {

    public ExplicitAAARLearnerDFATest() {
        super(ExamplePaulAndMary.createExample());
    }

    @Override
    protected ExplicitAAARLearnerDFA<?, String, String> getLearner(Alphabet<String> alphabet,
                                                                   MembershipOracle<String, Boolean> oracle) {
        return new ExplicitAAARLearnerDFA<>(AAARTestUtil.<String>getDFALearners().get(0).getSecond(),
                                            oracle,
                                            new IdentityInitialAbstraction<>(alphabet),
                                            new NoopIncrementor<>());
    }
}
