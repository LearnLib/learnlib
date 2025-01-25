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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithm.aaar.abstraction.ExplicitAbstractionTree;
import de.learnlib.algorithm.aaar.explicit.ExplicitAAARLearnerMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.example.aaar.Event.Msg;
import de.learnlib.example.aaar.Event.Recv;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.Graph;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.dot.DOT;
import net.automatalib.word.Word;

/**
 * Example from the paper "Automata Learning with Automated Alphabet Abstraction Refinement" by Howar et al., which uses
 * the explicit version of the AAAR learner.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class AlternatingBitExampleExplicit {

    private static final int ID1 = 72;
    private static final int ID2 = 73;

    private AlternatingBitExampleExplicit() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {

        final Protocol mqo = new Protocol();

        final LearnerConstructor<ExtensibleLStarMealy<Event, String>, Event, Word<String>> lstar =
                (alph, mq) -> new ExtensibleLStarMealyBuilder<Event, String>().withAlphabet(alph)
                                                                              .withOracle(mq)
                                                                              .create();

        final ExplicitAAARLearnerMealy<ExtensibleLStarMealy<Event, String>, String, Event, String> learner =
                new ExplicitAAARLearnerMealy<>(lstar, mqo, new InitialAbstraction(), new Incrementor());

        learner.startLearning();
        printInfo(learner);

        // Since we directly start with the two concrete symbols, this counterexample does not refine the hypothesis
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

    private static <AI, CI, O> void printInfo(ExplicitAAARLearnerMealy<? extends OTLearnerMealy<CI, O>, AI, CI, O> learner)
            throws IOException {

        System.out.println("-------------------------------------------------------");
        new ObservationTableASCIIWriter<>().write(learner.getLearner().getObservationTable(), System.out);

        if (DOT.checkUsable()) {
            final MealyMachine<?, AI, ?, O> hyp = learner.getHypothesisModel();
            final Map<AI, ExplicitAbstractionTree<AI, CI, Word<O>>> trees = learner.getAbstractionTrees();
            final List<Graph<?, ?>> treesAsGraphs = new ArrayList<>(trees.size());

            for (ExplicitAbstractionTree<AI, CI, Word<O>> t : trees.values()) {
                treesAsGraphs.add(t.graphView());
            }

            try (StringWriter hypWriter = new StringWriter();
                 StringWriter treeWriter = new StringWriter()) {

                GraphDOT.write(hyp.transitionGraphView(learner.getAbstractAlphabet()), hypWriter);
                GraphDOT.write(treesAsGraphs, treeWriter);

                DOT.renderDOTStrings(Arrays.asList(Pair.of("Hypothesis", hypWriter.toString()),
                                                   Pair.of("Abstraction Trees", treeWriter.toString())), true);
            }
        }
    }

    static final class InitialAbstraction implements ExplicitInitialAbstraction<String, Event> {

        @Override
        public String getAbstractSymbol(Event c) {
            if (c instanceof Recv) {
                return "recv";
            } else if (c instanceof Msg) {
                return "msg";
            } else {
                throw new IllegalArgumentException("Unknown event: " + c);
            }
        }

        @Override
        public Event getRepresentative(String a) {
            switch (a) {
                case "recv":
                    return new Recv();
                case "msg":
                    return new Msg<>(0, "d");
                default:
                    throw new IllegalArgumentException("Unknown abstract: " + a);
            }
        }

        @Override
        public Collection<String> getInitialAbstracts() {
            return Arrays.asList("recv", "msg");
        }
    }

    static final class Incrementor implements Function<String, String> {

        private int cnt;

        @Override
        public String apply(String s) {
            return s + cnt++;
        }
    }
}
