/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.testsupport.it.learner;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.examples.LearningExample.StateLocalInputMealyLearningExample;
import de.learnlib.examples.LearningExamples;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SLIMealyLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SLIMealyLearnerVariantListImpl;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.words.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for {@link StateLocalInputMealyMachine} learning algorithms.
 *
 * @author frohme
 */
public abstract class AbstractSLIMealyLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<LearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();
        final List<StateLocalInputMealyLearningExample<?, ?>> examples = LearningExamples.createSLIMealyExamples();

        for (StateLocalInputMealyLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<LearnerVariantITCase<I, Word<OutputAndLocalInputs<I, O>>, StateLocalInputMealyMachine<?, I, ?, O>>> createAllVariantsITCase(
            StateLocalInputMealyLearningExample<I, O> example) {

        final StateLocalInputMealyOracle<I, OutputAndLocalInputs<I, O>> mqOracle =
                new StateLocalInputSULOracle<>(new StateLocalInputMealySimulatorSUL<>(example.getReferenceAutomaton()));
        final SLIMealyLearnerVariantListImpl<I, O> variants = new SLIMealyLearnerVariantListImpl<>();
        addLearnerVariants(mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  new StateLocalInputMealySimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    /**
     * Adds, for a given setup, all the variants of the Mealy machine learner to be tested to the specified {@link
     * LearnerVariantList variant list}.
     *
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I, O> void addLearnerVariants(StateLocalInputMealyOracle<I, OutputAndLocalInputs<I, O>> mqOracle,
                                                      SLIMealyLearnerVariantList<I, O> variants);
}
