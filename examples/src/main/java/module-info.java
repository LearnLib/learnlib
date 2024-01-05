open module de.learnlib.example {

    requires java.desktop;
    requires de.learnlib.algorithm.ttt;
    requires de.learnlib.api;
    requires de.learnlib.algorithm.aaar;
    requires de.learnlib.algorithm.lstar;
    requires de.learnlib.algorithm.rpni;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure.observationtable;
    requires de.learnlib.driver;
    requires de.learnlib.driver.simulator;
    requires de.learnlib.filter.cache;
    requires de.learnlib.filter.reuse;
    requires de.learnlib.filter.statistic;
    requires de.learnlib.oracle.emptiness;
    requires de.learnlib.oracle.equivalence;
    requires de.learnlib.oracle.membership;
    requires de.learnlib.oracle.parallelism;
    requires de.learnlib.oracle.property;
    requires de.learnlib.testsupport.example;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.modelchecker.ltsmin;
    requires net.automatalib.util;
    requires net.automatalib.serialization.dot;
    requires net.automatalib.visualization.dot;
    requires org.checkerframework.checker.qual;
    requires xstream;

    exports de.learnlib.example;
    exports de.learnlib.example.aaar;
    exports de.learnlib.example.bbc;
    exports de.learnlib.example.parallelism;
    exports de.learnlib.example.passive;
    exports de.learnlib.example.resumable;
    exports de.learnlib.example.sli;
}