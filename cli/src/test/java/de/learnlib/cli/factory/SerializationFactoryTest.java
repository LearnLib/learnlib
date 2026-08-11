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
package de.learnlib.cli.factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import de.learnlib.cli.Application;
import de.learnlib.cli.ApplicationIT;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Output;
import de.learnlib.cli.util.Util;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.serialization.InputModelSerializer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

public class SerializationFactoryTest {

    private static final String[] AUT_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fAUT"};
    private static final String[] BA_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fBA"};
    private static final String[] DOT_CMD = new String[] {ApplicationIT.STATELESS, "-sa"};
    private static final String[] LV2_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fLEARNLIBV2"};
    private static final String[] MATA_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fMATA"};
    private static final String[] SAF_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fSAF"};
    private static final String[] TAF_CMD = new String[] {ApplicationIT.STATELESS, "-sa", "-fTAF"};

    private final CommandLine cmd;

    public SerializationFactoryTest() {
        cmd = new CommandLine(new Application());
        cmd.setErr(new PrintWriter(OutputStream.nullOutputStream()));
    }

    @DataProvider(name = "dfa")
    private static Object[][] dfaConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/dfa.aut"};
                case BA -> new Object[] {BA_CMD, "/ser/dfa.ba"};
                case DOT -> new Object[] {DOT_CMD, "/ser/dfa.dot"};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/dfa.lv2"};
                case MATA -> new Object[] {MATA_CMD, "/ser/dfa.mata"};
                case SAF -> new Object[] {SAF_CMD, "/ser/dfa.saf"};
                case TAF -> new Object[] {TAF_CMD, "/ser/dfa.taf"};
            };
        }

        return result;
    }

    @Test(dataProvider = "dfa")
    public void testDFASerializers(String[] args, String resource) throws IOException {
        final Options options = Util.parseOptions(cmd, args);
        final InputModelSerializer<String, DFA<?, String>> dfaSerializer = SerializerFactory.getDFASerializer(options);
        final CompactDFA<String> dfa = Util.getExampleDFA();

        testSerializer(dfaSerializer, dfa, dfa.getInputAlphabet(), resource);
    }

    @DataProvider(name = "mealy")
    private static Object[][] mealyConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/mealy.aut", true};
                case BA -> new Object[] {BA_CMD, "/ser/mealy.ba", true};
                case DOT -> new Object[] {DOT_CMD, "/ser/mealy.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/mealy.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/mealy.mata", true};
                case SAF -> new Object[] {SAF_CMD, "/ser/mealy.saf", false};
                case TAF -> new Object[] {TAF_CMD, "/ser/mealy.taf", false};
            };
        }

        return result;
    }

    @Test(dataProvider = "mealy")
    public void testMealySerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getMealySerializer(options));
        } else {
            final InputModelSerializer<String, MealyMachine<?, String, ?, String>> mealySerializer =
                    SerializerFactory.getMealySerializer(options);
            final CompactMealy<String, String> mealy = Util.getExampleMealy();

            testSerializer(mealySerializer, mealy, mealy.getInputAlphabet(), resource);
        }
    }

    @DataProvider(name = "nfa")
    private static Object[][] nfaConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/nfa.aut", false};
                case BA -> new Object[] {BA_CMD, "/ser/nfa.ba", false};
                case DOT -> new Object[] {DOT_CMD, "/ser/nfa.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/nfa.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/nfa.mata", false};
                case SAF -> new Object[] {SAF_CMD, "/ser/nfa.saf", false};
                case TAF -> new Object[] {TAF_CMD, "/ser/nfa.taf", true};
            };
        }

        return result;
    }

    @Test(dataProvider = "nfa")
    public void testNFASerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getNFASerializer(options));
        } else {
            final InputModelSerializer<String, NFA<?, String>> nfaSerializer =
                    SerializerFactory.getNFASerializer(options);
            final CompactNFA<String> nfa = Util.getExampleNFA();

            testSerializer(nfaSerializer, nfa, nfa.getInputAlphabet(), resource);
        }
    }

    @DataProvider(name = "sba")
    private static Object[][] sbaConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/sba.aut", true};
                case BA -> new Object[] {BA_CMD, "/ser/sba.ba", true};
                case DOT -> new Object[] {DOT_CMD, "/ser/sba.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/sba.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/sba.mata", true};
                case SAF -> new Object[] {SAF_CMD, "/ser/sba.saf", true};
                case TAF -> new Object[] {TAF_CMD, "/ser/sba.taf", true};
            };
        }

        return result;
    }

    @Test(dataProvider = "sba")
    public void testSBASerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getSBASerializer(options));
        } else {
            final InputModelSerializer<String, SBA<?, String>> sbaSerializer =
                    SerializerFactory.getSBASerializer(options);
            final SBA<?, String> sba = Util.getExampleSBA();

            testSerializer(sbaSerializer, sba, sba.getInputAlphabet(), resource);
        }
    }

    @DataProvider(name = "spa")
    private static Object[][] spaConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/spa.aut", true};
                case BA -> new Object[] {BA_CMD, "/ser/spa.ba", true};
                case DOT -> new Object[] {DOT_CMD, "/ser/spa.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/spa.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/spa.mata", true};
                case SAF -> new Object[] {SAF_CMD, "/ser/spa.saf", true};
                case TAF -> new Object[] {TAF_CMD, "/ser/spa.taf", true};
            };
        }

        return result;
    }

    @Test(dataProvider = "spa")
    public void testSPASerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getSPASerializer(options));
        } else {
            final InputModelSerializer<String, SPA<?, String>> spaSerializer =
                    SerializerFactory.getSPASerializer(options);
            final SPA<?, String> spa = Util.getExampleSPA();

            testSerializer(spaSerializer, spa, spa.getInputAlphabet(), resource);
        }
    }

    @DataProvider(name = "spmm")
    private static Object[][] spmmConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/spmm.aut", true};
                case BA -> new Object[] {BA_CMD, "/ser/spmm.ba", true};
                case DOT -> new Object[] {DOT_CMD, "/ser/spmm.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/spmm.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/spmm.mata", true};
                case SAF -> new Object[] {SAF_CMD, "/ser/spmm.saf", true};
                case TAF -> new Object[] {TAF_CMD, "/ser/spmm.taf", true};
            };
        }

        return result;
    }

    @Test(dataProvider = "spmm")
    public void testSPMMSerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getSPMMSerializer(options));
        } else {
            final InputModelSerializer<String, SPMM<?, String, ?, String>> spmmSerializer =
                    SerializerFactory.getSPMMSerializer(options);
            final SPMM<?, String, ?, String> spmm = Util.getExampleSPMM();

            testSerializer(spmmSerializer, spmm, spmm.getInputAlphabet(), resource);
        }
    }

    @DataProvider(name = "vpa")
    private static Object[][] vpaConfigs() {
        final Object[][] result = new Object[Output.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Output value : Output.values()) {
            result[value.ordinal()] = switch (value) {
                case AUT -> new Object[] {AUT_CMD, "/ser/vpa.aut", true};
                case BA -> new Object[] {BA_CMD, "/ser/vpa.ba", true};
                case DOT -> new Object[] {DOT_CMD, "/ser/vpa.dot", false};
                case LEARNLIBV2 -> new Object[] {LV2_CMD, "/ser/vpa.lv2", true};
                case MATA -> new Object[] {MATA_CMD, "/ser/vpa.mata", true};
                case SAF -> new Object[] {SAF_CMD, "/ser/vpa.saf", true};
                case TAF -> new Object[] {TAF_CMD, "/ser/vpa.taf", true};
            };
        }

        return result;
    }

    @Test(dataProvider = "vpa")
    public void testVPASerializers(String[] args, String resource, boolean shouldFail) throws IOException {
        final Options options = Util.parseOptions(cmd, args);

        if (shouldFail) {
            Assert.assertThrows(() -> SerializerFactory.getVPASerializer(options));
        } else {
            final InputModelSerializer<String, OneSEVPA<?, String>> vpaSerializer =
                    SerializerFactory.getVPASerializer(options);
            final OneSEVPA<?, String> vpa = Util.getExampleVPA();

            testSerializer(vpaSerializer, vpa, vpa.getInputAlphabet(), resource);
        }
    }

    private <I, M> void testSerializer(InputModelSerializer<I, M> serializer,
                                       M model,
                                       Alphabet<I> alphabet,
                                       String resource) throws IOException {
        try (ByteArrayOutputStream actual = new ByteArrayOutputStream();
             ByteArrayOutputStream expected = new ByteArrayOutputStream();
             InputStream is = SerializationFactoryTest.class.getResourceAsStream(resource)) {

            serializer.writeModel(actual, model, alphabet);

            Assert.assertNotNull(is);
            is.transferTo(expected);

            Assert.assertEquals(actual.toString(), expected.toString(), resource);
        }
    }

}
