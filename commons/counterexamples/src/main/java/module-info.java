open module de.learnlib.common.counterexample {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.acex;
    exports de.learnlib.counterexample;
}