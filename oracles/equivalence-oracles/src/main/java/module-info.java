open module de.learnlib.oracle.equivalence {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.oracle.equivalence;
    exports de.learnlib.oracle.equivalence.mealy;
    exports de.learnlib.oracle.equivalence.sba;
    exports de.learnlib.oracle.equivalence.spa;
    exports de.learnlib.oracle.equivalence.spmm;
    exports de.learnlib.oracle.equivalence.vpa;
}