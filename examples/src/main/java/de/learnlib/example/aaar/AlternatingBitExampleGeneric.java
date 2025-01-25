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
package de.learnlib.example.aaar;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.aaar.generic.GenericAAARLearnerMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.example.aaar.Event.Msg;
import de.learnlib.example.aaar.Event.Recv;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.dot.DOT;
import net.automatalib.word.Word;

/**
 * Example from the paper "Automata Learning with Automated Alphabet Abstraction Refinement" by Howar et al., which uses
 * the generic version of the AAAR learner.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class AlternatingBitExampleGeneric {

    private static final int ID1 = 72;
    private static final int ID2 = 73;

    private AlternatingBitExampleGeneric() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {

        final Protocol mqo = new Protocol();

        final LearnerConstructor<ExtensibleLStarMealy<Event, String>, Event, Word<String>> lstar =
                (alph, mq) -> new ExtensibleLStarMealyBuilder<Event, String>().withAlphabet(alph)
                                                                              .withOracle(mq)
                                                                              .create();

        final GenericAAARLearnerMealy<ExtensibleLStarMealy<Event, String>, String, Event, String> learner =
                new GenericAAARLearnerMealy<>(lstar, mqo, new Recv(), new EventAbstractor());

        learner.startLearning();
        printInfo(learner);

        learner.refineHypothesis(new DefaultQuery<>(Word.epsilon(),
                                                    Word.fromSymbols(new Msg<>(0, "d"), new Recv()),
                                                    Word.fromSymbols("ind", "ack(0)")));
        printInfo(learner);

        learner.refineHypothesis(new DefaultQuery<>(Word.epsilon(),
                                                    Word.fromSymbols(new Msg<>(ID1, "d'"),
                                                                     new Recv(),
                                                                     new Msg<>(ID2, "d''")),
                                                    Word.fromSymbols("ind", "ack(0)", "ind")));
        printInfo(learner);
    }

    private static <AI, CI, O> void printInfo(GenericAAARLearnerMealy<? extends OTLearnerMealy<CI, O>, AI, CI, O> learner)
            throws IOException {

        System.out.println("-------------------------------------------------------");
        new ObservationTableASCIIWriter<>().write(learner.getLearner().getObservationTable(), System.out);

        if (DOT.checkUsable()) {
            final MealyMachine<?, AI, ?, O> hyp = learner.getHypothesisModel();

            try (StringWriter hypWriter = new StringWriter();
                 StringWriter treeWriter = new StringWriter()) {

                GraphDOT.write(hyp.transitionGraphView(learner.getAbstractAlphabet()), hypWriter);
                GraphDOT.write((GraphViewable) learner.getAbstractionTree(), treeWriter);

                DOT.renderDOTStrings(Arrays.asList(Pair.of("Hypothesis", hypWriter.toString()),
                                                   Pair.of("Abstraction Tree", treeWriter.toString())), true);
            }
        }
    }

    static final class EventAbstractor implements Function<Event, String> {

        private int cnt;

        @Override
        public String apply(Event event) {
            if (event instanceof Recv) {
                return "recv";
            } else if (event instanceof Msg) {
                return "msg" + cnt++;
            } else {
                throw new IllegalArgumentException("Unknown event: " + event);
            }
        }
    }
}
