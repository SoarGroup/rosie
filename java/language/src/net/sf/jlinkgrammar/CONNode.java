package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class CONNode {
    DISList dl; /* the list of children */
    DISList current; /* defines the current child */
    int word; /* the word defining this node */

    boolean advance_CON() {
        /* Cycically advance the current state of this CON node.
           If it's now at the beginning of its cycle return false;
           Otherwise return true;
        */
        if (current.dn.advance_DIS()) {
            return true;
        } else {
            if (current.next == null) {
                current = dl;
                return false;
            } else {
                current = current.next;
                return true;
            }
        }
    }

}
