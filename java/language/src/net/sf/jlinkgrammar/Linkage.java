package net.sf.jlinkgrammar;

import java.util.StringTokenizer;

/**
 * The linkage routines are primarily used to walk linkages and retrieve information
 * about the sentence. You must fist call dictionary_create, sentence,_create, sentence_parse,
 * in that order.  Then you can process links.
 *@see Linkage#Linkage(int, Sentence, ParseOptions)
 */
public class Linkage {
    /**
     * number of (tokenized) words
     */
    public int num_words;
    /**
     * array of word spellings
     */
    public String word[];
    /**
     * index and cost information
     */
    public LinkageInfo info;
    /**
     * One for thin linkages, bigger for fat 
     */
    public int num_sublinkages;
    /**
     * Allows user to select particular sublinkage
     */
    public int current;
    /**
     * A parse with conjunctions will have several
     */
    public Sublinkage sublinkage[];
    /**
     * if true, union of links has been computed 
     */
    public boolean unionized; 
    public Sentence sent;
    public ParseOptions opts;

    /* the following are all for generating postscript */
    private static int word_used[][] = new int[GlobalBean.MAXSUBL][GlobalBean.MAX_SENTENCE];
    /* tells the height of the links above the sentence */
    private static int link_heights[] = new int[GlobalBean.MAX_LINKS];
    /* the word beginning each row of the display */
    private static int row_starts[] = new int[GlobalBean.MAX_SENTENCE];
    /* the number of rows */
    private static int N_rows;
    /* version of N_words in this file for printing links */
    private static int N_words_to_print;

    private static int center[] = new int[GlobalBean.MAX_SENTENCE];
    // TODO - make constituent a linked list - jlr
    private static Constituent constituent[] = new Constituent[GlobalBean.MAXCONSTITUENTS];
    private static int templist[] = new int[100];
    private static int r_limit = 0;

    static class LinkageAndList {
        int num;
        int e[] = new int[10];
        boolean valid;
    };

    public static LinkageAndList andlist[] = new LinkageAndList[1024];

    public static int wordtype[] = new int[GlobalBean.MAX_SENTENCE];

    /**
     * setter for object's copy of the sentence
     * @param sent
     */
    public void setSentence(Sentence sent) {
        this.sent = sent;
    }
    
    /**
     * setter for object's copy of the ParseOptions
     * @param opts
     */
    public void setParseOptions(ParseOptions opts) {
        this.opts = opts;
    }
     
        
    /*
     * Create a linkage object and copy the sentence and parse option into it.
     * One potential problem is changing parse options in one linkage does not propogate
     * to other linkages as well. Call it a design decision but you should be aware that post
     * processing one linkage with a differrent set of options will not propogate those
     * parse options to other linkages.  For those of you looking for the linkage_create function
     * from the C API this is it.  Use Linkage myLinkage = new Linkage(int, Sentence, ParseOptions); where
     * you formerly used: Linkage l; l = linkage_create(int, Sentence, ParseOptions);
     *
     * Caveat - The creator function calls post process. If you want a non-standard post-processor
     * you will have to call linkage_post_porcessor(PostProcessor p) yourself.
     * 
     * Each sentence has an array of linkages.  To find out how many call
     * num = mySentence.sentence_num_linkages_found() on your instantiated sentence.  You can then
     * loop from 0 to num and then  call
     *
     * @see linkage_get_num_sublinkages()
     * @see linkage_set_current_subinkage(int index)
     * linkage_compute_union()
     * linkage_get_num_words()
     * linkage_get_num_links()
     * linkage_get_link_length(int index) - number of words spanned by link
     * linkage_get_link_lword(int link)
     * linkage_get_link_rword(int link)
     * linkage_get_link_label(int link)
     * linkage_get_link_llabel(int link)
     * linkage_get_link_rlabel(int link)
     * linkage_get_word(int wordFromLink)
     *
     * TODO - There is some sloppy coding (or thinking in this creator) the sent argument overides this.sent
     * but this.sent is coppied at the beginning of the method and not at the end.  Therefore the
     * object's sentence (this.sent) may be out of sync with the sentence it analyzes.  Also new linakges
     * can alter the enclosing sentence object. Some better object work needs to be done here.
     *
     * @param k create a new linkage and give it the index k
     * @param sent  set the linkage objects private copy of the sentence to sent
     * @param opts  set the linkage objects private copy of the ParseOptions to opts
     * @return if the index is valid it creates a new linkage if the link already exists
     * it throws a runtime exception "index out of range"
     *
     */ 
    

    
    public Linkage(int k, Sentence sent, ParseOptions opts) {

        if (!((k < sent.num_linkages_post_processed) && (k >= 0))) {
            throw new RuntimeException("index out of range");
        }
        for (int i = 0; i < constituent.length; i++) {
            constituent[i] = new Constituent();
        }
        for (int i = 0; i < andlist.length; i++) {
            andlist[i] = new LinkageAndList();
        }

        /* Using exalloc since this is external to the parser itself. */

        num_words = sent.length;
        word = new String[num_words];
        current = 0;
        num_sublinkages = 0;
        sublinkage = null;
        unionized = false;
        this.sent = sent;
        this.opts = opts;
        info = sent.link_info[k];

        extract_links(sent.link_info[k].index, sent.null_count, sent.parse_info);
        compute_chosen_words(sent);

        if (sent.set_has_fat_down()) {
            extract_fat_linkage(sent, opts);
        } else {
            extract_thin_linkage(sent, opts);
        }

        if (sent.dict.postprocessor != null) {
            linkage_post_process(sent.dict.postprocessor);
        }

    }

    void compute_chosen_words(Sentence sent) {
        /*
           This takes the current chosen_disjuncts array and uses it to
           compute the chosen_words array.  "I.xx" suffixes are eliminated.
           */
        int i, l;
        String s, t, u;
        ParseInfo pi = sent.parse_info;
        String chosen_words[] = new String[GlobalBean.MAX_SENTENCE];

        for (i = 0; i < sent.length; i++) { /* get rid of those ugly ".Ixx" */
            chosen_words[i] = sent.word[i].string;
            if (pi.chosen_disjuncts[i] == null) {
                /* no disjunct is used on this word because of null-links */
                t = "[" + chosen_words[i] + "]";
                chosen_words[i] = t;
            } else if (opts.display_word_subscripts) {
                t = pi.chosen_disjuncts[i].string;
                if (Dictionary.is_idiom_word(t)) {
                    int idx = t.indexOf('.');
                    if (idx >= 0) {
                        t = t.substring(0, idx);
                    }
                    chosen_words[i] = t;
                } else {
                    chosen_words[i] = t;
                }
            }
        }
        if (sent.dict.left_wall_defined) {
            chosen_words[0] = GlobalBean.LEFT_WALL_DISPLAY;
        }
        if (sent.dict.right_wall_defined) {
            chosen_words[sent.length - 1] = GlobalBean.RIGHT_WALL_DISPLAY;
        }
        for (i = 0; i < num_words; ++i) {
            word[i] = chosen_words[i];
        }
    }

    /**
     * Generate the list of all links of the indexth parsing of the
           sentence.  For this to work, you must have already called parse, and
           already built the whole_set.
     */
    static void extract_links(int index, int cost, ParseInfo pi) {
        
        pi.initialize_links();
        if (index < 0) {
            MyRandom.my_random_initialize(index);
            pi.list_random_links(pi.parse_set);
            MyRandom.my_random_finalize();
        } else {
            pi.list_links(pi.parse_set, index);
        }
    }
    /**
         * extract thin links from a sentence
         *
         * @param sent  sentence to extract links from. 
         * TODO - does not set the current Linkage objects this.sent to sent. This may be a bug!
         * @param opts options to use when processing
         * TODO - does not set the current Linkage objects this.opts to opts.  This may be a bug!   
         */
    void extract_thin_linkage(Sentence sent, ParseOptions opts) {
        int i;
        ParseInfo pi = sent.parse_info;

        sublinkage = new Sublinkage[1];
        sublinkage[0] = new Sublinkage(pi);
        sent.compute_link_names();
        for (i = 0; i < pi.N_links; i++) {
            sublinkage[0].link[i] = pi.link_array[i];
            // TODO: copy_full=link(&sublinkage.link[i],&(pi.link_array[i]));
        }

        num_sublinkages = 1;
    }

        /**
         * 
         * The old C comment said -- This procedure mimics analyze_fat_linkage in order to 
           extract the sublinkages and copy them to the Linkage
           data structure passed in.  BUT no linkage structure is passed in the
         * Linkage structure is the instance variable of this Linkage.  
         *
         * @param sent  sentence to extract links from. 
         * TODO - does not set the current Linkage objects this.sent to sent. This may be a bug!
         * @param opts options to use when processing
         * TODO - does not set the current Linkage objects this.opts to opts.  This may be a bug!   
         */
    public void extract_fat_linkage(Sentence sent, ParseOptions opts) {
        
        int i, j, N_thin_links;
        DISNode d_root;
        Sublinkage sublinkage;
        ParseInfo pi = sent.parse_info;

        sublinkage = new Sublinkage(pi);
        pi.build_digraph();
        Sentence.structure_violation = false;
        d_root = pi.build_DIS_CON_tree();

        if (Sentence.structure_violation) {
            sent.compute_link_names();
            for (i = 0; i < pi.N_links; i++) {
                // TODO: copy_full_link(&sublinkage.link[i],&(pi.link_array[i]));
                sublinkage.link[i] = pi.link_array[i];
            }

            num_sublinkages = 1;
            this.sublinkage = new Sublinkage[1];
            this.sublinkage[0] = new Sublinkage(pi);

            /* This will have fat links! */
            for (i = 0; i < pi.N_links; ++i) {
                this.sublinkage[0].link[i] = sublinkage.link[i];
            }

            return;
        }

        /* first get number of sublinkages and allocate space */
        num_sublinkages = 0;
        for (;;) {
            num_sublinkages++;
            if (!d_root.advance_DIS())
                break;
        }

        this.sublinkage = new Sublinkage[num_sublinkages];
        for (i = 0; i < num_sublinkages; ++i) {
            this.sublinkage[i] = new Sublinkage();
            this.sublinkage[i].pp_info = null;
            this.sublinkage[i].violation = null;
        }

        /* now fill out the sublinkage arrays */
        sent.compute_link_names();

        int idx = 0;
        for (;;) {
            for (i = 0; i < pi.N_links; i++) {
                sent.patch_array[i].used = sent.patch_array[i].changed = false;
                sent.patch_array[i].newl = pi.link_array[i].l;
                sent.patch_array[i].newr = pi.link_array[i].r;
                // TODO:copy_full_link(& sublinkage.link[i], & (pi.link_array[i]));
                //sublinkage.link[i] = pi.link_array[i];
                sublinkage.link[i] = new Link();
                sublinkage.link[i].l = pi.link_array[i].l;
                sublinkage.link[i].lc = pi.link_array[i].lc;
                sublinkage.link[i].r = pi.link_array[i].r;
                sublinkage.link[i].rc = pi.link_array[i].rc;
                sublinkage.link[i].name = pi.link_array[i].name;
            }
            sent.fill_patch_array_DIS(d_root, null);

            for (i = 0; i < pi.N_links; i++) {
                if (sent.patch_array[i].changed || sent.patch_array[i].used) {
                    sublinkage.link[i].l = sent.patch_array[i].newl;
                    sublinkage.link[i].r = sent.patch_array[i].newr;
                } else if (
                    (ParseInfo.dfs_root_word[pi.link_array[i].l] != -1)
                        && (ParseInfo.dfs_root_word[pi.link_array[i].r] != -1)) {
                    sublinkage.link[i].l = -1;
                }
            }

            sent.compute_pp_link_array_connectors(sublinkage);
            sent.compute_pp_link_names(sublinkage);

            /* Don't copy the fat links into the linkage */
            N_thin_links = 0;
            for (i = 0; i < pi.N_links; ++i) {
                if (sublinkage.link[i].l == -1)
                    continue;
                N_thin_links++;
            }

            this.sublinkage[idx].num_links = N_thin_links;
            this.sublinkage[idx].link = new Link[N_thin_links];
            this.sublinkage[idx].pp_info = null;
            this.sublinkage[idx].violation = null;

            for (i = 0, j = 0; i < pi.N_links; ++i) {
                if (sublinkage.link[i].l == -1)
                    continue;
                this.sublinkage[idx].link[j++] = sublinkage.link[i];
            }

            idx++;
            if (!d_root.advance_DIS())
                break;
        }
    }

