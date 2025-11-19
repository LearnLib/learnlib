package de.learnlib.filter.statistic.container;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A boolean flag that is unset by default and can be set.
 */
class FlagStatistic extends LearnerStatistic {
    private boolean flagged;

    public FlagStatistic(String id, @Nullable String description, boolean value) {
        super(id, description);
        this.flagged = value;
    }

    public void setFlag(boolean value) {
        this.flagged = value;
    }

    public boolean isFlagged() {
        return flagged;
    }
}
