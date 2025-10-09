package de.learnlib.algorithm.lstar.mmlt.cex;

import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.oracle.AbstractTimedQueryOracle;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * An abstract counterexample used by the MMLT learner.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyInconsPrefixTransformAcex<I, O> extends AbstractBaseCounterexample<Word<LocalTimerMealyOutputSymbol<O>>> {

    private final static Logger logger = LoggerFactory.getLogger(LocalTimerMealyInconsPrefixTransformAcex.class);

    private final AbstractTimedQueryOracle<I, O> timeOracle;
    private final Word<LocalTimerMealySemanticInputSymbol<I>> suffix;

    private final Function<Word<LocalTimerMealySemanticInputSymbol<I>>, Word<LocalTimerMealySemanticInputSymbol<I>>> asTransform;

    /**
     * Constructor.
     *
     * @param suffix      suffix of the counterexample (= the word that we analyze)
     * @param timeOracle  membership oracle
     * @param asTransform retrieves the prefix of the system state in the hypothesis addressed by a word
     */
    public LocalTimerMealyInconsPrefixTransformAcex(Word<LocalTimerMealySemanticInputSymbol<I>> suffix, AbstractTimedQueryOracle<I, O> timeOracle, Function<Word<LocalTimerMealySemanticInputSymbol<I>>, Word<LocalTimerMealySemanticInputSymbol<I>>> asTransform) {
        super(suffix.length());
        this.timeOracle = timeOracle;
        this.suffix = suffix;
        this.asTransform = asTransform;
    }

    public Function<Word<LocalTimerMealySemanticInputSymbol<I>>, Word<LocalTimerMealySemanticInputSymbol<I>>> getAsTransform() {
        return asTransform;
    }

    @Override
    public Word<LocalTimerMealyOutputSymbol<O>> computeEffect(int index) {
        // Split the word at our index:
        Word<LocalTimerMealySemanticInputSymbol<I>> prefix = this.suffix.prefix(index); // everything up to *index* (exclusive)
        Word<LocalTimerMealySemanticInputSymbol<I>> suffix = this.suffix.subWord(index); // everything from *index* (inclusive)

        // Identify access sequence of system state for prefix:
        Word<LocalTimerMealySemanticInputSymbol<I>> accessSequence = this.asTransform.apply(prefix);

        // Query *hypothesis state* + *suffix*:
        return this.timeOracle.querySuffixOutput(accessSequence, suffix);
    }


    @Override
    public boolean checkEffects(Word<LocalTimerMealyOutputSymbol<O>> eff1, Word<LocalTimerMealyOutputSymbol<O>> eff2) {
        // Same behavior at different indices?
        logger.debug(String.format("Comparing (%s) AND (%s): %s", eff1, eff2, eff2.isSuffixOf(eff1)));
        return eff2.isSuffixOf(eff1);
    }
}