    /**
     * 
     * @return an integer value of the unused word cost
     */
    public int linkage_unused_word_cost() {
        return info.unused_word_cost;
    }

    /**
     * 
     * @return an integer value of the disjunct cost
     */
    public int linkage_disjunct_cost() {
        return info.disjunct_cost;
    }
    
    /**
     * 
     * @return the cos of And's
     */
    public int linkage_and_cost() {
        return info.and_cost;
    }
    
    /**
     * 
     * @return the link cost
     */
    public int linkage_link_cost() {
        return info.link_cost;
    }
    
    /**
     * This is a toplevel routine used to setup a for loop
     * to process each sublinkage (usually an AND disjunct).
     * @return the number of sublinkages
     */
    public int linkage_get_num_sublinkages() {
        return num_sublinkages;
    }
    
    /**
     * Gives the number or word spellings or individual word spellings for the
     * current sublinkage
     * @return integer number of spellings
     */
    public int linkage_get_num_words() {
        return num_words;
    }
    
    /**
     * This is need to set up the for loop to walk down a set of 
     * links for printing or other processing
     * returns the size of the linkage array as  the number of links
     * @return the size of the linkage array as  the number of links
     */
    public int linkage_get_num_links() {
        return sublinkage[current].num_links;
    }
    
    /**
     * A method used to find the current number of linkages.  
     * To create a new linkage you will have to add one to the result of this method
     * It uses a looop to find the next available linkage index
     * 
     * @return the number of current links
     * @see Linkage
     */
    private boolean verify_link_index(int index) {
        if ((index < 0) || (index >= sublinkage[current].num_links)) {
            return false;
        }
        return true;
    }
    
    /**
     * setter function to set the current sublinkage to indicated index
     * TODO - does this function have to insure a compact array? If so
     * should it return the next available index?
     * @return false if the likage index is already in use, true otherwise
     */
    public boolean linkage_set_current_sublinkage(int index) {
        if ((index < 0) || (index >= num_sublinkages)) {
            return false;
        }
        current = index;
        return true;
    }
    
    /**
     * getter for the sentence from an instantiated linkage
     * @return the Sentence instance object held in this Linkage instance object
     */
    public Sentence linkage_get_sentence() {
        return sent;
    }

    boolean links_are_equal(Link l, Link m) {
        return l.l == m.l && l.r == m.r && l.name.equals(m.name);
    }

    boolean link_already_appears(Link link, int a) {
        int i, j;

        for (i = 0; i < a; ++i) {
            for (j = 0; j < sublinkage[i].num_links; ++j) {
                if (links_are_equal(sublinkage[i].link[j], link))
                    return true;
            }
        }
        return false;
    }

    Sublinkage unionize_linkage() {
        int i, j, num_in_union = 0;
        Sublinkage u = new Sublinkage();
        Link link;
        String p;

        for (i = 0; i < num_sublinkages; ++i) {
            for (j = 0; j < sublinkage[i].num_links; ++j) {
                link = sublinkage[i].link[j];
                if (!link_already_appears(link, i))
                    num_in_union++;
            }
        }

        u.num_links = num_in_union;
        u.link = new Link[num_in_union];
        u.pp_info = new PPInfo[num_in_union];
        u.violation = null;

        num_in_union = 0;

        for (i = 0; i < num_sublinkages; ++i) {
            for (j = 0; j < sublinkage[i].num_links; ++j) {
                link = sublinkage[i].link[j];
                if (!link_already_appears(link, i)) {
                    u.link[num_in_union] = link; // TODO: excopy_link(link);
                    u.pp_info[num_in_union] = sublinkage[i].pp_info[j];
                    // TODO: excopy_pp_info(sublinkage[i].pp_info[j]);
                    if (((p = sublinkage[i].violation) != null) && (u.violation == null)) {
                        u.violation = p;
                    }
                    num_in_union++;
                }
            }
        }

        return u;
    }

    /**
     * computes the union of the current sublinkages. Arcs may cross.
     * @return  the integer number of sublinkages 
     */
    public int linkage_compute_union() {
        int i, num_subs = num_sublinkages;
        Sublinkage new_sublinkage[];

        if (unionized) {
            current = num_sublinkages - 1;
            return 0;
        }
        if (num_subs == 1) {
            unionized = true;
            return 1;
        }

        new_sublinkage = new Sublinkage[num_subs + 1];

        for (i = 0; i < num_subs; ++i) {
            new_sublinkage[i] = sublinkage[i];
        }
        sublinkage = new_sublinkage;
        sublinkage[num_subs] = unionize_linkage();

        /* The domain data will not be needed for the union -- zero it out */
        /* SBW removing this, pp_data is null...
        sublinkage[num_subs].pp_data.N_domains = 0;
        sublinkage[num_subs].pp_data.length = 0;
        sublinkage[num_subs].pp_data.links_to_ignore = null;
        for (i = 0; i < GlobalBean.MAX_SENTENCE; ++i) {
            sublinkage[num_subs].pp_data.word_links[i] = null;
        }*/

        num_sublinkages++;

        unionized = true;
        current = num_sublinkages - 1;
        return 1;
    }

    public void process_linkage(ParseOptions opts) {
        String string;
        int j, mode, first_sublinkage;

        if (opts.parse_options_get_display_union()) {
            linkage_compute_union();
            first_sublinkage = linkage_get_num_sublinkages() - 1;
        } else {
            first_sublinkage = 0;
        }

        for (j = first_sublinkage; j < linkage_get_num_sublinkages(); ++j) {
            linkage_set_current_sublinkage(j);
            if (opts.parse_options_get_display_on()) {
                string = linkage_print_diagram();
                opts.out.println(string);
            }
            if (opts.parse_options_get_display_links()) {
                string = linkage_print_links_and_domains();
                opts.out.print(string);
            }
            if (opts.parse_options_get_display_postscript()) {
                string = linkage_print_postscript(0);
                opts.out.println(string);
            }
        }
        if ((mode = opts.parse_options_get_display_constituents()) != 0) {
            string = linkage_print_constituent_tree(mode);
            if (string != null) {
                opts.out.println(string);
            } else {
                opts.out.println("Can't generate constituents.");
                opts.out.println("Constituent processing has been turned off.");
            }
        }
    }

    public String linkage_get_word(int w) {
        return word[w];
    }

    public String linkage_get_link_label(int index) {
        Link link;
        if (!verify_link_index(index))
            return null;
        link = sublinkage[current].link[index];
        return link.name;
    }

    public String linkage_get_link_llabel(int index) {
        Link link;
        if (!verify_link_index(index))
            return null;
        link = sublinkage[current].link[index];
        return link.lc.string;
    }

    public String linkage_get_link_rlabel(int index) {
        Link link;
        if (!verify_link_index(index))
            return null;
        link = sublinkage[current].link[index];
        return link.rc.string;
    }

    public int linkage_get_link_rword(int index) {
        Link link;
        if (!verify_link_index(index))
            return -1;
        link = sublinkage[current].link[index];
        return link.r;
    }

    public int linkage_get_link_lword(int index) {
        Link link;
        if (!verify_link_index(index))
            return -1;
        link = sublinkage[current].link[index];
        return link.l;
    }

    public int linkage_get_link_num_domains(int index) {
        PPInfo pp_info;
        if (!verify_link_index(index))
            return -1;
        pp_info = sublinkage[current].pp_info[index];
        return pp_info.num_domains;
    }

    public String[] linkage_get_link_domain_names(int index) {
        PPInfo pp_info;
        if (!verify_link_index(index))
            return null;
        pp_info = sublinkage[current].pp_info[index];
        return pp_info.domain_name;
    }

    public String linkage_get_violation_name() {
        return sublinkage[current].violation;
    }

    public boolean linkage_is_canonical() {
        return info.canonical;
    }

    public boolean linkage_is_improper() {
        return info.improper_fat_linkage;
    }

    public boolean linkage_has_inconsistent_domains() {
        return info.inconsistent_domains;
    }

    public String linkage_print_links_and_domains() {
        /* To the left of each link, print the sequence of domains it is in. */
        /* Printing a domain means printing its type                         */
        /* Takes info from pp_link_array and pp and chosen_words.            */
        int link, longest, j;
        int N_links = linkage_get_num_links();
        StringBuffer s = new StringBuffer();
        String links_string;
        String dname[];

        longest = 0;
        for (link = 0; link < N_links; link++) {
            if (linkage_get_link_lword(link) == -1)
                continue;
            if (linkage_get_link_num_domains(link) > longest)
                longest = linkage_get_link_num_domains(link);
        }
        for (link = 0; link < N_links; link++) {
            if (linkage_get_link_lword(link) == -1)
                continue;
            dname = linkage_get_link_domain_names(link);
            for (j = 0; j < linkage_get_link_num_domains(link); ++j) {
                s.append("(");
                s.append(dname[j]);
                s.append(")");
            }
            for (; j < longest; j++) {
                s.append("    ");
            }
            s.append("   ");
            print_a_link(s, link);
        }
        s.append("\n");
        if (linkage_get_violation_name() != null) {
            s.append("P.P. violations:\n");
            s.append("        ");
            s.append(linkage_get_violation_name());
            s.append("\n\n");
        }

        links_string = s.toString();

        return links_string;
    }

    /**
     * Prints the information contained in a link into a supplied string buffer.
     *
     * @param s Empty stringbuffer supplied by the caller shich the method will use
     * @param link  the index of the link
     * 
     */
    public void print_a_link(StringBuffer s, int link) {
        Sentence sent = linkage_get_sentence();
        Dictionary dict = sent.dict;
        int l, r;
        String label, llabel, rlabel;

        l = linkage_get_link_lword(link);
        r = linkage_get_link_rword(link);
        label = linkage_get_link_label(link);
        llabel = linkage_get_link_llabel(link);
        rlabel = linkage_get_link_rlabel(link);

        if ((l == 0) && dict.left_wall_defined) {
            GlobalBean.left_append_string(s, GlobalBean.LEFT_WALL_DISPLAY, "               ");
        } else if ((l == (linkage_get_num_words() - 1)) && dict.right_wall_defined) {
            GlobalBean.left_append_string(s, GlobalBean.RIGHT_WALL_DISPLAY, "               ");
        } else {
            GlobalBean.left_append_string(s, linkage_get_word(l), "               ");
        }
        GlobalBean.left_append_string(s, llabel, "     ");
        s.append("   <---");
        GlobalBean.left_append_string(s, label, "-----");
        s.append("->  ");
        GlobalBean.left_append_string(s, rlabel, "     ");
        s.append("     ");
        s.append(linkage_get_word(r));
        s.append("\n");
    }

    /**
     * builds a postscript printable string of the current sublinkage
     */
    public String build_linkage_postscript_string() {
        int link, i, j;
        int d;
        boolean print_word_0 = false, print_word_N = false;
        int N_wall_connectors;
        boolean suppressor_used;
        Sublinkage sublinkage = this.sublinkage[current];
        int N_links = sublinkage.num_links;
        Link ppla[] = sublinkage.link;
        StringBuffer string;
        String ps_string;
        Dictionary dict = sent.dict;

        string = new StringBuffer();

        N_wall_connectors = 0;
        if (dict.left_wall_defined) {
            suppressor_used = false;
            if (!opts.display_walls)
                for (j = 0; j < N_links; j++) {
                    if (ppla[j].l == 0) {
                        if (ppla[j].r == num_words - 1)
                            continue;
                        N_wall_connectors++;
                        if (ppla[j].lc.string.equals(GlobalBean.LEFT_WALL_SUPPRESS)) {
                            suppressor_used = true;
                        }
                    }
                }
            print_word_0 =
                (((!suppressor_used) && (N_wall_connectors != 0)) || (N_wall_connectors > 1) || opts.display_walls);
        } else {
            print_word_0 = true;
        }

        N_wall_connectors = 0;
        if (dict.right_wall_defined) {
            suppressor_used = false;
            for (j = 0; j < N_links; j++) {
                if (ppla[j].r == num_words - 1) {
                    N_wall_connectors++;
                    if (ppla[j].lc.string.equals(GlobalBean.RIGHT_WALL_SUPPRESS)) {
                        suppressor_used = true;
                    }
                }
            }
            print_word_N =
                (((!suppressor_used) && (N_wall_connectors != 0)) || (N_wall_connectors > 1) || opts.display_walls);
        } else {
            print_word_N = true;
        }

        if (print_word_0)
            d = 0;
        else
            d = 1;

        i = 0;
        N_words_to_print = num_words;
        if (!print_word_N)
            N_words_to_print--;

        string.append("[");
        for (j = d; j < N_words_to_print; j++) {
            if ((i % 10 == 0) && (i > 0))
                string.append("\n");
            i++;
            string.append("(");
            string.append(word[j]);
            string.append(")");

        }
        string.append("]");
        string.append("\n");

        string.append("[");
        j = 0;
        for (link = 0; link < N_links; link++) {
            if (!print_word_0 && (ppla[link].l == 0))
                continue;
            if (!print_word_N && (ppla[link].r == num_words - 1))
                continue;
            if (ppla[link].l == -1)
                continue;
            if ((j % 7 == 0) && (j > 0))
                string.append("\n");
            j++;
            string.append("[");
            string.append(ppla[link].l - d);
            string.append(" ");
            string.append(ppla[link].r - d);
            string.append(" ");
            string.append(link_heights[link]);
            if (ppla[link].lc.label < 0) {
                string.append(" (");
                string.append(ppla[link].name);
                string.append(")]");
            } else {
                string.append(" ()]");
            }
        }
        string.append("]");
        string.append("\n");
        string.append("[");
        for (j = 0; j < N_rows; j++) {
            if (j > 0) {
                string.append(" ");
                string.append(row_starts[j]);
            } else
                string.append(row_starts[j]);
        }
        string.append("]\n");

        ps_string = string.toString();

        return ps_string;
    }

