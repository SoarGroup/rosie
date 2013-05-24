package net.sf.jlinkgrammar;

/**
 * CNode is a standard tree data structure. The children of a 
 * node are stored as a linked list, with the end of the list 
 * indicated by next==NULL. The start and end fields of a node 
 * indicate the span of the constituent, with the first word 
 * indexed by 0. Leaves are defined by the condition child==NULL. 
 * There are three basic functions to work with the constituent structure:
 * 
 * 
 * CNode linkage_constituent_tree();
 * String linkage_print_constituent_tree(int mode);
 * 
 * The function linkage_constituent_tree returns a pointer to a tree;
 * 
 * In the function linkage_print_constituent_tree, the parameter mode=1 
 * specifies that the tree is displayed using the nested Lisp format, 
 * and mode=2 specifies that a flat tree is displayed using brackets. 
 * When mode=0, no constituent structure is generated and a null string 
 * is returned.
 * 
 * The string returned by a call to linkage_print_constituent_tree is
 * automatically freed by garbage collection.
 *
 */

public class CNode {
    String label;
    CNode child;
    CNode next;
    int start, end;

    CNode(String q) {
        label = q;
        child = next = null;
        next = null;
        start = end = -1;
    }
    
    /**
     * class debugging method
     */
    public String toString(CNode n) {
        return n.label;
        
    }
    
    public String toString() {
        return toString(this);
    }

}
