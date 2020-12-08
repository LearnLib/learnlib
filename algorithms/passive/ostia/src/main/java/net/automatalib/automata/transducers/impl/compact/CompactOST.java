package net.automatalib.automata.transducers.impl.compact;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class CompactOST<I, O> extends UniversalCompactDet<I, Word<O>, Word<O>>
        implements SequentialTransducer<Integer, I, CompactMealyTransition<Word<O>>, O> {

    public CompactOST(Alphabet<I> alphabet) {
        super(alphabet);
    }
}
