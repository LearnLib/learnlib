open module de.learnlib.testsupport.it {

    requires de.learnlib.api;
    requires de.learnlib.common.util;
    requires de.learnlib.driver.simulator;
    requires de.learnlib.oracle.membership;
    requires de.learnlib.oracle.equivalence;
    requires de.learnlib.testsupport.example;
    requires net.automatalib.api;
    requires net.automatalib.core;
    requires net.automatalib.util;
    requires org.slf4j;
    requires org.testng;

    exports de.learnlib.testsupport.it.learner;
}