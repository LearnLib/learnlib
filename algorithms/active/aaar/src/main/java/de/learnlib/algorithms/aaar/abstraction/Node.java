package de.learnlib.algorithms.aaar.abstraction;

import net.automatalib.words.Word;

class Node {

    static class InnerNode<CI, D> extends Node {

        Word<CI> prefix;
        Word<CI> suffix;
        D out;

        //private HashMap<Word,Node> next = new HashMap<Word, Node>();

        Node equalsNext;
        Node otherNext;

    }

    static class Leaf<AI, CI> extends Node {

        AI abs;
        CI rep;
    }
}
