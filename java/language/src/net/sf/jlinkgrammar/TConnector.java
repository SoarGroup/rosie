package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 * Temporary connectors used while converting expressions into disjunct lists
 *
 */
public class TConnector {
    boolean multi; /* true if this is a multi-connector */
    char dir; /* '-' for left and '+' for right */
    TConnector next;
    String string;

    TConnector(TConnector e) {
        multi = e.multi;
        dir = e.dir;
        next = e.next;
        string = e.string;
    }

    TConnector(Exp e) {
        multi = e.multi;
        dir = e.dir;
        next = null;
        string = e.string;
    }

    static TConnector catenate(TConnector e1, TConnector e2) {
        /* Builds a new list of connectors that is the catenation of e1 with e2.
           does not effect lists e1 or e2.   Order is maintained. */

        TConnector e, head;
        head = null;
        for (; e1 != null; e1 = e1.next) {
            e = new TConnector(e1);
            e.next = head;
            head = e;
        }
        for (; e2 != null; e2 = e2.next) {
            e = new TConnector(e2);
            e.next = head;
            head = e;
        }
        return Treverse(head);
    }

    static private TConnector Treverse(TConnector e) {
        /* reverse the order of the list e.  destructive */
        TConnector head, x;
        head = null;
        while (e != null) {
            x = e.next;
            e.next = head;
            head = e;
            e = x;
        }
        return head;
    }

}
