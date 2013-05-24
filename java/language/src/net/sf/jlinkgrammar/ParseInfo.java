package net.sf.jlinkgrammar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * TODO add javadoc
 *
 */
public class ParseInfo {
    int x_table_size;
    XTableConnector x_table[];
    ParseSet parse_set;
    int N_words;
    Disjunct chosen_disjuncts[] = new Disjunct[GlobalBean.MAX_SENTENCE];
    int N_links;
    Link link_array[] = new Link[GlobalBean.MAX_LINKS];

    void initialize_links() {
        int i;
        N_links = 0;
        for (i = 0; i < N_words; ++i) {
            chosen_disjuncts[i] = null;
        }
    }

    void list_links(ParseSet set, int index) {
        ParseChoice pc;
        int n;

        if (set == null || set.first == null)
            return;
        for (pc = set.first; pc != null; pc = pc.next) {
            n = pc.set[0].count * pc.set[1].count;
            if (index < n)
                break;
            index -= n;
        }
        if (pc == null) {
            throw new RuntimeException("walked off the end in list_links");
        }
        issue_links_for_choice(pc);
        list_links(pc.set[0], index % pc.set[0].count);
        list_links(pc.set[1], index / pc.set[0].count);
    }

    void list_random_links(ParseSet set) {
        ParseChoice pc;
        int num_pc, new_index;

        if (set == null || set.first == null)
            return;
        num_pc = 0;
        for (pc = set.first; pc != null; pc = pc.next) {
            num_pc++;
        }

        new_index = MyRandom.my_random() % num_pc;

        num_pc = 0;
        for (pc = set.first; pc != null; pc = pc.next) {
            if (new_index == num_pc)
                break;
            num_pc++;
        }

        if (pc == null) {
            throw new RuntimeException("Couldn't get a random parse choice");
        }
        issue_links_for_choice(pc);
        list_random_links(pc.set[0]);
        list_random_links(pc.set[1]);
    }

    void issue_links_for_choice(ParseChoice pc) {
        if (pc.link[0].lc != null) { /* there is a link to generate */
            issue_link(pc.ld, pc.md, pc.link[0]);
        }
        if (pc.link[1].lc != null) {
            issue_link(pc.md, pc.rd, pc.link[1]);
        }
    }

    void issue_link(Disjunct ld, Disjunct rd, Link link) {
        if (N_links > GlobalBean.MAX_LINKS - 1) {
            throw new RuntimeException("Too many links");
        }
        link_array[N_links] = link;
        N_links++;

        chosen_disjuncts[link.l] = ld;
        chosen_disjuncts[link.r] = rd;
    }

    int link_cost() {
        /* computes the cost of the current parse of the current sentence */
        /* due to the length of the links                                 */
        int lcost, i;
        lcost = 0;
        for (i = 0; i < N_links; i++) {
            lcost += cost_for_length(link_array[i].r - link_array[i].l);
        }
        return lcost;
    }

    int null_cost() {
        /* computes the number of null links in the linkage */
        /* No one seems to care about this -- ALB */
        return 0;
    }

    int unused_word_cost() {
        int lcost, i;
        lcost = 0;
        for (i = 0; i < N_words; i++)
            if (chosen_disjuncts[i] == null) {
                lcost++;
            }
        return lcost;
    }

    int disjunct_cost() {
        /* computes the cost of the current parse of the current sentence     */
        /* due to the cost of the chosen disjuncts                            */
        int lcost, i;
        lcost = 0;
        for (i = 0; i < N_words; i++) {
            if (chosen_disjuncts[i] != null)
                lcost += chosen_disjuncts[i].cost;
        }
        return lcost;
    }

    private int cost_for_length(int length) {
        /* this function defines the cost of a link as a function of its length */
        return length - 1;
    }

