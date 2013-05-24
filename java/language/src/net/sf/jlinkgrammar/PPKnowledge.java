package net.sf.jlinkgrammar;

import java.io.IOException;
import java.io.Reader;

/**
 *
      I also, unfortunately, want to propose a new type of domain. These
      would include everything that can be reached from the root word of the
      link, to the right, that is closer than the right word of the link.
      (They would not include the link itself.)
 *<p>
    
      In the following sentence, then, the "Urfl_Only Domain" of the G link
      would include only the "O" link:
    <pre>
      +-----G----+    
      +---O--+   +-AI+
      |      |   |   |
      hitting dogs is fun.a 
    </PRE>
      In the following sentence it would include the "O", the "TT", the "I",
      the second "O", and the "A".
    <PRE>
      +----------------G---------------+    
      +-----TT-----+  +-----O-----+    |    
      +---O---+    +-I+    +---A--+    +-AI+
      |       |    |  |    |      |    |   |
      telling people to do stupid things is fun.a 
    </PRE>
      This would allow us to judge the following:
    <p>
      kicking dogs bores me
 *<p>
      *kicking dogs kicks dogs
 *<p>
      explaining the program is easy
 *<p>
      *explaining the program is running
 *<p>
    
      (These are distinctions that I thought we would never be able to make,
      so I told myself they were semantic rather than syntactic. But with
      domains, they should be easy.)
 *<p>
 *Modifications 
 *<p>
       3) when postprocessing a sentence, the links of each domain are
          placed in a set for quick lookup, ('contains one' and 'contains none')
 *<p>
       7) observation: the 'contains one' is, empirically, by far the most 
          violated rule, so it should come first in applying the rules.
    

 *
 */
public class PPKnowledge {
    /** Internal rep'n of sets of strings from knowledge file */
    PPLexTable lt;
    /** Name of file we loaded from */
    String path; 

    /** handles to sets of links specified in knowledge file. These constitute
       auxiliary data, necessary to implement the rules, below. See comments
     in post-process.c for a description of these. */
    PPLinkset domain_starter_links;
    PPLinkset urfl_domain_starter_links;
    PPLinkset urfl_only_domain_starter_links;
    PPLinkset domain_contains_links;
    PPLinkset must_form_a_cycle_links;
    PPLinkset restricted_links;
    PPLinkset ignore_these_links;
    PPLinkset left_domain_starter_links;

    /* arrays of rules specified in knowledge file */
    PPRule connected_rules[], form_a_cycle_rules[];
    PPRule contains_one_rules[], contains_none_rules[];
    PPRule bounded_rules[];

    int n_connected_rules, n_form_a_cycle_rules;
    int n_contains_one_rules, n_contains_none_rules;
    int n_bounded_rules;

    PPLinkset set_of_links_starting_bounded_domain;
    StartingLinkAndDomain starting_link_lookup_table[];
    int nStartingLinks;


    /** 
     *read knowledge from disk into PPKnowledge
     */

    public PPKnowledge(ParseOptions opts, String dictname, String path) throws IOException {
        /*  */
        Reader f = Dictionary.dictopen(opts, dictname, path);
        if (f == null)
            throw new RuntimeException("pp_lexer_open: passed a null file");
        lt = new PPLexTable();
        Reader f1 = f;
        lt.yylex(this, f1);
        lt.idx_of_active_label = -1;
        f.close();
        this.path = path;
        read_starting_link_table();
        read_link_sets(opts);
        read_rules(opts);
        initialize_set_of_links_starting_bounded_domain();
    }

    private void read_rules(ParseOptions opts) {
        read_form_a_cycle_rules(opts, "FORM_A_CYCLE_RULES");
        read_connected_rule(opts, "CONNECTED_RULES");
        read_bounded_rules(opts, "BOUNDED_RULES");
        contains_one_rules = read_contains_rules(opts, "CONTAINS_ONE_RULES");
        n_contains_one_rules = contains_one_rules.length - 1;
        contains_none_rules = read_contains_rules(opts, "CONTAINS_NONE_RULES");
        n_contains_none_rules = contains_none_rules.length - 1;
    }

