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
package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RandomWpMethodEQOracleTest {

    @Test
    public void testEquivalence() {
        var example = MMLTExamples.sensorCollector();
        var mmlt = example.getReferenceAutomaton();
        var alphabet = example.getAlphabet();

        var mqo = new TimedSULOracle<>(new MMLTSimulatorSUL<>(mmlt), example.getParams());
        var eqo = new RandomWpMethodEQOracle<>(mqo, 123, 10, 0, 100);
        var cex = eqo.findCounterExample(mmlt, alphabet);

        Assert.assertNull(cex);
    }

    @Test
    public void testInequivalence() {
        var mmlt = buildMMLT();
        var alphabet = mmlt.getSemantics().getInputAlphabet();
        var params = new MMLTModelParams<>("void", StringSymbolCombiner.getInstance(), 4, 80);

        var hyp = buildMMLT();
        var t = hyp.getTransition(1, "abort");
        hyp.setTransitionOutput(t, "part");

        var mqo = new TimedSULOracle<>(new MMLTSimulatorSUL<>(mmlt), params);
        var eqo = new RandomWpMethodEQOracle<>(mqo, 42, 0, 2, 100);
        var cex = eqo.findCounterExample(hyp, alphabet);

        Assert.assertNotNull(cex);
        Assert.assertEquals(cex.getOutput(), mmlt.getSemantics().computeSuffixOutput(cex.getPrefix(), cex.getSuffix()));
        Assert.assertNotEquals(mmlt.getSemantics().computeSuffixOutput(cex.getPrefix(), cex.getSuffix()),
                               hyp.getSemantics().computeSuffixOutput(cex.getPrefix(), cex.getSuffix()));
    }

    private static CompactMMLT<String, String> buildMMLT() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addInitialState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.addTransition(s0, "p1", s1, "go");
        model.addTransition(s1, "abort", s1, "ok");
        model.addLocalReset(s1, "abort");

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, "p2", s2, "go");
        model.addTransition(s2, "abort", s3, "void");
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, "collect", s0, "void");

        return model;
    }
}