    void build_digraph() {
        /* Constructs a graph in the word_links array based on the contents of    */
        /* the global link_array.  Makes the word_links array point to a list of  */
        /* words neighboring each word (actually a list of links).  This is a     */
        /* directed graph, constructed for dealing with "and".  For a link in     */
        /* which the priorities are UP or DOWN_priority, the edge goes from the   */
        /* one labeled DOWN to the one labeled UP.                                */
        /* Don't generate links edges for the bogus comma connectors              */
        int i, link, N_fat;
        Link lp;
        ListOfLinks lol;
        N_fat = 0;
        for (i = 0; i < N_words; i++) {
            word_links[i] = null;
        }
        for (link = 0; link < N_links; link++) {
            lp = link_array[link];
            i = lp.lc.label;
            if (i < GlobalBean.NORMAL_LABEL) { /* one of those special links for either-or, etc */
                continue;
            }
            lol = new ListOfLinks();
            lol.next = word_links[lp.l];
            word_links[lp.l] = lol;
            lol.link = link;
            lol.word = lp.r;
            i = lp.lc.priority;
            if (i == GlobalBean.THIN_priority) {
                lol.dir = 0;
            } else if (i == GlobalBean.DOWN_priority) {
                lol.dir = 1;
            } else {
                lol.dir = -1;
            }
            lol = new ListOfLinks();
            lol.next = word_links[lp.r];
            word_links[lp.r] = lol;
            lol.link = link;
            lol.word = lp.l;
            i = lp.rc.priority;
            if (i == GlobalBean.THIN_priority) {
                lol.dir = 0;
            } else if (i == GlobalBean.DOWN_priority) {
                lol.dir = 1;
            } else {
                lol.dir = -1;
            }
        }
    }

    DISNode build_DIS_CON_tree() {
        int xw, w;
        DISNode dnroot, dn;
        CONList child, xchild;
        ListOfLinks lol, xlol;

        /* The algorithm used here to build the DIS_CON tree depends on
           the search percolating down from the "top" of the tree.  The
           original version of this started its search at the wall.  This
           was fine because doing a DFS from the wall explore the tree in
           the right order.
        
           However, in order to handle null links correctly, a more careful
           ordering process must be used to explore the tree.  We use
           dfs_height[] for this.
         */

        for (w = 0; w < N_words; w++)
            dfs_height[w] = 0;
        for (w = 0; w < N_words; w++)
            height_dfs(w, GlobalBean.MAX_SENTENCE);

        for (w = 0; w < N_words; w++)
            height_perm[w] = new Integer(w);
        Arrays.sort(height_perm, 0,N_words, new Comparator() {
            public int compare(Object o1, Object o2) {
                return dfs_height[((Integer)o2).intValue()] - dfs_height[((Integer)o1).intValue()];
            }
        });

        for (w = 0; w < N_words; w++)
            dfs_root_word[w] = -1;

        dnroot = null;
        for (xw = 0; xw < N_words; xw++) {
            w = height_perm[xw].intValue();
            if (dfs_root_word[w] == -1) {
                dn = build_DISNode(w);
                if (dnroot == null) {
                    dnroot = dn;
                } else {
                    for (child = dn.cl; child != null; child = xchild) {
                        xchild = child.next;
                        child.next = dnroot.cl;
                        dnroot.cl = child;
                    }
                    for (lol = dn.lol; lol != null; lol = xlol) {
                        xlol = lol.next;
                        lol.next = dnroot.lol;
                        dnroot.lol = lol;
                    }
                }
            }
        }
        return dnroot;
    }

    boolean verify_set() {
        XTableConnector t;
        int i;
        boolean overflowed;

        overflowed = false;
        if (x_table == null) {
            throw new RuntimeException("called verify_set when x_table==null");
        }
        for (i = 0; i < x_table_size; i++) {
            for (t = x_table[i]; t != null; t = t.next) {
                overflowed = (overflowed || verify_set_node(t.set));
            }
        }
        return overflowed;
    }

    boolean verify_set_node(ParseSet set) {
        ParseChoice pc;
        double dn;
        int n;
        if (set == null || set.first == null)
            return false;
        dn = n = 0;
        for (pc = set.first; pc != null; pc = pc.next) {
            n += pc.set[0].count * pc.set[1].count;
            dn += ((double)pc.set[0].count) * ((double)pc.set[1].count);
        }
        if (n != set.count) {
            throw new RuntimeException("verify_set failed");
        }
        return (n < 0) || (n != (int)dn);
    }

