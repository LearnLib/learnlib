/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.learnlib.algorithms.aaar.abstraction;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import net.automatalib.words.Alphabet;

/**
 * @author fhowar
 * @author frohme
 */
public class ExplicitInitialAbstraction<AI, CI> implements InitialAbstraction<AI, CI> {

    private final Alphabet<CI> sigmaC;
    private final Alphabet<AI> sigmaA;

    private final Map<CI, AI> alpha = new HashMap<>();
    private final Map<AI, CI> gamma = new HashMap<>();

    public ExplicitInitialAbstraction(Alphabet<CI> sigmaC, Alphabet<AI> sigmaA) {
        Preconditions.checkArgument(sigmaC.size() == sigmaA.size());

        this.sigmaC = sigmaC;
        this.sigmaA = sigmaA;

        for (int i = 0; i < sigmaC.size(); i++) {
            final AI ai = sigmaA.getSymbol(i);
            final CI ci = sigmaC.getSymbol(i);

            alpha.put(ci, ai);
            gamma.put(ai, ci);
        }
    }

    @Override
    public Alphabet<CI> getSigmaC() {
        return sigmaC;
    }

    @Override
    public Alphabet<AI> getSigmaA() {
        return sigmaA;
    }

    @Override
    public AI getAbstractSymbol(CI c) {
        return alpha.get(c);
    }

    @Override
    public CI getRepresentative(AI a) {
        return gamma.get(a);
    }

}
