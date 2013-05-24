package net.sf.jlinkgrammar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * O.K. is this a static class?  No it is used in the dictionary instance object which is then
 * copied to the Sentence instance object, and then to the various Linkage instance objects.
 * The programmmer will have to pay special attention to acertain wich postprocessor is used 
 * on any given linkage. 
 *
 */
public class Postprocessor {
    /**
     * internal rep'n of the actual rules 
     */
    PPKnowledge knowledge;
    /**
     * this is diagnostic     
     */
    int n_global_rules_firing;
    /**
     * this is diagnostic     
     */
    int n_local_rules_firing;
    /**
     * seen in *any* linkage of sent 
     */
    PPLinkset set_of_links_of_sentence;
    /**
     * used in *some* linkage of sentence
     */
    PPLinkset set_of_links_in_an_active_rule;
    /**
     * a "-1" -terminated list of index  
     */
    int relevant_contains_one_rules[];
    /**
     * a "-1" -terminated list of index  
     */
    int relevant_contains_none_rules[];
    /**
     *  maintain state during a call to post_process() for the depth-first search 
     */
    boolean visited[] = new boolean[GlobalBean.MAX_SENTENCE];
    /**
     * root of the pp_node tree
     * @see PPNode
     */
    PPNode pp_node;
    /**
     * root of the pp_data tree
     * @see PPData
     */
    PPData pp_data;
    
    /**
     * Constructor class placehoder for javadoc
     */
    Postprocessor() {
        
    }

    /**
     *
      string comparison in postprocessing. The first parameter is a
      post-processing symbol. The second one is a connector name from a link. The
      upper case parts must match. We imagine that the first arg is padded with an
      infinite sequence of "#" and that the 2nd one is padded with "*". "#"
      matches anything, but "*" is just like an ordinary char for matching 
      purposes. For efficiency sake there are several different versions of these 
      functions 
      */

    static boolean post_process_match(String s, String t) {
        char c;
        int i = 0;
        while (i < s.length()
            && i < t.length()
            && (Character.isUpperCase(s.charAt(i)) || Character.isUpperCase(t.charAt(i)))) {
            if (s.charAt(i) != t.charAt(i))
                return false;
            i++;
        }
        while (i < s.length()) {
            if (s.charAt(i) != '#') {
                if (i >= t.length())
                    c = '*';
                else
                    c = t.charAt(i);
                if (s.charAt(i) != c)
                    return false;
            }
            i++;
        }
        return true;
    }

    /**
     * 
     * @param pp 
     */
    static void post_process_close_sentence(Postprocessor pp) {
        if (pp == null)
            return;
        PPLinkset.PPLinkset_clear(pp.set_of_links_of_sentence);
        PPLinkset.PPLinkset_clear(pp.set_of_links_in_an_active_rule);
        pp.n_local_rules_firing = 0;
        pp.n_global_rules_firing = 0;
        pp.relevant_contains_one_rules[0] = -1;
        pp.relevant_contains_none_rules[0] = -1;
        free_pp_node(pp);
    }

    /**
     * Walk the sublinkage and apply rules. This helps determine if a linkage is valid
     * @param pp 
     * @param sublinkage 
     * @param msg 
     * @return 0 if the linakge satisfied all the rules
     * @see #build_graph(Postprocessor, Sublinkage)
     */
    static int internal_process(Postprocessor pp, Sublinkage sublinkage, String msg[]) {
        int i;
        /* quick test: try applying just the relevant global rules */
        if (!apply_relevant_rules(pp,
            new ApplyContainsOneGlobally(),
            sublinkage,
            pp.knowledge.contains_one_rules,
            pp.relevant_contains_one_rules,
            msg)) {
            for (i = 0; i < pp.pp_data.length; i++)
                pp.pp_data.word_links[i] = null;
            pp.pp_data.N_domains = 0;
            return -1;
        }

        /* build graph; confirm that it's legally connected */
        build_graph(pp, sublinkage);
        build_domains(pp, sublinkage);
        build_domain_forest(pp, sublinkage);

        /* The order below should be optimal for most cases */
        if (!apply_relevant_rules(pp,
            new ApplyContainsOne(),
            sublinkage,
            pp.knowledge.contains_one_rules,
            pp.relevant_contains_one_rules,
            msg))
            return 1;
        if (!apply_relevant_rules(pp,
            new ApplyContainsNone(),
            sublinkage,
            pp.knowledge.contains_none_rules,
            pp.relevant_contains_none_rules,
            msg))
            return 1;
        if (!apply_rules(pp, new ApplyMustFormACycle(), sublinkage, pp.knowledge.form_a_cycle_rules, msg))
            return 1;
        if (!apply_rules(pp, new ApplyConnected(), sublinkage, pp.knowledge.connected_rules, msg))
            return 1;
        if (!apply_rules(pp, new ApplyBounded(), sublinkage, pp.knowledge.bounded_rules, msg))
            return 1;
        return 0; /* This linkage satisfied all the rules */
    }

