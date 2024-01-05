open module de.learnlib.algorithm.adt {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.adt.ads;
    exports de.learnlib.algorithm.adt.adt;
    exports de.learnlib.algorithm.adt.api;
    exports de.learnlib.algorithm.adt.automaton;
    exports de.learnlib.algorithm.adt.config;
    exports de.learnlib.algorithm.adt.learner;
    exports de.learnlib.algorithm.adt.model;
    exports de.learnlib.algorithm.adt.util;
}