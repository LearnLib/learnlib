package de.learnlib.algorithms.ostia;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.Arrays;

interface IntSeq {

    static <I> IntSeq of(Word<I> word, Alphabet<I> alphabet) {
        return new IntSeq() {
            @Override
            public int size() {
                return word.size();
            }

            @Override
            public int get(int index) {
                return alphabet.applyAsInt(word.getSymbol(index));
            }
        };
    }

    int size();

    int get(int index);

    static IntSeq seq(int... ints) {
        return new IntSeq() {

            @Override
            public int size() {
                return ints.length;
            }

            @Override
            public int get(int index) {
                return ints[index];
            }

            @Override
            public String toString() {
                return Arrays.toString(ints);
            }
        };
    }
}
