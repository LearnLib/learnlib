package de.learnlib.algorithms.ostia;

class Blue {

    State parent;
    int symbol;

    State state() {
        return parent.transitions[symbol].target;
    }

    public Blue(State parent, int symbol) {
        this.symbol = symbol;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return state().toString();
    }
}