    ParseSet parse_set(Sentence sent,Disjunct ld, Disjunct rd, int lw, int rw, Connector le, Connector re, int cost, ParseOptions opts) {

        /* returns null if there are no ways to parse, or returns a pointer
           to a set structure representing all the ways to parse */

        Disjunct d, dis;
        int start_word, end_word, w;
        int lcost, rcost;
        boolean Lmatch, Rmatch;
        int i, j;
        ParseSet ls[] = new ParseSet[4];
        ParseSet rs[] = new ParseSet[4];
        ParseSet lset;
        ParseSet rset;
        ParseChoice a_choice;

        MatchNode m, m1;
        XTableConnector xt;
        int count;

        if (cost < 0) {
            throw new RuntimeException("parse_set() called with cost < 0.");
        }

        count = Sentence.table_lookup(lw, rw, le, re, cost);

        /*
          if(!(count >= 0)) {
        throw new RuntimeException( "parse_set() called on params that were not in the table.");
        }
          Actually, we can't assert this, because of the pseudocount technique that's
          used in count().  It's not the case that every call to parse_set() has already
          been put into the table.
         */

        if ((count == 0) || (count == -1))
            return null;

        xt = x_table_pointer(lw, rw, le, re, cost);

        if (xt == null) {
            xt = x_table_store(lw, rw, le, re, cost, ParseSet.empty_set());
            /* start it out with the empty set of options */
            /* this entry must be updated before we return */
        } else {
            return xt.set; /* we've already computed it */
        }

        xt.set.count = count; /* the count we already computed */
        /* this count is non-zero */

        if (rw == 1 + lw)
            return xt.set;
        if ((le == null) && (re == null)) {
            if (!opts.islands_ok && (lw != -1)) {
                return xt.set;
            }
            if (cost == 0) {
                return xt.set;
            } else {
                w = lw + 1;
                for (dis = Sentence.local_sent[w].d; dis != null; dis = dis.next) {
                    if (dis.left == null) {
                        rs[0] = parse_set(sent,dis, null, w, rw, dis.right, null, cost - 1,opts);
                        if (rs[0] == null)
                            continue;
                        a_choice =
                        new ParseChoice(ParseSet.dummy_set, lw, w, null, null, rs[0], w, rw, null, null, null, null, null);
                        xt.set.put_choice_in_set(a_choice);
                    }
                }
                rs[0] = parse_set(sent,null, null, w, rw, null, null, cost - 1,opts);
                if (rs[0] != null) {
                    a_choice = new ParseChoice(ParseSet.dummy_set, lw, w, null, null, rs[0], w, rw, null, null, null, null, null);
                    xt.set.put_choice_in_set(a_choice);
                }
                return xt.set;
            }
        }

        if (le == null) {
            start_word = lw + 1;
        } else {
            start_word = le.word;

        }

        if (re == null) {
            end_word = rw - 1;
        } else {
            end_word = re.word;
        }

        for (w = start_word; w <= end_word; w++) {
            m1 = m = Sentence.form_match_list(w, le, lw, re, rw);
            for (; m != null; m = m.next) {
                d = m.d;
                for (lcost = 0; lcost <= cost; lcost++) {
                    rcost = cost - lcost;
                    /* now lcost and rcost are the costs we're assigning to those parts respectively */

                    /* Now, we determine if (based on table only) we can see that
                       the current range is not parsable. */

                    Lmatch = (le != null) && (d.left != null) && Connector.match(sent,le, d.left, lw, w);
                    Rmatch = (d.right != null) && (re != null) && Connector.match(sent,d.right, re, w, rw);
                    for (i = 0; i < 4; i++) {
                        ls[i] = rs[i] = null;
                    }
                    if (Lmatch) {
                        ls[0] = parse_set(sent,ld, d, lw, w, le.next, d.left.next, lcost,opts);
                        if (le.multi)
                            ls[1] = parse_set(sent,ld, d, lw, w, le, d.left.next, lcost,opts);
                        if (d.left.multi)
                            ls[2] = parse_set(sent,ld, d, lw, w, le.next, d.left, lcost,opts);
                        if (le.multi && d.left.multi)
                            ls[3] = parse_set(sent,ld, d, lw, w, le, d.left, lcost,opts);
                    }
                    if (Rmatch) {
                        rs[0] = parse_set(sent,d, rd, w, rw, d.right.next, re.next, rcost,opts);
                        if (d.right.multi)
                            rs[1] = parse_set(sent,d, rd, w, rw, d.right, re.next, rcost,opts);
                        if (re.multi)
                            rs[2] = parse_set(sent,d, rd, w, rw, d.right.next, re, rcost,opts);
                        if (d.right.multi && re.multi)
                            rs[3] = parse_set(sent,d, rd, w, rw, d.right, re, rcost,opts);
                    }

                    for (i = 0; i < 4; i++) {
                        /* this ordering is probably not consistent with that needed to use list_links */
                        if (ls[i] == null)
                            continue;
                        for (j = 0; j < 4; j++) {
                            if (rs[j] == null)
                                continue;
                            a_choice = new ParseChoice(ls[i], lw, w, le, d.left, rs[j], w, rw, d.right, re, ld, d, rd);
                            xt.set.put_choice_in_set(a_choice);
                        }
                    }

                    if (ls[0] != null || ls[1] != null || ls[2] != null || ls[3] != null) {
                        /* evaluate using the left match, but not the right */
                        rset = parse_set(sent,d, rd, w, rw, d.right, re, rcost,opts);
                        if (rset != null) {
                            for (i = 0; i < 4; i++) {
                                if (ls[i] == null)
                                    continue;
                                /* this ordering is probably not consistent with that needed to use list_links */
                                a_choice = new ParseChoice(ls[i], lw, w, le, d.left, rset, w, rw, null /* d.right */
                                , re, /* the null indicates no link*/
                                ld, d, rd);
                                xt.set.put_choice_in_set(a_choice);
                            }
                        }
                    }
                    if ((le == null) && (rs[0] != null || rs[1] != null || rs[2] != null || rs[3] != null)) {
                        /* evaluate using the right match, but not the left */
                        lset = parse_set(sent,ld, d, lw, w, le, d.left, lcost,opts);

                        if (lset != null) {
                            for (i = 0; i < 4; i++) {
                                if (rs[i] == null)
                                    continue;
                                /* this ordering is probably not consistent with that needed to use list_links */
                                a_choice = new ParseChoice(lset, lw, w, null /* le */
                                , d.left, /* null indicates no link */
                                rs[i], w, rw, d.right, re, ld, d, rd);
                                xt.set.put_choice_in_set(a_choice);
                            }
                        }
                    }
                }
            }
        }
        xt.set.current = xt.set.first;
        return xt.set;
    }

