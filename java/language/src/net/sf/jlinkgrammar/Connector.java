package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Connector {
    int label;
    /* The nearest word to my left (or right) that
       this could connect to.  Computed by power pruning */
    int word;
    /* If this is a length limited connector, this
    gives the limit of the length of the link
    that can be used on this connector.  Since
    this is strictly a funcion of the connector
    name, efficiency is the only reason to store
    this.  If no limit, the value is set to 255. */
    int length_limit;
    int priority; /* one of the three priorities above */
    boolean multi; /* true if this is a multi-connector */
    Connector next;
    String string;

    Connector(Connector c) {
        label = c.label;
        word = c.word;
        length_limit = c.length_limit;
        priority = c.priority;
        multi = c.multi;
        next = c.next;
        string = c.string;
    }

    Connector() {
    }

    Connector init_connector() {
        length_limit = GlobalBean.UNLIMITED_LEN;
        /*    c.my_word = NO_WORD;  */ /* mark it unset, to make sure it gets set later */
        return this;
    }

    static Connector reverse(Connector e) {
        /* reverse the order of the list e.  destructive */
        Connector head, x;
        head = null;
        while (e != null) {
            x = e.next;
            e.next = head;
            head = e;
            e = x;
        }
        return head;
    }

    boolean connectors_equal_prune(Connector c2) {
        /* The connectors must be exactly equal.  A similar function
           is connectors_equal_AND(), but that ignores priorities,
           this does not.
        */
        return label == c2.label && multi == c2.multi && priority == c2.priority && string.equals(c2.string);
    }

    boolean connector_types_equal(Connector c2) {
        /* Two connectors are said to be of the same type if they have
           the same label, and the initial upper case letters of their
           strings match.
        */
        String s, t;
        if (label != c2.label)
            return false;
        s = string;
        t = c2.string;
        int i = 0;
        while (i < s.length()
            && i < t.length()
            && (Character.isUpperCase(s.charAt(i)) || Character.isUpperCase(t.charAt(i)))) {
            if (s.charAt(i) != t.charAt(i))
                return false;
            i++;
        }
        return true;
    }

    boolean connectors_equal_AND(Connector c2) {
        /* Two connectors are said to be equal if they are of the same type
           (defined above), they have the same multi field, and they have
           exactly the same connectors (including lower case chars).
           (priorities ignored).
        */
        return label == c2.label && multi == c2.multi && string.equals(c2.string);
    }

    boolean match_in_connector_set(Sentence sent, ConnectorSet conset, int d) {
        /* Returns true the given connector is in this conset.  false otherwise.
           d='+' means this connector is on the right side of the disjunct.
           d='-' means this connector is on the left side of the disjunct.
        */
        int h;
        Connector c1;
        if (conset == null)
            return false;
        h = conset.connector_set_hash(string, d);
        for (c1 = conset.hash_table[h]; c1 != null; c1 = c1.next) {
            if (x_match(sent, c1, this) && (d == c1.word))
                return true;
        }
        return false;
    }

    int and_connector_hash(int i) {
        /* This hash function that takes a connector and a seed value i.
           It only looks at the leading upper case letters of
           the string, and the label.  This ensures that if two connectors
           match, then they must hash to the same place. 
        */
        String s;
        s = string;

        i = i + (i << 1) + MyRandom.randtable[(label + i) & (GlobalBean.RTSIZE - 1)];
        int j = 0;
        while (j < s.length() && Character.isUpperCase(s.charAt(j))) {
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
            j++;
        }
        return (i & (GlobalBean.HT_SIZE - 1));
    }

    static boolean x_match(Sentence sent, Connector a, Connector b) {
        return match(sent, a, b, 0, 0);
    }

    static boolean match(Sentence sent, Connector a, Connector b, int aw, int bw) {
        /* 
           Returns true if s and t match according to the connector matching
           rules.  The connector strings must be properly formed, starting with
           zero or more upper case letters, followed by some other letters, and
           The algorithm is symmetric with respect to a and b.
        
           It works as follows:  The labels must match.  The priorities must be
           compatible (both THIN_priority, or one UP_priority and one DOWN_priority).
           The sequence of upper case letters must match exactly.  After these comes
           a sequence of lower case letters "*"s or "^"s.  The matching algorithm
           is different depending on which of the two priority cases is being
           considered.  See the comments below.  */
        String s, t;
        int x, y, dist;
        if (a.label != b.label)
            return false;
        x = a.priority;
        y = b.priority;

        s = a.string;
        t = b.string;
        int i = 0;

        while (i < s.length()
            && i < t.length()
            && (Character.isUpperCase(s.charAt(i)) || Character.isUpperCase(t.charAt(i)))) {
            if (s.charAt(i) != t.charAt(i))
                return false;
            i++;
        }
        if ((i < s.length() && Character.isUpperCase(s.charAt(i)))
            || (i < t.length() && Character.isUpperCase(t.charAt(i)))) {
            return false;
        }

        if (aw == 0 && bw == 0) { /* probably not necessary, as long as effective_dist[0][0]=0 and is defined */
            dist = 0;
        } else {
            if (!(aw < bw)) {
                throw new RuntimeException("match() did not receive params in the natural order.");
            }
            dist = sent.effective_dist[aw][bw];
        }
        /*    printf("M: a=%4s b=%4s  ap=%d bp=%d  aw=%d  bw=%d  a.ll=%d b.ll=%d  dist=%d\n",
           s, t, x, y, aw, bw, a.length_limit, b.length_limit, dist); */
        if (dist > a.length_limit || dist > b.length_limit)
            return false;

        if ((x == GlobalBean.THIN_priority) && (y == GlobalBean.THIN_priority)) {
            /*
               Remember that "*" matches anything, and "^" matches nothing
               (except "*").  Otherwise two characters match if and only if
               they're equal.  ("^" can be used in the dictionary just like
               any other connector.)
               */
            while (i < s.length() && i < t.length()) {
                if ((s.charAt(i) == '*')
                    || (t.charAt(i) == '*')
                    || ((s.charAt(i) == t.charAt(i)) && (s.charAt(i) != '^'))) {
                    i++;
                } else
                    return false;
            }
            return true;
        } else if ((x == GlobalBean.UP_priority) && (y == GlobalBean.DOWN_priority)) {
            /*
               As you go up (namely from x to y) the set of strings that
               match (in the normal THIN sense above) should get no larger.
               Read the comment in and.c to understand this.
               In other words, the y string (t) must be weaker (or at least
               no stronger) that the x string (s).
            
               This code is only correct if the strings are the same
               length.  This is currently true, but perhaps for safty
               this assumption should be removed.
               */
            while (i < s.length() && i < t.length()) {
                if ((s.charAt(i) == t.charAt(i)) || (s.charAt(i) == '*') || (t.charAt(i) == '^')) {
                    i++;
                } else
                    return false;
            }
            return true;
        } else if ((y == GlobalBean.UP_priority) && (x == GlobalBean.DOWN_priority)) {
            while (i < s.length() && i < t.length()) {
                if ((s.charAt(i) == t.charAt(i)) || (t.charAt(i) == '*') || (s.charAt(i) == '^')) {
                    i++;
                } else
                    return false;
            }
            return true;
        } else
            return false;
    }

    static boolean x_prune_match(Sentence sent, Connector a, Connector b) {
        return prune_match(sent, a, b, 0, 0);
    }

    static boolean prune_match(Sentence sent, Connector a, Connector b, int aw, int bw) {
        /* This is almost identical to match().  Its reason for existance
           is the rather subtle fact that with "and" can transform a "Ss"
           connector into "Sp".  This means that in order for pruning to
           work, we must allow a "Ss" connector on word match an "Sp" connector
           on a word to its right.  This is what this version of match allows.
           we assume that a is on a word to the left of b.
        */
        String s, t;
        int x, y, dist;
        if (a.label != b.label)
            return false;
        x = a.priority;
        y = b.priority;

        s = a.string;
        t = b.string;

        int i = 0;
        while (i < s.length()
            && i < t.length()
            && (Character.isUpperCase(s.charAt(i)) || Character.isUpperCase(t.charAt(i)))) {
            if (s.charAt(i) != t.charAt(i))
                return false;
            i++;
        }
        if ((i < s.length() && Character.isUpperCase(s.charAt(i)))
            || (i < t.length() && Character.isUpperCase(t.charAt(i)))) {
            return false;
        }

        if (aw == 0 && bw == 0) { /* probably not necessary, as long as effective_dist[0][0]=0 and is defined */
            dist = 0;
        } else {
            if (!(aw < bw)) {
                throw new RuntimeException("prune_match() did not receive params in the natural order.");
            }
            dist = sent.effective_dist[aw][bw];
        }
        /*    printf("PM: a=%4s b=%4s  ap=%d bp=%d  aw=%d  bw=%d  a.ll=%d b.ll=%d  dist=%d\n", 
           s, t, x, y, aw, bw, a.length_limit, b.length_limit, dist); */
        if (dist > a.length_limit || dist > b.length_limit) {
            return false;
        }

        if ((x == GlobalBean.THIN_priority) && (y == GlobalBean.THIN_priority)) {
            //    if PLURALIZATION
            /*
                    if ((*(a.string)=='S') && ((*s=='s') || (*s=='p')) &&  (*t=='p')) {
                    return true;
                }
            */
            /*
               The above is a kludge to stop pruning from killing off disjuncts
               which (because of pluralization in and) might become valid later.
               Recall that "and" converts a singular subject into a plural one.
               The (*s=='p') part is so that "he and I are good" doesn't get killed off.
               The above hack is subsumed by the following one:
            */
            if (i < s.length()
                && i < t.length()
                && a.string.charAt(0) == 'S'
                && ((s.charAt(i) == 's') || (s.charAt(i) == 'p'))
                && ((t.charAt(i) == 'p') || (t.charAt(i) == 's'))
                && (i == 1 || (i == 2 && s.charAt(i - 1) == 'I'))) {
                return true;
            }
            /*
               This change is to accommodate "nor".  In particular we need to
               prevent "neither John nor I likes dogs" from being killed off.
               We want to allow this to apply to "are neither a dog nor a cat here"
               and "is neither a dog nor a cat here".  This uses the "SI" connector.
               The third line above ensures that the connector is either "S" or "SI".
            */
            //     end PLURALIZATION
            while (i < s.length() && i < t.length()) {
                if (s.charAt(i) == '*' || t.charAt(i) == '*' || (s.charAt(i) == t.charAt(i) && s.charAt(i) != '^')) {
                    /* this last case here is rather obscure.  It prevents
                       '^' from matching '^'.....Is this necessary?
                    ......yes, I think it is.   */
                    i++;
                } else {
                    return false;
                }
            }
            return true;
        } else if ((x == GlobalBean.UP_priority) && (y == GlobalBean.DOWN_priority)) {
            while (i < s.length() && i < t.length()) {
                if (s.charAt(i) == t.charAt(i) || s.charAt(i) == '*' || t.charAt(i) == '^') {
                    /* that '^' should match on the DOWN_priority
                    node is subtle, but correct */
                    i++;
                } else {
                    return false;
                }
            }
            return true;
        } else if ((y == GlobalBean.UP_priority) && (x == GlobalBean.DOWN_priority)) {
            while (i < s.length() && i < t.length()) {
                if (s.charAt(i) == t.charAt(i) || t.charAt(i) == '*' || s.charAt(i) == '^') {
                    i++;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    static Connector copy_connectors(Connector c) {
        /* This builds a new copy of the connector list pointed to by c.
           Strings, as usual, are not copied.
        */
        Connector c1;
        if (c == null)
            return null;
        c1 = new Connector(c);
        c1.next = copy_connectors(c.next);
        return c1;
    }

}
