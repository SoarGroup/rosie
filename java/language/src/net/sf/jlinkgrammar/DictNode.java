package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class DictNode {
    /* The dictionary is stored as a binary tree comprised of the following   */
    /* nodes.  A list of these (via right pointers) is used to return         */
    /* the result of a dictionary lookup.                                     */

    String string; /* the word itself */
    WordFile file; /* the file the word came from (null if dict file) */
    Exp exp;
    DictNode left, right;

    DictNode() {

    }

    DictNode(DictNode d) {
        string = d.string;
        file = d.file;
        exp = d.exp;
        left = d.left;
        right = d.right;
    }

    Disjunct build_disjuncts_for_dict_node() {
        /* still need this for counting the number of disjuncts */
        Clause c;
        Disjunct dis;
        /*                 print_expression(dn.exp);   */
        /*                 printf("\n");                */
        c = exp.build_Clause(GlobalBean.NOCUTOFF);
        /*                 print_Clause_list(c);        */
        dis = Clause.build_disjunct(c, string, GlobalBean.NOCUTOFF);
        return dis;
    }

    static DictNode list_whole_dictionary(DictNode root, DictNode dn) {
        DictNode c, d;
        if (root == null)
            return dn;
        c = new DictNode(root);
        d = list_whole_dictionary(root.left, dn);
        c.right = list_whole_dictionary(root.right, d);
        return c;
    }

    boolean word_has_connector(String cs, int direction) {

        /* This function takes a dict_node (corresponding to an entry in a given dictionary), a
           string (representing a connector), and a direction (0 = right-pointing, 1 = left-pointing);
           it returns 1 if the dictionary expression for the word includes the connector, 0 otherwise.
           This can be used to see if a word is in a certain category (checking for a category 
           connector in a table), or to see if a word has a connector in a normal dictionary. The
           connector check uses a "smart-match", the same kind used by the parser. */

        Connector c2 = null;
        Disjunct d, d0;
        d0 = d = build_disjuncts_for_dict_node();
        if (d == null)
            return false;
        for (; d != null; d = d.next) {
            if (direction == 0)
                c2 = d.right;
            if (direction == 1)
                c2 = d.left;
            for (; c2 != null; c2 = c2.next) {
                if (easy_match(c2.string, cs)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean easy_match(String s, String t) {

        /* This is like the basic "match" function in count.c - the basic connector-matching 
           function used in parsing - except it ignores "priority" (used to handle fat links) */

        int i = 0;
        while (i < s.length()
            && i < t.length()
            && (Character.isUpperCase(s.charAt(i)) || Character.isUpperCase(t.charAt(i)))) {
            if (s.charAt(i) != t.charAt(i))
                return false;
            i++;
        }

        while (i < s.length() && i < t.length()) {
            if ((s.charAt(i) == '*')
                || (t.charAt(i) == '*')
                || ((s.charAt(i) == t.charAt(i)) && (s.charAt(i) != '^'))) {
                i++;
            } else
                return false;
        }
        return true;

    }

}
