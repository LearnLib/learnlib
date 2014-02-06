package de.learnlib.algorithms.features.observationtable.reader;

import com.sun.istack.internal.NotNull;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Alphabet;

/**
 * Reads an {@link ObservationTable} from a string source.
 *
 * @param <I>
 * @param <O>
 */
public interface ObservationTableReader<I,O> {

	/**
	 * Reads the string representation of an observation table and returns an implementation
	 * of {@link ObservationTable} which allows access to short and long prefixes as well as
	 * suffixes.
	 *
	 * @param source
	 *      The string representation to read, must not be {@code null}.
	 * @param alphabet
	 *      The learning alphabet containing the symbols used.
	 *
	 * @return
	 *      an implementation of {@link ObservationTable} which allows at lease access to
	 *      {@link ObservationTable#getSuffixes()}, {@link ObservationTable#getShortPrefixes()},
	 *      and {@link ObservationTable#getLongPrefixes()}. Will never be {@code null}.
	 */
	@NotNull
	public ObservationTable<I,O> read(@NotNull String source, Alphabet<I> alphabet);

}
