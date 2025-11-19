package de.learnlib.filter.statistic.container;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Various types of statistical data to be stored in a StatsContainer.
 */
abstract class LearnerStatistic {
    private final String id;
    private final String description;

    /**
     * Creates a new LearnerStatistic.
     *
     * @param id          Unique id of the statistic. Must be unique within the StatsContainer.
     * @param description Optional description of the statistic. If no description is provided, the id is used.
     */
    public LearnerStatistic(String id, @Nullable String description) {
        this.id = id;

        if (description == null) {
            this.description = id;
        } else {
            this.description = description;
        }
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}
