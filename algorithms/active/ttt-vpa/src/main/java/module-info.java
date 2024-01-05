open module de.learnlib.algorithm.ttt.vpa {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.algorithm.observationpack.vpa;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.datastructure.discriminationtree;
    requires net.automatalib.api;
    requires org.checkerframework.checker.qual;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.ttt.vpa;
}