package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class ConnectorSet {
    Connector hash_table[];
    int table_size;
    int is_defined; /* if 0 then there is no such set */

    int connector_set_hash(String s, int d) {
        /* This hash function only looks at the leading upper case letters of
           the string, and the direction, '+' or '-'.
        */
        int i = d;
        for (int j=0; j < s.length() && Character.isUpperCase(s.charAt(j)); j++)
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
        return (i & (table_size - 1));
    }

}
