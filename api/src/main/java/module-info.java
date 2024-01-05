open module de.learnlib.api {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    exports de.learnlib;
    exports de.learnlib.algorithm;
    exports de.learnlib.exception;
    exports de.learnlib.logging;
    exports de.learnlib.oracle;
    exports de.learnlib.query;
    exports de.learnlib.statistic;
    exports de.learnlib.sul;
}