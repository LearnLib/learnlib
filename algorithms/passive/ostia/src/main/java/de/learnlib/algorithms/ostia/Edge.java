package de.learnlib.algorithms.ostia;

class Edge {

    IntQueue out;
    State target;

    public Edge() {

    }

    public Edge(Edge edge) {
        out = OSTIA.copyAndConcat(edge.out, null);
        target = edge.target;
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
