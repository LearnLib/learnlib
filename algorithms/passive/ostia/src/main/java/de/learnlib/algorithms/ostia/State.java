package de.learnlib.algorithms.ostia;

import net.automatalib.commons.util.Pair;

class State {

    public void assign(State other) {
        out = other.out;
        transitions = other.transitions;
    }

    public Out out;
    public Edge[] transitions;

    State(int alphabetSize) {
        transitions = new Edge[alphabetSize];
    }

    State(State copy) {
        transitions = OSTIA.copyTransitions(copy.transitions);
        out = copy.out == null ? null : new Out(IntQueue.copyAndConcat(copy.out.str, null));
    }

    /**
     * The IntQueue is consumed and should not be reused after calling this method
     */
    void prepend(IntQueue prefix) {
        for (Edge edge : transitions) {
            if (edge != null) {
                edge.out = IntQueue.copyAndConcat(prefix, edge.out);
            }
        }
        if (out == null) {
            out = new Out(prefix);
        } else {
            out.str = IntQueue.copyAndConcat(prefix, out.str);
        }
    }

    /**
     * The IntQueue is consumed and should not be reused after calling this method
     */
    void prependButIgnoreMissingStateOutput(IntQueue prefix) {
        for (Edge edge : transitions) {
            if (edge != null) {
                edge.out = IntQueue.copyAndConcat(prefix, edge.out);
            }
        }
        if (out != null) {
            out.str = IntQueue.copyAndConcat(prefix, out.str);
        }
    }


    IntQueue dequeueLongestCommonPrefix() {
        Out lcp = out;
        int len = out==null?-1:IntQueue.len(out.str);
        for (Edge outgoing : transitions) {
            if(outgoing==null)continue;
            if (lcp == null){
                lcp = new Out(outgoing.out);
                len = IntQueue.len(outgoing.out);
            }else{
                len = Math.min(len,IntQueue.lcpLen(lcp.str,outgoing.out));
            }
        }
        if(lcp==null||len==0)return null;
        assert len>0;
        IntQueue dequeuedLcp = lcp.str;
        if(out!=null){
            out.str = IntQueue.offset(out.str,len);
        }
        for (Edge outgoing : transitions) {
            if (outgoing != null){
                outgoing.out = IntQueue.offset(outgoing.out,len);
            }
        }
        IntQueue.offset(dequeuedLcp,len-1).next = null;
        return dequeuedLcp;
    }

}
