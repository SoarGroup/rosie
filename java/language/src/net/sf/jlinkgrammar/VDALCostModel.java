package net.sf.jlinkgrammar;

import java.util.Comparator;

/**
 * TODO add javadoc
 *
 */
public class VDALCostModel implements Comparator {
    public int compare(Object o1, Object o2) {
        /* for sorting the linkages in postprocessing */
        LinkageInfo p1 = (LinkageInfo)o1;
        LinkageInfo p2 = (LinkageInfo)o2;
        if (p1.N_violations != p2.N_violations) {
            return (p1.N_violations - p2.N_violations);
        } else if (p1.unused_word_cost != p2.unused_word_cost) {
            return (p1.unused_word_cost - p2.unused_word_cost);
        } else if (p1.disjunct_cost != p2.disjunct_cost) {
            return (p1.disjunct_cost - p2.disjunct_cost);
        } else if (p1.and_cost != p2.and_cost) {
            return (p1.and_cost - p2.and_cost);
        } else {
            return (p1.link_cost - p2.link_cost);
        }
    }
}
