package net.automatalib.automata.transducers.impl.compact;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.words.Word;

public interface MutableSubsequentialTransducer<S, I, T, O>
        extends SubsequentialTransducer<S, I, T, O>, MutableDeterministic<S, I, T, Word<O>, Word<O>> {}
