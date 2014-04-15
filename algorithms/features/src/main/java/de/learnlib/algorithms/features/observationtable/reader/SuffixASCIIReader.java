package de.learnlib.algorithms.features.observationtable.reader;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuffixASCIIReader<I,O> implements ObservationTableReader<I,O> {

	private static final String WORD_DELIMITER = ";";
	private static final String SYMBOL_DELIMITER = ",";

	@Override
	public ObservationTable<I, O> read(String source, Alphabet<I> alphabet) {
		Map<String, I> nameToSymbol = generateNameToSymbolMap(alphabet);
		String[] suffixWords = source.split(WORD_DELIMITER);

		List<Word<I>> suffixes = new ArrayList<>(suffixWords.length);

		for (String suffixWord : suffixWords) {
			String[] symbolNames = suffixWord.split(SYMBOL_DELIMITER);
			Word<I> word = Word.epsilon();
			if (!suffixWord.isEmpty()) {
				for (String symbolName : symbolNames) {
					word = word.append(nameToSymbol.get(symbolName));
				}
			}
			suffixes.add(word);
		}

		return new SimpleObservationTable<>(suffixes);
	}

	private Map<String, I> generateNameToSymbolMap(Alphabet<I> alphabet) {
		Map<String, I> nameToSymbol = Maps.newHashMapWithExpectedSize(alphabet.size());

		for (I symbol : alphabet) {
			String symbolName = symbol.toString();
			if (nameToSymbol.containsKey(symbolName)) {
				throw new IllegalArgumentException(
						"Symbol name '" + symbolName + "' is used more than once in alphabet");
			}
			else {
				nameToSymbol.put(symbolName, symbol);
			}
		}

		return nameToSymbol;
	}

}
