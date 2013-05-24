package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class ListOfLinks {
    int link;       /* the link number */
    int word;       /* the word at the other end of this link */
    int dir;        /* 0: undirected, 1: away from me, -1: toward me */
    ListOfLinks next;

}
