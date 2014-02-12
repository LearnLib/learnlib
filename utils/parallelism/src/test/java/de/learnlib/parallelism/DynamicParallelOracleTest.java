package de.learnlib.parallelism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.parallelism.ParallelOracle.PoolPolicy;

import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DynamicParallelOracleTest {
	
	private static final class NullOracle implements MembershipOracle<Void,Void> {
		@Override
		public void processQueries(
				Collection<? extends Query<Void, Void>> queries) {
			for(Query<Void,Void> q : queries) {
				q.answer(null);
			}
		}
	}
	
	private static final class AnswerOnceQuery extends Query<Void, Void> {
		
		private final AtomicBoolean answered = new AtomicBoolean(false);

		@Override
		public void answer(Void output) {
			boolean wasAnswered = answered.getAndSet(true);
			if(wasAnswered) {
				throw new IllegalStateException("Query was already answered");
			}
		}

		@Override
		public Word<Void> getPrefix() {
			return Word.epsilon();
		}

		@Override
		public Word<Void> getSuffix() {
			return Word.epsilon();
		}
		
	}
	
	private static List<AnswerOnceQuery> createQueries(int numQueries) {
		List<AnswerOnceQuery> queries = new ArrayList<>(numQueries);
		
		for(int i = 0; i < numQueries; i++) {
			queries.add(new AnswerOnceQuery());
		}
		
		return queries;
	}
	

	
	@Test
	public void testDistinctQueries() {
		ParallelOracle<Void, Void> oracle
			= ParallelOracleBuilders.newDynamicParallelOracle(new NullOracle())
				.withBatchSize(1)
				.withPoolSize(4)
				.withPoolPolicy(PoolPolicy.CACHED)
				.create();
		
		try {
			List<AnswerOnceQuery> queries = createQueries(100);
			
			oracle.processQueries(queries);
			
			for(AnswerOnceQuery query : queries) {
				Assert.assertTrue(query.answered.get());
			}
		}
		finally {
			oracle.shutdown();
		}
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void testDuplicateQueries() {
		ParallelOracle<Void, Void> oracle
			= ParallelOracleBuilders.newDynamicParallelOracle(new NullOracle())
				.withBatchSize(1)
				.withPoolSize(4)
				.withPoolPolicy(PoolPolicy.CACHED)
				.create();
		try {
			List<AnswerOnceQuery> queries = new ArrayList<>(createQueries(100));
			queries.add(queries.get(0));
			
			oracle.processQueries(queries);
		}
		finally {
			oracle.shutdown();
		}
	}
	

}
