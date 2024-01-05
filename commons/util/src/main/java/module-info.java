open module de.learnlib.common.util {

    requires de.learnlib.api;
    requires de.learnlib.filter.statistic;
    requires net.automatalib.api;
    requires net.automatalib.core;
    requires net.automatalib.common.util;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    exports de.learnlib.util;
    exports de.learnlib.util.mealy;
    exports de.learnlib.util.moore;
    exports de.learnlib.util.nfa;
    exports de.learnlib.util.statistic;
}