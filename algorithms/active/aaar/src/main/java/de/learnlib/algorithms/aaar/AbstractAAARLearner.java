/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.learnlib.algorithms.aaar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import de.learnlib.algorithms.aaar.abstraction.AbstractionTree;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.GrowingMapAlphabet;

/**
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        implements LearningAlgorithm<AM, CI, D> {

    private final L learner;
    private final MembershipOracle<CI, D> oracle;

    private final AbstractionTree<AI, CI, D> tree;

    private final GrowingAlphabet<CI> rep;
    protected final GrowingAlphabet<AI> abs;

    public AbstractAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider,
                               MembershipOracle<CI, D> o,
                               CI initialConcrete,
                               Function<CI, AI> abstractor) {
        this.oracle = o;
        this.rep = new GrowingMapAlphabet<>();
        this.abs = new GrowingMapAlphabet<>();

        final AI initialAbstract = abstractor.apply(initialConcrete);
        this.tree = new AbstractionTree<>(initialAbstract, initialConcrete, o, abstractor);
        this.rep.addSymbol(initialConcrete);
        this.abs.addSymbol(initialAbstract);

        this.learner = learnerProvider.createLearner(rep, oracle);
    }

    @Override
    public void startLearning() {
        learner.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<CI, D> query) {

        Word<CI> input = query.getInput();
        Word<CI> prefix = Word.epsilon();
        Word<CI> suffix;

        WordBuilder<CI> wb = new WordBuilder<>(input.size());

        for (int i = 0; i < input.size(); i++) {
            CI cur = input.getSymbol(i);
            //            AbstractionTree<AI, CI, D> tree = evolving.get(initial.getAbstractSymbol(cur));
            // lift & lower
            AI a = tree.getAbstractSymbol(cur);
            CI r = tree.getRepresentative(a);

            suffix = input.suffix(input.size() - i - 1);

            Word<CI> test1 = prefix.append(cur).concat(suffix);
            Word<CI> test2 = prefix.append(r).concat(suffix);

            D out1 = oracle.answerQuery(test1);
            D out2 = oracle.answerQuery(test2);

            if (out1.equals(out2)) {
                prefix = prefix.append(r);
                wb.append(r);
                continue;
            }

            // add new abstraction
            AI newA = tree.splitLeaf(r, cur, prefix, suffix, out2, out1);
            abs.addSymbol(newA);
            rep.addSymbol(cur);
            learner.addAlphabetSymbol(cur);
            return true;
        }

        final DefaultQuery<CI, D> concreteCE = new DefaultQuery<>(wb.toWord(0, query.getPrefix().length()),
                                                                  wb.toWord(query.getPrefix().length(), wb.size()),
                                                                  query.getOutput());
        return learner.refineHypothesis(concreteCE);
    }

    public CM getConcreteHypothesisModel() {
        return learner.getHypothesisModel();
    }

    protected <S1, S2, SP, TP> void copyAbstract(UniversalDeterministicAutomaton<S1, CI, ?, SP, TP> src,
                                                 MutableDeterministic<S2, AI, ?, SP, TP> tgt) {
        // states
        Map<S2, S1> states = new HashMap<>();
        Map<S1, S2> statesRev = new HashMap<>();

        for (S1 s : src.getStates()) {
            final SP sp = src.getStateProperty(s);
            final S2 n = tgt.addState(sp);
            tgt.setInitial(n, Objects.equals(s, src.getInitialState()));

            states.put(n, s);
            statesRev.put(s, n);
        }

        // transitions
        for (S2 s : states.keySet()) {
            for (CI r : rep) {
                AI a = tree.getAbstractSymbol(r);
                tgt.setTransition(s,
                                  a,
                                  statesRev.get(src.getSuccessor(states.get(s), r)),
                                  src.getTransitionProperty(states.get(s), r));
            }
        }
    }

    public L getLearner() {
        return this.learner;
    }

    public Alphabet<AI> getAbstractAlphabet() {
        return this.abs;
    }

    public AbstractionTree<AI, CI, D> getAbstractionTree() {
        return tree;
    }
}
