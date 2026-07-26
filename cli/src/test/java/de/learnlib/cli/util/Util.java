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
package de.learnlib.cli.util;

import java.util.Map;
import java.util.Random;

import de.learnlib.cli.option.Options;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.transducer.MutableMealyMachines;
import picocli.CommandLine;

public final class Util {

    private Util() {
        // prevent instantiation
    }

    public static Options parseOptions(CommandLine cmd, String... args) {
        return (Options) cmd.parseArgs(args).commandSpec().mixins().get("options").userObject();
    }

    public static CompactDFA<String> getExampleDFA() {
        // @formatter:off
        return AutomatonBuilders.newDFA(Alphabets.closedCharStringRange('a', 'b'))
                                .from("s0").on("a", "b").to("s1")
                                .from("s1").on("a", "b").to("s0")
                                .withInitial("s0")
                                .withAccepting("s1")
                                .create();
        // @formatter:on
    }

    public static CompactMealy<String, String> getExampleMealy() {
        // @formatter:off
        return AutomatonBuilders.<String, String>newMealy(Alphabets.closedCharStringRange('a', 'b'))
                                .from("s0").on("a").withOutput("97").loop()
                                .from("s0").on("b").withOutput("98").loop()
                                .withInitial("s0")
                                .create();
        // @formatter:on
    }

    public static CompactNFA<String> getExampleNFA() {
        // @formatter:off
        return AutomatonBuilders.newNFA(Alphabets.closedCharStringRange('a', 'b'))
                                .from("s0").on("a", "b").to("s1")
                                .from("s1").on("a", "b").to("s0")
                                .withInitial("s0")
                                .withAccepting("s1")
                                .create();
        // @formatter:on
    }

    public static SBA<?, String> getExampleSBA() {
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.closedCharStringRange('a', 'b'),
                                                     Alphabets.singleton("S"),
                                                     "R");
        // @formatter:off
        final var dfa = AutomatonBuilders.newDFA(alphabet)
                                         .from("s0").on("R").to("s1")
                                                    .on("a", "b").to("s2")
                                         .from("s2").on("R").to("s3")
                                         .withInitial("s0")
                                         .withAccepting("s0", "s2", "s3")
                                         .create();
        // @formatter:on
        MutableDFAs.complete(dfa, alphabet, true);

        return new StackSBA<>(alphabet, "S", Map.of("S", dfa));
    }

    public static SPA<?, String> getExampleSPA() {
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.closedCharStringRange('a', 'b'),
                                                     Alphabets.singleton("S"),
                                                     "R");
        // @formatter:off
        final var dfa = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                         .from("s0").on("a", "b").to("s1")
                                                    .on("S").to("s0")
                                         .from("s1").on("a", "b").to("s0")
                                                    .on("S").to("s0")
                                         .withInitial("s0")
                                         .withAccepting("s1")
                                         .create();
        // @formatter:on
        return new StackSPA<>(alphabet, "S", Map.of("S", dfa));
    }

    public static SPMM<?, String, ?, String> getExampleSPMM() {
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.closedCharStringRange('a', 'b'),
                                                     Alphabets.singleton("S"),
                                                     "R");
        final String error = "error";

        // @formatter:off
        final var mealy = AutomatonBuilders.<String, String>newMealy(alphabet)
                                           .from("s0").on("a").withOutput("97").to("s1")
                                                      .on("b").withOutput("98").to("s1")
                                           .from("s1").on("R").withOutput("ok").to("s2")
                                           .withInitial("s0")
                                           .create();
        // @formatter:on
        MutableMealyMachines.complete(mealy, alphabet, error, true);
        return new StackSPMM<>(alphabet, "S", "✓", error, Map.of("S", mealy));
    }

    public static OneSEVPA<?, String> getExampleVPA() {
        final VPAlphabet<String> alphabet = new DefaultVPAlphabet<>(Alphabets.singleton("a"),
                                                                    Alphabets.singleton("S"),
                                                                    Alphabets.fromArray("R1", "R2"));
        return RandomAutomata.randomOneSEVPA(new Random(42), 2, alphabet, 0.5, 0.5, true);
    }
}
