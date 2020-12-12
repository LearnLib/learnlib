package de.learnlib.algorithms.ostia;

import java.util.HashSet;

class IntQueue {

    int value;
    IntQueue next;

    public static String str(IntQueue q) {
        if(q==null)return "[]";
        StringBuilder sb = new StringBuilder("[").append(q.value);
        while(q.next!=null){
            q = q.next;
            sb.append(", ").append(q.value);
        }
        return sb.append("]").toString();
    }

    public static int lcpLen(IntQueue a, IntQueue b) {
        int len = 0;
        while (a != null && b != null && a.value==b.value) {
            len++;
            a = a.next;
            b = b.next;
        }
        return len;
    }

    /**offset(q,0) returns q, offset(q,1) returns q.next and so on*/
    public static IntQueue offset(IntQueue queue,int len) {
        while(len-->0){//it looks sort of like, "len approaches 0" which is neat
            queue = queue.next;
        }
        return queue;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(value);
        IntQueue next = this.next;
        while (next != null) {
            sb.append(" ").append(next.value);
            next = next.next;
        }
        return sb.toString();
    }

    public static int len(IntQueue q) {
        int len = 0;
        while (q != null) {
            len++;
            q = q.next;
        }
        return len;
    }

    public static int[] arr(IntQueue q) {
        final int[] arr = new int[len(q)];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = q.value;
            q = q.next;
        }
        return arr;
    }

    static boolean hasCycle(IntQueue q) {
        final HashSet<IntQueue> elements = new HashSet<>();
        while (q != null) {
            if (!elements.add(q)) {
                return true;
            }
            q = q.next;
        }
        return false;
    }

    static IntQueue copyAndConcat(IntQueue q, IntQueue tail) {
        assert !hasCycle(q) && !hasCycle(tail);
        if (q == null) {
            return tail;
        }
        final IntQueue root = new IntQueue();
        root.value = q.value;
        IntQueue curr = root;
        q = q.next;
        while (q != null) {
            curr.next = new IntQueue();
            curr = curr.next;
            curr.value = q.value;
            q = q.next;
        }
        curr.next = tail;
        assert !hasCycle(root);
        return root;
    }

    static IntQueue concat(IntQueue q, IntQueue tail) {
        assert !hasCycle(q) && !hasCycle(tail);
        if (q == null) {
            return tail;
        }
        final IntQueue first = q;
        while (q.next != null) {
            q = q.next;
        }
        q.next = tail;
        assert !hasCycle(first);
        return first;
    }

    static IntQueue concatAndCopy(IntQueue q, IntQueue tail) {
        return concat(q,copyAndConcat(tail,null));
    }
}
