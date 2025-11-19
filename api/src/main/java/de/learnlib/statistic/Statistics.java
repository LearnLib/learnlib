package de.learnlib.statistic;

import java.util.ServiceLoader;

public class Statistics {

    private static final StatisticsProvider PROVIDER;

    static {
        final ServiceLoader<StatisticsProvider> loader = ServiceLoader.load(StatisticsProvider.class);

        StatisticsProvider bestProvider = new DummyProvider();
        for (StatisticsProvider sp : loader) {
            if (sp.getPriority() > bestProvider.getPriority()) {
                bestProvider = sp;
            }
        }

        PROVIDER = bestProvider;
    }

    public static StatsContainer getContainer() {
        return PROVIDER.getContainer();
    }

    private static class DummyProvider implements StatisticsProvider {

        private static final StatsContainer CONTAINER = new DummyStatsContainer();

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE;
        }

        @Override
        public StatsContainer getContainer() {
            return CONTAINER;
        }
    }

}
