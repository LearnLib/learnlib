package de.learnlib.algorithms.oml.ttt.pt;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import net.automatalib.words.Word;

public interface PTNode<I> {

    Word<I> word();

    PTNode<I> append(I a);

    void setState(DTLeaf node);

    DTLeaf state();

    PTNode<I> succ(I a);

    void makeShortPrefix();
}
