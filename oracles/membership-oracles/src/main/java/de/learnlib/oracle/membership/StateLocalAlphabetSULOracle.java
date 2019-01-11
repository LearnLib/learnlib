package de.learnlib.oracle.membership;

import java.util.Collection;

import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.exception.SULException;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.api.oracle.OutputAndLocalInputs;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class StateLocalAlphabetSULOracle<I, O> implements MealyMembershipOracle<I, OutputAndLocalInputs<I, O>> {

    private final StateLocalInputSUL<I, O> sul;
    private final ThreadLocal<StateLocalInputSUL<I, O>> localSul;

    public StateLocalAlphabetSULOracle(StateLocalInputSUL<I, O> sul) {
        this.sul = sul;
        if (sul.canFork()) {
            this.localSul = ThreadLocal.withInitial(sul::fork);
        } else {
            this.localSul = null;
        }
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<OutputAndLocalInputs<I, O>>>> queries) {
        if (localSul != null) {
            processQueries(localSul.get(), queries);
        } else {
            synchronized (sul) {
                processQueries(sul, queries);
            }
        }
    }

    public static <I, O> void processQueries(StateLocalInputSUL<I, O> sul,
                                             Collection<? extends Query<I, Word<OutputAndLocalInputs<I, O>>>> queries) {
        for (Query<I, Word<OutputAndLocalInputs<I, O>>> q : queries) {
            Word<OutputAndLocalInputs<I, O>> output = answerQuery(sul, q.getPrefix(), q.getSuffix());
            q.answer(output);
        }
    }

    public static <I, O> Word<OutputAndLocalInputs<I, O>> answerQuery(StateLocalInputSUL<I, O> sul,
                                                                      Word<I> prefix,
                                                                      Word<I> suffix) throws SULException {

        final WordBuilder<OutputAndLocalInputs<I, O>> wb = answerQueryPartial(sul, prefix, suffix);
        final int outputLen = suffix.length() + 1;

        while (wb.size() < outputLen) {
            wb.add(OutputAndLocalInputs.undefined());
        }

        return wb.toWord();
    }

    private static <I, O> WordBuilder<OutputAndLocalInputs<I, O>> answerQueryPartial(StateLocalInputSUL<I, O> sul,
                                                                                     Word<I> prefix,
                                                                                     Word<I> suffix)
            throws SULException {

        final WordBuilder<OutputAndLocalInputs<I, O>> wb = new WordBuilder<>(suffix.length() + 1);

        sul.pre();
        Collection<I> enabledInputs = sul.currentlyEnabledInputs();

        try {
            for (I sym : prefix) {
                if (!enabledInputs.contains(sym)) {
                    return wb;
                }

                sul.step(sym);
                enabledInputs = sul.currentlyEnabledInputs();
            }

            wb.add(new OutputAndLocalInputs<>(null, enabledInputs));

            for (I sym : suffix) {
                if (!enabledInputs.contains(sym)) {
                    return wb;
                }

                final O out = sul.step(sym);
                enabledInputs = sul.currentlyEnabledInputs();
                wb.add(new OutputAndLocalInputs<>(out, enabledInputs));
            }
        } finally {
            sul.post();
        }

        return wb;
    }
}
