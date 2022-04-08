package de.learnlib.algorithms.oml.ttt.st;

import java.util.LinkedHashMap;
import java.util.Map;

import net.automatalib.words.Word;

class STNodeImpl<I> implements STNode<I> {

    private final STNodeImpl<I> parent;

    private final I symbol;

    private final Map<I, STNodeImpl> children = new LinkedHashMap<>();

    STNodeImpl(STNodeImpl<I> parent, I symbol) {
        this.parent = parent;
        this.symbol = symbol;
    }

    @Override
    public Word<I> word() {
        return toWord( Word.<I>epsilon() );
    }

    private Word<I> toWord(Word<I> prefix) {
        if (symbol == null) {
            return prefix;
        }
        return parent.toWord(prefix.append(symbol));
    }

    @Override
    public STNodeImpl<I> prepend(I a) {
        STNodeImpl<I> n = children.get(a);
        if (n == null) {
            n = new STNodeImpl<>(this, a);
            children.put(a, n);
        }
        return n;
    }
}

