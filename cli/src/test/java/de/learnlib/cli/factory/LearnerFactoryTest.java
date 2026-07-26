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

import java.io.OutputStream;
import java.io.PrintWriter;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.adt.learner.ADTLearner;
import de.learnlib.algorithm.dhc.mealy.MealyDHC;
import de.learnlib.algorithm.kv.dfa.KearnsVaziraniDFA;
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithm.lambda.lstar.LLambdaDFA;
import de.learnlib.algorithm.lambda.lstar.LLambdaMealy;
import de.learnlib.algorithm.lambda.ttt.dfa.TTTLambdaDFA;
import de.learnlib.algorithm.lambda.ttt.mealy.TTTLambdaMealy;
import de.learnlib.algorithm.lsharp.LSharpMealy;
import de.learnlib.algorithm.lstar.dfa.ExtensibleLStarDFA;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.nlstar.NLStarLearner;
import de.learnlib.algorithm.observationpack.dfa.OPLearnerDFA;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.observationpack.vpa.OPLearnerVPA;
import de.learnlib.algorithm.procedural.sba.SBALearner;
import de.learnlib.algorithm.procedural.spa.SPALearner;
import de.learnlib.algorithm.procedural.spmm.SPMMLearner;
import de.learnlib.algorithm.sparse.SparseLearner;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithm.ttt.vpa.TTTLearnerVPA;
import de.learnlib.cli.Application;
import de.learnlib.cli.ApplicationIT;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.AcceptorNullOracle;
import de.learnlib.cli.util.AdaptiveNullOracle;
import de.learnlib.cli.util.Constructor.AdaptiveConstructor;
import de.learnlib.cli.util.Constructor.DFAConstructor;
import de.learnlib.cli.util.Constructor.MealyConstructor;
import de.learnlib.cli.util.Constructor.PresetConstructor;
import de.learnlib.cli.util.TransducerNullOracle;
import de.learnlib.cli.util.Util;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

public class LearnerFactoryTest {

    private final CommandLine cmd;

    public LearnerFactoryTest() {
        cmd = new CommandLine(new Application());
        cmd.setErr(new PrintWriter(OutputStream.nullOutputStream()));
    }

