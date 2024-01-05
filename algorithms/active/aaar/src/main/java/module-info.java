open module de.learnlib.algorithm.aaar {

    requires com.google.common;
    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.algorithm.aaar;
    exports de.learnlib.algorithm.aaar.abstraction;
    exports de.learnlib.algorithm.aaar.explicit;
    exports de.learnlib.algorithm.aaar.generic;
}