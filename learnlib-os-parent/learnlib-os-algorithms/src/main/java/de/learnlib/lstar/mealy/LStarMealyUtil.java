package de.learnlib.lstar.mealy;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;

public class LStarMealyUtil {
	
	public static <I> List<Word<I>> ensureSuffixCompliancy(List<Word<I>> suffixes, Alphabet<I> alphabet,
			boolean needsConsistencyCheck) {
		List<Word<I>> compSuffixes = new ArrayList<Word<I>>();
		if(needsConsistencyCheck) {
			for(int i = 0; i < alphabet.size(); i++)
				compSuffixes.add(Word.fromLetter(alphabet.getSymbol(i)));
		}
		
		for(Word<I> w : suffixes) {
			if(w.isEmpty())
				continue;
			if(needsConsistencyCheck && w.length() == 1)
				continue;
			compSuffixes.add(w);
		}
		
		return compSuffixes;
	}

}
