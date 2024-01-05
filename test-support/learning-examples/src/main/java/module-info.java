open module de.learnlib.testsupport.example {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.learnlibv2;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    exports de.learnlib.testsupport.example;
    exports de.learnlib.testsupport.example.dfa;
    exports de.learnlib.testsupport.example.mealy;
    exports de.learnlib.testsupport.example.moore;
    exports de.learnlib.testsupport.example.sba;
    exports de.learnlib.testsupport.example.spa;
    exports de.learnlib.testsupport.example.spmm;
    exports de.learnlib.testsupport.example.sst;
    exports de.learnlib.testsupport.example.vpa;
}