/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.examples.aaar;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.examples.aaar.Event.Msg;
import de.learnlib.examples.aaar.Event.Recv;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.dot.DOTPanel;
import net.automatalib.words.Word;

/**
 * Example from the paper "Automata Learning with Automated Alphabet Abstraction Refinement" by Howar et al.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class AlternatingBitExample {

    private static final int ID1 = 72;
    private static final int ID2 = 73;

    private AlternatingBitExample() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException {

        final Protocol sul = new Protocol();
        final MembershipOracle<Event, Word<String>> mqo = new SimulatorOracle<>(sul);

        LearnerProvider<ExtensibleLStarMealy<Event, String>, MealyMachine<?, Event, ?, String>, Event, Word<String>>
                lstar = (alph, orcl) -> new ExtensibleLStarMealyBuilder<Event, String>().withAlphabet(alph)
                                                                                        .withOracle(orcl)
                                                                                        .create();

        final AAARLearnerMealy<ExtensibleLStarMealy<Event, String>, String, Event, String> learner =
                new AAARLearnerMealy<>(lstar, mqo, new Recv(), new EventAbstractor());

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

    private static <AI, CI, O> void printInfo(AAARLearnerMealy<? extends OTLearnerMealy<CI, O>, AI, CI, O> learner)
            throws IOException {

        System.out.println("-------------------------------------------------------");
        new ObservationTableASCIIWriter<>().write(learner.getLearner().getObservationTable(), System.out);

        final MealyMachine<?, AI, ?, O> hyp = learner.getHypothesisModel();

        final DOTDialog frame = new DOTDialog();

        frame.addGraph("Hypothesis", graphViewableToString(hyp.transitionGraphView(learner.getAbstractAlphabet())));
        frame.addGraph("Abstraction Tree", graphViewableToString(learner.getAbstractionTree()));

        frame.setVisible(true);
    }

    private static String graphViewableToString(Graph<?, ?> gv) throws IOException {

        final StringWriter writer = new StringWriter();

        GraphDOT.write(gv, writer);

        return writer.toString();
    }

    private static class EventAbstractor implements Function<Event, String> {

        private int cnt = 0;

        @Override
        public String apply(Event event) {
            if (event instanceof Recv) {
                return "recv";
            } else if (event instanceof Msg) {
                return "msg" + cnt++;
            } else {
                throw new IllegalArgumentException("unknown event" + event);
            }
        }
    }

    static class DOTDialog extends JDialog {

        private final DOTPanel dotPanel = new DOTPanel();

        public DOTDialog() {
            super((Dialog) null, true);
            setContentPane(dotPanel);

            JMenu menu = new JMenu("File");
            menu.add(dotPanel.getSavePngAction());
            menu.add(dotPanel.getSaveDotAction());
            menu.add(dotPanel.getRenameAction());
            menu.addSeparator();
            menu.add(new AbstractAction("Close") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DOTDialog.this.dispatchEvent(new WindowEvent(DOTDialog.this, WindowEvent.WINDOW_CLOSING));
                }
            });
            JMenuBar jMenuBar = new JMenuBar();
            jMenuBar.add(menu);
            setJMenuBar(jMenuBar);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
        }

        public void addGraph(String name, String dotText) {
            dotPanel.addGraph(name, dotText);
        }

    }
}