    public static char picture[][] = new char[GlobalBean.MAX_HEIGHT][GlobalBean.MAX_LINE];
    public static char xpicture[][] = new char[GlobalBean.MAX_HEIGHT][GlobalBean.MAX_LINE];

    public void set_centers(boolean print_word_0) {
        int i, len, tot;
        tot = 0;
        if (print_word_0)
            i = 0;
        else
            i = 1;
        for (; i < N_words_to_print; i++) {
            len = word[i].length();
            center[i] = tot + (len / 2);
            tot += len + 1;
        }
    }

    public String linkage_print_diagram() {
        int i, j, k, cl, cr, row, top_row, width;
        boolean flag;
        int ttt;
        String s;
        boolean print_word_0 = false, print_word_N = false;
        int N_wall_connectors;
        boolean suppressor_used;
        String connector;
        int line_len, link_length;
        Sublinkage sublinkage = this.sublinkage[current];
        int N_links = sublinkage.num_links;
        Link ppla[] = sublinkage.link;
        StringBuffer string;
        String gr_string;
        Dictionary dict = sent.dict;
        int x_screen_width = opts.parse_options_get_screen_width();
        
        string = new StringBuffer();

        N_wall_connectors = 0;
        if (dict.left_wall_defined) {
            suppressor_used = false;
            if (!opts.display_walls)
                for (j = 0; j < N_links; j++) {
                    if (ppla[j].l == 0) {
                        if (ppla[j].r == num_words - 1)
                            continue;
                        N_wall_connectors++;
                        if (ppla[j].lc.string.equals(GlobalBean.LEFT_WALL_SUPPRESS)) {
                            suppressor_used = true;
                        }
                    }
                }
            print_word_0 =
                (((!suppressor_used) && (N_wall_connectors != 0)) || (N_wall_connectors > 1) || opts.display_walls);
        } else {
            print_word_0 = true;
        }

        N_wall_connectors = 0;
        if (dict.right_wall_defined) {
            suppressor_used = false;
            for (j = 0; j < N_links; j++) {
                if (ppla[j].r == num_words - 1) {
                    N_wall_connectors++;
                    if (ppla[j].lc.string.equals(GlobalBean.RIGHT_WALL_SUPPRESS)) {
                        suppressor_used = true;
                    }
                }
            }
            print_word_N =
                (((!suppressor_used) && (N_wall_connectors != 0)) || (N_wall_connectors > 1) || opts.display_walls);
        } else {
            print_word_N = true;
        }

        N_words_to_print = num_words;
        if (!print_word_N)
            N_words_to_print--;

        set_centers(print_word_0);
        line_len = center[N_words_to_print - 1] + 1;

        for (k = 0; k < GlobalBean.MAX_HEIGHT; k++) {
            for (j = 0; j < line_len; j++)
                picture[k][j] = ' ';
            picture[k][line_len] = '\0';
        }
        top_row = 0;

        for (link_length = 1; link_length < N_words_to_print; link_length++) {
            for (j = 0; j < N_links; j++) {
                if (ppla[j].l == -1)
                    continue;
                if ((ppla[j].r - ppla[j].l) != link_length)
                    continue;
                /* gets rid of the irrelevant link to the left wall */
                if (!print_word_0 && (ppla[j].l == 0))
                    continue;
                /* gets rid of the irrelevant link to the right wall */
                if (!print_word_N && (ppla[j].r == num_words - 1))
                    continue;

                /* put it into the lowest position */
                cl = center[ppla[j].l];
                cr = center[ppla[j].r];
                for (row = 0; row < GlobalBean.MAX_HEIGHT; row++) {
                    for (k = cl + 1; k < cr; k++) {
                        if (picture[row][k] != ' ')
                            break;
                    }
                    if (k == cr)
                        break;
                }
                /* we know it fits, so put it in this row */

                link_heights[j] = row;

                if (2 * row + 2 > GlobalBean.MAX_HEIGHT - 1) {
                    string.append("The diagram is too high.\n");
                    return string.toString();
                }
                if (row > top_row)
                    top_row = row;

                picture[row][cl] = '+';
                picture[row][cr] = '+';
                for (k = cl + 1; k < cr; k++) {
                    picture[row][k] = '-';
                }
                s = ppla[j].name;

                if (opts.display_link_subscripts) {
                    if (!Character.isLetter(s.charAt(0)))
                        s = "";
                } else {
                    if (!Character.isUpperCase(s.charAt(0))) {
                        s = ""; /* Don't print fat link connector name */
                    }
                }
                connector = s;
                k = 0;
                if (opts.display_link_subscripts)
                    k = connector.length();
                else
                    while (k < connector.length() && Character.isUpperCase(connector.charAt(k)))
                        k++; /* uppercase len of conn*/
                if ((cl + cr - k) / 2 + 1 <= cl) {
                    ttt = cl + 1;
                } else {
                    ttt = (cl + cr - k) / 2 + 1;
                }
                s = connector;
                int sss = 0;
                if (opts.display_link_subscripts) {
                    while (sss < s.length() && picture[row][ttt] == '-') {
                        picture[row][ttt] = s.charAt(sss);
                        ttt++;
                        sss++;
                    }
                } else {
                    while (sss < s.length() && Character.isUpperCase(s.charAt(sss)) && picture[row][ttt] == '-') {
                        picture[row][ttt] = s.charAt(sss);
                        ttt++;
                        sss++;
                    }
                }

                /* now put in the | below this one, where needed */
                for (k = 0; k < row; k++) {
                    if (picture[k][cl] == ' ') {
                        picture[k][cl] = '|';
                    }
                    if (picture[k][cr] == ' ') {
                        picture[k][cr] = '|';
                    }
                }
            }
        }

        /* we have the link picture, now put in the words and extra "|"s */

        int sss = 0;
        if (print_word_0)
            k = 0;
        else
            k = 1;
        for (; k < N_words_to_print; k++) {
            ttt = 0;
            i = 0;
            while (ttt < word[k].length()) {
                xpicture[0][sss] = word[k].charAt(ttt);
                sss++;
                ttt++;
                i++;
            }
            xpicture[0][sss] = ' ';
            sss++;
        }
        xpicture[0][sss] = '\0';

        if (opts.display_short) {
            for (k = 0; picture[0][k] != '\0'; k++) {
                if ((picture[0][k] == '+') || (picture[0][k] == '|')) {
                    xpicture[1][k] = '|';
                } else {
                    xpicture[1][k] = ' ';
                }
            }
            xpicture[1][k] = '\0';
            for (row = 0; row <= top_row; row++) {
                k = 0;
                while (picture[row][k] != '\0') {
                    xpicture[row + 2][k] = picture[row][k];
                    k++;
                }
            }
            top_row = top_row + 2;
        } else {
            for (row = 0; row <= top_row; row++) {
                k = 0;
                while (picture[row][k] != '\0') {
                    xpicture[2 * row + 2][k] = picture[row][k];
                    k++;
                }
                for (k = 0; picture[row][k] != '\0'; k++) {
                    if ((picture[row][k] == '+') || (picture[row][k] == '|')) {
                        xpicture[2 * row + 1][k] = '|';
                    } else {
                        xpicture[2 * row + 1][k] = ' ';
                    }
                }
                xpicture[2 * row + 1][k] = '\0';
            }
            top_row = 2 * top_row + 2;
        }

        /* we've built the picture, now print it out */

        if (print_word_0)
            i = 0;
        else
            i = 1;
        k = 0;
        N_rows = 0;
        row_starts[N_rows] = 0;
        N_rows++;
        while (i < N_words_to_print) {
            string.append("\n");
            width = 0;
            do {
                width += word[i].length() + 1;
                i++;
            } while ((i < N_words_to_print) && (word[i].length() + 1) + 1 < x_screen_width);
            row_starts[N_rows] = i - (print_word_0 ? 0 : 1); /* PS junk */
            if (i < N_words_to_print)
                N_rows++; /* same */
            for (row = top_row; row >= 0; row--) {
                flag = true;
                for (j = k; flag && (j < k + width) && (xpicture[row][j] != '\0'); j++) {
                    flag = flag && (xpicture[row][j] == ' ');
                }
                if (!flag) {
                    for (j = k;(j < k + width) && (xpicture[row][j] != '\0'); j++) {
                        string.append(xpicture[row][j]);
                    }
                    string.append("\n");
                }
            }
            string.append("\n");
            k += width;
        }
        gr_string = string.toString();
        return gr_string;
    }

    /**
     * 
     * String linkage_print_constituent_tree(int mode)
     * @param mode mode 1: treebank-style constituent tree
     * mode 2: flat, bracketed tree [A like [B this B] A] 
     * mode 3: flat, treebank-style tree (A like (B this) )
     * @return Returns the constituent tree in one of three modes, treebank, flat bracketed, or flat treebank
     */

    public String linkage_print_constituent_tree(int mode) {
        StringBuffer cs;
        CNode root;
        String p;

        if (mode == 0 || sent.dict.constituent_pp == null) {
            return null;
        } else if (mode == 1 || mode == 3) {
            cs = new StringBuffer();
            root = linkage_constituent_tree();
            print_tree(cs, (mode == 1), root, 0, 0);
            cs.append("\n");
            return cs.toString();
        } else if (mode == 2) {
            return print_flat_constituents();
        }
        throw new RuntimeException("Illegal mode in linkage_print_constituent_tree");
    }

    int token_type(String token) {
        if (token.charAt(0) == GlobalBean.OPEN_BRACKET && token.length() > 1)
            return GlobalBean.CType_OPEN;
        if (token.length() > 1 && token.charAt(token.length() - 1) == GlobalBean.CLOSE_BRACKET)
            return GlobalBean.CType_CLOSE;
        return GlobalBean.CType_WORD;
    }

    public CNode parse_string(CNode n, StringTokenizer tok) {
        String q;
        CNode m, last_child = null;

        while (tok.hasMoreTokens()) {
            q = tok.nextToken();
            switch (token_type(q)) {
                case GlobalBean.CType_CLOSE :
                    q = q.substring(0, q.length() - 1);
                    if (!q.equals(n.label)) {
                        throw new RuntimeException("Constituent tree: Labels do not match.");
                    }
                    return n;
                case GlobalBean.CType_OPEN :
                    m = new CNode(q.substring(1));
                    m = parse_string(m, tok);
                    break;
                case GlobalBean.CType_WORD :
                    m = new CNode(q);
                    break;
                default :
                    throw new RuntimeException("Constituent tree: Illegal token type");
            }
            if (n.child == null) {
                last_child = n.child = m;
            } else {
                last_child.next = m;
                last_child = m;
            }
        }
        throw new RuntimeException("Constituent tree: Constituent did not close");
    }

