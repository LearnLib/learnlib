open module de.learnlib.algorithm.kv {

    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure.discriminationtree;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.kv;
    exports de.learnlib.algorithm.kv.dfa;
    exports de.learnlib.algorithm.kv.mealy;
}