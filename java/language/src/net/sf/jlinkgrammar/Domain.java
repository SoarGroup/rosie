package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Domain {
    String string;
    int size;
    ListOfLinks lol;
    int start_link; /* the link that started this domain */
    int type; /* one letter name */
    DTreeLeaf child;
    Domain parent;

}
