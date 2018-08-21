package de.learnlib.datastructure.observationtable;

import net.automatalib.words.Alphabet;

/**
 * An Observation Table that supports resuming.
 *
 * @param <I> input symbol type
 * @param <D> output type
 */
public interface ResumeSupportingObservationTable<I, D> extends MutableObservationTable<I, D> {

    /**
     * This is an internal method used for de-serializing. Do not deliberately set input alphabets.
     *
     * @param alphabet
     *         the input alphabet corresponding to the previously serialized one.
     */
    void setInputAlphabet(Alphabet<I> alphabet);
}
