/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.learnlib.algorithms.aaar.abstraction;

/**
 * @author fhowar
 * @author frohme
 */
public interface Abstraction<AI, CI> {

    AI getAbstractSymbol(CI c);

    CI getRepresentative(AI a);

}
