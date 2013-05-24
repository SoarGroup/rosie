package net.sf.jlinkgrammar;

import java.util.Arrays;

/**
 * TODO add javadoc
 *
 */
public class LinksetSet {
    /* stores information for one 'instance' of pset */
    int hash_table_size;
    LinksetNode hash_table[]; /* data actually lives here */

    static LinksetSet ss[] = new LinksetSet[GlobalBean.LINKSET_MAX_SETS];
    static boolean q_unit_is_used[] = new boolean[GlobalBean.LINKSET_MAX_SETS];

    static void linkset_clear(int unit) {
        int i;
        if (!q_unit_is_used[unit])
            return;
        clear_hash_table(unit);
    }

    static void clear_hash_table(int unit) {
        Arrays.fill(ss[unit].hash_table, null);
    }

}
