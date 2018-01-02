/* Copyright (C) 2013-2018 TU Dortmund
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

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.pta.pta.BlueFringePTAState;
import de.learnlib.datastructure.pta.pta.RedBlueMerge;
import net.automatalib.words.Alphabet;

/**
 * A state-merging learning algorithm based on the minimal description length principle. On an operational level this
 * algorithm is very similar to the {@link BlueFringeRPNIDFA} algorithm. However, whereas the basic RPNI approach merges
 * the very first pair of nodes that resemble a valid merge, the MDL variant computes an additional score and only
 * commits to a merge, if the resulting hypothesis will yield a better score.
 * <p>
 * This passive approach to state-merging works better in scenarios where only positive training data is available.
 * Hence, this algorithm only expect positive training data.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class BlueFringeMDLDFA<I> extends BlueFringeRPNIDFA<I> {

    private double currentScore = Double.POSITIVE_INFINITY;

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public BlueFringeMDLDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
        if (samples.stream().anyMatch(q -> !q.getOutput())) {
            throw new IllegalArgumentException("Only positive examples are allowed");
        }
        super.addSamples(samples);
    }

    @Override
    protected boolean decideOnValidMerge(RedBlueMerge<Boolean, Void, BlueFringePTAState<Boolean, Void>> merge) {
        final double score = MDLUtil.score(merge.toMergedAutomaton(), super.alphabetSize, super.positive);
        if (score < currentScore) {
            currentScore = score;
            return true;
        }

        return false;
    }
}
