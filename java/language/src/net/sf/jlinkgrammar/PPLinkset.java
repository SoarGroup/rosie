package net.sf.jlinkgrammar;

import java.util.Arrays;

/**
 * TODO add javadoc
 *
 */
public class PPLinkset {
    int hash_table_size;
    int population;
    PPLinksetNode hash_table[]; /* data actually lives here */

    static PPLinkset PPLinkset_open(int size) {
        PPLinkset ls;
        if (size == 0)
            return null;
        ls = new PPLinkset();
        initialize(ls, size);
        return ls;
    }

    static void initialize(PPLinkset ls, int size) {
        ls.hash_table_size = size * GlobalBean.LINKSET_SPARSENESS;
        ls.population = 0;
        ls.hash_table = new PPLinksetNode[ls.hash_table_size];
        clear_hash_table(ls);
    }

    static int PPLinkset_add(PPLinkset ls, String str) {
        /* returns 0 if already there, 1 if new. Stores only the pointer */
        if (ls == null)
            throw new RuntimeException("PPLinkset internal error: Trying to add to a null set");
        if (add_internal(ls, str) == null)
            return 0;
        ls.population++;
        return 1;
    }

    static boolean PPLinkset_match(PPLinkset ls, String str) {
        /* Set query. Returns 1 if str pp-matches something in the set, 0 otherwise */
        int hashval;
        PPLinksetNode p;
        if (ls == null)
            return false;
        hashval = compute_hash(ls, str);
        p = ls.hash_table[hashval];
        while (p != null) {
            if (Postprocessor.post_process_match(p.str, str))
                return true;
            p = p.next;
        }
        return false;
    }

    static boolean PPLinkset_match_bw(PPLinkset ls, String str) {
        int hashval;
        PPLinksetNode p;
        if (ls == null)
            return false;
        hashval = compute_hash(ls, str);
        p = ls.hash_table[hashval];
        while (p != null) {
            if (Postprocessor.post_process_match(str, p.str))
                return true;
            p = p.next;
        }
        return false;
    }

    static int PPLinkset_population(PPLinkset ls) {
        return (ls == null) ? 0 : ls.population;
    }

    static PPLinksetNode add_internal(PPLinkset ls, String str) {
        PPLinksetNode p, n;
        int hashval;

        /* look for str (exactly) in linkset */
        hashval = compute_hash(ls, str);
        for (p = ls.hash_table[hashval]; p != null; p = p.next)
            if (p.str.equals(str))
                return null; /* already present */

        /* create a new node for u; stick it at head of linked list */
        n = new PPLinksetNode();
        n.next = ls.hash_table[hashval];
        n.str = str;
        ls.hash_table[hashval] = n;
        return n;
    }

    static int compute_hash(PPLinkset ls, String str) {
        /* hash is computed from capitalized prefix only */
        int i, hashval;
        hashval = GlobalBean.LINKSET_SEED_VALUE;
        for (i = 0; i < str.length() && Character.isUpperCase(str.charAt(i)); i++)
            hashval = str.charAt(i) + 31 * hashval;
        hashval = hashval % ls.hash_table_size;
        if (hashval < 0)
            hashval = -hashval;
        return hashval;
    }

    static void PPLinkset_clear(PPLinkset ls) {
        /* clear dangling linked lists, but retain hash table itself */
        if (ls == null)
            return;
        clear_hash_table(ls);
        ls.population = 0;
    }

    static void clear_hash_table(PPLinkset ls) {
        Arrays.fill(ls.hash_table, null);
    }

}
