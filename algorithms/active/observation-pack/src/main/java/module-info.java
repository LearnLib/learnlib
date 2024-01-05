open module de.learnlib.algorithm.observationpack {

    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure.discriminationtree;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.observationpack;
    exports de.learnlib.algorithm.observationpack.dfa;
    exports de.learnlib.algorithm.observationpack.hypothesis;
    exports de.learnlib.algorithm.observationpack.mealy;
    exports de.learnlib.algorithm.observationpack.moore;
}