    void free_x_table() {
        x_table_size = 0;
        x_table = null;
    }

    XTableConnector x_table_pointer(int lw, int rw, Connector le, Connector re, int cost) {
        /* returns the pointer to this info, null if not there */
        XTableConnector t;
        t = x_table[x_hash(lw, rw, le, re, cost)];
        for (; t != null; t = t.next) {
            if ((t.lw == lw) && (t.rw == rw) && (t.le == le) && (t.re == re) && (t.cost == cost))
                return t;
        }
        return null;
    }

    XTableConnector x_table_store(int lw, int rw, Connector le, Connector re, int cost, ParseSet set) {
        /* Stores the value in the x_table.  Assumes it's not already there */
        XTableConnector t, n;
        int h;

        n = new XTableConnector();
        n.set = set;
        n.lw = lw;
        n.rw = rw;
        n.le = le;
        n.re = re;
        n.cost = cost;
        h = x_hash(lw, rw, le, re, cost);
        t = x_table[h];
        n.next = t;
        x_table[h] = n;
        return n;
    }

    void x_table_update(int lw, int rw, Connector le, Connector re, int cost, ParseSet set) {
        /* Stores the value in the x_table.  Unlike x_table_store, it assumes it's already there */
        XTableConnector t = x_table_pointer(lw, rw, le, re, cost);

        if (t == null) {
            throw new RuntimeException("This entry is supposed to be in the x_table.");
        }
        t.set = set;
    }

