package de.learnlib.algorithms.oml.ttt.mealy;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import net.automatalib.words.Word;

public class MealyTransition<I, O> {

    final DTLeaf<I, Word<O>> source;

    final I input;

    MealyTransition(DTLeaf<I, Word<O>> source, I input) {
        this.source = source;
        this.input = input;
    }
}
