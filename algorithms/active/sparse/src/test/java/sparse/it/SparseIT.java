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
package sparse.it;

import java.io.IOException;
import java.io.InputStream;

import de.learnlib.algorithm.sparse.SparseLearner;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

public class SparseIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             LearnerVariantList.MealyLearnerVariantList<I, O> variants) {
        variants.addLearnerVariant("sparse", new SparseLearner<>(alphabet, mqOracle));
    }

    private static CompactMealy<String, String> loadModel(String modelName) throws IOException, FormatException {
        try (InputStream is = SparseIT.class.getResourceAsStream(modelName)) {
            return DOTParsers.mealy().readModel(is).model;
        }
    }

    private static void runTest(String modelName) throws IOException, FormatException {
        final var model = loadModel(modelName);
        final var alp = model.getInputAlphabet();
        final var mqo = new MealySimulatorOracle<>(model);
        final var l = new SparseLearner<>(alp, mqo);
        l.startLearning();
        var cex = Automata.findSeparatingWord(model, l.getHypothesisModel(), alp);
        while (cex != null) {
            if (!l.refineHypothesis(new DefaultQuery<>(Word.epsilon(), cex, mqo.answerQuery(cex)))) {
                throw new RuntimeException("counterexample did not cause refinement");
            }

            cex = Automata.findSeparatingWord(model, l.getHypothesisModel(), alp);
        }
    }

    @Test
    public void testPassport() throws IOException, FormatException {
        runTest("/passport.flat_0_10.dot");
    }

    @Test
    public void testVolksbank() throws IOException, FormatException {
        runTest("/Volksbank_learnresult_MAESTRO_fix.dot");
    }

    @Test
    public void testTLS() throws IOException, FormatException {
        runTest("/GnuTLS_3.3.8_client_full.dot");
    }

    @Test
    public void testMQTT() throws IOException, FormatException {
        runTest("/mosquitto__two_client_will_retain.dot");
    }

    @Test
    public void testTCP() throws IOException, FormatException {
        runTest("/tcp_server_ubuntu.dot");
    }

    @Test
    public void testSSH() throws IOException, FormatException {
        runTest("/BitVise.dot");
    }
}
