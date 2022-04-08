/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.algorithms.oml.ttt.st;



/**
 *
 * @author falk
 */
public class SuffixTrie<I> {

    private final STNode<I> epsilon = new STNodeImpl<>(null, null);
    
    public STNode<I> root() {
        return epsilon;
    }
}
