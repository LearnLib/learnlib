open module de.learnlib.algorithm.oml {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.oml.lstar;
    exports de.learnlib.algorithm.oml.ttt;
    exports de.learnlib.algorithm.oml.ttt.dfa;
    exports de.learnlib.algorithm.oml.ttt.dt;
    exports de.learnlib.algorithm.oml.ttt.mealy;
    exports de.learnlib.algorithm.oml.ttt.pt;
    exports de.learnlib.algorithm.oml.ttt.st;
}