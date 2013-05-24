package net.sf.jlinkgrammar;

/**
 * Domain type class holds the domain type information
 *
 */
public class DTypeList {
    /* The following three structs comprise what is returned by post_process(). */
    DTypeList next;
    int type;

    DTypeList() {

    }

    DTypeList(DTypeList d) {
        next = d.next;
        type = d.type;
    }
}