    /**
     * CNode linkage_constituent_tree();
     * 
     * 
     * The other useful method for dealing with a cosntituent tree is:
     * 
     * String linkage_print_constituent_tree(int mode);
     * @return  A CNode is a standard tree data structure. The children of a 
     * node are stored as a linked list, with the end of the list 
     * indicated by next==NULL. The start and end fields of a node 
     * indicate the span of the constituent, with the first word 
     * indexed by 0. Leaves are defined by the condition child==NULL. 
     */
    public CNode linkage_constituent_tree() {
        String p, q;
        int len;
        CNode root;
        p = print_flat_constituents();
        StringTokenizer tok = new StringTokenizer(p);
        q = tok.nextToken();
        if (!(token_type(q) == GlobalBean.CType_OPEN)) {
            throw new RuntimeException("Illegal beginning of string");
        }
        root = new CNode(q.substring(1));
        root = parse_string(root, tok);
        assign_spans(root, 0);
        return root;
    }

    private int assign_spans(CNode n, int start) {
        int num_words = 0;
        CNode m = null;
        if (n == null)
            return 0;
        n.start = start;
        if (n.child == null) {
            n.end = start;
            return 1;
        } else {
            for (m = n.child; m != null; m = m.next) {
                num_words += assign_spans(m, start + num_words);
            }
            n.end = start + num_words - 1;
        }
        return num_words;
    }

    /**
     * Print out a postscript version of the constituent tree
     * @param mode mode 1: treebank-style constituent tree
     *  mode 2: flat, bracketed tree [A like [B this B] A] 
     *  mode 3: flat, treebank-style tree (A like (B this) )
     * @return A Java String version of a postscript file that can be sent to a print device.
     */
    public String linkage_print_postscript(int mode) {
        String ps, qs;
        int size;

        ps = build_linkage_postscript_string();

        return header(mode) + ps + trailer(mode);
    }

    private void count_words_used() {

        /*This function generates a table, word_used[i][w], showing 
          whether each word w is used in each sublinkage i; if so, 
          the value for that cell of the table is 1 */

        int i, w, link, num_subl;

        num_subl = num_sublinkages;
        if (unionized && num_subl > 1)
            num_subl--;

        if (opts.verbosity >= 2)
            opts.out.println("Number of sublinkages = " + num_subl);
        for (i = 0; i < num_subl; i++) {
            for (w = 0; w < num_words; w++)
                word_used[i][w] = 0;
            current = i;
            for (link = 0; link < linkage_get_num_links(); link++) {
                word_used[i][linkage_get_link_lword(link)] = 1;
                word_used[i][linkage_get_link_rword(link)] = 1;
            }
            if (opts.verbosity >= 2) {
                opts.out.print("Sublinkage " + i + ": ");
                for (w = 0; w < num_words; w++) {
                    if (word_used[i][w] == 0)
                        opts.out.print("0 ");
                    if (word_used[i][w] == 1)
                        opts.out.print("1 ");
                }
                opts.out.println();
            }
        }
    }

    private String print_flat_constituents() {
        int num_words;
        Sentence sent;
        Postprocessor pp;
        int s, numcon_total, numcon_subl, num_subl;
        String q;

        sent = linkage_get_sentence();
        pp = sent.dict.constituent_pp;
        numcon_total = 0;

        count_words_used();

        num_subl = num_sublinkages;
        if (num_subl > GlobalBean.MAXSUBL) {
            num_subl = GlobalBean.MAXSUBL;
            if (opts.verbosity >= 2)
                opts.out.println(
                    "Number of sublinkages exceeds maximum: only considering first " + GlobalBean.MAXSUBL + " sublinkages");
        }
        if (unionized && num_subl > 1)
            num_subl--;
        for (s = 0; s < num_subl; s++) {
            linkage_set_current_sublinkage(s);
            linkage_post_process(pp);
            num_words = linkage_get_num_words();
            generate_misc_word_info();
            numcon_subl = read_constituents_from_domains(numcon_total, s);
            numcon_total = numcon_total + numcon_subl;
        }
        numcon_total = merge_constituents(numcon_total);
        numcon_total = last_minute_fixes(numcon_total);
        q = exprint_constituent_structure(numcon_total);
        return q;
    }

    private int add_constituent(int cons, Domain domain, int l, int r, String name) {
        int c = cons;
        c++;

        /* Avoid running off end to walls **PV**/
        if (l < 1)
            l = 1;
        if (r > r_limit)
            r = r_limit;
        if (!(l <= r)) {
            throw new RuntimeException("negative constituent length!");
        }

        constituent[c].left = l;
        constituent[c].right = r;
        constituent[c].domain_type = domain.type;
        constituent[c].start_link = linkage_get_link_label(domain.start_link);
        constituent[c].start_num = domain.start_link;
        constituent[c].type = name;
        return c;
    }
    

    private String cons_of_domain(int domain_type) {
        switch (domain_type) {
            case 'a' :
                return "ADJP";
            case 'b' :
                return "SBAR";
            case 'c' :
                return "VP";
            case 'd' :
                return "QP";
            case 'e' :
                return "ADVP";
            case 'f' :
                return "SBAR";
            case 'g' :
                return "PP";
            case 'h' :
                return "QP";
            case 'i' :
                return "ADVP";
            case 'k' :
                return "PRT";
            case 'n' :
                return "NP";
            case 'p' :
                return "PP";
            case 'q' :
                return "SINV";
            case 's' :
                return "S";
            case 't' :
                return "VP";
            case 'u' :
                return "ADJP";
            case 'v' :
                return "VP";
            case 'y' :
                return "NP";
            case 'z' :
                return "VP";
            default :
                throw new RuntimeException("Illegal domain: " + domain_type);

        }
    }

    private String exprint_constituent_structure(int numcon_total) {
        int c, w;
        int leftdone[] = new int[GlobalBean.MAXCONSTITUENTS];
        int rightdone[] = new int[GlobalBean.MAXCONSTITUENTS];
        int best, bestright, bestleft;
        String s;
        String p;
        StringBuffer cs = new StringBuffer();

        if (numcon_total >= GlobalBean.MAXCONSTITUENTS)
            throw new RuntimeException("Too many constituents");

        for (c = 0; c < numcon_total; c++) {
            leftdone[c] = 0;
            rightdone[c] = 0;
        }

        if (opts.verbosity >= 2)
            opts.out.println();

        for (w = 1; w < num_words; w++) {
            /* Skip left wall; don't skip right wall, since it may 
               have constituent boundaries */

            while (true) {
                best = -1;
                bestright = -1;
                for (c = 0; c < numcon_total; c++) {
                    if ((constituent[c].left == w)
                        && (leftdone[c] == 0)
                        && (constituent[c].valid == 1)
                        && (constituent[c].right >= bestright)) {
                        best = c;
                        bestright = constituent[c].right;
                    }
                }
                if (best == -1)
                    break;
                leftdone[best] = 1;
                if (constituent[best].aux == 1)
                    continue;
                cs.append(GlobalBean.OPEN_BRACKET);
                cs.append(constituent[best].type);
                cs.append(" ");
            }

            if (w < num_words - 1) {
                /* Don't print out right wall */
                s = sent.word[w].string;

                /* Now, if the first character of the word was 
                   originally uppercase, we put it back that way */
                if (sent.word[w].firstupper) {

                    cs.append(Character.toUpperCase(s.charAt(0)));
                    cs.append(s.substring(1));
                } else {
                    cs.append(s);
                }
                cs.append(" ");
            }

            while (true) {
                best = -1;
                bestleft = -1;
                for (c = 0; c < numcon_total; c++) {
                    if ((constituent[c].right == w)
                        && (rightdone[c] == 0)
                        && (constituent[c].valid == 1)
                        && (constituent[c].left > bestleft)) {
                        best = c;
                        bestleft = constituent[c].left;
                    }
                }
                if (best == -1)
                    break;
                rightdone[best] = 1;
                if (constituent[best].aux == 1)
                    continue;
                cs.append(constituent[best].type);
                cs.append(GlobalBean.CLOSE_BRACKET);
                cs.append(" ");
            }
        }

        cs.append("\n");
        return cs.toString();
    }

