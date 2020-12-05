package de.learnlib.algorithms.ostia;

class State {

    public void assign(State other) {
        out = other.out;
        transitions = other.transitions;
    }

    public Out out;
    public Edge[] transitions;

    State(int alphabetSize) {
        transitions = new Edge[alphabetSize];
    }

    State(State copy) {
        transitions = OSTIA.copyTransitions(copy.transitions);
        out = copy.out == null ? null : new Out(OSTIA.copyAndConcat(copy.out.str, null));
    }

    /**
     * The IntQueue is consumed and should not be reused after calling this method
     */
    void prepend(IntQueue prefix) {
        for (Edge edge : transitions) {
            if (edge != null) {
                edge.out = OSTIA.copyAndConcat(prefix, edge.out);
            }
        }
        if (out == null) {
            out = new Out(prefix);
        } else {
            out.str = OSTIA.copyAndConcat(prefix, out.str);
        }
    }

}
