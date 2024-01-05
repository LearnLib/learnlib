open module de.learnlib.algorithm.lstar {

    requires de.learnlib.api;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.common.util;
    requires de.learnlib.datastructure.observationtable;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.algorithm.lstar;
    exports de.learnlib.algorithm.lstar.ce;
    exports de.learnlib.algorithm.lstar.closing;
    exports de.learnlib.algorithm.lstar.dfa;
    exports de.learnlib.algorithm.lstar.mealy;
    exports de.learnlib.algorithm.lstar.moore;
    exports de.learnlib.algorithm.malerpnueli;
    exports de.learnlib.algorithm.rivestschapire;

}