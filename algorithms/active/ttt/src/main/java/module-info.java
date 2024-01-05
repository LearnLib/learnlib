open module de.learnlib.algorithm.ttt {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure.discriminationtree;
    requires de.learnlib.datastructure.list;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.ttt.base;
    exports de.learnlib.algorithm.ttt.dfa;
    exports de.learnlib.algorithm.ttt.mealy;
    exports de.learnlib.algorithm.ttt.moore;
}