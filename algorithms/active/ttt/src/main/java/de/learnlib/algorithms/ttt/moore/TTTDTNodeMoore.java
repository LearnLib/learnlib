package de.learnlib.algorithms.ttt.moore;

import de.learnlib.algorithms.ttt.base.AbstractBaseDTNode;
import de.learnlib.algorithms.ttt.base.TTTState;

import java.util.HashMap;
import java.util.Map;

public class TTTDTNodeMoore<I, D> extends AbstractBaseDTNode<I, D> {

    public TTTDTNodeMoore() {
        this(null, null);
    }

    public TTTDTNodeMoore(AbstractBaseDTNode<I, D> parent, D parentEdgeLabel){
        super(parent, parentEdgeLabel);
    }

    @Override
    protected Map<D, AbstractBaseDTNode<I, D>> createChildMap() {
        return new HashMap<>();
    }

    @Override
    protected AbstractBaseDTNode<I, D> createChild(D outcome, TTTState<I, D> data) {
        return new TTTDTNodeMoore<>(this, outcome);
    }
}