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

import java.util.HashSet;
import java.util.Set;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class IncrementalWMethodEQOracleTest extends AbstractEQOracleTest<DFA<?, String>, String, Boolean> {

    private DFA<?, String> dfa;
    private Set<Word<String>> transitionCover;
    private Set<Word<String>> characterizingSet;

    @BeforeClass
    public void setUp() {
        this.dfa = ExamplePaulAndMary.constructMachine();

        this.transitionCover = new HashSet<>(Automata.transitionCover(this.dfa, getAlphabet()));
        this.characterizingSet = new HashSet<>(Automata.characterizingSet(this.dfa, getAlphabet()));
    }

    // TODO: this currently seems necessary to fix test scheduling (removing this breaks _other_ tests?!?).
    // Check with newer versions of TestNG again.
    @Test(dependsOnMethods = "testGeneratedEQQueries")
    public void testNGFix() {}

    @Override
    protected void checkGeneratedQuery(Word<String> query) {
        transitionCover.stream().filter(w -> w.isPrefixOf(query)).findAny().orElseThrow(AssertionError::new);
        characterizingSet.stream().filter(w -> w.isSuffixOf(query)).findAny().orElseThrow(AssertionError::new);
    }

    @Override
    protected EquivalenceOracle<DFA<?, String>, String, Boolean> getOracle(MembershipOracle<String, Boolean> mOracle) {
        return new IncrementalWMethodEQOracle<>(mOracle, getAlphabet());
    }

    @Override
    protected DFA<?, String> getHypothesis() {
        return dfa;
    }

    @Override
    protected Alphabet<String> getAlphabet() {
        return ExamplePaulAndMary.createInputAlphabet();
    }
}
