/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.learnlib.algorithms.aaar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.learnlib.algorithms.aaar.abstraction.AbstractionTree;
import de.learnlib.algorithms.aaar.abstraction.InitialAbstraction;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.GrowingMapAlphabet;

/**
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        implements LearningAlgorithm<AM, CI, D> {

    private final L learner;
    private final InitialAbstraction<AI, CI> initial;
    private final MembershipOracle<CI, D> oracle;

    private final Map<AI, AbstractionTree<AI, CI, D>> evolving = new HashMap<>();

    private final GrowingAlphabet<CI> rep = new GrowingMapAlphabet<>();
    protected final GrowingAlphabet<AI> abs = new GrowingMapAlphabet<>();

    public AbstractAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider,
                               InitialAbstraction<AI, CI> initial,
                               MembershipOracle<CI, D> o) {
        this.initial = initial;
        this.oracle = o;

        for (AI a : initial.getSigmaA()) {
            rep.addSymbol(initial.getRepresentative(a));
            abs.addSymbol(a);
            evolving.put(a, new AbstractionTree<>(a, initial.getRepresentative(a), o));
        }

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

        for (int i = 0; i < input.size(); i++) {
            CI cur = input.getSymbol(i);
            AbstractionTree<AI, CI, D> tree = evolving.get(initial.getAbstractSymbol(cur));
            // lift & lower
            CI r = tree.getRepresentative(tree.getAbstractSymbol(cur));

            suffix = input.suffix(input.size() - i - 1);

            Word<CI> test1 = prefix.append(cur).concat(suffix);
            Word<CI> test2 = prefix.append(r).concat(suffix);

            D out1 = oracle.answerQuery(test1);
            D out2 = oracle.answerQuery(test2);

            if (out1.equals(out2)) {
                prefix = prefix.append(r);
                continue;
            }

            // add new abstraction
            AI a = tree.splitLeaf(r, cur, prefix, suffix, out2, out1);
            abs.addSymbol(a);
            rep.addSymbol(cur);
            evolving.put(a, tree);
            learner.addAlphabetSymbol(cur);
            return true;

        }

        // TODO: pass representatives to local learner
        return learner.refineHypothesis(query);
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
                AI a = evolving.get(initial.getAbstractSymbol(r)).getAbstractSymbol(r);
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

    public int getAlphabetSize() {
        int sum = 0;
        for (AbstractionTree<AI, CI, D> at : evolving.values()) {
            sum += at.countLeaves();
            return sum;
        }
        return sum;
    }

}
