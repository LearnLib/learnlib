package de.learnlib.algorithms.discriminationtree;

import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFABuilder;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;

import net.automatalib.words.Alphabet;

import org.testng.annotations.Test;

@Test
public class DTLearnerDFAIT extends AbstractDFALearnerIT {
	
	private static final boolean[] boolValues = { false, true };

	@Override
	protected <I> void addLearnerVariants(Alphabet<I> alphabet,
			DFAMembershipOracle<I> mqOracle, DFALearnerVariantList<I> variants) {
		DTLearnerDFABuilder<I> builder = new DTLearnerDFABuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(boolean epsilonRoot : boolValues) {
			builder.setEpsilonRoot(epsilonRoot);
			for(LocalSuffixFinder<? super I, ? super Boolean> suffixFinder : LocalSuffixFinders.values()) {
				builder.setSuffixFinder(suffixFinder);
				
				String name = "epsilonRoot=" + epsilonRoot + ",suffixFinder=" + suffixFinder.toString();
				variants.addLearnerVariant(name, builder.create());
			}
		}
	}
}
