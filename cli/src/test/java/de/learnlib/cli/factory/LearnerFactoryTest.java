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
import de.learnlib.algorithm.malerpnueli.MalerPnueliDFA;
import de.learnlib.algorithm.malerpnueli.MalerPnueliMealy;
import de.learnlib.algorithm.nlstar.NLStarLearner;
import de.learnlib.algorithm.observationpack.dfa.OPLearnerDFA;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.observationpack.vpa.OPLearnerVPA;
import de.learnlib.algorithm.procedural.sba.SBALearner;
import de.learnlib.algorithm.procedural.spa.SPALearner;
import de.learnlib.algorithm.procedural.spmm.SPMMLearner;
import de.learnlib.algorithm.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithm.rivestschapire.RivestSchapireMealy;
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lKEARNS_VAZIRANI"},
                                      KearnsVaziraniDFA.class};
                case L_LAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lL_LAMBDA"}, LLambdaDFA.class};
                case L_SHARP ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lL_SHARP"}, Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lL_STAR"},
                                             ExtensibleLStarDFA.class};
                case MALER_PNUELI -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lMALER_PNUELI"},
                                                   MalerPnueliDFA.class};
                case NL_STAR ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lNL_STAR"}, Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lOBSERVATION_PACK"},
                                      OPLearnerDFA.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lRIVEST_SCHAPIRE"},
                                      RivestSchapireDFA.class};
                case SPARSE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lSPARSE"}, Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lTTT"}, TTTLearnerDFA.class};
                case TTT_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-lTTT_LAMBDA"},
                                                 TTTLambdaDFA.class};
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lKEARNS_VAZIRANI"},
                                      KearnsVaziraniMealy.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_LAMBDA"},
                                               LLambdaMealy.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_STAR"},
                                             ExtensibleLStarMealy.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lMALER_PNUELI"},
                                      MalerPnueliMealy.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lOBSERVATION_PACK"},
                                      OPLearnerMealy.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lRIVEST_SCHAPIRE"},
                                      RivestSchapireMealy.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lSPARSE"},
                                             SparseLearner.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT"},
                                          TTTLearnerMealy.class};
                case TTT_LAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lKEARNS_VAZIRANI"},
                                      Exception.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_LAMBDA"},
                                               Exception.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_SHARP"},
                                              LSharpMealy.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lL_STAR"},
                                             Exception.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lMALER_PNUELI"},
                                      Exception.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lOBSERVATION_PACK"},
                                      Exception.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lRIVEST_SCHAPIRE"},
                                      Exception.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT"},
                                          Exception.class};
                case TTT_LAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tMEALY", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lKEARNS_VAZIRANI"},
                                      Exception.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lL_LAMBDA"},
                                               Exception.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lL_STAR"},
                                             Exception.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lMALER_PNUELI"},
                                      Exception.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lNL_STAR"},
                                              NLStarLearner.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lOBSERVATION_PACK"},
                                      Exception.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lRIVEST_SCHAPIRE"},
                                      Exception.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lSPARSE"},
                                             Exception.class};
                case TTT ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lTTT"}, Exception.class};
                case TTT_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tNFA", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lKEARNS_VAZIRANI"},
                                      SBALearner.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lL_LAMBDA"},
                                               SBALearner.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lL_STAR"},
                                             SBALearner.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lMALER_PNUELI"},
                                      SBALearner.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lOBSERVATION_PACK"},
                                      SBALearner.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lRIVEST_SCHAPIRE"},
                                      SBALearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lTTT"},
                                          SBALearner.class};
                case TTT_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSBA", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lKEARNS_VAZIRANI"},
                                      SPALearner.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lL_LAMBDA"},
                                               SPALearner.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lL_STAR"},
                                             SPALearner.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lMALER_PNUELI"},
                                      SPALearner.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lOBSERVATION_PACK"},
                                      SPALearner.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lRIVEST_SCHAPIRE"},
                                      SPALearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lTTT"},
                                          SPALearner.class};
                case TTT_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPA", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lKEARNS_VAZIRANI"},
                                      SPMMLearner.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lL_LAMBDA"},
                                               SPMMLearner.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lL_STAR"},
                                             SPMMLearner.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lMALER_PNUELI"},
                                      SPMMLearner.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lOBSERVATION_PACK"},
                                      SPMMLearner.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lRIVEST_SCHAPIRE"},
                                      SPMMLearner.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lSPARSE"},
                                             SPMMLearner.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lTTT"},
                                          SPMMLearner.class};
                case TTT_LAMBDA ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tSPMM", "-lTTT_LAMBDA"},
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
                case KEARNS_VAZIRANI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lKEARNS_VAZIRANI"},
                                      Exception.class};
                case L_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lL_LAMBDA"},
                                               Exception.class};
                case L_SHARP -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lL_SHARP"},
                                              Exception.class};
                case L_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lL_STAR"},
                                             Exception.class};
                case MALER_PNUELI ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lMALER_PNUELI"},
                                      Exception.class};
                case NL_STAR -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lNL_STAR"},
                                              Exception.class};
                case OBSERVATION_PACK ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lOBSERVATION_PACK"},
                                      OPLearnerVPA.class};
                case RIVEST_SCHAPIRE ->
                        new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lRIVEST_SCHAPIRE"},
                                      Exception.class};
                case SPARSE -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lSPARSE"},
                                             Exception.class};
                case TTT -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lTTT"},
                                          TTTLearnerVPA.class};
                case TTT_LAMBDA -> new Object[] {new String[] {ApplicationIT.STATELESS, "-sa", "-tVPA", "-lTTT_LAMBDA"},
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
