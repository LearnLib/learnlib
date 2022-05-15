/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.learnlib.algorithms.aaar.abstraction;

import net.automatalib.words.Alphabet;

/**
 * @author fhowar
 * @author frohme
 */
public interface InitialAbstraction<AI, CI> extends Abstraction<AI, CI> {

    /**
     * @return the sigmaC
     */
    Alphabet<CI> getSigmaC();

    /**
     * @return the sigmaA
     */
     Alphabet<AI> getSigmaA();
}
