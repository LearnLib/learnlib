package de.learnlib.algorithms.oml.ttt.pt;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import net.automatalib.words.Word;

public class PTNodeImpl<I> implements PTNode<I> {

    private final PTNodeImpl<I> parent;

    private final I symbol;

    private DTLeaf<I, ?> state;

    private final Map<I, PTNodeImpl<I>> children = new HashMap<>();

    public PTNodeImpl(PTNodeImpl<I> parent, I symbol) {
        this.parent = parent;
        this.symbol = symbol;
    }

    @Override
    public Word<I> word() {
        return toWord( Word.<I>epsilon() );
    }

    @Override
    public PTNode<I> append(I a) {
        assert !children.containsKey(a);
        PTNodeImpl<I> n = new PTNodeImpl<>(this, a);
        children.put(a, n);
        return n;
    }

    @Override
    public void setState(DTLeaf node) {
        this.state = node;
    }

    @Override
    public DTLeaf state() {
        return state;
    }

    private Word<I> toWord(Word<I> suffix) {
        if (symbol == null) {
            return suffix;
        }
        return parent.toWord(suffix.prepend(symbol));
    }

    @Override
    public PTNode<I> succ(I a) {
        return children.get(a);
    }

    @Override
    public void makeShortPrefix() {
        this.state.makeShortPrefix(this);
    }
}