    /**
     * This is a degenerate class of rules: either a single rule asserting
           connectivity is there, or it isn't. The only information in the
           rule (besides its presence) is the error message to display if 
           the rule is violated */
    private void read_connected_rule(ParseOptions opts, String label) {
        
        connected_rules = new PPRule[1];
        connected_rules[0] = new PPRule();
        if (!lt.pp_lexer_set_label(label)) {
            connected_rules[0].msg = null; /* rule not there */
            if (opts.verbosity > 0)
                opts.out.println("PP warning: Not using 'link is connected' rule");
            return;
        }
        if (lt.pp_lexer_count_tokens_of_label() > 1)
            throw new RuntimeException("post_process: Invalid syntax in " + label);
        connected_rules[0].msg = lt.pp_lexer_get_next_token_of_label();
    }

    private void read_form_a_cycle_rules(ParseOptions opts, String label) {
        int n_commas, n_tokens, r, i;
        PPLinkset lsHandle;
        String tokens[];
        if (!lt.pp_lexer_set_label(label)) {
            n_form_a_cycle_rules = 0;
            if (opts.verbosity > 0)
                opts.out.println("PP warning: Not using any 'form a cycle' rules");
        } else {
            n_commas = lt.pp_lexer_count_commas_of_label();
            n_form_a_cycle_rules = (n_commas + 1) / 2;
        }
        form_a_cycle_rules = new PPRule[1 + n_form_a_cycle_rules];
        for (r = 0; r < n_form_a_cycle_rules; r++) {
            /* read link set */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens <= 0)
                throw new RuntimeException("syntax error in knowledge file");
            lsHandle = PPLinkset.PPLinkset_open(n_tokens);
            for (i = 0; i < n_tokens; i++)
                PPLinkset.PPLinkset_add(lsHandle, tokens[i]);
            form_a_cycle_rules[r] = new PPRule();
            form_a_cycle_rules[r].link_set = lsHandle;

            /* read error message */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens > 1)
                throw new RuntimeException("post_process: Invalid syntax: rule " + (r + 1) + " of " + label);
            form_a_cycle_rules[r].msg = tokens[0];
        }

        /* sentinel entry */
        form_a_cycle_rules[n_form_a_cycle_rules] = new PPRule();
        form_a_cycle_rules[n_form_a_cycle_rules].msg = null;
    }

    private void read_bounded_rules(ParseOptions opts, String label) {
        String tokens[];
        int n_commas, n_tokens, r;
        if (!lt.pp_lexer_set_label(label)) {
            n_bounded_rules = 0;
            if (opts.verbosity > 0)
                opts.out.println("PP warning: Not using any 'bounded' rules");
        } else {
            n_commas = lt.pp_lexer_count_commas_of_label();
            n_bounded_rules = (n_commas + 1) / 2;
        }
        bounded_rules = new PPRule[1 + n_bounded_rules];
        for (r = 0; r < n_bounded_rules; r++) {
            /* read domain */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens != 1)
                throw new RuntimeException("post_process: Invalid syntax: rule " + (r + 1) + " of " + label);
            bounded_rules[r] = new PPRule();
            bounded_rules[r].domain = tokens[0].charAt(0);

            /* read error message */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens != 1)
                throw new RuntimeException("post_process: Invalid syntax: rule " + (r + 1) + " of " + label);
            bounded_rules[r].msg = tokens[0];
        }

        /* sentinel entry */
        bounded_rules[n_bounded_rules] = new PPRule();
        bounded_rules[n_bounded_rules].msg = null;
    }

    /**
     * Reading the 'contains_one_rules' and reading the 
           'contains_none_rules' into their respective arrays 
     */
    private PPRule[] read_contains_rules(ParseOptions opts, String label) {
        
        int n_commas, n_tokens, i, r;
        String p, tokens[];
        int nRules;
        PPRule rules[];
        if (!lt.pp_lexer_set_label(label)) {
            nRules = 0;
            if (opts.verbosity > 0)
                opts.out.println("PP warning: Not using any " + label + " rules");
        } else {
            n_commas = lt.pp_lexer_count_commas_of_label();
            nRules = (n_commas + 1) / 3;
        }
        rules = new PPRule[1 + nRules];
        for (r = 0; r < nRules; r++) {
            /* first read link */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens > 1)
                throw new RuntimeException("post_process: Invalid syntax in " + label + " (rule " + (r + 1) + ")");
            rules[r] = new PPRule();
            rules[r].selector = tokens[0];

            /* read link set */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            rules[r].link_set = PPLinkset.PPLinkset_open(n_tokens);
            rules[r].link_set_size = n_tokens;
            rules[r].link_array = new String[1 + n_tokens];
            for (i = 0; i < n_tokens; i++) {
                p = tokens[i];
                PPLinkset.PPLinkset_add(rules[r].link_set, p);
                rules[r].link_array[i] = p;
            }
            rules[r].link_array[i] = null; /* null-terminator */

            /* read error message */
            tokens = lt.pp_lexer_get_next_group_of_tokens_of_label();
            n_tokens = tokens.length;
            if (n_tokens > 1)
                throw new RuntimeException("post_process: Invalid syntax in " + label + " (rule " + (r + 1) + ")");
            rules[r].msg = tokens[0];
        }

        /* sentinel entry */
        rules[nRules] = new PPRule();
        rules[nRules].msg = null;
        return rules;
    }

    private void initialize_set_of_links_starting_bounded_domain() {
        int i, j, d, domain_of_rule;
        set_of_links_starting_bounded_domain = PPLinkset.PPLinkset_open(GlobalBean.PP_MAX_UNIQUE_LINK_NAMES);
        for (i = 0; bounded_rules[i].msg != null; i++) {
            domain_of_rule = bounded_rules[i].domain;
            for (j = 0;(d = (starting_link_lookup_table[j].domain)) != -1; j++)
                if (d == domain_of_rule)
                    PPLinkset.PPLinkset_add(
                        set_of_links_starting_bounded_domain,
                        starting_link_lookup_table[j].starting_link);
        }
    }

    /**
     * read link set, marked by label in knowledge file, into a set of links
           whose handle is returned. Return null if link set not defined in file,
           in which case the set is taken to be empty. 
     */
    private PPLinkset read_link_set(ParseOptions opts, String label) {
        
        int n_strings, i;
        PPLinkset ls;
        if (!lt.pp_lexer_set_label(label)) {
            if (opts.verbosity > 0)
                opts.out.println("PP warning: Link set " + label + " not defined: assuming empty.");
            n_strings = 0;
        } else
            n_strings = lt.pp_lexer_count_tokens_of_label();
        ls = PPLinkset.PPLinkset_open(n_strings);
        for (i = 0; i < n_strings; i++)
            PPLinkset.PPLinkset_add(ls, lt.pp_lexer_get_next_token_of_label());
        return ls;
    }

    private void read_link_sets(ParseOptions opts) {
        domain_starter_links = read_link_set(opts, "DOMAIN_STARTER_LINKS");
        urfl_domain_starter_links = read_link_set(opts, "URFL_DOMAIN_STARTER_LINKS");
        domain_contains_links = read_link_set(opts, "DOMAIN_CONTAINS_LINKS");
        ignore_these_links = read_link_set(opts, "IGNORE_THESE_LINKS");
        restricted_links = read_link_set(opts, "RESTRICTED_LINKS");
        must_form_a_cycle_links = read_link_set(opts, "MUST_FORM_A_CYCLE_LINKS");
        urfl_only_domain_starter_links = read_link_set(opts, "URFL_ONLY_DOMAIN_STARTER_LINKS");
        left_domain_starter_links = read_link_set(opts, "LEFT_DOMAIN_STARTER_LINKS");
    }

    /**
     * read table of [link, domain type]. 
           This tells us what domain type each link belongs to. 
           This lookup table *must* be defined in the knowledge file. 
     */
    private void read_starting_link_table() {
        
        String p;
        String label = "STARTING_LINK_TYPE_TABLE";
        int i, n_tokens;
        if (!lt.pp_lexer_set_label(label))
            throw new RuntimeException("post_process: Couldn't find starting link table " + label);
        n_tokens = lt.pp_lexer_count_tokens_of_label();
        if ((n_tokens % 2) != 0)
            throw new RuntimeException("post_process: Link table must have format [<link> <domain name>]+");
        nStartingLinks = n_tokens / 2;
        starting_link_lookup_table = new StartingLinkAndDomain[1 + nStartingLinks];
        for (i = 0; i < nStartingLinks; i++) {
            /* read the starting link itself */
            starting_link_lookup_table[i] = new StartingLinkAndDomain();
            starting_link_lookup_table[i].starting_link = lt.pp_lexer_get_next_token_of_label();

            /* read the domain type of the link */
            p = lt.pp_lexer_get_next_token_of_label();
            check_domain_is_legal(p);
            starting_link_lookup_table[i].domain = p.charAt(0);
        }

        /* end sentinel */
        starting_link_lookup_table[nStartingLinks] = new StartingLinkAndDomain();
        starting_link_lookup_table[nStartingLinks].domain = -1;
    }

    static void check_domain_is_legal(String p) {
        if (p.length() > 1)
            throw new RuntimeException("post_process: Domain (" + p + ") must be a single character");
    }

}
