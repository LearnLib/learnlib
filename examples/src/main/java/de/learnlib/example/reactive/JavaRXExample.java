package de.learnlib.example.reactive;

import java.util.Objects;
import java.util.Random;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracle;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.query.DefaultQuery;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

public class JavaRXExample {

    public static void main(String[] args) {
        // setup symbols
        var inputs = Alphabets.integers(0, 4);
        var outputs = Alphabets.characters('a', 'd');

        // setup membership oracle
        var mealy = RandomAutomata.randomMealy(new Random(42), 10, inputs, outputs);
        var sul = new MealySimulatorSUL<>(mealy);
        // make sure to use parallel-aware oracle (with thread local instances)
        // because it will be called from different threads in the reactive environment
        var mqo = ParallelOracleBuilders.newDynamicParallelOracle(sul).create();

        // setup equivalence oracles
        var eqo = new RandomWMethodEQOracle<>(mqo, 2, 4);
        var eqo2 = new KWayStateCoverEQOracle<>(mqo, new Random(42), 2, 4, CombinationMethod.COMBINATIONS, 2);

        // setup learner
        var learner = new TTTLearnerMealy<>(inputs, mqo);
        var tracker = new ProgressTracker<>(learner, mqo);

        // learning loop
        learner.startLearning();
        var hyp = learner.getHypothesisModel();

        while (!tracker.hasFinished()) {
            final var finalHyp = hyp;
            Flowable.fromStream(eqo.generateTestWords(finalHyp, inputs))
                    .take(100)
                    .mergeWith(Flowable.fromStream(eqo2.generateTestWords(finalHyp, inputs)))
                    .parallel()
                    .runOn(Schedulers.computation())
                    .filter(w -> !Objects.equals(mqo.answerQuery(w), finalHyp.computeOutput(w)))
                    .sequential()
                    .firstElement()
                    .subscribe(tracker, System.out::println, tracker);
        }

        hyp = learner.getHypothesisModel();

        System.out.println(Automata.testEquivalence(mealy, hyp, inputs));
    }

    private static class ProgressTracker<I, D> implements Consumer<Word<I>>, Action {

        private boolean finished;
        private final LearningAlgorithm<?, I, D> learner;
        private final MembershipOracle<I, D> oracle;

        private ProgressTracker(LearningAlgorithm<?, I, D> learner, MembershipOracle<I, D> oracle) {
            this.learner = learner;
            this.oracle = oracle;
        }

        @Override
        public void accept(Word<I> w) {
            this.finished = learner.refineHypothesis(new DefaultQuery<>(w, oracle.answerQuery(w)));
        }

        public boolean hasFinished() {
            return finished;
        }

        @Override
        public void run() throws Throwable {
            this.finished = true;
        }
    }
}
