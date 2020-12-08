package de.learnlib.algorithms.ostia;

import java.util.Arrays;

interface IntSeq {

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
