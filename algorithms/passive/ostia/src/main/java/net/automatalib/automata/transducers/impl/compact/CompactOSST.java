package net.automatalib.automata.transducers.impl.compact;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class CompactOSST<I, O> extends UniversalCompactDet<I, Word<O>, Word<O>>
        implements MutableOnwardSubsequentialTransducer<Integer, I, CompactMealyTransition<Word<O>>, O> {

    public CompactOSST(Alphabet<I> alphabet) {
        super(alphabet);
    }

//    @Override
//    public void setTransitionProperty(CompactMealyTransition<Word<O>> transition, Word<O> property) {
//        super.setTransitionProperty(transition, property);
//    }
//
//    @Override
//    public void setTransition(int state, int input, int successor, Word<O> property) {
//        super.setTransition(state, input, successor, property);
//    }
}
