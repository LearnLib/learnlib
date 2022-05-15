package de.learnlib.algorithms.aaar.abstraction;

import net.automatalib.words.Alphabet;

public class IdentityAbstraction<I> implements InitialAbstraction<I, I> {

    private final Alphabet<I> alphabet;

    public IdentityAbstraction(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Alphabet<I> getSigmaC() {
        return alphabet;
    }

    @Override
    public Alphabet<I> getSigmaA() {
        return alphabet;
    }

    @Override
    public I getAbstractSymbol(I c) {
        return c;
    }

    @Override
    public I getRepresentative(I a) {
        return a;
    }
}
