package de.learnlib.query;

public interface AdaptiveQuery<I, O> {

    I getInput();

    Response processOutput(O out);

    enum Response {
        FINISHED,
        RESET,
        SYMBOL
    }
}
