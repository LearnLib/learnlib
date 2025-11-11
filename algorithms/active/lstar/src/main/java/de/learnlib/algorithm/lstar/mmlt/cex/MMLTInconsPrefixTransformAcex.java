package de.learnlib.algorithm.lstar.mmlt.cex;

import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.oracle.TimedQueryOracle;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
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
public class MMLTInconsPrefixTransformAcex<I, O> extends AbstractBaseCounterexample<Word<TimedOutput<O>>> {

    private final static Logger logger = LoggerFactory.getLogger(MMLTInconsPrefixTransformAcex.class);

    private final TimedQueryOracle<I, O> timeOracle;
    private final Word<TimedInput<I>> suffix;

    private final Function<Word<TimedInput<I>>, Word<TimedInput<I>>> asTransform;

    /**
     * Constructor.
     *
     * @param suffix      suffix of the counterexample (= the word that we analyze)
     * @param timeOracle  membership oracle
     * @param asTransform retrieves the prefix of the system state in the hypothesis addressed by a word
     */
    public MMLTInconsPrefixTransformAcex(Word<TimedInput<I>> suffix, TimedQueryOracle<I, O> timeOracle, Function<Word<TimedInput<I>>, Word<TimedInput<I>>> asTransform) {
        super(suffix.length());
        this.timeOracle = timeOracle;
        this.suffix = suffix;
        this.asTransform = asTransform;
    }

    public Function<Word<TimedInput<I>>, Word<TimedInput<I>>> getAsTransform() {
        return asTransform;
    }

    @Override
    public Word<TimedOutput<O>> computeEffect(int index) {
        // Split the word at our index:
        Word<TimedInput<I>> prefix = this.suffix.prefix(index); // everything up to *index* (exclusive)
        Word<TimedInput<I>> suffix = this.suffix.subWord(index); // everything from *index* (inclusive)

        // Identify access sequence of system state for prefix:
        Word<TimedInput<I>> accessSequence = this.asTransform.apply(prefix);

        // Query *hypothesis state* + *suffix*:
        return this.timeOracle.answerQuery(accessSequence, suffix);
    }


    @Override
    public boolean checkEffects(Word<TimedOutput<O>> eff1, Word<TimedOutput<O>> eff2) {
        // Same behavior at different indices?
        logger.debug(String.format("Comparing (%s) AND (%s): %s", eff1, eff2, eff2.isSuffixOf(eff1)));
        return eff2.isSuffixOf(eff1);
    }
}