open module de.learnlib.datastructure.observationtable {

    requires java.desktop;
    requires com.google.common;
    requires de.learnlib.api;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.datastructure.observationtable;
    exports de.learnlib.datastructure.observationtable.reader;
    exports de.learnlib.datastructure.observationtable.writer;
}