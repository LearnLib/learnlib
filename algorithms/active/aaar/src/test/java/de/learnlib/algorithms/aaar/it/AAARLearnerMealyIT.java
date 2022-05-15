package de.learnlib.algorithms.aaar.it;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.IdentityAbstraction;
import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class AAARLearnerMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {

        LearnerProvider<DTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMealy<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        LearnerProvider<TTTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMealy<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);

        variants.addLearnerVariant("DT", new AAARLearnerMealy<>(dt, new IdentityAbstraction<>(alphabet), mqOracle));
        variants.addLearnerVariant("TTT", new AAARLearnerMealy<>(ttt, new IdentityAbstraction<>(alphabet), mqOracle));
    }
}
