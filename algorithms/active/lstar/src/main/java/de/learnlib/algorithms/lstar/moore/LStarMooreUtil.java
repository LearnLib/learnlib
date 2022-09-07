package de.learnlib.algorithms.lstar.moore;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;

public class LStarMooreUtil {

    private LStarMooreUtil() {
        // prevent instantiation
    }

    public static <I> List<Word<I>> ensureSuffixCompliancy(List<Word<I>> suffixes) {
        List<Word<I>> compSuffixes = new ArrayList<>();
        compSuffixes.add(Word.epsilon());
        for (Word<I> suff : suffixes) {
            if (!suff.isEmpty()) {
                compSuffixes.add(suff);
            }
        }

        return compSuffixes;
    }

}