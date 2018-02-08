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
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Blue-fringe version of RPNI for inferring Mealy machines.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */
public class BlueFringeRPNIMealy<I, O> extends AbstractBlueFringeRPNI<I, Word<O>, Void, O, MealyMachine<?, I, ?, O>>
        implements PassiveLearningAlgorithm.PassiveMealyLearner<I, O> {

    private final List<Pair<int[], Word<O>>> samples = new ArrayList<>();

    public BlueFringeRPNIMealy(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public void addSamples(Collection<? extends DefaultQuery<I, Word<O>>> samples) {
        for (DefaultQuery<I, Word<O>> qry : samples) {
            this.samples.add(new Pair<>(qry.getInput().toIntArray(alphabet), qry.getOutput()));
        }
    }

    @Override
    protected void initializePTA(BlueFringePTA<Void, O> pta) {
        for (Pair<int[], Word<O>> sample : samples) {
            pta.addSampleWithTransitionProperties(sample.getFirst(), sample.getSecond().asList());
        }
    }

    @Override
    protected MealyMachine<?, I, ?, O> ptaToModel(BlueFringePTA<Void, O> pta) {
        CompactMealy<I, O> mealy = new CompactMealy<>(alphabet, pta.getNumRedStates());
        pta.toAutomaton(mealy, alphabet);
        return mealy;
    }

}
