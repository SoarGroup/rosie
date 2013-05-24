package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Link {
    int l, r;
    Connector lc, rc;
    String name; /* spelling of full link name */

    void replace_link_name(String s) {
        name = s;
    }

}