    /**
     * returns false if the string s does not match anything in 
     * the array.  The array elements are post-processing symbols
     * @param s 
     * @param a 
     * @return false if the string s does not match anything in the array
     */
    static boolean string_in_list(String s, String a[]) {
        /*  */
        int i;
        for (i = 0; a[i] != null; i++)
            if (Postprocessor.post_process_match(a[i], s))
                return true;
        return false;
    }

    /**
     * 
     * @param pp 
     * @param w 
     */
    static void mark_reachable_words(Postprocessor pp, int w) {
        ListOfLinks lol;
        if (pp.visited[w])
            return;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next)
            mark_reachable_words(pp, lol.word);
    }

    /**
     * Returns true if the linkage is connected, considering words
     * that have at least one edge....this allows conjunctive sentences
     * not to be thrown out.
     * @param pp 
     * @return true if the linkage is connected
     */
    static boolean is_connected(Postprocessor pp) {
        /* Returns true if the linkage is connected, considering words
           that have at least one edge....this allows conjunctive sentences
           not to be thrown out. */
        int i;
        for (i = 0; i < pp.pp_data.length; i++)
            pp.visited[i] = (pp.pp_data.word_links[i] == null);
        mark_reachable_words(pp, 0);
        for (i = 0; i < pp.pp_data.length; i++)
            if (!pp.visited[i])
                return false;
        return true;
    }

    /************************ rule application *******************************/

    /********************* various non-exported functions ***********************/
    /**
     * fill in the pp.pp_data.word_links array with a list of words neighboring each
     *     word (actually a list of links).  The dir fields are not set, since this
     *     (after fat-link-extraction) is an undirected graph.
     * <p>
     * TODO - Why does this pass in a PostProcessor object? How can this.pp_data
     * ever be filled out if we don't copy it into this instance object?
     *
     * @param pp Postprocessor to use - why not this.pp_data.sordlinks etc?
     * @param sublinkage linkage to build into graph structure
     * 
     */
    private static void build_graph(Postprocessor pp, Sublinkage sublinkage) {
        
        int i, link;
        ListOfLinks lol;

        for (i = 0; i < pp.pp_data.length; i++)
            pp.pp_data.word_links[i] = null;

        for (link = 0; link < sublinkage.num_links; link++) {
            if (sublinkage.link[link].l == -1)
                continue;
            if (PPLinkset.PPLinkset_match(pp.knowledge.ignore_these_links, sublinkage.link[link].name)) {
                lol = new ListOfLinks();
                lol.next = pp.pp_data.links_to_ignore;
                pp.pp_data.links_to_ignore = lol;
                lol.link = link;
                lol.word = sublinkage.link[link].r;
                continue;
            }

            lol = new ListOfLinks();
            lol.next = pp.pp_data.word_links[sublinkage.link[link].l];
            pp.pp_data.word_links[sublinkage.link[link].l] = lol;
            lol.link = link;
            lol.word = sublinkage.link[link].r;

            lol = new ListOfLinks();
            lol.next = pp.pp_data.word_links[sublinkage.link[link].r];
            pp.pp_data.word_links[sublinkage.link[link].r] = lol;
            lol.link = link;
            lol.word = sublinkage.link[link].l;
        }
    }

    /**
     * build the domains acociated with a sublinkage.
     * TODO - Why does this pass in a PostProcessor object? How can this.pp_data
     * ever be filled out if we don't copy it into this instance object?
     * @param pp 
     * @param sublinkage 
     */
    static void build_domains(Postprocessor pp, Sublinkage sublinkage) {
        int link, i, d;
        String s;
        pp.pp_data.N_domains = 0;

        for (link = 0; link < sublinkage.num_links; link++) {
            if (sublinkage.link[link].l == -1)
                continue;
            s = sublinkage.link[link].name;

            if (PPLinkset.PPLinkset_match(pp.knowledge.ignore_these_links, s))
                continue;
            if (PPLinkset.PPLinkset_match(pp.knowledge.domain_starter_links, s)) {
                setup_domain_array(pp, pp.pp_data.N_domains, s, link);
                if (PPLinkset.PPLinkset_match(pp.knowledge.domain_contains_links, s))
                    add_link_to_domain(pp, link);
                depth_first_search(pp, sublinkage, sublinkage.link[link].r, sublinkage.link[link].l, link);

                pp.pp_data.N_domains++;
                if (!(pp.pp_data.N_domains < GlobalBean.PP_MAX_DOMAINS)) {
                    throw new RuntimeException("raise value of PP_MAX_DOMAINS");
                }
            } else {
                if (PPLinkset.PPLinkset_match(pp.knowledge.urfl_domain_starter_links, s)) {
                    setup_domain_array(pp, pp.pp_data.N_domains, s, link);
                    /* always add the starter link to its urfl domain */
                    add_link_to_domain(pp, link);
                    bad_depth_first_search(pp, sublinkage, sublinkage.link[link].r, sublinkage.link[link].l, link);
                    pp.pp_data.N_domains++;
                    if (!(pp.pp_data.N_domains < GlobalBean.PP_MAX_DOMAINS)) {
                        throw new RuntimeException("raise PP_MAX_DOMAINS value");
                    }
                } else if (PPLinkset.PPLinkset_match(pp.knowledge.urfl_only_domain_starter_links, s)) {
                    setup_domain_array(pp, pp.pp_data.N_domains, s, link);
                    /* do not add the starter link to its urfl_only domain */
                    d_depth_first_search(
                        pp,
                        sublinkage,
                        sublinkage.link[link].l,
                        sublinkage.link[link].l,
                        sublinkage.link[link].r,
                        link);
                    pp.pp_data.N_domains++;
                    if (!(pp.pp_data.N_domains < GlobalBean.PP_MAX_DOMAINS)) {
                        throw new RuntimeException("raise PP_MAX_DOMAINS value");
                    }
                } else if (PPLinkset.PPLinkset_match(pp.knowledge.left_domain_starter_links, s)) {
                    setup_domain_array(pp, pp.pp_data.N_domains, s, link);
                    /* do not add the starter link to a left domain */
                    left_depth_first_search(pp, sublinkage, sublinkage.link[link].l, sublinkage.link[link].r, link);
                    pp.pp_data.N_domains++;
                    if (!(pp.pp_data.N_domains < GlobalBean.PP_MAX_DOMAINS)) {
                        throw new RuntimeException("raise PP_MAX_DOMAINS value");
                    }
                }
            }
        }

        /* sort the domains by size */
        Arrays.sort(pp.pp_data.domain_array, 0,pp.pp_data.N_domains, new Comparator() {

            public int compare(Object d1, Object d2) {
                return ((Domain)d1).size - ((Domain)d2).size; /* for sorting the domains by size */
            }
        });

        /* sanity check: all links in all domains have a legal domain name */
        for (d = 0; d < pp.pp_data.N_domains; d++) {
            i = find_domain_name(pp, pp.pp_data.domain_array[d].string);
            if (i == -1)
                throw new RuntimeException(
                    "\tpost_process: Need an entry for " + pp.pp_data.domain_array[d].string + " in LINK_TYPE_TABLE");
            pp.pp_data.domain_array[d].type = i;
        }
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     */
    static void build_domain_forest(Postprocessor pp, Sublinkage sublinkage) {
        int d, d1, link;
        DTreeLeaf dtl;
        if (pp.pp_data.N_domains > 0)
            pp.pp_data.domain_array[pp.pp_data.N_domains - 1].parent = null;
        for (d = 0; d < pp.pp_data.N_domains - 1; d++) {
            for (d1 = d + 1; d1 < pp.pp_data.N_domains; d1++) {
                if (contained_in(pp.pp_data.domain_array[d], pp.pp_data.domain_array[d1], sublinkage)) {
                    pp.pp_data.domain_array[d].parent = pp.pp_data.domain_array[d1];
                    break;
                }
            }
            if (d1 == pp.pp_data.N_domains) {
                /* we know this domain is a root of a new tree */
                pp.pp_data.domain_array[d].parent = null;
                /* It's now ok for this to happen.  It used to do:
                printf("I can't find a parent domain for this domain\n");
                print_domain(d);
                exit(1); */
            }
        }
        /* the parent links of domain nodes have been established.
           now do the leaves */
        for (d = 0; d < pp.pp_data.N_domains; d++) {
            pp.pp_data.domain_array[d].child = null;
        }
        for (link = 0; link < sublinkage.num_links; link++) {
            if (sublinkage.link[link].l == -1)
                continue; /* probably not necessary */
            for (d = 0; d < pp.pp_data.N_domains; d++) {
                if (link_in_domain(link, pp.pp_data.domain_array[d])) {
                    dtl = new DTreeLeaf();
                    dtl.link = link;
                    dtl.parent = pp.pp_data.domain_array[d];
                    dtl.next = pp.pp_data.domain_array[d].child;
                    pp.pp_data.domain_array[d].child = dtl;
                    break;
                }
            }
        }
    }

    /***************** utility routines (not exported) ***********************/

    static int find_domain_name(Postprocessor pp, String link) {
        /* Return the name of the domain associated with the provided starting 
           link. Return -1 if link isn't associated with a domain. */
        int i, domain;
        StartingLinkAndDomain sllt[] = pp.knowledge.starting_link_lookup_table;
        for (i = 0;; i++) {
            domain = sllt[i].domain;
            if (domain == -1)
                return -1; /* hit the end-of-list sentinel */
            if (Postprocessor.post_process_match(sllt[i].starting_link, link))
                return domain;
        }
    }

    /**
     * Used to determine if one domain is containd in another
     * @param d1 
     * @param d2 
     * @param sublinkage 
     * @return returns true if domain d1 is contained in domain d2
     */
    static boolean contained_in(Domain d1, Domain d2, Sublinkage sublinkage) {
        /*  */
        boolean mark[] = new boolean[GlobalBean.MAX_LINKS];
        ListOfLinks lol;
        Arrays.fill(mark, false);
        for (lol = d2.lol; lol != null; lol = lol.next)
            mark[lol.link] = true;
        for (lol = d1.lol; lol != null; lol = lol.next)
            if (!mark[lol.link])
                return false;
        return true;
    }

    /**
     * determine if the given link is in the given domain
     * @param link 
     * @param d 
     * @return the predicate "the given link is in the given domain"
     */
    static boolean link_in_domain(int link, Domain d) {
        /* returns the predicate "the given link is in the given domain" */
        ListOfLinks lol;
        for (lol = d.lol; lol != null; lol = lol.next)
            if (lol.link == link)
                return true;
        return false;
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     * @param w 
     * @param ls 
     */
    static void connectivity_dfs(Postprocessor pp, Sublinkage sublinkage, int w, PPLinkset ls) {
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (!pp.visited[lol.word] && !PPLinkset.PPLinkset_match(ls, sublinkage.link[lol.link].name))
                connectivity_dfs(pp, sublinkage, lol.word, ls);
        }
    }

    /**
     * Add a link to the domain
     * @param pp 
     * @param link 
     */
    static void add_link_to_domain(Postprocessor pp, int link) {
        ListOfLinks lol;
        lol = new ListOfLinks();
        lol.next = pp.pp_data.domain_array[pp.pp_data.N_domains].lol;
        pp.pp_data.domain_array[pp.pp_data.N_domains].lol = lol;
        pp.pp_data.domain_array[pp.pp_data.N_domains].size++;
        lol.link = link;
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     * @param w 
     * @param root 
     * @param start_link 
     */
    static void depth_first_search(Postprocessor pp, Sublinkage sublinkage, int w, int root, int start_link) {
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (lol.word < w && lol.link != start_link) {
                add_link_to_domain(pp, lol.link);
            }
        }
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (!pp.visited[lol.word]
                && (lol.word != root)
                && !(lol.word < root
                    && lol.word < w
                    && PPLinkset.PPLinkset_match(pp.knowledge.restricted_links, sublinkage.link[lol.link].name)))
                depth_first_search(pp, sublinkage, lol.word, root, start_link);

        }
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     * @param w 
     * @param root 
     * @param start_link 
     */
    static void bad_depth_first_search(Postprocessor pp, Sublinkage sublinkage, int w, int root, int start_link) {
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if ((lol.word < w) && (lol.link != start_link) && (w != root)) {
                add_link_to_domain(pp, lol.link);
            }
        }
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if ((!pp.visited[lol.word])
                && !(w == root && lol.word < w)
                && !(lol.word < root
                    && lol.word < w
                    && PPLinkset.PPLinkset_match(pp.knowledge.restricted_links, sublinkage.link[lol.link].name)))
                bad_depth_first_search(pp, sublinkage, lol.word, root, start_link);
        }
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     * @param w 
     * @param root 
     * @param right 
     * @param start_link 
     */
    static void d_depth_first_search(
        Postprocessor pp,
        Sublinkage sublinkage,
        int w,
        int root,
        int right,
        int start_link) {
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if ((lol.word < w) && (lol.link != start_link) && (w != root)) {
                add_link_to_domain(pp, lol.link);
            }
        }
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (!pp.visited[lol.word]
                && !(w == root && lol.word >= right)
                && !(w == root && lol.word < root)
                && !(lol.word < root
                    && lol.word < w
                    && PPLinkset.PPLinkset_match(pp.knowledge.restricted_links, sublinkage.link[lol.link].name)))
                d_depth_first_search(pp, sublinkage, lol.word, root, right, start_link);
        }
    }

    /**
     * 
     * @param pp 
     * @param sublinkage 
     * @param w 
     * @param right 
     * @param start_link 
     */
    static void left_depth_first_search(Postprocessor pp, Sublinkage sublinkage, int w, int right, int start_link) {
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (lol.word < w && lol.link != start_link) {
                add_link_to_domain(pp, lol.link);
            }
        }
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (!pp.visited[lol.word] && (lol.word != right))
                depth_first_search(pp, sublinkage, lol.word, right, start_link);
        }
    }

    /**
     * 
     * @param pp 
     * @param n 
     * @param string 
     * @param start_link 
     */
    static void setup_domain_array(Postprocessor pp, int n, String string, int start_link) {
        Arrays.fill(pp.visited, false);
        pp.pp_data.domain_array[n]=new Domain();
        pp.pp_data.domain_array[n].string = string;
        pp.pp_data.domain_array[n].lol = null;
        pp.pp_data.domain_array[n].size = 0;
        pp.pp_data.domain_array[n].start_link = start_link;
    }

    /**
     * This is a depth first search of words reachable from w, excluding any direct edge 
     * between word a and word b.
     * @param pp 
     * @param sublinkage 
     * @param a 
     * @param b 
     * @param w 
     */
    static void reachable_without_dfs(Postprocessor pp, Sublinkage sublinkage, int a, int b, int w) {
      
        ListOfLinks lol;
        pp.visited[w] = true;
        for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
            if (!pp.visited[lol.word] && !(w == a && lol.word == b) && !(w == b && lol.word == a)) {
                reachable_without_dfs(pp, sublinkage, a, b, lol.word);
            }
        }
    }

    static class ApplyContainsOne implements ApplyFn {
        /**
         * 
         * @param pp 
         * @param sublinkage 
         * @param rule 
         * @return returns true if and only if all groups containing the specified link 
         *                  contain at least one from the required list.  (as determined by exact
         *                  string matching)
         */
        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {
            /*  */
            DTreeLeaf dtl;
            int d, count;
            for (d = 0; d < pp.pp_data.N_domains; d++) {
                for (dtl = pp.pp_data.domain_array[d].child;
                    dtl != null && !Postprocessor.post_process_match(rule.selector, sublinkage.link[dtl.link].name);
                    dtl = dtl.next);
                if (dtl != null) {
                    /* selector link of rule appears in this domain */
                    count = 0;
                    for (dtl = pp.pp_data.domain_array[d].child; dtl != null; dtl = dtl.next)
                        if (string_in_list(sublinkage.link[dtl.link].name, rule.link_array)) {
                            count = 1;
                            break;
                        }
                    if (count == 0)
                        return false;
                }
            }
            return true;
        }
    }

    static class ApplyContainsNone implements ApplyFn {

        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {
            /* returns true if and only if:
               all groups containing the selector link do not contain anything
               from the link_array contained in the rule. Uses exact string matching. */
            DTreeLeaf dtl;
            int d;
            for (d = 0; d < pp.pp_data.N_domains; d++) {
                for (dtl = pp.pp_data.domain_array[d].child;
                    dtl != null && !Postprocessor.post_process_match(rule.selector, sublinkage.link[dtl.link].name);
                    dtl = dtl.next);
                if (dtl != null) {
                    /* selector link of rule appears in this domain */
                    for (dtl = pp.pp_data.domain_array[d].child; dtl != null; dtl = dtl.next)
                        if (string_in_list(sublinkage.link[dtl.link].name, rule.link_array))
                            return false;
                }
            }
            return true;
        }
    }

    static class ApplyContainsOneGlobally implements ApplyFn {

        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {
            /* returns true if and only if 
               (1) the sentence doesn't contain the selector link for the rule, or 
               (2) it does, and it also contains one or more from the rule's link set */

            int i, j, count;
            for (i = 0; i < sublinkage.num_links; i++) {
                if (sublinkage.link[i].l == -1)
                    continue;
                if (Postprocessor.post_process_match(rule.selector, sublinkage.link[i].name))
                    break;
            }
            if (i == sublinkage.num_links)
                return true;

            /* selector link of rule appears in sentence */
            count = 0;
            for (j = 0; j < sublinkage.num_links && count == 0; j++) {
                if (sublinkage.link[j].l == -1)
                    continue;
                if (string_in_list(sublinkage.link[j].name, rule.link_array)) {
                    count = 1;
                    break;
                }
            }
            if (count == 0)
                return false;
            else
                return true;
        }
    }

    static class ApplyConnected implements ApplyFn {

        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {
            /* There is actually just one (or none, if user didn't specify it)
               rule asserting that linkage is connected. */
            if (!is_connected(pp))
                return false;
            return true;
        }
    }

    /* Here's the new algorithm: For each link in the linkage that is in the
       must_form_a_cycle list, we want to make sure that that link
       is in a cycle.  We do this simply by deleting the link, then seeing if the
       end points of that link are still connected.
    */

    static class ApplyMustFormACycle implements ApplyFn {

        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {
            /* Returns true if the linkage is connected when ignoring the links
               whose names are in the given list of link names.
               Actually, what it does is this: it returns false if the connectivity
               of the subgraph reachable from word 0 changes as a result of deleting
               these links. */

            ListOfLinks lol;
            int w;
            for (w = 0; w < pp.pp_data.length; w++) {
                for (lol = pp.pp_data.word_links[w]; lol != null; lol = lol.next) {
                    if (w > lol.word)
                        continue; /* only consider each edge once */
                    if (!PPLinkset.PPLinkset_match(rule.link_set, sublinkage.link[lol.link].name))
                        continue;
                    Arrays.fill(pp.visited, false);
                    reachable_without_dfs(pp, sublinkage, w, lol.word, w);
                    if (!pp.visited[lol.word])
                        return false;
                }
            }

            for (lol = pp.pp_data.links_to_ignore; lol != null; lol = lol.next) {
                w = sublinkage.link[lol.link].l;
                /* (w, lol.word) are the left and right ends of the edge we're considering */
                if (!PPLinkset.PPLinkset_match(rule.link_set, sublinkage.link[lol.link].name))
                    continue;
                Arrays.fill(pp.visited, false);
                reachable_without_dfs(pp, sublinkage, w, lol.word, w);
                if (!pp.visited[lol.word])
                    return false;
            }

            return true;
        }
    }

    static class ApplyBounded implements ApplyFn {

        public boolean applyfn(Postprocessor pp, Sublinkage sublinkage, PPRule rule) {

            /* Checks to see that all domains with this name have the property that
               all of the words that touch a link in the domain are not to the left
               of the root word of the domain. */
            int d, lw, d_type;
            ListOfLinks lol;
            d_type = rule.domain;
            for (d = 0; d < pp.pp_data.N_domains; d++) {
                if (pp.pp_data.domain_array[d].type != d_type)
                    continue;
                lw = sublinkage.link[pp.pp_data.domain_array[d].start_link].l;
                for (lol = pp.pp_data.domain_array[d].lol; lol != null; lol = lol.next) {
                    if (sublinkage.link[lol.link].l < lw)
                        return false;
                }
            }
            return true;
        }
    }

    static interface ApplyFn {

        boolean applyfn(Postprocessor postprocessor, Sublinkage sublinkage, PPRule ppRule);
    }

    /**
     * 
     * @param pp 
     * @param applyfn 
     * @param sublinkage 
     * @param rule_array 
     * @param msg 
     * @return false if rule is violated
     */
    static boolean apply_rules(
        Postprocessor pp,
        ApplyFn applyfn,
        Sublinkage sublinkage,
        PPRule rule_array[],
        String msg[]) {
        int i;
        for (i = 0;(msg[0] = rule_array[i].msg) != null; i++)
            if (!applyfn.applyfn(pp, sublinkage, rule_array[i]))
                return false;
        return true;
    }

    /**
     * accumulate link names for this sentence, and need to apply
     *     all rules
     * @param pp 
     * @param applyfn 
     * @param sublinkage 
     * @param rule_array 
     * @param relevant_rules 
     * @param msg 
     * @return true if we need to apply all rules false of not
     */
    static boolean apply_relevant_rules(
        Postprocessor pp,
        ApplyFn applyfn,
        Sublinkage sublinkage,
        PPRule rule_array[],
        int relevant_rules[],
        String msg[]) {
        int i, idx;

        /* if we didn't accumulate link names for this sentence, we need to apply
           all rules */
        if (PPLinkset.PPLinkset_population(pp.set_of_links_of_sentence) == 0) {
            return apply_rules(pp, applyfn, sublinkage, rule_array, msg);
        }

        /* we did, and we don't */
        for (i = 0;(idx = relevant_rules[i]) != -1; i++) {
            msg[0] = rule_array[idx].msg; /* Adam had forgotten this -- DS 4/9/98 */
            if (!applyfn.applyfn(pp, sublinkage, rule_array[idx]))
                return false;
        }
        return true;
    }

    /**
     * 
     * @param pp 
     */
    static void free_pp_node(Postprocessor pp) {
        pp.pp_node = null;
    }

    /**
     * set up a fresh pp_node for later use
     * @param pp 
     */
    static void alloc_pp_node(Postprocessor pp) {
       
        int i;
        pp.pp_node = new PPNode();
        pp.pp_node.violation = null;
        for (i = 0; i < GlobalBean.MAX_LINKS; i++)
            pp.pp_node.d_type_array[i] = null;
    }

    /**
     * 
     * @param pp 
     */
    static void reset_pp_node(Postprocessor pp) {
        free_pp_node(pp);
        alloc_pp_node(pp);
    }

    /**
     * 
     * @param ppd 
     */
    static void post_process_free_data(PPData ppd) {
        ppd.links_to_ignore = null;
    }

    /**
     * Builds and array of domain types in the named post processor.
     * <p>
     * TODO - Should this reference this.pp_node.d_type_array? - jlr
     * @param pp 
     */
    static void build_type_array(Postprocessor pp) {
        DTypeList dtl;
        int d;
        ListOfLinks lol;
        for (d = 0; d < pp.pp_data.N_domains; d++) {
            for (lol = pp.pp_data.domain_array[d].lol; lol != null; lol = lol.next) {
                dtl = new DTypeList();
                dtl.next = pp.pp_node.d_type_array[lol.link];
                pp.pp_node.d_type_array[lol.link] = dtl;
                dtl.type = pp.pp_data.domain_array[d].type;
            }
        }
    }

}
