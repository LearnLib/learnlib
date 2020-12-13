package net.automatalib.automata.transducers.impl.compact;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class CompactSST<I, O> extends UniversalCompactDet<I, Word<O>, Word<O>>
        implements MutableSubsequentialTransducer<Integer, I, CompactMealyTransition<Word<O>>, O> {

    public CompactSST(Alphabet<I> alphabet) {
        super(alphabet);
    }
}
