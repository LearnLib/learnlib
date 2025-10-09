package de.learnlib.algorithm.lstar.mmlt.cex;

import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.word.Word;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CexPreprocessor {

    private static final Logger logger = LoggerFactory.getLogger(CexPreprocessor.class);

    /**
     * Cuts-off a counterexample when its output deviates from the hypothesis output.
     *
     * @param cexQuery  Counterexample
     * @param hypAnswer Hypothesis response to suffix of the counterexample
     * @return The shortened counterexample, or null, if hypothesis and SUL show identical behavior.
     */
    public static <I, O> DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> truncateCEX(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> cexQuery,
                                                                                                                                Word<LocalTimerMealyOutputSymbol<O>> hypAnswer) {

        for (int i = 0; i < cexQuery.getSuffix().size(); i++) {
            if (!hypAnswer.getSymbol(i).equals(cexQuery.getOutput().getSymbol(i))) {
                // Cut after deviation:
                return new DefaultQuery<>(cexQuery.getPrefix(),
                        cexQuery.getSuffix().prefix(i + 1),
                        cexQuery.getOutput().prefix(i + 1));
            }
        }
        return null; // no deviation found
    }

}

