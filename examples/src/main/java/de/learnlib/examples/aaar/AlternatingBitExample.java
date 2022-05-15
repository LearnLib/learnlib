package de.learnlib.examples.aaar;

import java.util.Collections;

import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.ExplicitInitialAbstraction;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.examples.aaar.Event.Msg;
import de.learnlib.examples.aaar.Event.Recv;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * Example from the paper "Automata Learning with Automated Alphabet Abstraction Refinement" by Howar et al..
 */
public class AlternatingBitExample {

    public static void main(String[] args) {

        final Alphabet<Event> initialAlphabet = Alphabets.fromArray(new Msg<>(0, "d"), new Recv());

        final Protocol sul = new Protocol();
        final MembershipOracle<Event, Word<String>> mqo = new SimulatorOracle<>(sul);

        //        final ExtensibleLStarMealy<Event, String> lstar =
        //                new ExtensibleLStarMealyBuilder<Event, String>().withAlphabet(initialAlphabet).withOracle(mqo).create();

        LearnerProvider<ExtensibleLStarMealy<Event, String>, MealyMachine<?, Event, ?, String>, Event, Word<String>>
                lstar = (alph, orcl) -> new ExtensibleLStarMealyBuilder<Event, String>().withAlphabet(alph)
                                                                                        .withOracle(orcl)
                                                                                        .create();

        final ExplicitInitialAbstraction<String, Event> initialAbstraction =
                new ExplicitInitialAbstraction<>(initialAlphabet, Alphabets.fromArray("msg", "recv"));

        final AAARLearnerMealy<ExtensibleLStarMealy<Event, String>, String, Event, String> learner =
                new AAARLearnerMealy<>(lstar, initialAbstraction, mqo);

        learner.startLearning();

        MealyMachine<?, String, ?, String> hyp = learner.getHypothesisModel();

        System.out.println("-------------------------------------------------------");
        new ObservationTableASCIIWriter<>().write(learner.getLearner().getObservationTable(), System.out);
        Visualization.visualize(hyp, learner.getAbstractAlphabet());

        learner.refineHypothesis(new DefaultQuery<>(Word.epsilon(),
                                                    Word.fromSymbols(new Msg<>(72, "d'"),
                                                                     new Recv(),
                                                                     new Msg<>(73, "d''")),
                                                    Word.fromSymbols("ind", "ack(0)", "ind")));

        hyp = learner.getHypothesisModel();

        System.out.println("-------------------------------------------------------");
        new ObservationTableASCIIWriter<>().write(learner.getLearner().getObservationTable(), System.out);
        Visualization.visualize(hyp, learner.getAbstractAlphabet());
    }

}
