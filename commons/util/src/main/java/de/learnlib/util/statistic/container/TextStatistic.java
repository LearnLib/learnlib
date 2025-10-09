package de.learnlib.util.statistic.container;

import org.checkerframework.checker.nullness.qual.Nullable;

class TextStatistic extends LearnerStatistic {
    private final String text;

    public TextStatistic(String id, @Nullable String description, String text) {
        super(id, description);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
