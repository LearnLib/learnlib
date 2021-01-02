/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.algorithms.rpni;

import java.util.Collection;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.algorithm.PassiveLearningAlgorithm.PassiveMealyLearner;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.pta.PTAUtil;
import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Blue-fringe version of RPNI for inferring Mealy machines.
 * <p>
 * <b>Implementation note:</b> This implementation does not support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
public class BlueFringeRPNIMealy<I, O> extends AbstractBlueFringeRPNI<I, Word<O>, Void, O, MealyMachine<?, I, ?, O>>
        implements PassiveMealyLearner<I, O> {

    private final BlueFringePTA<Void, O> pta;
    private boolean merged;

    public BlueFringeRPNIMealy(Alphabet<I> alphabet) {
        super(alphabet);
        this.pta = new BlueFringePTA<>(alphabetSize);
        this.merged = false;
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        for (DefaultQuery<I, Word<O>> qry : samples) {
            pta.addSampleWithTransitionProperties(qry.getInput().asIntSeq(alphabet), qry.getOutput().asList());
        }
    }

    @Override
    protected BlueFringePTA<Void, O> fetchPTA() {
        if (merged) {
            throw new IllegalStateException(
                    "A model has already been computed once. This learner does not support repeated model constructions");
        }
        merged = true;

        return this.pta;
    }

    @Override
    protected MealyMachine<?, I, ?, O> ptaToModel(BlueFringePTA<Void, O> pta) {
        return PTAUtil.toMealy(pta, alphabet);
    }

}
