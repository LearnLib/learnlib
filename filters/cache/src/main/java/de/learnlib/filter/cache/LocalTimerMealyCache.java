package de.learnlib.filter.cache;

import de.learnlib.sul.LocalTimerMealySUL;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.word.Word;

import java.util.List;

/**
 * Abstract class for caches for {@link LocalTimerMealySUL}.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public abstract class LocalTimerMealyCache<I, O> extends LocalTimerMealySUL<I, O> {

    /**
     * Lists all words that are currently in the cache.
     *
     * @return List of all stored words.
     */
    public abstract List<Word<LocalTimerMealySemanticInputSymbol<I>>> listAllWords();

}
