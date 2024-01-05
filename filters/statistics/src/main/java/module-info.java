open module de.learnlib.filter.statistic {

    requires de.learnlib.api;
    requires net.automatalib.api;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.filter.statistic;
    exports de.learnlib.filter.statistic.learner;
    exports de.learnlib.filter.statistic.oracle;
    exports de.learnlib.filter.statistic.sul;
}