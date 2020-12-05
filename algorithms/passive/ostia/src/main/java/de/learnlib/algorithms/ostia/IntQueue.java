package de.learnlib.algorithms.ostia;

import java.util.HashSet;

class IntQueue {

    int value;
    IntQueue next;

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
}
