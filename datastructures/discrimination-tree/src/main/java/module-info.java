open module de.learnlib.datastructure.discriminationtree {

    requires com.google.common;
    requires de.learnlib.api;
    requires de.learnlib.datastructure.list;
    requires net.automatalib.api;
    requires net.automatalib.common.smartcollection;
    requires net.automatalib.common.util;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;

    exports de.learnlib.datastructure.discriminationtree;
    exports de.learnlib.datastructure.discriminationtree.iterators;
    exports de.learnlib.datastructure.discriminationtree.model;
}