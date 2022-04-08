package de.learnlib.algorithms.oml.lstar;

import net.automatalib.words.Word;

interface Hypothesis<M, I, D> {

    int size();

    D[] rowForState(Word<I> input);

    D getOutput(Word<I> input, int length);

    M getModel();
}
