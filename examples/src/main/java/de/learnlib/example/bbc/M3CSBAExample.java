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
package de.learnlib.example.bbc;

import java.util.ArrayList;

import de.learnlib.algorithm.procedural.sba.SBALearner;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.oracle.PropertyOracle;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.sba.WMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.oracle.property.LoggingPropertyOracle;
import de.learnlib.oracle.property.SBAPropertyOracle;
import de.learnlib.testsupport.example.sba.ExamplePalindrome;
import de.learnlib.util.Experiment;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.exception.FormatException;
import net.automatalib.modelchecker.m3c.checker.M3CCheckers;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import net.automatalib.util.automaton.procedural.SBAs;
import net.automatalib.word.Word;

/**
 * An example of using the M3C modelchecker for black-box checking of {@link SBA}s.
 */
@SuppressWarnings({"PMD.UseExplicitTypes", "PMD.SystemPrintln"}) // allow vars and logging in examples
public final class M3CSBAExample {

    private M3CSBAExample() {}

    public static void main(String[] args) throws FormatException {
        // setup example
        final var example = ExamplePalindrome.createExample();
        final var alphabet = example.getAlphabet();
        final var sba = example.getReferenceAutomaton();

        // setup model checkers
        final var cfmpsChecker = M3CCheckers.<Character, Void>typedChecker();
        final var sbaChecker = SBAs.transformModelChecker(cfmpsChecker);
        final var formulas =
                new String[] {// it should not be possible to eventually do a c-step followed by another c-step.
                              "!(EF <c> (EF <c> true))",
                              // it should not be possible to perform a call right after a return
                              "!(EF (<R><S> true || <R><T> true))",
                              // it should not be possible to perform a call eventually after a return
                              "!(EF <R> (EF <S> true || <T> true))",
                              // it should not be possible to perform any action after a return
                              "!(EF <R> (EF <> true))",
                              // globally, every S path must be followed by an R eventually.
                              // the model checker can't generate counterexamples for this, but this property holds invariantly.
                              "AG ([S] (AF <R> true))"};

        // setup oracles
        final var mqo = new SimulatorOracle<>(sba);
        final var eqo = new EQOracleChain<SBA<?, Character>, Character, Boolean>();

        // the model checker currently can't handle empty hypothesis models so we help with an initial fixed trace
        final var sampleSetEqo = new SampleSetEQOracle<Character, Boolean>().add(Word.fromString("S"), true);
        eqo.addOracle(sampleSetEqo);

        // property oracles
        final var propEqos = new ArrayList<PropertyOracle<?, ?, ?, ?>>(formulas.length);
        for (String f : formulas) {
            var o = new LoggingPropertyOracle<>(new SBAPropertyOracle<>(M3CParser.parse(f, s -> s.charAt(0), s -> null),
                                                                        mqo,
                                                                        sbaChecker,
                                                                        WitnessTree::getWitness));
            propEqos.add(o);
            eqo.addOracle(o);
        }

        // remaining conformance test
        eqo.addOracle(new WMethodEQOracle<>(mqo));

        // setup learner
        final var learner = new SBALearner<>(alphabet, mqo, TTTLearnerDFA::new);

        // run learn loop
        final var exp = new Experiment<>(learner, eqo, alphabet);
        exp.run();

        // report results
        System.out.println();

        for (var oracle : propEqos) {
            System.out.print("Property '" + oracle.getProperty() + "' ");

            if (oracle.isDisproved()) {
                System.out.println("disproved by '" + oracle.getCounterExample() + "'");
            } else {
                System.out.println("holds");
            }
        }

        System.out.println();
        System.out.println("Equivalence?: " + SBAs.testEquivalence(sba, exp.getFinalHypothesis(), alphabet));
    }
}
