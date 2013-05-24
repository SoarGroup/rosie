package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Clause {
    Clause next;
    int cost;
    int maxcost;
    TConnector c;

    static Disjunct build_disjunct(Clause cl, String string, int cost_cutoff) {
        /* build a disjunct list out of the Clause list c */
        /* string is the print name of word that generated this disjunct */

        Disjunct dis, ndis;
        dis = null;
        for (; cl != null; cl = cl.next) {
            if (cl.maxcost <= cost_cutoff) {
                ndis = new Disjunct();
                ndis.left = Connector.reverse(extract_connectors(cl.c, '-'));
                ndis.right = Connector.reverse(extract_connectors(cl.c, '+'));
                ndis.string = string;
                ndis.cost = cl.cost;
                ndis.next = dis;
                dis = ndis;
            }
        }
        return dis;
    }

    static Connector extract_connectors(TConnector e, int c) {
        /* Build a new list of connectors starting from the TConnectors
           in the list pointed to by e.  Keep only those whose strings whose
           direction has the value c.
        */
        Connector e1;
        if (e == null)
            return null;
        if (e.dir == c) {
            e1 = new Connector();
            e1.init_connector();
            e1.next = extract_connectors(e.next, c);
            e1.multi = e.multi;
            e1.string = e.string;
            e1.label = GlobalBean.NORMAL_LABEL;
            e1.priority = GlobalBean.THIN_priority;
            e1.word = 0;
            return e1;
        } else {
            return extract_connectors(e.next, c);
        }
    }

}