    int x_hash(int lw, int rw, Connector le, Connector re, int cost) {
        int i;
        i = 0;

        i = i + (i << 1) + MyRandom.randtable[(lw + i) & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(rw + i) & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(((le==null?0:le.hashCode()) + i) % (x_table_size + 1)) & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(((re==null?0:re.hashCode()) + i) % (x_table_size + 1)) & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(cost + i) & (GlobalBean.RTSIZE - 1)];
        return i & (x_table_size - 1);
    }

    void height_dfs(int w, int height) {
        ListOfLinks lol;
        if (dfs_height[w] != 0)
            return;
        dfs_height[w] = height;
        for (lol = word_links[w]; lol != null; lol = lol.next) {
            /* The dir is 1 for a down link. */
            height_dfs(lol.word, height - lol.dir);
        }
    }

    static DISNode build_DISNode(int w) {
        /* This node is connected to its parent via a fat link.  Search the
           region reachable via thin links, and put all reachable nodes with fat
           links out of them in its list of children.
        */
        DISNode dn;
        dn = new DISNode();
        dn.word = w; /* must do this before dfs so it knows the start word */
        dn.lol = null;
        dn.cl = c_dfs(w, dn, null);
        return dn;
    }

    static CONList c_dfs(int w, DISNode start_dn, CONList c) {

        /* Does a depth-first-search starting from w.  Puts on the front of the
           list pointed to by c all of the CON nodes it finds, and returns the
           result.  Also construct the list of all edges reached as part of this
           DISNode search and append it to the lol list of start_dn.
        
           Both of the structure violations actually occur, and represent
           linkages that have improper structure.  Fortunately, they
           seem to be rather rare.
        */
        CONList cx;
        ListOfLinks lol, lolx;
        if (dfs_root_word[w] != -1) {
            if (dfs_root_word[w] != start_dn.word) {
                Sentence.structure_violation = true;
            }
            return c;
        }
        dfs_root_word[w] = start_dn.word;
        for (lol = word_links[w]; lol != null; lol = lol.next) {
            if (lol.dir < 0) { /* a backwards link */
                if (dfs_root_word[lol.word] == -1) {
                    Sentence.structure_violation = true;
                }
            } else if (lol.dir == 0) {
                lolx = new ListOfLinks();
                lolx.next = start_dn.lol;
                lolx.link = lol.link;
                start_dn.lol = lolx;
                c = c_dfs(lol.word, start_dn, c);
            }
        }
        if (is_CON_word(w)) { /* if the current node is CON, put it first */
            cx = new CONList();
            cx.next = c;
            c = cx;
            c.cn = build_CONNode(w);
        }
        return c;
    }

    static boolean is_CON_word(int w) {
        /* Returns true if there is at least one fat link pointing out of this word. */
        ListOfLinks lol;
        for (lol = word_links[w]; lol != null; lol = lol.next) {
            if (lol.dir == 1) {
                return true;
            }
        }
        return false;
    }

    static CONNode build_CONNode(int w) {
        /* This word is a CON word (has fat links down).  Build the tree for it.  */
        ListOfLinks lol;
        CONNode a;
        DISList d, dx;
        d = null;
        for (lol = word_links[w]; lol != null; lol = lol.next) {
            if (lol.dir == 1) {
                dx = new DISList();
                dx.next = d;
                d = dx;
                d.dn = build_DISNode(lol.word);
            }
        }
        a = new CONNode();
        a.dl = a.current = d;
        a.word = w;
        return a;
    }

    static ListOfLinks word_links[] = new ListOfLinks[GlobalBean.MAX_SENTENCE]; /* ptr to l.o.l. out of word */
    static int dfs_root_word[] = new int[GlobalBean.MAX_SENTENCE]; /* for the depth-first search */
    static int dfs_height[] = new int[GlobalBean.MAX_SENTENCE]; /* to determine the order to do the root word dfs */
    static Integer height_perm[] = new Integer[GlobalBean.MAX_SENTENCE]; /* permute the vertices from highest to lowest */

}
