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
package de.learnlib.algorithm.rpni;

import java.util.Collection;

import de.learnlib.algorithm.PassiveLearningAlgorithm;
import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveMooreLearner;
import de.learnlib.datastructure.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.wrapper.MooreWrapper;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;

/**
 * Blue-fringe version of RPNI for inferring Moore machines.
 * <p>
 * <b>Implementation note:</b> This implementation does not support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class BlueFringeRPNIMoore<I, O> extends AbstractBlueFringeRPNI<I, Word<O>, O, Void, MooreMachine<?, I, ?, O>>
        implements PassiveMooreLearner<I, O> {

    private final BlueFringePTA<O, Void> pta;
    private boolean merged;

    public BlueFringeRPNIMoore(Alphabet<I> alphabet) {
        super(alphabet);
        this.pta = new BlueFringePTA<>(alphabet.size());
        this.merged = false;
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        for (DefaultQuery<I, Word<O>> qry : samples) {
            pta.addSampleWithStateProperties(qry.getInput().asIntSeq(alphabet), qry.getOutput().asList());
        }
    }

    @Override
    protected BlueFringePTA<O, Void> fetchPTA() {
        if (merged) {
            throw new IllegalStateException(
                    "A model has already been computed once. This learner does not support repeated model constructions");
        }
        merged = true;

        return this.pta;
    }

    @Override
    protected MooreMachine<?, I, ?, O> ptaToModel(BlueFringePTA<O, Void> pta) {
        return new MooreWrapper<>(alphabet, pta);
    }

}