    private int gen_comp(int numcon_total, int numcon_subl, String ctype1, String ctype2, String ctype3, int x) {

        /* This function looks for constituents of type ctype1. Say it finds 
           one, call it c1. It searches for the next larger constituent of 
           type ctype2, call it c2. It then generates a new constituent of 
           ctype3, containing all the words in c2 but not c1. */

        int w, w2, w3, c, c1, c2, done;
        c = numcon_total + numcon_subl;

        for (c1 = numcon_total; c1 < numcon_total + numcon_subl; c1++) {

            /* If ctype1 is NP, it has to be an appositive to continue */
            if ((x == 4) && !Postprocessor.post_process_match("MX#*", constituent[c1].start_link))
                continue;

            /* If ctype1 is X, and domain_type is t, it's an infinitive - skip it */
            if ((x == 2) && (constituent[c1].domain_type == 't'))
                continue;

            /* If it's domain-type z, it's a subject-relative Clause; 
               the VP doesn't need an NP */
            if (constituent[c1].domain_type == 'z')
                continue;

            /* If ctype1 is X or VP, and it's not started by an S, don't generate an NP
             (Neither of the two previous checks are necessary now, right?) */
            if ((x == 1 || x == 2)
                && ((!Postprocessor.post_process_match("S", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("SX", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("SF", constituent[c1].start_link))
                    || Postprocessor.post_process_match("S##w", constituent[c1].start_link)))
                continue;

            /* If it's an SBAR (relative Clause case), it has to be a relative Clause */
            if ((x == 3)
                && (!Postprocessor.post_process_match("Rn", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("R*", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("MX#r", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("Mr", constituent[c1].start_link)
                    && !Postprocessor.post_process_match("MX#d", constituent[c1].start_link)))
                continue;

            /* If ctype1 is SBAR (Clause opener case), it has to be an f domain */
            if ((x == 5) && (constituent[c1].domain_type != 'f'))
                continue;

            /* If ctype1 is SBAR (pp opener case), it has to be a g domain */
            if ((x == 6) && (constituent[c1].domain_type != 'g'))
                continue;

            /* If ctype1 is NP (paraphrase case), it has to be started by an SI */
            if ((x == 7) && !Postprocessor.post_process_match("SI", constituent[c1].start_link))
                continue;

            /* If ctype1 is VP (participle modifier case), it has to be 
               started by an Mv or Mg */
            if ((x == 8) && !Postprocessor.post_process_match("M", constituent[c1].start_link))
                continue;

            /* If ctype1 is VP (participle opener case), it has 
               to be started by a COp */
            if ((x == 9) && !Postprocessor.post_process_match("COp", constituent[c1].start_link))
                continue;

            /* Now start at the bounds of c1, and work outwards until you 
               find a larger constituent of type ctype2 */
            if (!(constituent[c1].type.equals(ctype1)))
                continue;

            if (opts.verbosity >= 2)
                opts.out.print("Generating complement constituent for c " + c1 + " of type " + ctype1);
            done = 0;
            for (w2 = constituent[c1].left;(done == 0) && (w2 >= 0); w2--) {
                for (w3 = constituent[c1].right; w3 < num_words; w3++) {
                    for (c2 = numcon_total;(done == 0) && (c2 < numcon_total + numcon_subl); c2++) {
                        if (!((constituent[c2].left == w2) && (constituent[c2].right == w3)) || (c2 == c1))
                            continue;
                        if (!(constituent[c2].type.equals(ctype2)))
                            continue;

                        /* if the new constituent (c) is to the left 
                           of c1, its right edge should be adjacent to the 
                           left edge of c1 - or as close as possible 
                           without going outside the current sublinkage. 
                           (Or substituting right and left as necessary.) */

                        if ((x == 5) || (x == 6) || (x == 9)) {
                            /* This is the case where c is to the 
                               RIGHT of c1 */
                            w = constituent[c1].right + 1;
                            while (true) {
                                if (word_used[current][w] == 1)
                                    break;
                                w++;
                            }
                            if (w > constituent[c2].right) {
                                done = 1;
                                continue;
                            }
                            constituent[c].left = w;
                            constituent[c].right = constituent[c2].right;
                        } else {
                            w = constituent[c1].left - 1;
                            while (true) {
                                if (word_used[current][w] == 1)
                                    break;
                                w--;
                            }
                            if (w < constituent[c2].left) {
                                done = 1;
                                continue;
                            }
                            constituent[c].right = w;
                            constituent[c].left = constituent[c2].left;
                        }

                        adjust_for_left_comma(c1);
                        adjust_for_right_comma(c1);

                        constituent[c].type = ctype3;
                        constituent[c].domain_type = 'x';
                        constituent[c].start_link = "XX";
                        constituent[c].start_num = constituent[c1].start_num; /* bogus */
                        if (opts.verbosity >= 2) {
                            opts.out.print("Larger c found: c " + c2 + " (" + ctype2 + "); ");
                            opts.out.println("Adding constituent:");
                            print_constituent(c);
                        }
                        c++;
                        if (!(c < GlobalBean.MAXCONSTITUENTS)) {
                            throw new RuntimeException("Too many constituents");
                        }
                        done = 1;
                    }
                }
            }
            if (opts.verbosity >= 2) {
                if (done == 0)
                    opts.out.println("No constituent added, because no larger " + ctype2 + " was found");
            }
        }
        numcon_subl = c - numcon_total;
        return numcon_subl;
    }

    public void adjust_subordinate_clauses(int numcon_total, int numcon_subl) {

        /* Look for a constituent started by an MVs or MVg. 
           Find any VP's or ADJP's that contain it (without going 
           beyond a larger S or NP). Adjust them so that 
           they end right before the m domain starts. */

        int c, w, c2, w2, done;

        for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
            if (Postprocessor.post_process_match("MVs", constituent[c].start_link)
                || Postprocessor.post_process_match("MVg", constituent[c].start_link)) {
                done = 0;
                for (w2 = constituent[c].left - 1;(done == 0) && w2 >= 0; w2--) {
                    for (c2 = numcon_total; c2 < numcon_total + numcon_subl; c2++) {
                        if (!((constituent[c2].left == w2) && (constituent[c2].right >= constituent[c].right)))
                            continue;
                        if ((constituent[c2].type.equals("S")) || (constituent[c2].type.equals("NP"))) {
                            done = 1;
                            break;
                        }
                        if ((constituent[c2].domain_type == 'v') || (constituent[c2].domain_type == 'a')) {
                            w = constituent[c].left - 1;
                            while (true) {
                                if (word_used[current][w] == 1)
                                    break;
                                w--;
                            }
                            constituent[c2].right = w;

                            if (opts.verbosity >= 2)
                                opts.out.println("Adjusting constituent " + c2 + ":");
                            print_constituent(c2);
                        }
                    }
                }
                if (word[constituent[c].left].equals(","))
                    constituent[c].left++;
            }
        }
    }

    private void print_constituent(int c) {
        int w;
        /* Sentence sent;
           sent = linkage_get_sentence(linkage); **PV* using linkage.word not sent.word */
        if (opts.verbosity < 2)
            return;
        opts.out.print(
            "  c "
                + c
                + " "
                + constituent[c].type
                + " ["
                + constituent[c].domain_type
                + "] ("
                + constituent[c].left
                + "-"
                + constituent[c].right
                + "): ");
        for (w = constituent[c].left; w <= constituent[c].right; w++) {
            opts.out.print(word[w]); /**PV**/
            opts.out.print(" ");
        }
        opts.out.println();
    }

    private void adjust_for_left_comma(int c) {

        /* If a constituent c has a comma at either end, we exclude the
           comma. (We continue to shift the boundary until we get to
           something inside the current sublinkage) */
        int w;

        w = constituent[c].left;
        if (word[constituent[c].left].equals(",")) {
            w++;
            while (true) {
                if (word_used[current][w] == 1)
                    break;
                w++;
            }
        }
        constituent[c].left = w;
    }

    private void adjust_for_right_comma(int c) {

        int w;
        w = constituent[c].right;
        if ((word[constituent[c].right].equals(",")) || (word[constituent[c].right].equals("RIGHT-WALL"))) {
            w--;
            while (true) {
                if (word_used[current][w] == 1)
                    break;
                w--;
            }
        }
        constituent[c].right = w;
    }

    private void print_tree(StringBuffer cs, boolean indent, CNode n, int o1, int o2) {
        int i, child_offset;
        CNode m;

        if (n == null)
            return;

        if (indent)
            for (i = 0; i < o1; ++i)
                cs.append(" ");
        cs.append("(");
        cs.append(n.label);
        cs.append(" ");
        child_offset = o2 + n.label.length() + 2;

        for (m = n.child; m != null; m = m.next) {
            if (m.child == null) {
                cs.append(m.label);
                if ((m.next != null) && (m.next.child == null))
                    cs.append(" ");
            } else {
                if (m != n.child) {
                    if (indent)
                        cs.append("\n");
                    else
                        cs.append(" ");
                    print_tree(cs, indent, m, child_offset, child_offset);
                } else {
                    print_tree(cs, indent, m, 0, child_offset);
                }
                if ((m.next != null) && (m.next.child == null)) {
                    if (indent) {
                        cs.append("\n");
                        for (i = 0; i < child_offset; ++i)
                            cs.append(" ");
                    } else
                        cs.append(" ");
                }
            }
        }
        cs.append(")");
    }

    private int read_constituents_from_domains(int numcon_total, int s) {

        int d, c, leftlimit, l, leftmost, rightmost, w, c2, numcon_subl = 0, w2;
        ListOfLinks dlink;
        int rootright, rootleft, adjustment_made;
        Sublinkage subl;
        String name;
        Domain domain;

        r_limit = num_words - 2; /**PV**/

        subl = sublinkage[s];

        for (d = 0, c = numcon_total; d < subl.pp_data.N_domains; d++, c++) {
            domain = subl.pp_data.domain_array[d];
            rootright = linkage_get_link_rword(domain.start_link);
            rootleft = linkage_get_link_lword(domain.start_link);

            if ((domain.type == 'c')
                || (domain.type == 'd')
                || (domain.type == 'e')
                || (domain.type == 'f')
                || (domain.type == 'g')
                || (domain.type == 'u')
                || (domain.type == 'y')) {
                leftlimit = 0;
                leftmost = linkage_get_link_lword(domain.start_link);
                rightmost = linkage_get_link_lword(domain.start_link);
            } else {
                leftlimit = linkage_get_link_lword(domain.start_link) + 1;
                leftmost = linkage_get_link_rword(domain.start_link);
                rightmost = linkage_get_link_rword(domain.start_link);
            }

            /* Start by assigning both left and right limits to the 
               right word of the start link. This will always be contained 
               in the constituent. This will also handle the case 
               where the domain contains no links. */

            for (dlink = domain.lol; dlink != null; dlink = dlink.next) {
                l = dlink.link;

                if ((linkage_get_link_lword(l) < leftmost) && (linkage_get_link_lword(l) >= leftlimit))
                    leftmost = linkage_get_link_lword(l);

                if (linkage_get_link_rword(l) > rightmost)
                    rightmost = linkage_get_link_rword(l);
            }

            c--;
            c = add_constituent(c, domain, leftmost, rightmost, cons_of_domain(domain.type));

            if (domain.type == 'z') {
                c = add_constituent(c, domain, leftmost, rightmost, "S");
            }
            if (domain.type == 'c') {
                c = add_constituent(c, domain, leftmost, rightmost, "S");
            }
            if (Postprocessor.post_process_match("Ce*", constituent[c].start_link)
                || Postprocessor.post_process_match("Rn", constituent[c].start_link)) {
                c = add_constituent(c, domain, leftmost, rightmost, "SBAR");
            }
            if (Postprocessor.post_process_match("R*", constituent[c].start_link)
                || Postprocessor.post_process_match("MX#r", constituent[c].start_link)) {
                w = leftmost;
                if (word[w].equals(","))
                    w++;
                c = add_constituent(c, domain, w, w, "WHNP");
            }
            if (Postprocessor.post_process_match("Mj", constituent[c].start_link)) {
                w = leftmost;
                if (word[w].equals(","))
                    w++;
                c = add_constituent(c, domain, w, w + 1, "WHPP");
                c = add_constituent(c, domain, w + 1, w + 1, "WHNP");
            }
            if (Postprocessor.post_process_match("Ss#d", constituent[c].start_link)
                || Postprocessor.post_process_match("B#d", constituent[c].start_link)) {
                c = add_constituent(c, domain, rootleft, rootleft, "WHNP");
                c = add_constituent(c, domain, rootleft, constituent[c - 1].right, "SBAR");
            }
            if (Postprocessor.post_process_match("CP", constituent[c].start_link)) {
                if (word[leftmost].equals(","))
                    constituent[c].left++;
                c = add_constituent(c, domain, 1, num_words - 1, "S");
            }
            if (Postprocessor.post_process_match("MVs", constituent[c].start_link) || (domain.type == 'f')) {
                w = constituent[c].left;
                if (word[w].equals(","))
                    w++;
                if (word[w].equals("when")) {
                    c = add_constituent(c, domain, w, w, "WHADVP");
                }
            }
            if (domain.type == 't') {
                c = add_constituent(c, domain, leftmost, rightmost, "S");
            }
            if (Postprocessor.post_process_match("QI", constituent[c].start_link)
                || Postprocessor.post_process_match("Mr", constituent[c].start_link)
                || Postprocessor.post_process_match("MX#d", constituent[c].start_link)) {
                w = leftmost;
                if (word[w].equals(","))
                    w++;
                if (wordtype[w] == GlobalBean.WType_NONE)
                    name = "WHADVP";
                else if (wordtype[w] == GlobalBean.WType_QTYPE)
                    name = "WHNP";
                else if (wordtype[w] == GlobalBean.WType_QDTYPE)
                    name = "WHNP";
                else if (true) {
                    throw new RuntimeException("Unexpected word type");
                }
                c = add_constituent(c, domain, w, w, name);

                if (wordtype[w] == GlobalBean.WType_QDTYPE) {
                    /* Now find the finite verb to the right, start an S */
                    /*PV* limited w2 to sentence len*/
                    for (w2 = w + 1; w2 < r_limit - 1; w2++)
                        if ((wordtype[w2] == GlobalBean.WType_STYPE) || (wordtype[w2] == GlobalBean.WType_PTYPE))
                            break;
                    /* adjust the right boundary of previous constituent */
                    constituent[c].right = w2 - 1;
                    c = add_constituent(c, domain, w2, rightmost, "S");
                }
            }

            if (constituent[c].domain_type == '\0') {
                throw new RuntimeException("Error: no domain type assigned to constituent\n");
            }
            if (constituent[c].start_link == null) {
                throw new RuntimeException("Error: no type assigned to constituent\n");
            }
        }

        numcon_subl = c - numcon_total;
        /* numcon_subl = handle_islands(linkage, numcon_total, numcon_subl);  */

        if (opts.verbosity >= 2)
            opts.out.println("Constituents added at first stage for subl " + current + ":");
        for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
            print_constituent(c);
        }

        /* Opener case - generates S around main Clause. 
           (This must be done first; the S generated will be needed for 
           later cases.) */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "SBAR", "S", "S", 5);

        /* pp opener case */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "PP", "S", "S", 6);

        /* participle opener case */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "S", "S", "S", 9);

        /* Subject-phrase case; every main VP generates an S */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "VP", "S", "NP", 1);

