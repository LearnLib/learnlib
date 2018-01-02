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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

/**
 * A Blue Fringe version of RPNI for learning DFAs.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class BlueFringeRPNIDFA<I> extends AbstractBlueFringeRPNI<I, Boolean, Boolean, Void, DFA<?, I>>
        implements PassiveLearningAlgorithm.PassiveDFALearner<I> {

    protected final List<int[]> positive = new ArrayList<>();
    protected final List<int[]> negative = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param alphabet
     *         the alphabet
     */
    public BlueFringeRPNIDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
        for (DefaultQuery<I, Boolean> query : samples) {
            int[] arr = query.getInput().toIntArray(alphabet);
            if (query.getOutput()) {
                positive.add(arr);
            } else {
                negative.add(arr);
            }
        }
    }

    @Override
    protected void initializePTA(BlueFringePTA<Boolean, Void> pta) {
        for (int[] sample : positive) {
            pta.addSample(sample, true);
        }
        for (int[] sample : negative) {
            pta.addSample(sample, false);
        }
    }

    @Override
    protected CompactDFA<I> ptaToModel(BlueFringePTA<Boolean, Void> pta) {
        CompactDFA<I> dfa = new CompactDFA<>(alphabet, pta.getNumRedStates());
        pta.toAutomaton(dfa, alphabet, b -> b, x -> x);

        return dfa;
    }

}
