open module de.learnlib.filter.cache {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.incremental;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    // only required by documentation
    requires static de.learnlib.oracle.parallelism;

    exports de.learnlib.filter.cache;
    exports de.learnlib.filter.cache.dfa;
    exports de.learnlib.filter.cache.mealy;
    exports de.learnlib.filter.cache.moore;
    exports de.learnlib.filter.cache.sul;
}