        /* Relative Clause case; an SBAR generates a complement NP */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "SBAR", "NP", "NP", 3);

        /* Participle modifier case */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "VP", "NP", "NP", 8);

        /* PP modifying NP */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "PP", "NP", "NP", 8);

        /* Appositive case */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "NP", "NP", "NP", 4);

        /* S-V inversion case; an NP generates a complement VP */
        numcon_subl = gen_comp(numcon_total, numcon_subl, "NP", "SINV", "VP", 7);

        adjust_subordinate_clauses(numcon_total, numcon_subl);
        for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
            if ((constituent[c].domain_type == 'p') && (word[constituent[c].left].equals(","))) {
                constituent[c].left++;
            }
        }

        /* Make sure the constituents are nested. If two constituents are not nested: whichever 
           constituent has the furthest left boundary, shift that boundary rightwards to the left 
           boundary of the other one */

        while (true) {
            adjustment_made = 0;
            for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
                for (c2 = numcon_total; c2 < numcon_total + numcon_subl; c2++) {
                    if ((constituent[c].left < constituent[c2].left)
                        && (constituent[c].right < constituent[c2].right)
                        && (constituent[c].right >= constituent[c2].left)) {

                        /* We've found two overlapping constituents. 
                           If one is larger, except the smaller one
                           includes an extra comma, adjust the smaller one 
                           to exclude the comma */

                        if ((word[constituent[c2].right].equals(","))
                            || (word[constituent[c2].right].equals("RIGHT-WALL"))) {
                            if (opts.verbosity >= 2)
                                opts.out.println("Adjusting " + c2 + " to fix comma overlap");
                            adjust_for_right_comma(c2);
                            adjustment_made = 1;
                        } else if (word[constituent[c].left].equals(",")) {
                            if (opts.verbosity >= 2)
                                opts.out.println("Adjusting c " + c + " to fix comma overlap");
                            adjust_for_left_comma(c);
                            adjustment_made = 1;
                        } else {
                            if (opts.verbosity >= 2) {
                                opts.out.println(
                                    "WARNING: the constituents aren't nested! Adjusting them.(" + c + ", " + c2 + ")");
                            }
                            constituent[c].left = constituent[c2].left;
                        }
                    }
                }
            }
            if (adjustment_made == 0)
                break;
        }

        /* This labels certain words as auxiliaries (such as forms of "be" 
           with passives, forms of "have" wth past participles, 
           "to" with infinitives). These words start VP's which include
           them. In Treebank I, these don't get printed unless they're part of an 
           andlist, in which case they get labeled "X". (this is why we need to 
           label them as "aux".) In Treebank II, however, they seem to be treated 
           just like other verbs, so the "aux" stuff isn't needed. */

        for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
            constituent[c].subl = current;
            if (((constituent[c].domain_type == 'v')
                && (wordtype[linkage_get_link_rword(constituent[c].start_num)] == GlobalBean.WType_PTYPE))
                || ((constituent[c].domain_type == 't') && (constituent[c].type.equals("VP")))) {
                constituent[c].aux = 1;
            } else
                constituent[c].aux = 0;
        }

        for (c = numcon_total; c < numcon_total + numcon_subl; c++) {
            constituent[c].subl = current;
            constituent[c].aux = 0;
        }

        return numcon_subl;
    }

    private int find_next_element(int start, int numcon_total, int num_elements, int num_lists) {
        /* Here we're looking for the next andlist element to add on 
           to a conjectural andlist, stored in the array templist.
           We go through the constituents, starting at "start". */

        int c, a, ok, c2, c3, addedone = 0, n;

        n = num_lists;
        for (c = start + 1; c < numcon_total; c++) {
            if (constituent[c].valid == 0)
                continue;
            if (!constituent[templist[0]].type.equals(constituent[c].type))
                continue;
            ok = 1;

            /* We're considering adding constituent c to the andlist. 
               If c is in the same sublinkage as one of the other andlist 
               elements, don't add it. If it overlaps with one of the other 
               constituents, don't add it. If there's a constituent
               identical to c that occurs in a sublinkage in which one of 
               the other elements occurs, don't add it. */

            for (a = 0; a < num_elements; a++) {
                if (constituent[c].subl == constituent[templist[a]].subl)
                    ok = 0;
                if (((constituent[c].left < constituent[templist[a]].left)
                    && (constituent[c].right > constituent[templist[a]].left))
                    || ((constituent[c].right > constituent[templist[a]].right)
                        && (constituent[c].left < constituent[templist[a]].right))
                    || ((constituent[c].right > constituent[templist[a]].right)
                        && (constituent[c].left < constituent[templist[a]].right))
                    || ((constituent[c].left > constituent[templist[a]].left)
                        && (constituent[c].right < constituent[templist[a]].right)))
                    ok = 0;
                for (c2 = 0; c2 < numcon_total; c2++) {
                    if (constituent[c2].canon != constituent[c].canon)
                        continue;
                    for (c3 = 0; c3 < numcon_total; c3++) {
                        if ((constituent[c3].canon == constituent[templist[a]].canon)
                            && (constituent[c3].subl == constituent[c2].subl))
                            ok = 0;
                    }
                }
            }
            if (ok == 0)
                continue;
            templist[num_elements] = c;
            addedone = 1;
            num_lists = find_next_element(c, numcon_total, num_elements + 1, num_lists);
        }
        if (addedone == 0 && num_elements > 1) {
            for (a = 0; a < num_elements; a++) {
                andlist[num_lists].e[a] = templist[a];
                andlist[num_lists].num = num_elements;
            }
            num_lists++;
        }
        return num_lists;
    }

    public static boolean uppercompare(String s, String t) {
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

    private void generate_misc_word_info() {

        /* Go through all the words. If a word is on the right end of 
           an S (or SF or SX), wordtype[w]=STYPE.  If it's also on the left end of a 
           Pg*b, I, PP, or Pv, wordtype[w]=PTYPE. If it's a question-word 
           used in an indirect question, wordtype[w]=QTYPE. If it's a 
           question-word determiner,  wordtype[w]=QDTYPE. Else wordtype[w]=NONE. 
           (This function is called once for each sublinkage.) */

        int l1, l2, w1, w2;
        String label1, label2;

        for (w1 = 0; w1 < num_words; w1++)
            wordtype[w1] = GlobalBean.WType_NONE;

        for (l1 = 0; l1 < linkage_get_num_links(); l1++) {
            w1 = linkage_get_link_rword(l1);
            label1 = linkage_get_link_label(l1);
            if (uppercompare(label1, "S") || uppercompare(label1, "SX") || uppercompare(label1, "SF")) {
                wordtype[w1] = GlobalBean.WType_STYPE;
                for (l2 = 0; l2 < linkage_get_num_links(); l2++) {
                    w2 = linkage_get_link_lword(l2);
                    label2 = linkage_get_link_label(l2);
                    if ((w1 == w2)
                        && (Postprocessor.post_process_match("Pg#b", label2)
                            || uppercompare(label2, "I")
                            || uppercompare(label2, "PP")
                            || Postprocessor.post_process_match("Pv", label2))) {
                        /* Pvf, Pgf? */
                        wordtype[w1] = GlobalBean.WType_PTYPE;
                    }
                }
            }
            if (Postprocessor.post_process_match("QI#d", label1)) {
                wordtype[w1] = GlobalBean.WType_QDTYPE;
                for (l2 = 0; l2 < linkage_get_num_links(); l2++) {
                    w2 = linkage_get_link_lword(l2);
                    label2 = linkage_get_link_label(l2);
                    if ((w1 == w2) && (Postprocessor.post_process_match("D##w", label2))) {
                        wordtype[w1] = GlobalBean.WType_QDTYPE;
                    }
                }
            }
            if (Postprocessor.post_process_match("Mr", label1))
                wordtype[w1] = GlobalBean.WType_QDTYPE;
            if (Postprocessor.post_process_match("MX#d", label1))
                wordtype[w1] = GlobalBean.WType_QDTYPE;
        }
    }

    private int last_minute_fixes(int numcon_total) {

        int c, c2, global_leftend_found, adjustment_made, global_rightend_found, lastword, newcon_total = 0;

        for (c = 0; c < numcon_total; c++) {

            /* In a paraphrase construction ("John ran, he said"), 
               the paraphrasing Clause doesn't get
               an S. (This is true in Treebank II, not Treebank I) */

            if (uppercompare(constituent[c].start_link, "CP")) {
                constituent[c].valid = 0;
            }

            /* If it's a possessive with an "'s", the NP on the left 
               should be extended to include the "'s". */
            if (uppercompare(constituent[c].start_link, "YS") || uppercompare(constituent[c].start_link, "YP")) {
                constituent[c].right++;
            }

            /* If a constituent has starting link MVpn, it's a time 
               expression like "last week"; label it as a noun phrase 
               (incorrectly) */

            if (constituent[c].start_link.equals("MVpn")) {
                constituent[c].type = "NP";
            }
            if (constituent[c].start_link.equals("COn")) {
                constituent[c].type = "NP";
            }
            if (constituent[c].start_link.equals("Mpn")) {
                constituent[c].type = "NP";
            }

            /* If the constituent is an S started by "but" or "and" at 
               the beginning of the sentence, it should be ignored. */

            if ((constituent[c].start_link.equals("Wdc")) && (constituent[c].left == 2)) {
                constituent[c].valid = 0;
            }

            /* For prenominal adjectives, an ADJP constituent is assigned 
               if it's a hyphenated (Ah) or comparative (Am) adjective; 
               otherwise no ADJP is assigned, unless the phrase is more
               than one word long (e.g. "very big"). The same with certain 
               types of adverbs. */
            /* That was for Treebank I. For Treebank II, the rule only 
               seems to apply to prenominal adjectives (of all kinds). 
               However, it also applies to number expressions ("QP"). */

            if (Postprocessor.post_process_match("A", constituent[c].start_link)
                || (constituent[c].domain_type == 'd')
                || (constituent[c].domain_type == 'h')) {
                if (constituent[c].right - constituent[c].left == 0) {
                    constituent[c].valid = 0;
                }
            }

            if ((constituent[c].domain_type == 'h') && (word[constituent[c].left - 1].equals("$"))) {
                constituent[c].left--;
            }

            /* If a constituent has type VP and its aux value is 2, 
               this means it's an aux that should be printed; change its 
               type to "X". If its aux value is 1, set "valid" to 0. (This
               applies to Treebank I only) */

            if (constituent[c].aux == 2) {
                constituent[c].type = "X";
            }
            if (constituent[c].aux == 1) {
                constituent[c].valid = 0;
            }
        }

        numcon_total = numcon_total + newcon_total;

        /* If there's a global S constituent that includes everything 
           except a final period or question mark, extend it by one word */

        for (c = 0; c < numcon_total; c++) {
            if ((constituent[c].right == (num_words) - 3)
                && (constituent[c].left == 1)
                && (constituent[c].type.equals("S"))
                && (sent.word[(num_words) - 2].string.equals(".")))
                constituent[c].right++;
        }

        /* If there's no S boundary at the very left end of the sentence, 
           or the very right end, create a new S spanning the entire sentence */

        lastword = (num_words) - 2;
        global_leftend_found = 0;
        global_rightend_found = 0;
        for (c = 0; c < numcon_total; c++) {
            if ((constituent[c].left == 1) && (constituent[c].type.equals("S")) && (constituent[c].valid == 1))
                global_leftend_found = 1;
        }
        for (c = 0; c < numcon_total; c++) {
            if ((constituent[c].right >= lastword) && (constituent[c].type.equals("S")) && (constituent[c].valid == 1))
                global_rightend_found = 1;
        }
        if ((global_leftend_found == 0) || (global_rightend_found == 0)) {
            c = numcon_total;
            constituent[c].left = 1;
            constituent[c].right = num_words - 1;
            constituent[c].type = "S";
            constituent[c].valid = 1;
            constituent[c].domain_type = 'x';
            numcon_total++;
            if (opts.verbosity >= 2)
                opts.out.println("Adding global sentence constituent:");
            print_constituent(c);
        }

        /* Check once more to see if constituents are nested (checking BETWEEN sublinkages
           this time) */

        while (true) {
            adjustment_made = 0;
            for (c = 0; c < numcon_total; c++) {
                if (constituent[c].valid == 0)
                    continue;
                for (c2 = 0; c2 < numcon_total; c2++) {
                    if (constituent[c2].valid == 0)
                        continue;
                    if ((constituent[c].left < constituent[c2].left)
                        && (constituent[c].right < constituent[c2].right)
                        && (constituent[c].right >= constituent[c2].left)) {

                        if (opts.verbosity >= 2) {
                            opts.out.println(
                                "WARNING: the constituents aren't nested! Adjusting them. (" + c + ", " + c2 + ")");
                        }
                        constituent[c].left = constituent[c2].left;
                    }
                }
            }
            if (adjustment_made == 0)
                break;
        }
        return numcon_total;
    }

    private int merge_constituents(int numcon_total) {

        int c1, c2 = 0, c3, ok, a, n, a2, n2, match, listmatch, a3;
        int num_lists, num_elements;
        int leftend, rightend;

        for (c1 = 0; c1 < numcon_total; c1++) {
            constituent[c1].valid = 1;
            /* Find and invalidate any constituents with negative length */
            if (constituent[c1].right < constituent[c1].left) {
                if (opts.verbosity >= 2)
                    opts.out.println("WARNING: Constituent " + c1 + " has negative length. Deleting it.");
                constituent[c1].valid = 0;
            }
            constituent[c1].canon = c1;
        }

        /* First go through and give each constituent a canonical number 
           (the index number of the lowest-numbered constituent 
           identical to it) */

        for (c1 = 0; c1 < numcon_total; c1++) {
            if (constituent[c1].canon != c1)
                continue;
            for (c2 = c1 + 1; c2 < numcon_total; c2++) {
                if ((constituent[c1].left == constituent[c2].left)
                    && (constituent[c1].right == constituent[c2].right)
                    && (constituent[c1].type.equals(constituent[c2].type))) {
                    constituent[c2].canon = c1;
                }
            }
        }

        /* If constituents A and B in different sublinkages X and Y 
           have one endpoint in common, but A is larger at the other end, 
           and B has no duplicate in X, then declare B invalid. (Example: 
           " [A [B We saw the cat B] and the dog A] " */

        for (c1 = 0; c1 < numcon_total; c1++) {
            if (constituent[c1].valid == 0)
                continue;
            for (c2 = 0; c2 < numcon_total; c2++) {
                if (constituent[c2].subl == constituent[c1].subl)
                    continue;
                ok = 1;
                /* Does c2 have a duplicate in the sublinkage containing c1? 
                   If so, bag it */
                for (c3 = 0; c3 < numcon_total; c3++) {
                    if ((constituent[c2].canon == constituent[c3].canon)
                        && (constituent[c3].subl == constituent[c1].subl))
                        ok = 0;
                }
                for (c3 = 0; c3 < numcon_total; c3++) {
                    if ((constituent[c1].canon == constituent[c3].canon)
                        && (constituent[c3].subl == constituent[c2].subl))
                        ok = 0;
                }
                if (ok == 0)
                    continue;
                if ((constituent[c1].left == constituent[c2].left)
                    && (constituent[c1].right > constituent[c2].right)
                    && (constituent[c1].type.equals(constituent[c2].type))) {
                    constituent[c2].valid = 0;
                }

                if ((constituent[c1].left < constituent[c2].left)
                    && (constituent[c1].right == constituent[c2].right)
                    && (constituent[c1].type.equals(constituent[c2].type))) {
                    constituent[c2].valid = 0;
                }
            }
        }

        /* Now go through and find duplicates; if a pair is found, 
           mark one as invalid. (It doesn't matter if they're in the 
           same sublinkage or not) */

        for (c1 = 0; c1 < numcon_total; c1++) {
            if (constituent[c1].valid == 0)
                continue;
            for (c2 = c1 + 1; c2 < numcon_total; c2++) {
                if (constituent[c2].canon == constituent[c1].canon)
                    constituent[c2].valid = 0;
            }
        }

        /* Now we generate the and-lists. An and-list is a set of mutually 
           exclusive constituents. Each constituent in the list may not 
           be present in the same sublinkage as any of the others. */

        num_lists = 0;
        for (c1 = 0; c1 < numcon_total; c1++) {
            if (constituent[c1].valid == 0)
                continue;
            num_elements = 1;
            templist[0] = c1;
            num_lists = find_next_element(c1, numcon_total, num_elements, num_lists);
        }

        if (opts.verbosity >= 2) {
            opts.out.println("And-lists:");
            for (n = 0; n < num_lists; n++) {
                opts.out.print("  " + n + ": ");
                for (a = 0; a < andlist[n].num; a++) {
                    opts.out.print("" + andlist[n].e[a] + " ");
                }
                opts.out.println();
            }
        }

        /* Now we prune out any andlists that are subsumed by other 
           andlists--e.g. if andlist X contains constituents A and B, 
           and Y contains A B and C, we throw out X */

        for (n = 0; n < num_lists; n++) {
            andlist[n].valid = true;
            for (n2 = 0; n2 < num_lists; n2++) {
                if (n2 == n)
                    continue;
                if (andlist[n2].num < andlist[n].num)
                    continue;
                listmatch = 1;
                for (a = 0; a < andlist[n].num; a++) {
                    match = 0;
                    for (a2 = 0; a2 < andlist[n2].num; a2++) {
                        if (andlist[n2].e[a2] == andlist[n].e[a])
                            match = 1;
                    }
                    if (match == 0)
                        listmatch = 0;
                    /* At least one element was not matched by n2 */
                }
                if (listmatch == 1)
                    andlist[n].valid = false;
            }
        }

        /* If an element of an andlist contains an element of another 
           andlist, it must contain the entire andlist. */

        for (n = 0; n < num_lists; n++) {
            if (!andlist[n].valid)
                continue;
            for (a = 0;(a < andlist[n].num) && (andlist[n].valid); a++) {
                for (n2 = 0;(n2 < num_lists) && (andlist[n].valid); n2++) {
                    if ((n2 == n) || (!andlist[n2].valid))
                        continue;
                    for (a2 = 0;(a2 < andlist[n2].num) && (andlist[n].valid); a2++) {
                        c1 = andlist[n].e[a];
                        c2 = andlist[n2].e[a2];
                        if (c1 == c2)
                            continue;
                        if (!((constituent[c2].left <= constituent[c1].left)
                            && (constituent[c2].right >= constituent[c1].right)))
                            continue;
                        if (opts.verbosity >= 2)
                            opts.out.println(
                                "Found that c" + c2 + " in list " + n2 + "  is bigger than c" + c1 + " in list " + n);
                        ok = 1;

                        /* An element of n2 contains an element of n. 
                           Now, we check to see if that element of n2 
                           contains ALL the elements of n. 
                           If not, n is invalid. */

                        for (a3 = 0; a3 < andlist[n].num; a3++) {
                            c3 = andlist[n].e[a3];
                            if ((constituent[c2].left > constituent[c3].left)
                                || (constituent[c2].right < constituent[c3].right))
                                ok = 0;
                        }
                        if (ok != 0)
                            continue;
                        andlist[n].valid = false;
                        if (opts.verbosity >= 2) {
                            opts.out.print(
                                "Eliminating andlist, n=" + n + ", a=" + a + ", n2=" + n2 + ", a2=" + a2 + ": ");
                            for (a3 = 0; a3 < andlist[n].num; a3++) {
                                opts.out.print("" + andlist[n].e[a3] + " ");
                            }
                            opts.out.println();
                        }
                    }
                }
            }
        }

        if (opts.verbosity >= 2) {
            opts.out.println("And-lists after pruning:");
            for (n = 0; n < num_lists; n++) {
                if (!andlist[n].valid)
                    continue;
                opts.out.print("  " + n + ": ");
                for (a = 0; a < andlist[n].num; a++) {
                    opts.out.print("" + andlist[n].e[a] + " ");
                }
                opts.out.println();
            }
        }

        c1 = numcon_total;
        for (n = 0; n < num_lists; n++) {
            if (!andlist[n].valid)
                continue;
            leftend = 256;
            rightend = -1;
            for (a = 0; a < andlist[n].num; a++) {
                c2 = andlist[n].e[a];
                if (constituent[c2].left < leftend) {
                    leftend = constituent[c2].left;
                }
                if (constituent[c2].right > rightend) {
                    rightend = constituent[c2].right;
                }
            }

            constituent[c1].left = leftend;
            constituent[c1].right = rightend;
            constituent[c1].type = constituent[c2].type;
            constituent[c1].domain_type = 'x';
            constituent[c1].valid = 1;
            constituent[c1].start_link = constituent[c2].start_link; /* bogus */
            constituent[c1].start_num = constituent[c2].start_num; /* bogus */

            /* If a constituent within the andlist is an aux (aux==1), 
               set aux for the whole-list constituent to 2, also set 
               aux for the smaller constituent to 2, meaning they'll both
               be printed (as an "X"). (If aux is 2 for the smaller 
               constituent going in, the same thing should be done, 
               though I doubt this ever happens.) */

            for (a = 0; a < andlist[n].num; a++) {
                c2 = andlist[n].e[a];
                if ((constituent[c2].aux == 1) || (constituent[c2].aux == 2)) {
                    constituent[c1].aux = 2;
                    constituent[c2].aux = 2;
                }
            }

            if (opts.verbosity >= 2)
                opts.out.println("Adding constituent:");
            print_constituent(c1);
            c1++;
        }
        numcon_total = c1;
        return numcon_total;
    }

    /**
     *  post process the sublinkages according to the named postprocessor
     */
    public void linkage_post_process(Postprocessor postprocessor) {
        int N_sublinkages = linkage_get_num_sublinkages();
        Sublinkage subl;
        PPNode pp;
        int i, j, k;
        DTypeList d;

        for (i = 0; i < N_sublinkages; ++i) {

            subl = sublinkage[i];
            subl.pp_info = new PPInfo[subl.num_links];
            for (j = 0; j < subl.num_links; j++) {
                subl.pp_info[j] = new PPInfo();
                subl.pp_info[j].num_domains = 0;
                subl.pp_info[j].domain_name = null;
            }
            if (subl.violation != null) {
                subl.violation = null;
            }

            if (info.improper_fat_linkage) {
                pp = null;
            } else {
                pp = sent.post_process(postprocessor, opts, subl, false);
                /* This can return null, for example if there is no
                   post-processor */
            }

            if (pp == null) {
                for (j = 0; j < subl.num_links; ++j) {
                    subl.pp_info[j].num_domains = 0;
                    subl.pp_info[j].domain_name = null;
                }
            } else {
                for (j = 0; j < subl.num_links; ++j) {
                    k = 0;
                    for (d = pp.d_type_array[j]; d != null; d = d.next)
                        k++;
                    subl.pp_info[j].num_domains = k;
                    if (k > 0) {
                        subl.pp_info[j].domain_name = new String[k];
                    }
                    k = 0;
                    for (d = pp.d_type_array[j]; d != null; d = d.next) {
                        char c[] = new char[1];
                        c[0] = (char)d.type;
                        subl.pp_info[j].domain_name[k] = new String(c);
                        k++;
                    }
                }
                subl.pp_data = postprocessor.pp_data;
                if (pp.violation != null) {
                    subl.violation = pp.violation;
                }
            }
        }
        Postprocessor.post_process_close_sentence(postprocessor);
    }

    public String trailer(int mode) {
        String trailer_string = "diagram\n" + "\n" + "%%EndDocument\n";

        if (mode == 1)
            return trailer_string;
        else
            return "";
    }

    /**
     * Supplies the postscript header used when generating a postscript output file
     * @param mode Print out the constituent tree.  
     *  mode 1: treebank-style constituent tree
     *  mode 2: flat, bracketed tree [A like [B this B] A] 
     *  mode 3: flat, treebank-style tree (A like (B this) )
     * @return Retunrs the String of the necessary postscript header.
     */
    public String header(int mode) {
        String header_string =
            "%!PS-Adobe-2.0 EPSF-1.2\n"
                + "%%Pages: 1\n"
                + "%%BoundingBox: 0 -20 500 200\n"
                + "%%EndComments\n"
                + "%%BeginDocument: \n"
                + "\n"
                + "% compute size of diagram by adding\n"
                + "% #rows x 8.5\n"
                + "% (#rows -1) x 10\n"
                + "% \\sum maxheight x 10\n"
                + "/nulllink () def                     % The symbol of a null link\n"
                + "/wordfontsize 11 def      % the size of the word font\n"
                + "/labelfontsize 9 def      % the size of the connector label font\n"
                + "/ex 10 def  % the horizontal radius of all the links\n"
                + "/ey 10 def  % the height of the level 0 links\n"
                + "/ed 10 def  % amount to add to this height per level\n"
                + "/radius 10 def % radius for rounded arcs\n"
                + "/row-spacing 10 def % the space between successive rows of the diagram\n"
                + "\n"
                + "/gap wordfontsize .5 mul def  % the gap between words\n"
                + "/top-of-words wordfontsize .85 mul def\n"
                + "             % the delta y above where the text is written where\n"
                + "             % the major axis of the ellipse is located\n"
                + "/label-gap labelfontsize .1 mul def\n"
                + "\n"
                + "/xwordfontsize 10 def      % the size of the word font\n"
                + "/xlabelfontsize 10 def      % the size of the connector label font\n"
                + "/xex 10 def  % the horizontal radius of all the links\n"
                + "/xey 10 def  % the height of the level 0 links\n"
                + "/xed 10 def  % amount to add to this height per level\n"
                + "/xradius 10 def % radius for rounded arcs\n"
                + "/xrow-spacing 10 def % the space between successive rows of the diagram\n"
                + "/xgap wordfontsize .5 mul def  % the gap between words\n"
                + "\n"
                + "/centerpage 6.5 72 mul 2 div def\n"
                + "  % this number of points from the left margin is the center of page\n"
                + "\n"
                + "/rightpage 6.5 72 mul def\n"
                + "  % number of points from the left margin is the the right margin\n"
                + "\n"
                + "/show-string-centered-dict 5 dict def\n"
                + "\n"
                + "/show-string-centered {\n"
                + "  show-string-centered-dict begin\n"
                + "  /string exch def\n"
                + "  /ycenter exch def\n"
                + "  /xcenter exch def\n"
                + "  xcenter string stringwidth pop 2 div sub\n"
                + "  ycenter labelfontsize .3 mul sub\n"
                + "  moveto\n"
                + "  string show\n"
                + "  end\n"
                + "} def\n"
                + "\n"
                + "/clear-word-box {\n"
                + "  show-string-centered-dict begin\n"
                + "  /string exch def\n"
                + "  /ycenter exch def\n"
                + "  /xcenter exch def\n"
                + "  newpath\n"
                + "  /urx string stringwidth pop 2 div def\n"
                + "  /ury labelfontsize .3 mul def\n"
                + "  xcenter urx sub ycenter ury sub moveto\n"
                + "  xcenter urx add ycenter ury sub lineto\n"
                + "  xcenter urx add ycenter ury add lineto\n"
                + "  xcenter urx sub ycenter ury add lineto\n"
                + "  closepath\n"
                + "  1 setgray fill\n"
                + "  0 setgray\n"
                + "  end\n"
                + "} def\n"
                + "\n"
                + "/diagram-sentence-dict 20 dict def\n"
                + "\n"
                + "/diagram-sentence-circle\n"
                + "{diagram-sentence-dict begin  \n"
                + "   /links exch def\n"
                + "   /words exch def\n"
                + "   /n words length def\n"
                + "   /Times-Roman findfont wordfontsize scalefont setfont\n"
                + "   /x 0 def\n"
                + "   /y 0 def\n"
                + "\n"
                + "   /left-ends [x dup words {stringwidth pop add gap add dup}\n"
                + "                        forall pop pop] def\n"
                + "   /right-ends [x words {stringwidth pop add dup gap add} forall pop] def\n"
                + "   /centers [0 1 n 1 sub {/i exch def\n"
                + "             left-ends i get\n"
                + "             right-ends i get\n"
                + "             add 2 div\n"
                + "           } for ] def\n"
                + "\n"
                + "   x y moveto\n"
                + "   words {show gap 0 rmoveto} forall\n"
                + "\n"
                + "   .5 setlinewidth \n"
                + "\n"
                + "   links {dup 0 get /leftword exch def\n"
                + "          dup 1 get /rightword exch def\n"
                + "          dup 2 get /level exch def\n"
                + "          3 get /string exch def\n"
                + "          newpath\n"
                + "          string nulllink eq {[2] 1 setdash}{[] 0 setdash} ifelse\n"
                + "%          string nulllink eq {.8 setgray}{0 setgray} ifelse\n"
                + "          centers leftword get\n"
                + "     y top-of-words add\n"
                + "          moveto\n"
                + "      \n"
                + "          centers rightword get\n"
                + "          centers leftword get\n"
                + "          sub 2  div dup\n"
                + "          radius \n"
                + "          lt {/radiusx exch def}{pop /radiusx radius def} ifelse\n"
                + "  \n"
                + "          \n"
                + " \n"
                + "          centers leftword get\n"
                + "     y top-of-words add ey ed level mul add add\n"
                + "          centers rightword get\n"
                + "     y top-of-words add ey ed level mul add add\n"
                + "     radiusx\n"
                + "          arcto\n"
                + "          4 {pop} repeat\n"
                + "     centers rightword get\n"
                + "          y top-of-words add ey ed level mul add add\n"
                + "     centers rightword get\n"
                + "     y top-of-words add\n"
                + "     radiusx\n"
                + "     arcto\n"
                + "          4 {pop} repeat\n"
                + "     centers rightword get\n"
                + "     y top-of-words add\n"
                + "     lineto\n"
                + "\n"
                + "     stroke\n"
                + "\n"
                + "          /radius-y    ey ed level mul add   def\n"
                + "\n"
                + "     /center-arc-x\n"
                + "        centers leftword get centers rightword get add 2 div\n"
                + "     def\n"
                + "     \n"
                + "          /center-arc-y\n"
                + "             y top-of-words radius-y add add\n"
                + "     def\n"
                + "\n"
                + "          /Courier-Bold findfont labelfontsize scalefont setfont \n"
                + "     center-arc-x center-arc-y string clear-word-box\n"
                + "     center-arc-x center-arc-y string show-string-centered\n"
                + "          } forall\n"
                + "     end\n"
                + "  } def\n"
                + "\n"
                + "/diagramdict 20 dict def\n"
                + "\n"
                + "/diagram\n"
                + "{diagramdict begin\n"
                + "   /break-words exch def\n"
                + "   /links exch def\n"
                + "   /words exch def\n"
                + "   /n words length def\n"
                + "   /n-rows break-words length def\n"
                + "   /Times-Roman findfont wordfontsize scalefont setfont\n"
                + "\n"
                + "   /left-ends [0 dup words {stringwidth pop add gap add dup}\n"
                + "                        forall pop pop] def\n"
                + "   /right-ends [0 words {stringwidth pop add dup gap add} forall pop] def\n"
                + "\n"
                + "   /lwindows [ break-words {left-ends exch get gap 2 div sub } forall ] def\n"
                + "   /rwindows [1 1 n-rows 1 sub {/i exch def\n"
                + "             lwindows i get } for\n"
                + "                 right-ends n 1 sub get gap 2 div add\n"
                + "         ] def\n"
                + "\n"
                + "\n"
                + "    /max 0 def\n"
                + "    0 1 links length 1 sub {\n"
                + "   /i exch def\n"
                + "   /t links i get 2 get def\n"
                + "   t max gt {/max t def} if\n"
                + "      } for\n"
                + "\n"
                + "    /max-height ed max mul ey add top-of-words add row-spacing add def\n"
                + "    /total-height n-rows max-height mul row-spacing sub def\n"
                + "\n"
                + "    /max-width 0 def            % compute the widest window\n"
                + "    0 1 n-rows 1 sub {\n"
                + "        /i exch def\n"
                + "        /t rwindows i get lwindows i get sub def\n"
                + "        t max-width gt {/max-width t def} if\n"
                + "      } for\n"
                + "\n"
                + "    centerpage max-width 2 div sub 0 translate  % centers it\n"
                + "   % rightpage max-width sub 0 translate      % right justified\n"
                + "                        % Delete both of these to make it left justified\n"
                + "\n"
                + "   n-rows 1 sub -1 0\n"
                + "     {/i exch def\n"
                + "   gsave\n"
                + "   newpath\n"
                + "        %/centering centerpage rwindows i get lwindows i get sub 2 div sub def\n"
                + "               % this line causes each row to be centered\n"
                + "        /centering 0 def\n"
                + "               % set centering to 0 to prevent centering of each row \n"
                + "\n"
                + "   centering -100 moveto  % -100 because some letters go below zero\n"
                + "        centering max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       -100 lineto\n"
                + "   closepath\n"
                + "        clip\n"
                + "   lwindows i get neg n-rows i sub 1 sub max-height mul translate\n"
                + "        centerpage centering 0 translate\n"
                + "        words links diagram-sentence-circle\n"
                + "   grestore\n"
                + "     } for\n"
                + "     end\n"
                + "} def \n"
                + "\n"
                + "/diagramx\n"
                + "{diagramdict begin\n"
                + "   /break-words exch def\n"
                + "   /links exch def\n"
                + "   /words exch def\n"
                + "   /n words length def\n"
                + "   /n-rows break-words length def\n"
                + "   /Times-Roman findfont xwordfontsize scalefont setfont\n"
                + "\n"
                + "   /left-ends [0 dup words {stringwidth pop add gap add dup}\n"
                + "                        forall pop pop] def\n"
                + "   /right-ends [0 words {stringwidth pop add dup gap add} forall pop] def\n"
                + "\n"
                + "   /lwindows [ break-words {left-ends exch get gap 2 div sub } forall ] def\n"
                + "   /rwindows [1 1 n-rows 1 sub {/i exch def\n"
                + "             lwindows i get } for\n"
                + "                 right-ends n 1 sub get xgap 2 div add\n"
                + "         ] def\n"
                + "\n"
                + "\n"
                + "    /max 0 def\n"
                + "    0 1 links length 1 sub {\n"
                + "   /i exch def\n"
                + "   /t links i get 2 get def\n"
                + "   t max gt {/max t def} if\n"
                + "      } for\n"
                + "\n"
                + "    /max-height xed max mul xey add top-of-words add xrow-spacing add def\n"
                + "    /total-height n-rows max-height mul xrow-spacing sub def\n"
                + "\n"
                + "    /max-width 0 def            % compute the widest window\n"
                + "    0 1 n-rows 1 sub {\n"
                + "        /i exch def\n"
                + "        /t rwindows i get lwindows i get sub def\n"
                + "        t max-width gt {/max-width t def} if\n"
                + "      } for\n"
                + "\n"
                + "    centerpage max-width 2 div sub 0 translate  % centers it\n"
                + "   % rightpage max-width sub 0 translate      % right justified\n"
                + "                        % Delete both of these to make it left justified\n"
                + "\n"
                + "   n-rows 1 sub -1 0\n"
                + "     {/i exch def\n"
                + "   gsave\n"
                + "   newpath\n"
                + "        %/centering centerpage rwindows i get lwindows i get sub 2 div sub def\n"
                + "               % this line causes each row to be centered\n"
                + "        /centering 0 def\n"
                + "               % set centering to 0 to prevent centering of each row \n"
                + "\n"
                + "   centering -100 moveto  % -100 because some letters go below zero\n"
                + "        centering max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       -100 lineto\n"
                + "   closepath\n"
                + "        clip\n"
                + "   lwindows i get neg n-rows i sub 1 sub max-height mul translate\n"
                + "        centerpage centering 0 translate\n"
                + "        words links diagram-sentence-circle\n"
                + "   grestore\n"
                + "     } for\n"
                + "     end\n"
                + "} def \n"
                + "\n"
                + "/ldiagram\n"
                + "{diagramdict begin\n"
                + "   /break-words exch def\n"
                + "   /links exch def\n"
                + "   /words exch def\n"
                + "   /n words length def\n"
                + "   /n-rows break-words length def\n"
                + "   /Times-Roman findfont wordfontsize scalefont setfont\n"
                + "\n"
                + "   /left-ends [0 dup words {stringwidth pop add gap add dup}\n"
                + "                        forall pop pop] def\n"
                + "   /right-ends [0 words {stringwidth pop add dup gap add} forall pop] def\n"
                + "\n"
                + "   /lwindows [ break-words {left-ends exch get gap 2 div sub } forall ] def\n"
                + "   /rwindows [1 1 n-rows 1 sub {/i exch def\n"
                + "             lwindows i get } for\n"
                + "                 right-ends n 1 sub get gap 2 div add\n"
                + "         ] def\n"
                + "\n"
                + "\n"
                + "    /max 0 def\n"
                + "    0 1 links length 1 sub {\n"
                + "   /i exch def\n"
                + "   /t links i get 2 get def\n"
                + "   t max gt {/max t def} if\n"
                + "      } for\n"
                + "\n"
                + "    /max-height ed max mul ey add top-of-words add row-spacing add def\n"
                + "    /total-height n-rows max-height mul row-spacing sub def\n"
                + "\n"
                + "    /max-width 0 def            % compute the widest window\n"
                + "    0 1 n-rows 1 sub {\n"
                + "        /i exch def\n"
                + "        /t rwindows i get lwindows i get sub def\n"
                + "        t max-width gt {/max-width t def} if\n"
                + "      } for\n"
                + "\n"
                + "   % centerpage max-width 2 div sub 0 translate  % centers it\n"
                + "   % rightpage max-width sub 0 translate      % right justified\n"
                + "                        % Delete both of these to make it left justified\n"
                + "\n"
                + "   n-rows 1 sub -1 0\n"
                + "     {/i exch def\n"
                + "   gsave\n"
                + "   newpath\n"
                + "        %/centering centerpage rwindows i get lwindows i get sub 2 div sub def\n"
                + "               % this line causes each row to be centered\n"
                + "        /centering 0 def\n"
                + "               % set centering to 0 to prevent centering of each row \n"
                + "\n"
                + "   centering -100 moveto  % -100 because some letters go below zero\n"
                + "        centering max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       max-height n-rows mul lineto\n"
                + "        rwindows i get lwindows i get sub centering add\n"
                + "                       -100 lineto\n"
                + "   closepath\n"
                + "        clip\n"
                + "   lwindows i get neg n-rows i sub 1 sub max-height mul translate\n"
                + "        centerpage centering 0 translate\n"
                + "        words links diagram-sentence-circle\n"
                + "   grestore\n"
                + "     } for\n"
                + "     end\n"
                + "} def \n";
        if (mode == 1)
            return header_string;
        else
            return "";
    }


}
