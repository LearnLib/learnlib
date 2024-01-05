open module de.learnlib.algorithm.dhc {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.dhc.mealy;
}