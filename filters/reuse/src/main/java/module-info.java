open module de.learnlib.filter.reuse {

    requires de.learnlib.api;
    requires net.automatalib.api;
    requires org.checkerframework.checker.qual;

    requires static de.learnlib.tooling.annotation;

    exports de.learnlib.filter.reuse;
    exports de.learnlib.filter.reuse.tree;
}