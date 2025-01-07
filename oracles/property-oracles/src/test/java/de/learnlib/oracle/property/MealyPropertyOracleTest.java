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
package de.learnlib.oracle.property;

import java.util.function.Function;

import de.learnlib.oracle.EmptinessOracle.MealyEmptinessOracle;
import de.learnlib.oracle.InclusionOracle.MealyInclusionOracle;
import de.learnlib.oracle.LassoEmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.oracle.PropertyOracle.MealyPropertyOracle;
import de.learnlib.oracle.emptiness.MealyBFEmptinessOracle;
import de.learnlib.oracle.emptiness.MealyLassoEmptinessOracleImpl;
import de.learnlib.oracle.equivalence.MealyBFInclusionOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.MealySimulatorOmegaOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.mealy.ExampleTinyMealy;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.modelchecker.ltsmin.LTSminUtil;
import net.automatalib.modelchecker.ltsmin.LTSminVersion;
import net.automatalib.modelchecker.ltsmin.ltl.LTSminLTLIOBuilder;
import net.automatalib.modelchecker.ltsmin.monitor.LTSminMonitorIOBuilder;
import net.automatalib.modelchecking.ModelChecker.MealyModelChecker;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class MealyPropertyOracleTest {

    private static final Function<String, Character> EDGE_PARSER = s -> s.charAt(0);

    // @formatter:off
    private static final CompactMealy<Character, Character> HYP =
            AutomatonBuilders.<Character, Character>newMealy(Alphabets.singleton('a'))
                             .withInitial("q0")
                             .from("q0").on('a').withOutput('1').loop()
                             .create();
    // @formatter:on

    @Test
    public void testTinyExample() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 0, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        final ExampleTinyMealy example = ExampleTinyMealy.createExample();
        final Alphabet<Character> inputAlphabet = example.getAlphabet();

        final MealyMachine<?, Character, ?, Character> sys = example.getReferenceAutomaton();

        final MealyOmegaMembershipOracle<?, Character, Character> omqo = new MealySimulatorOmegaOracle<>(sys);
        final MealyMembershipOracle<Character, Character> mqo = omqo.getMembershipOracle();

        final MealyLassoEmptinessOracle<Character, Character> emptinessOracle =
                new MealyLassoEmptinessOracleImpl<>(omqo);
        final MealyInclusionOracle<Character, Character> inclusionOracle = new MealyBFInclusionOracle<>(mqo, 1.0);

        final MealyModelCheckerLasso<Character, Character, String> modelChecker =
                new LTSminLTLIOBuilder<Character, Character>().withString2Input(EDGE_PARSER)
                                                              .withString2Output(EDGE_PARSER)
                                                              .create();

        final MealyPropertyOracle<Character, Character, String> propertyOracle =
                new MealyLassoPropertyOracle<>("X output==\"2\"", inclusionOracle, emptinessOracle, modelChecker);

        final DefaultQuery<Character, Word<Character>> ce = propertyOracle.findCounterExample(HYP, inputAlphabet);

        Assert.assertNotNull(ce);
        Assert.assertNotEquals(ce.getOutput(), HYP.computeOutput(ce.getInput()));
    }

    /**
     * Test-case issue <a href="https://github.com/LearnLib/automatalib/issues/46">#46</a>.
     */
    @Test
    public void testIssue46() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 1, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        final ExampleTinyMealy example = ExampleTinyMealy.createExample();
        final Alphabet<Character> inputAlphabet = example.getAlphabet();
        final MealyMachine<?, Character, ?, Character> sys = example.getReferenceAutomaton();

        final int multiplier = 1;
        final MealyMembershipOracle<Character, Character> mqo = new MealySimulatorOracle<>(sys);

        final MealyEmptinessOracle<Character, Character> emptinessOracle =
                new MealyBFEmptinessOracle<>(mqo, multiplier);
        final MealyInclusionOracle<Character, Character> inclusionOracle =
                new MealyBFInclusionOracle<>(mqo, multiplier);

        final MealyModelChecker<Character, Character, String, MealyMachine<?, Character, ?, Character>> modelChecker =
                new LTSminMonitorIOBuilder<Character, Character>().withString2Input(EDGE_PARSER)
                                                                  .withString2Output(EDGE_PARSER)
                                                                  .create();

        final MealyFinitePropertyOracle<Character, Character, String> propertyOracle =
                new MealyFinitePropertyOracle<>("X (output == \"2\")", inclusionOracle, emptinessOracle, modelChecker);

        final DefaultQuery<Character, Word<Character>> ce = propertyOracle.findCounterExample(HYP, inputAlphabet);

        Assert.assertNotNull(ce);
        Assert.assertNotEquals(ce.getOutput(), HYP.computeOutput(ce.getInput()));
    }
}
