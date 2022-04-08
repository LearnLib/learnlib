package de.learnlib.algorithms.oml.ttt.st;

import net.automatalib.words.Word;

public interface STNode<I> {

    Word<I> word();

    STNodeImpl<I> prepend(I a);

}
