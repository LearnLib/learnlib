open module de.learnlib.algorithm.procedural {

    requires com.google.common;
    requires de.learnlib.algorithm.kv;
    requires de.learnlib.algorithm.lstar;
    requires de.learnlib.algorithm.observationpack;
    requires de.learnlib.algorithm.oml;
    requires de.learnlib.algorithm.ttt;
    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires de.learnlib.common.counterexample;
    requires de.learnlib.datastructure.observationtable;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.procedural;
    exports de.learnlib.algorithm.procedural.adapter.dfa;
    exports de.learnlib.algorithm.procedural.adapter.mealy;
    exports de.learnlib.algorithm.procedural.sba;
    exports de.learnlib.algorithm.procedural.sba.manager;
    exports de.learnlib.algorithm.procedural.spa;
    exports de.learnlib.algorithm.procedural.spa.manager;
    exports de.learnlib.algorithm.procedural.spmm;
    exports de.learnlib.algorithm.procedural.spmm.manager;
}