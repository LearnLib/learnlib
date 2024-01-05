open module de.learnlib.algorithm.observationpack.vpa {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.datastructure.discriminationtree;
    requires de.learnlib.datastructure.list;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.observationpack.vpa;
    exports de.learnlib.algorithm.observationpack.vpa.hypothesis;
}