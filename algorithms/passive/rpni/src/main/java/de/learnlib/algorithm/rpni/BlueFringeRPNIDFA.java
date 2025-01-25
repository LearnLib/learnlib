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
import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.datastructure.pta.BlueFringePTA;
import de.learnlib.datastructure.pta.wrapper.DFAWrapper;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;

/**
 * A Blue Fringe version of RPNI for learning DFAs.
 * <p>
 * <b>Implementation note:</b> This implementation does not support repeated calls to {@link
 * PassiveLearningAlgorithm#computeModel()}.
 *
 * @param <I>
 *         input symbol type
 */
public class BlueFringeRPNIDFA<I>
        extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?, I>>
        implements PassiveDFALearner<I> {

    private final BlueFringePTA<Boolean, Void> pta;
    private boolean merged;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public BlueFringeRPNIDFA(Alphabet<I> alphabet) {
        super(alphabet);
        this.pta = new BlueFringePTA<>(alphabet.size());
        this.merged = false;
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
        for (DefaultQuery<I, Boolean> query : samples) {
            pta.addSample(query.getInput().asIntSeq(alphabet), query.getOutput());
        }
    }

    @Override
    protected BlueFringePTA<Boolean, Void> fetchPTA() {
        if (merged) {
            throw new IllegalStateException(
                    "A model has already been computed once. This learner does not support repeated model constructions");
        }
        merged = true;

        return this.pta;
    }

    @Override
    protected DFA<?, I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
        return new DFAWrapper<>(alphabet, pta);
    }

}