    @DataProvider(name = "dfa")
    private static Object[][] dfaConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lADT"}, Exception.class};
                case DHC -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lDHC"}, Exception.class};
                case KV ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lKV"}, KearnsVaziraniDFA.class};
                case LLAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lLLAMBDA"}, LLambdaDFA.class};
                case LSHARP ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lLSHARP"}, Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lLSTAR"},
                                            ExtensibleLStarDFA.class};
                case NLSTAR ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lNLSTAR"}, Exception.class};
                case OP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lOP"}, OPLearnerDFA.class};
                case SPARSE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lSPARSE"}, Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lTTT"}, TTTLearnerDFA.class};
                case TTTLAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lTTTLAMBDA"}, TTTLambdaDFA.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "dfa")
    public void testDFALearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getDFALearner(options));
        } else {
            final DFAConstructor<Alphabet<String>, String> dfaConstructor = LearnerFactory.getDFALearner(options);
            final DFALearner<String> learner =
                    dfaConstructor.constructLearner(Alphabets.fromArray(), new AcceptorNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "mealy")
    private static Object[][] mealyConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lADT"},
                                          Exception.class};
                case DHC -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lDHC"},
                                          MealyDHC.class};
                case KV -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lKV"},
                                         KearnsVaziraniMealy.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLLAMBDA"},
                                              LLambdaMealy.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLSTAR"},
                                            ExtensibleLStarMealy.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lNLSTAR"},
                                             Exception.class};
                case OP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lOP"},
                                         OPLearnerMealy.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lSPARSE"},
                                             SparseLearner.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT"},
                                          TTTLearnerMealy.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTTLAMBDA"},
                                                TTTLambdaMealy.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "mealy")
    public void testMealyLearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getMealyLearner(options));
        } else {
            final MealyConstructor<Alphabet<String>, String, String> mealyConstructor =
                    LearnerFactory.getMealyLearner(options);
            final MealyLearner<String, String> learner =
                    mealyConstructor.constructLearner(Alphabets.fromArray(), new TransducerNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "adaptive")
    private static Object[][] adaptiveConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lADT"},
                                          ADTLearner.class};
                case DHC -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lDHC"},
                                          Exception.class};
                case KV -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lKV"},
                                         Exception.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLLAMBDA"},
                                              Exception.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLSHARP"},
                                             LSharpMealy.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lLSTAR"},
                                            Exception.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lNLSTAR"},
                                             Exception.class};
                case OP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lOP"},
                                         Exception.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT"},
                                          Exception.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTTLAMBDA"},
                                                Exception.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "adaptive")
    public void testAdaptiveLearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getAdaptiveLearner(options));
        } else {
            final AdaptiveConstructor<Alphabet<String>, MealyMachine<?, String, ?, String>, String, String>
                    adaptiveConstructor = LearnerFactory.getAdaptiveLearner(options);
            final LearningAlgorithm<MealyMachine<?, String, ?, String>, String, Word<String>> learner =
                    adaptiveConstructor.constructLearner(Alphabets.fromArray(), new AdaptiveNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "nfa")
    private static Object[][] nfaConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lADT"}, Exception.class};
                case DHC ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lDHC"}, Exception.class};
                case KV ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lKV"}, Exception.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lLLAMBDA"},
                                              Exception.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lLSTAR"},
                                            Exception.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lNLSTAR"},
                                             NLStarLearner.class};
                case OP ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lOP"}, Exception.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lSPARSE"},
                                             Exception.class};
                case TTT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lTTT"}, Exception.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lTTTLAMBDA"},
                                                Exception.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "nfa")
    public void testNFALearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getNFALearner(options));
        } else {
            final PresetConstructor<Alphabet<String>, NFA<?, String>, String, Boolean> nfaConstructor =
                    LearnerFactory.getNFALearner(options);
            final LearningAlgorithm<NFA<?, String>, String, Boolean> learner =
                    nfaConstructor.constructLearner(Alphabets.fromArray(), new AcceptorNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "sba")
    private static Object[][] sbaConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lADT"}, Exception.class};
                case DHC ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lDHC"}, Exception.class};
                case KV ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lKV"}, SBALearner.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lLLAMBDA"},
                                              SBALearner.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lLSTAR"},
                                            SBALearner.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lNLSTAR"},
                                             Exception.class};
                case OP ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lOP"}, SBALearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lTTT"},
                                          SBALearner.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lTTTLAMBDA"},
                                                SBALearner.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "sba")
    public void testSBALearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getSBALearner(options));
        } else {
            final PresetConstructor<ProceduralInputAlphabet<String>, SBA<?, String>, String, Boolean> sbaConstructor =
                    LearnerFactory.getSBALearner(options);
            final LearningAlgorithm<SBA<?, String>, String, Boolean> learner =
                    sbaConstructor.constructLearner(new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(),
                                                                                         Alphabets.fromArray(),
                                                                                         ""), new AcceptorNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "spa")
    private static Object[][] spaConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lADT"}, Exception.class};
                case DHC ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lDHC"}, Exception.class};
                case KV ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lKV"}, SPALearner.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lLLAMBDA"},
                                              SPALearner.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lLSTAR"},
                                            SPALearner.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lNLSTAR"},
                                             Exception.class};
                case OP ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lOP"}, SPALearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lTTT"},
                                          SPALearner.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lTTTLAMBDA"},
                                                SPALearner.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "spa")
    public void testSPALearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getSBALearner(options));
        } else {
            final PresetConstructor<ProceduralInputAlphabet<String>, SPA<?, String>, String, Boolean> spaConstructor =
                    LearnerFactory.getSPALearner(options);
            final LearningAlgorithm<SPA<?, String>, String, Boolean> learner =
                    spaConstructor.constructLearner(new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(),
                                                                                         Alphabets.fromArray(),
                                                                                         ""), new AcceptorNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "spmm")
    private static Object[][] spmmConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lADT"},
                                          Exception.class};
                case DHC -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lDHC"},
                                          SPMMLearner.class};
                case KV -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lKV"},
                                         SPMMLearner.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lLLAMBDA"},
                                              SPMMLearner.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lLSTAR"},
                                            SPMMLearner.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lNLSTAR"},
                                             Exception.class};
                case OP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lOP"},
                                         SPMMLearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lSPARSE"},
                                             SPMMLearner.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lTTT"},
                                          SPMMLearner.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lTTTLAMBDA"},
                                                SPMMLearner.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "spmm")
    public void testSPMMLearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getSBALearner(options));
        } else {
            final PresetConstructor<ProceduralInputAlphabet<String>, SPMM<?, String, ?, String>, String, Word<String>>
                    spmmConstructor = LearnerFactory.getSPMMLearner(options);
            final LearningAlgorithm<SPMM<?, String, ?, String>, String, Word<String>> learner =
                    spmmConstructor.constructLearner(new DefaultProceduralInputAlphabet<>(Alphabets.fromArray(),
                                                                                          Alphabets.fromArray(),
                                                                                          ""),
                                                     new TransducerNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }

    @DataProvider(name = "vpa")
    private static Object[][] vpaConfigs() {
        final Object[][] result = new Object[Learner.values().length][];

        // use for-each loop + switch case to make compiler check for completeness
        for (Learner value : Learner.values()) {
            result[value.ordinal()] = switch (value) {
                case ADT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lADT"}, Exception.class};
                case DHC ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lDHC"}, Exception.class};
                case KV ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lKV"}, Exception.class};
                case LLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lLLAMBDA"},
                                              Exception.class};
                case LSHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lLSHARP"},
                                             Exception.class};
                case LSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lLSTAR"},
                                            Exception.class};
                case NLSTAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lNLSTAR"},
                                             Exception.class};
                case OP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lOP"},
                                         OPLearnerVPA.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lTTT"},
                                          TTTLearnerVPA.class};
                case TTTLAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lTTTLAMBDA"},
                                                Exception.class};
            };
        }

        return result;
    }

    @Test(dataProvider = "vpa")
    public void testVPALearners(String[] args, Class<?> clazz) {
        final Options options = Util.parseOptions(cmd, args);

        if (Exception.class.isAssignableFrom(clazz)) {
            Assert.assertThrows(() -> LearnerFactory.getVPALearner(options));
        } else {
            final PresetConstructor<VPAlphabet<String>, OneSEVPA<?, String>, String, Boolean> vpaConstructor =
                    LearnerFactory.getVPALearner(options);
            final LearningAlgorithm<OneSEVPA<?, String>, String, Boolean> learner =
                    vpaConstructor.constructLearner(new DefaultVPAlphabet<>(Alphabets.fromArray(),
                                                                            Alphabets.fromArray(),
                                                                            Alphabets.fromArray()),
                                                    new AcceptorNullOracle());
            Assert.assertTrue(clazz.isInstance(learner));
        }
    }
}
