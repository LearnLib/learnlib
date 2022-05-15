package de.learnlib.examples.aaar;

class Event {

    static class Msg<D> extends Event {

        final int seq;
        final D data;

        public Msg(int seq, D data) {
            this.seq = seq;
            this.data = data;
        }

        @Override
        public String toString() {
            return "msg(" + seq + ',' + data + ')';
        }
    }

    static class Recv extends Event {

        @Override
        public String toString() {
            return "recv";
        }
    }
}
