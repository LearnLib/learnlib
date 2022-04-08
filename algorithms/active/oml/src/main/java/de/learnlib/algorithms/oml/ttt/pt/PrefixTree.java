/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.oml.ttt.pt;

/**
 *
 * @author falk
 */
public class PrefixTree<I> {
    
    private final PTNodeImpl<I> epsilon = new PTNodeImpl<>(null, null);
    
    public PTNode<I> root() {
        return epsilon;
    }
    
}
