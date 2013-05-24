package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class DISNode {
    CONList cl; /* the list of children */
    ListOfLinks lol; /* the links that comprise this region of the graph */
    int word; /* the word defining this node */

    boolean advance_DIS() {
        /* Cycically advance the current state of this DIS node.
           If it's now at the beginning of its cycle" return false;
           Otherwise return true;
        */
        CONList dcl;
        for (dcl = cl; dcl != null; dcl = dcl.next) {
            if (dcl.cn.advance_CON()) {
                return true;
            }
        }
        return false;
    }

}
