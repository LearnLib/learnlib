package de.learnlib.algorithms.discriminationtree;

import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealyBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.annotations.Test;

@Test
public class DTLearnerMealyIT extends AbstractMealyLearnerIT {

	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MealyMembershipOracle<I, O> mqOracle,
			MealyLearnerVariantList<I, O> variants) {
		DTLearnerMealyBuilder<I, O> builder = new DTLearnerMealyBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder : LocalSuffixFinders.values()) {
			builder.setSuffixFinder(suffixFinder);
			
			String name = "suffixFinder=" + suffixFinder.toString();
			variants.addLearnerVariant(name, builder.create());
		}
	}

}
