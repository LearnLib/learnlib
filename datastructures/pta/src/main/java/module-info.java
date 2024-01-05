open module de.learnlib.datastructure.pta {

    requires java.desktop;
    requires com.google.common;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.datastructure.pta;
    exports de.learnlib.datastructure.pta.config;
    exports de.learnlib.datastructure.pta.wrapper;
}