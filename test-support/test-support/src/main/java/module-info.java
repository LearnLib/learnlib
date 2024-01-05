open module de.learnlib.testsupport {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires de.learnlib.driver.simulator;
    requires de.learnlib.oracle.membership;
    requires de.learnlib.testsupport.example;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;
    requires org.mockito;
    requires org.testng;
    requires xstream;

    exports de.learnlib.testsupport;
}