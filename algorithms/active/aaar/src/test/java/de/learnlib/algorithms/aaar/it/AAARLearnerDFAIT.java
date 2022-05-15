package de.learnlib.algorithms.aaar.it;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.aaar.AAARLearnerDFA;
import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.IdentityAbstraction;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class AAARLearnerDFAIT extends AbstractDFALearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqOracle,
                                          DFALearnerVariantList<I> variants) {

        LearnerProvider<DTLearnerDFA<I>, DFA<?, I>, I, Boolean> dt =
                (mqo, alph) -> new DTLearnerDFA<>(alphabet, mqOracle, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
        LearnerProvider<TTTLearnerDFA<I>, DFA<?, I>, I, Boolean> ttt =
                (mqo, alph) -> new TTTLearnerDFA<>(alphabet, mqOracle, AcexAnalyzers.BINARY_SEARCH_FWD);

        variants.addLearnerVariant("DT", new AAARLearnerDFA<>(dt, new IdentityAbstraction<>(alphabet), mqOracle));
        variants.addLearnerVariant("TTT", new AAARLearnerDFA<>(ttt, new IdentityAbstraction<>(alphabet), mqOracle));
    }
}
