package de.learnlib.oracle.parallelism;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import org.checkerframework.checker.index.qual.NonNegative;

public class DynamicParallelAdaptiveOracle<I, O>
        extends AbstractDynamicBatchProcessor<AdaptiveQuery<I, O>, AdaptiveMembershipOracle<I, O>>
        implements ParallelAdaptiveOracle<I, O> {

    public DynamicParallelAdaptiveOracle(Supplier<? extends AdaptiveMembershipOracle<I, O>> oracleSupplier,
                                         @NonNegative int batchSize,
                                         ExecutorService executor) {
        super(oracleSupplier, batchSize, executor);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        processBatch(queries);
    }
}
