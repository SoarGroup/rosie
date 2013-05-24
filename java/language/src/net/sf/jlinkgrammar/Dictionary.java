package net.sf.jlinkgrammar;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import com.soartech.bolt.LGSupport;


/**
 * This is one of the core classes of the link grammar.  It associates a dictionary 
 * with a sentence. The First routine you have to call is.
 *Dictionary(ParseOptions opts, String dict_name, String pp_name, String cons_name, String affix_name)
 *
 * <p>
 *     The dictionary format:
 * <p>  
 *     In what follows:
 *       Every "%" symbol and everything after it is ignored on every line. 
 *       Every newline or tab is replaced by a space.
 *  <p>  
 *     The dictionary file is a sequence of ENTRIES.  Each ENTRY is one or
 *     more WORDS (a sequence of upper or lower case letters) separated by
 *     spaces, followed by a ":", followed by an EXPRESSION followed by a
 *     ";".  An EXPRESSION is a lisp expression where the functions are "&"
 *     or "and" or "|" or "or", and there are three types of parentheses:
 *     "()", "{}", and "[]".  The terminal symbols of this grammar are the
 *     connectors, which are strings of letters or numbers or *s.  (This
 *     description applies to the prefix form of the dictionary.  the current
 *     dictionary is written in infix form.  If the defined constant
 *     INFIX_NOTATION is defined, then infix is used otherwise prefix is used.)
 * <p>   
 *     The connector begins with an optinal @, which is followed by an upper
 *     case sequence of letters. Each subsequent *, lower case letter or
 *     number is a subscript. At the end is a + or - sign.  The "@" allows
 *     this connector to attach to one or more other connectors.
 * <p>   
 *     Here is a sample dictionary entry (in infix form):
 * <p>   
 *         gone:         T- & {&#064EV+};
 *  <p>  
 *     (See our paper for more about how to interpret the meaning of the
 *     dictionary expressions.)
 * <p>   
 *     A previously defined word (such as "gone" above) may be used instead
 *     of a connector to specify the expression it was defined to be.  Of
 *     course, in this case, it must uniquely specify a word in the
 *     dictionary, and have been previously defined.
 * <p>   
 *     If a word is of the form "/foo", then the file current-dir/foo
 *     is a so-called word file, and is read in as a list of words.
 *     A word file is just a list of words separted by blanks or newlines.
 *  <p>  
 *     A word that contains the character "_" defines an idiomatic use of
 *     the words separated by the "_".  For example "kind of" is an idiomatic
 *     expression, so a word "kind_of" is defined in the dictionary.
 *     Idomatic expressions of any number of words can be defined in this way.
 *     When the word "kind" is encountered, all the idiomatic uses of the word
 *     are considered.
 * <p>   
 *     An expresion enclosed in "[..]" is give a cost of 1.  This means
 *     that if any of the connectors inside the square braces are used,
 *     a cost of 1 is incurred.  (This cost is the first element of the cost
 *     vector printed when a sentence is parsed.)  Of course if something is
 *     inside of 10 levels of "[..]" then using it incurs a cost of 10.
 *     These costs are called "disjunct costs".  The linkages are printed out
 *     in order of non-increasing disjunct cost.
 * <p>   
 *     The expression "(A+ or ())" means that you can choose either "A+" or
 *     the empty expression "()", that is, that the connector "A+" is
 *     optional.  This is more compactly expressed as "{A+}".  In other words,
 *     curly braces indicate an optional expression.
 * <p>   
 *     The expression "(A+ or [])" is the same as that above, but there is a
 *     cost of 1 incurred for choosing not to use "A+".  The expression
 *     "(EXP1 & [EXP2])" is exactly the same as "[EXP1 & EXP2]".  The difference
 *     between "({[A+]} & B+)" and "([{A+}] & B+)" is that the latter always
 *     incurrs a cost of 1, while the former only gets a cost of 1 if "A+" is
 *     used.  
 *  <p>  
 *     The dictionary writer is not allowed to use connectors that begin in
 *     "ID".  This is reserved for the connectors automatically
 *     generated for idioms.
 * <p> 
 * One more thing... 
 *<p>
 * <B>The Dictionary is a binary tree</b>
 *<p>  
 * The data structure storing the dictionary is simply a binary tree.  
    * There is one catch however.  The ordering of the words is not       
    * exactly the order given by strcmp.  It was necessary to             
    * modify the order to make it so that "make" < "make.n" < "make-up"   
    * The problem is that if some other string could  lie between '\0'    
    * and '.' (such as '-' which strcmp would give) then it makes it much 
    * harder to return all the strings that match a given word.           
    * For example, if "make-up" was inserted, then "make" was inserted    
    * the a search was done for "make.n", the obvious algorithm would     
    * not find the match.                                                 
    *
    *
    * <CODE>
    * int dict_compare(String s, String t) {
 *<p>
    *    int ss, tt;
 *<p>
    *    while (*s != '\0' && *s == *t) {
 *<p>
    *       s++;
 *<p>
    *       t++;
 *<p>
    *    }
 *<p>
    *    if (*s == '.') {
 *<p>
    *       ss = 1;
 *<p>
    *    } else {
 *<p>
    *       ss = (*s)<<1;
 *<p>
    *    }
 *<p>
    *    if (*t == '.') {
 *<p>
    *       tt = 1;
 *<p>
    *    } else {
 *<p>
    *       tt = (*t)<<1;
 *<p>
    *    }
 *<p>
    *    return (ss - tt);
 *<p>
    * }
    * </CODE>
    *
    * @see Dictionary#Dictionary(ParseOptions, String, String, String, String)
    * @see Sentence#Sentence(String input_string, Dictionary dict, ParseOptions opts)
    * @see Sentence#sentence_parse(ParseOptions opts)
    *
    */
public class Dictionary {
    public DictNode root;
    public String name;
    public boolean use_unknown_word;
    public boolean unknown_word_defined;
    public boolean capitalized_word_defined;
    public boolean pl_capitalized_word_defined;
    public boolean hyphenated_word_defined;
    public boolean number_word_defined;
    public boolean ing_word_defined;
    public boolean s_word_defined;
    public boolean ed_word_defined;
    public boolean ly_word_defined;
    public boolean left_wall_defined;
    public boolean right_wall_defined;
    public Postprocessor postprocessor;
    public Postprocessor constituent_pp;
    public Dictionary affix_table;
    public boolean andable_defined;
    public ConnectorSet andable_connector_set; /* null=everything is andable */
    public ConnectorSet unlimited_connector_set; /* null=everthing is unlimited */
    public int max_cost;
    public int num_entries;
    public ParseOptions opts;
    public WordFile word_file_header;
    public Exp exp_list;
    /**
     * We link together all the Exp structs that are allocated in reading 
     * this dictionary.  Used for freeing the dictionary 
     */
    public Reader fp;
    public StringBuffer token = new StringBuffer();
    public boolean is_special;
    public int already_got_it;
    public int line_number;

    /* a pointer to the temporary lookup list */
    DictNode lookup_list = null;

    static boolean rand_table_inited = false;
    /**
     * This is the dictionary_create function cast as a java object constructor with an extra 
     * paramater called "path".
     * If this is non-null, then the path used to find the file is taken from that path.
     * Otherwise the path is taken from the dict_name.  This is only needed because
     * an affix_file is opened by a recursive call to this function.
     *
     * @param opts the set of parse options is copied into the Sentence object and Linkage Object as well as the GlobalBean Object
     *             TODO - design the ParseOptions record keeping for better object encapsulation
     * @param dict_name If this is a FQPN then it is used to set the path to all resources
     * @param pp_name the make of the post processor
     * @param cons_name  the name of the constituent file to use
     * @param affix_name the anme oftha affix file to use
     * @param path the colon separated path variable (from env?) used to find the dictionaries
     */
    private Dictionary(
        ParseOptions opts,
        String dict_name,
        String pp_name,
        String cons_name,
        String affix_name,
        String path)
        throws IOException {
        Dictionary dict;
        DictNode dict_node;
        String dictionary_path_name;
        
        if (!rand_table_inited) {
            MyRandom.init_randtable();
            rand_table_inited = true;
        }

        this.opts = opts;
        name = dict_name;
        num_entries = 0;
        is_special = false;
        already_got_it = '\0';
        line_number = 1;
        root = null;
        word_file_header = null;
        exp_list = null;
        affix_table = null;

        if (path != null) {
            dictionary_path_name = path;
        } else {
            dictionary_path_name = dict_name;
        }

        if (!open_dictionary(dictionary_path_name)) {
            throw new RuntimeException("Could not open dictionary " + dict_name);
        }
        
        read_dictionary();

        left_wall_defined = boolean_dictionary_lookup(GlobalBean.LEFT_WALL_WORD);
        right_wall_defined = boolean_dictionary_lookup(GlobalBean.RIGHT_WALL_WORD);
        postprocessor = post_process_open(opts, name, pp_name);
        constituent_pp = post_process_open(opts, name, cons_name);

        affix_table = null;
        if (affix_name != null) {
            affix_table = new Dictionary(opts, affix_name, null, null, null, dict_name);
        }

        unknown_word_defined = boolean_dictionary_lookup(GlobalBean.UNKNOWN_WORD);
        use_unknown_word = true;
        capitalized_word_defined = boolean_dictionary_lookup(GlobalBean.PROPER_WORD);
        pl_capitalized_word_defined = boolean_dictionary_lookup(GlobalBean.PL_PROPER_WORD);
        hyphenated_word_defined = boolean_dictionary_lookup(GlobalBean.HYPHENATED_WORD);
        number_word_defined = boolean_dictionary_lookup(GlobalBean.NUMBER_WORD);
        ing_word_defined = boolean_dictionary_lookup(GlobalBean.ING_WORD);
        s_word_defined = boolean_dictionary_lookup(GlobalBean.S_WORD);
        ed_word_defined = boolean_dictionary_lookup(GlobalBean.ED_WORD);
        ly_word_defined = boolean_dictionary_lookup(GlobalBean.LY_WORD);
        max_cost = 1000;

        if ((dict_node = dictionary_lookup(GlobalBean.ANDABLE_CONNECTORS_WORD)) != null) {
            andable_connector_set = dict_node.exp.connector_set_create();
        } else {
            andable_connector_set = null;
        }
        if ((dict_node = dictionary_lookup(GlobalBean.UNLIMITED_CONNECTORS_WORD)) != null) {
            unlimited_connector_set = dict_node.exp.connector_set_create();
        } else {
            unlimited_connector_set = null;
        }

        lookup_list = null;
    }

    /**
     * This is the dictionary constructor method.  It is the equivilent of the C function create_dictionary
     * It calls a private constructor of the same name.
     */
    public Dictionary(ParseOptions opts, String dict_name, String pp_name, String cons_name, String affix_name)
        throws IOException {
        this(opts, dict_name, pp_name, cons_name, affix_name, null);
    }

    /**
     * read rules from path and initialize the appropriate fields in 
     *     a postprocessor structure, a pointer to which is returned.
     *     The only reason we need the dictname is to used it for the
     *     path, in case there is no DICTPATH set up.  If the dictname
     *     is null, and there is no DICTPATH, it just uses the filename
     *     as the full path.
     * <p>
     * @param opts the parse options. These are kept in many places, use care!
     * @param dictname the dictionary to use. If fully qualified then sets the path for affix, etc.
     * @param path Colon separated list of directories to search for ditionary, postprocessor etc.
     *
     * @see Postprocessor
     * @see Dictionary#Dictionary(ParseOptions, String, String, String, String, String)
     */
    Postprocessor post_process_open(ParseOptions opts, String dictname, String path) throws IOException {
        
        Postprocessor pp;
        if (path == null)
            return null;

        pp = new Postprocessor();
        pp.knowledge = new PPKnowledge(opts, dictname, path);
        pp.set_of_links_of_sentence = PPLinkset.PPLinkset_open(1024);
        pp.set_of_links_in_an_active_rule = PPLinkset.PPLinkset_open(1024);
        pp.relevant_contains_one_rules = new int[pp.knowledge.n_contains_one_rules + 1];
        pp.relevant_contains_none_rules = new int[pp.knowledge.n_contains_none_rules + 1];
        pp.relevant_contains_one_rules[0] = -1;
        pp.relevant_contains_none_rules[0] = -1;
        pp.pp_node = null;
        pp.pp_data = new PPData();
        pp.pp_data.links_to_ignore = null;
        pp.n_local_rules_firing = 0;
        pp.n_global_rules_firing = 0;
        return pp;
    }

    /**
     * Opens the dictionary, sets the path and assigns the Dictionary object's
     * filepointer to the dictionary specified in ParseOptions.
     * @param dict_path_name the fully qualified? path to the ditionary?
     * @see Dictionary#dictopen(ParseOptions, String, String)
     */
    boolean open_dictionary(String dict_path_name) throws IOException {
        if ((fp = dictopen(opts, dict_path_name, name)) == null) {
            return false;
        }
        return true;
    }

    /**
     * Read the dictionary into memory. This musr be preceded by opening the dictionary
     * and seting the file pointer using open_dictionary.
     * @see Dictionary#open_dictionary(String)
     */
    public void read_dictionary() throws IOException {
        GlobalBean.lperrno = 0;
//        System.out.println("In read_dictionary() #1");
        if (!advance()) {
            fp.close();
            throw new RuntimeException(GlobalBean.lperrmsg);
        }
        
//        System.out.println("In read_dictionary() #2");
        while (token.length() > 0) {
            if (!read_entry()) {
            	
//            	System.out.println("read_entry failed!");
                fp.close();
                throw new RuntimeException(GlobalBean.lperrmsg);
            }
            
//        	System.out.println("read_entry succeeded!");
        }
        fp.close();
//        System.out.println("Returning from read_dictionary()");
    }

    /**
     * Returns a pointer to a lookup list of the words in the dictionary.
     *      This list is made up of DictNodes, linked by their right pointers.
     *      The node, file and string fields are copied from the dictionary.
     *   
     *      Freeing this list elsewhere is unnecessary, as long as the rest of 
     *      the program merely examines the list (doesn't change it) 
     */
    public DictNode dictionary_lookup(String s) {
					
        lookup_list = null;
        rdictionary_lookup(root, s);
        prune_lookup_list(s);
        return lookup_list;
    }
		
    /**
     * 
     * @param s 
     */
    void prune_lookup_list(String s) {
        DictNode dn, dnx, dn_new;
        dn_new = null;
        for (dn = lookup_list; dn != null; dn = dnx) {
            dnx = dn.right;
            /* now put dn onto the answer list, or free it */
            if (true_dict_match(dn.string, s)) {
                dn.right = dn_new;
                dn_new = dn;
            }
        }
        /* now reverse the list back */
        lookup_list = null;
        for (dn = dn_new; dn != null; dn = dnx) {
            dnx = dn.right;
            dn.right = lookup_list;
            lookup_list = dn;
        }
    }

    /**
     * 
     * @param dn 
     * @param s 
     */
    void rdictionary_lookup(DictNode dn, String s) {
        /* see comment in dictionary_lookup below */
        int m;
        DictNode dn_new;
        if (dn == null)
            return;
        m = dict_match(s, dn.string);
        if (m >= 0) {
            rdictionary_lookup(dn.right, s);
        }
        if (m == 0) {
            dn_new = new DictNode(dn);
            dn_new.right = lookup_list;
            lookup_list = dn_new;
        }
        if (m <= 0) {
            rdictionary_lookup(dn.left, s);
        }
    }

    /**
     * 
     * @param s 
     * @return true if string is in this dictionary
     */
    boolean boolean_dictionary_lookup(String s) {
        return dictionary_lookup(s) != null;
    }

    /* the following routines are exactly the same as those above, 
       only they do not consider the idiom words
    */

    /**
     * 
     * @param dn 
     * @param s 
     */
    void rabridged_lookup(DictNode dn, String s) {
        int m;
        DictNode dn_new;
        if (dn == null)
            return;
        m = dict_match(s, dn.string);
        if (m >= 0) {
            rabridged_lookup(dn.right, s);
        }
        if (m == 0 && !is_idiom_word(dn.string)) {
            dn_new = new DictNode(dn);
            dn_new.right = lookup_list;
            lookup_list = dn_new;
        }
        if (m <= 0) {
            rabridged_lookup(dn.left, s);
        }
    }

    /**
     * 
     * @param s 
     * @return a linked list of dictionary nodes containing the string
     */
    DictNode abridged_lookup(String s) {
        lookup_list = null;
        rabridged_lookup(root, s);
        prune_lookup_list(s);
        return lookup_list;
    }

    /**
     *  @return true if in the dictionary, false otherwise 
     */
    boolean boolean_abridged_lookup(String s) {
        
        return abridged_lookup(s) != null;
    }

    /**
     * assuming that s is a pointer to a dictionary string, and that
     *      t is a pointer to a search string, this returns 0 if they
     *      match, >0 if s>t, and <0 if s<t.
     * <p>     
     *      The matching is done as follows.  Walk down the strings until
     *      you come to the end of one of them, or until you find unequal
     *      characters.  A "*" matches anything.  Otherwise, replace "."
     *      by "\0", and take the difference.  This behavior matches that
     *      of the function dict_compare().
     */
    int dict_match(String s, String t) {
        
        int i = 0;
        while (i < s.length() && i < t.length() && s.charAt(i) == t.charAt(i)) {
            i++;
        }
        if (i < s.length() && i < t.length() && (s.charAt(i) == '*' || t.charAt(i) == '*'))
            return 0;
        return ((i >= s.length() || s.charAt(i) == '.') ? 0 : s.charAt(i))
            - ((i >= t.length() || t.charAt(i) == '.') ? 0 : t.charAt(i));
    }

    /**
     * We need to prune out the lists thus generated.               
     * A sub string will only be considered a subscript if it    
     * followes the last "." in the word, and it does not begin  
     * with a digit.                                                
     */

    boolean true_dict_match(String s, String t) {
        int ds, dt;
        ds = s.lastIndexOf('.');
        dt = t.lastIndexOf('.');

        /* a dot at the end or a dot followed by a number is NOT considered a subscript */
        if ((dt >= 0) && (dt == t.length() - 1 || Character.isDigit(t.charAt(dt + 1))))
            dt = -1;
        if ((ds >= 0) && (ds == s.length() - 1 || Character.isDigit(s.charAt(ds + 1))))
            ds = -1;

        if (dt < 0 && ds >= 0) {
            if (t.length() > ds)
                return false; /* we need to do this to ensure that */
            return (s.startsWith(t)); /*"i.e." does not match "i.e" */
        } else if (dt >= 0 && ds < 0) {
            if (s.length() > dt)
                return false;
            return t.startsWith(s);
        } else {
            return s.equals(t);
        }
    }

    /**
     * 
     * @param s 
     */
    void dict_display_word_info(String s) {
        /* display the information about the given word */
        DictNode dn;
        Disjunct d1, d2;
        int len;
        if ((dn = dictionary_lookup(s)) == null) {
//DWL            opts.out.println("    \"" + s + "\" matches nothing in the dictionary.");
            return;
        }
//DWL        opts.out.println("Matches:");
        for (; dn != null; dn = dn.right) {
            len = 0;
            d1 = dn.build_disjuncts_for_dict_node();
            for (d2 = d1; d2 != null; d2 = d2.next) {
                len++;
            }
//DWL            opts.out.print("          ");
//DWL            opts.left_print_string(dn.string, "                  ");
//DWL            opts.out.print(" " + len + "  ");
            if (dn.file != null) {
//DWL                opts.out.print("<" + dn.file.file + ">");
            }
//DWL            opts.out.println();
        }
        return;
    }

    /**
     * Returns false if it is not a correctly formed idiom string.
     *<p>
     *      correct such string:
     * <p>
     *      () contains no "."
     * <p>
     *      () non-empty strings separated by _
     * <p>
     * @param s word to lookup
     * @return false if it is not a correctly formed idiom string.     
     */
    static boolean is_idiom_string(String s) {
        
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '.') {
                return false;
            }
            i++;
        }
        if ((s.charAt(0) == '_') || (s.charAt(s.length() - 1) == '_')) {
            return false;
        }
        for (i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '_' && s.charAt(i + 1) == '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param s 
     * @return true if the word is an idiom
     */
    static boolean is_idiom_word(String s) {
        /* returns true if this is a word ending in ".Ix", where x is a number. */
        return (numberfy(s) != -1);
    }

    /**
     * This might be a good place for entity extraction since all cap words
     * often represent entities US, DOD etc. Perhaps we should add a dictionary?
     * @param word 
     * @return true if the word is all capitol letters - probably an accronym
     */
    static boolean is_initials_word(String word) {
        /* This is rather esoteric and not terribly important.
           It returns true if the word matches the pattern /[A-Z]\.]+/
           */
        int i;
        for (i = 0; i < word.length() - 1; i += 2) {
            if (!Character.isUpperCase(word.charAt(i)))
                return false;
            if (word.charAt(i + 1) != '.')
                return false;
        }
        return true;
    }

    /**
     * 
     * @param s 
     * @return true if it is a number
     */
    static boolean is_number(String s) {
        if (!Character.isDigit(s.charAt(0)))
            return false;
        int i = 1;
        while (i < s.length()) {
            /* The ":" is included here so we allow "10:30" to be a number. */
            if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.' && s.charAt(i) != ',' && s.charAt(i) != ':')
                return false;
            i++;
        }
        return true;
    }

    /**
     * returns true iff it's an appropriately formed hyphenated word.
           This means all letters, numbers, or hyphens, not beginning and
           ending with a hyphen.
     * @param s 
     * @return true if hyphenated
     */
    static boolean ishyphenated(String s) {
        /* 
        */
        int hyp, nonalpha;
        hyp = nonalpha = 0;
        if (s.charAt(0) == '-')
            return false;
        int i = 0;
        while (i < s.length()) {
            if (!Character.isLetterOrDigit(s.charAt(i))
                && s.charAt(i) != '.'
                && s.charAt(i) != ','
                && s.charAt(i) != '-')
                return false;
            if (s.charAt(i) == '-')
                hyp++;
            i++;
        }
        return hyp > 0 && s.charAt(i - 1) != '-';
    }

    /**
     * 
     * @param s 
     * @return true if word ends in ing
     */
    static boolean is_ing_word(String s) {

        int i = s.length();
        if (i < 5)
            return false;
        if (s.endsWith("ing"))
            return true;
        return false;
    }

    /**
     * 
     * @param s 
     * @return if word ends in s
     */
    static boolean is_s_word(String s) {

        int i = s.length() - 1;
        if (i < 1)
            return false;
        if (s.charAt(i) != 's')
            return false;
        i--;
        if (s.charAt(i) == 'i' || s.charAt(i) == 'u' || s.charAt(i) == 'o' || s.charAt(i) == 'y' || s.charAt(i) == 's')
            return false;
        return true;
    }

    /**
     * 
     * @param s 
     * @return true if word ends in ed
     */
    static boolean is_ed_word(String s) {

        int i = s.length();
        if (i < 4)
            return false;
        if (s.endsWith("ed"))
            return true;
        return false;
    }

    /**
     * 
     * @param s 
     * @return true if word ends in ly
     */
    static boolean is_ly_word(String s) {

        int i = s.length();
        if (i < 4)
            return false;
        if (s.endsWith("ly"))
            return true;
        return false;
    }

    // TODO: return boolean
    /** if the string contains a single ".", and ends in ".Ix" where
           x is a number, return x.  Return -1 if not of this form.
           */
    static int numberfy(String s) {
        
        int i = 0;
        while (i < s.length() && s.charAt(i) != '.') {
            i++;
        }
        if (i >= s.length()) {
            return -1;
        }
        i++;
        if (i >= s.length() || s.charAt(i) != 'I') {
            return -1;
        }
        i++;
        // TODO: eliminate is_number
        if (i >= s.length() || !is_idiom_number(s.substring(i))) {
            return -1;
        }

        return Integer.parseInt(s.substring(i));
    }

    /** return true if the string s is a sequence of digits. */
    static boolean is_idiom_number(String s) {
        
        int i = 0;
        while (i < s.length()) {
            if (!Character.isDigit(s.charAt(i)))
                return false;
            i++;
        }
        return true;
    }

    /** Returns true if the string contains an underbar character.
         */
    static boolean contains_underbar(String s) {
        
        return s.indexOf('_') >= 0;
    }


    void dict_error(String s) throws IOException {
        int i;
        StringBuffer tokens = new StringBuffer(1024);

        for (i = 0; i < 5 && token.length() > 0; i++) {
            tokens.append('"');
            tokens.append(token);
            tokens.append("\" ");
            advance();
        }
        GlobalBean.lperrmsg = "Error parsing dictionary " + ". " + s + ", line " + line_number + ", tokens = " + tokens.toString();
    }

    void warning(String s) {
        opts.out.println();
        opts.out.println("Warning: " + s);
        opts.out.println("line " + line_number + ", current token = \"" + token.toString() + "\"");
        opts.out.println();
    }

    /** allocate a new Exp node and link it into the
           exp_list for freeing later */
    Exp Exp_create() {
        
        Exp e;
        e = new Exp();
        e.next = exp_list;
        exp_list = e;
        return e;
    }

    /** This gets the next character from the input, eliminating comments.
           If we're in quote mode, it does not consider the % character for
           comments */
    int get_character(boolean quote_mode) throws IOException {
        

        int c;

        c = fp.read();
        if ((c == '%') && (!quote_mode)) {
            while ((c >= 0) && (c != '\n'))
                c = fp.read();
        }
        if (c == '\n')
            line_number++;
        return c;
    }

    /** this reads the next token from the input into token */
    boolean advance() throws IOException {
        
        int c, i;
        boolean quote_mode;

        is_special = false;
        token.setLength(0);
        if (already_got_it != '\0') {
            is_special = (Dictionary.SPECIAL.indexOf(already_got_it) >= 0);
            if (already_got_it >= 0) {
                token.append((char)already_got_it);
            }
            already_got_it = '\0';
            return true;
        }

        do c = get_character(false);
        while (c >= 0 && Character.isWhitespace((char)c));

        quote_mode = false;

        i = 0;
        for (;;) {
            if (quote_mode) {
                if (c == '\"') {
                    quote_mode = false;
                    return true;
                }
                if (Character.isWhitespace((char)c)) {
                    dict_error("White space inside of token");
                    return false;
                }
                token.append((char)c);
                i++;
            } else {
                if (Dictionary.SPECIAL.indexOf(c) >= 0) {
                    if (i == 0) {
                        token.append((char)c);
                        is_special = true;
                        return true;
                    }
                    already_got_it = c;
                    return true;
                }
                if (c < 0) {
                    if (i == 0) {
                        return true;
                    }
                    already_got_it = c;
                    return true;
                }
                if (Character.isWhitespace((char)c)) {
                    return true;
                }
                if (c == '\"') {
                    quote_mode = true;
                } else {
                    token.append((char)c);
                    i++;
                }
            }
            c = get_character(quote_mode);
        }
    }

    /** returns true if this token is a special token and it is equal to c */
    boolean is_equal(int c) {
        
        return (is_special && token.length() == 1 && c == token.charAt(0));
    }

    /** makes sure the string s is a valid connector */
    boolean check_connector(String s) throws IOException {
        
        int i;
        i = s.length();
        int j = 0;
        if (i < 1) {
            dict_error("Expecting a connector.");
            return false;
        }
        if ((s.charAt(i - 1) != '+') && (s.charAt(i - 1) != '-')) {
            dict_error("A connector must end in a \"+\" or \"-\".");
            return false;
        }
        if (s.charAt(j) == '@')
            j++;
        if (!Character.isUpperCase(s.charAt(j))) {
            dict_error("The first letter of a connector must be in [A-Z] (was " + s.charAt(j) + ").");
            return false;
        }
        if ((s.charAt(j) == 'I') && j < s.length() - 1 && (s.charAt(j + 1) == 'D')) {
            dict_error("Connectors beginning with \"ID\" are forbidden");
            return false;
        }
        while (j < s.length() - 1) {
            if (!Character.isLetterOrDigit(s.charAt(j)) && s.charAt(j) != '*' && s.charAt(j) != '^') {
                dict_error("All letters of a connector must be alpha-numeric.");
                return false;
            }
            j++;
        }
        return true;
    }

    /** the current token is a connector (or a dictionary word)          
        make a node for it                                                
     */
    Exp connector() throws IOException {
        

        Exp n;
        DictNode dn;
        int i;

        i = token.length() - 1; /* this must be + or - if a connector */
        if (token.charAt(i) != '+' && token.charAt(i) != '-') {
            dn = abridged_lookup(token.toString());
            while (dn != null && !dn.string.equals(token.toString())) {
                dn = dn.right;
            }
            if (dn == null) {
                dict_error(
                    "\nPerhaps missing + or - in a connector.\n"
                        + "Or perhaps you forgot the suffix on a word.\n"
                        + "Or perhaps a word is used before it is defined.\n");
                return null;
            }
            n = make_unary_node(dn.exp);
        } else {
            if (!check_connector(token.toString())) {
                return null;
            }
            n = Exp_create();
            n.dir = token.charAt(i);
            token.deleteCharAt(i); /* get rid of the + or - */
            if (token.charAt(0) == '@') {
                n.string = token.substring(1);
                n.multi = true;
            } else {
                n.string = token.toString();
                n.multi = false;
            }
            n.type = GlobalBean.CONNECTOR_type;
            n.cost = 0;
        }
        if (!advance()) {
            return null;
        }
        return n;
    }
    /** This creates a node with one child (namely e).  Initializes 
     * the cost to zero 
     */
    Exp make_unary_node(Exp e) {
                                             
        Exp n;
        n = Exp_create();
        n.type = GlobalBean.AND_type; /* these must be AND types */
        n.cost = 0;
        n.l = new ExpList();
        n.l.next = null;
        n.l.e = e;
        return n;
    }

    /** This creates a node with zero children.  Initializes
     *
      the cost to zero
     */
    Exp make_zeroary_node() {
        
        Exp n;
        n = Exp_create();
        n.type = GlobalBean.AND_type; /* these must be AND types */
        n.cost = 0;
        n.l = null;
        return n;
    }

    /** This creates an OR node with two children, one the given node,
           and the other as zeroary node.  This has the effect of creating
           what used to be called an optional node.
        */
    Exp make_optional_node(Exp e) {
        
        Exp n;
        ExpList el, elx;
        n = Exp_create();
        n.type = GlobalBean.OR_type;
        n.cost = 0;
        n.l = el = new ExpList();
        el.e = make_zeroary_node();
        el.next = elx = new ExpList();
        elx.next = null;
        elx.e = e;
        return n;
    }

    /* This is for infix notation */

    /* Build (and return the root of) the tree for the expression beginning 
       with the current token.  At the end, the token is the first one not   
       part of this expression.                                              
     */
    Exp expression() throws IOException {
        
        return restricted_expression(true, true);
    }

    Exp restricted_expression(boolean and_ok, boolean or_ok) throws IOException {
        Exp nl = null, nr, n;
        ExpList ell, elr;
        if (is_equal('(')) {
            if (!advance()) {
                return null;
            }
            nl = expression();
            if (nl == null) {
                return null;
            }
            if (!is_equal(')')) {
                dict_error("Expecting a \")\".");
                return null;
            }
            if (!advance()) {
                return null;
            }
        } else if (is_equal('{')) {
            if (!advance()) {
                return null;
            }
            nl = expression();
            if (nl == null) {
                return null;
            }
            if (!is_equal('}')) {
                dict_error("Expecting a \"}\".");
                return null;
            }
            if (!advance()) {
                return null;
            }
            nl = make_optional_node(nl);
        } else if (is_equal('[')) {
            if (!advance()) {
                return null;
            }
            nl = expression();
            if (nl == null) {
                return null;
            }
            if (!is_equal(']')) {
                dict_error("Expecting a \"]\".");
                return null;
            }
            if (!advance()) {
                return null;
            }
            nl.cost += 1;
        } else if (!is_special) {
            nl = connector();
            if (nl == null) {
                return null;
            }
        } else if (is_equal(')') || is_equal(']')) {
            /* allows "()" or "[]" */
            nl = make_zeroary_node();
        } else {
            dict_error("Connector, \"(\", \"[\", or \"{\" expected.");
            return null;
        }

        if (is_equal('&') || (token.toString().equals("and"))) {
            if (!and_ok) {
                warning("\"and\" and \"or\" at the same level in an expression");
            }
            if (!advance()) {
                return null;
            }
            nr = restricted_expression(true, false);
            if (nr == null) {
                return null;
            }
            n = Exp_create();
            n.l = ell = new ExpList();
            ell.next = elr = new ExpList();
            elr.next = null;

            ell.e = nl;
            elr.e = nr;
            n.type = GlobalBean.AND_type;
            n.cost = 0;
        } else if (is_equal('|') || (token.toString().equals("or"))) {
            if (!or_ok) {
                warning("\"and\" and \"or\" at the same level in an expression");
            }
            if (!advance()) {
                return null;
            }
            nr = restricted_expression(false, true);
            if (nr == null) {
                return null;
            }
            n = Exp_create();
            n.l = ell = new ExpList();
            ell.next = elr = new ExpList();
            elr.next = null;

            ell.e = nl;
            elr.e = nr;
            n.type = GlobalBean.OR_type;
            n.cost = 0;
        } else
            return nl;
        return n;
    }

    /** The data structure storing the dictionary is simply a binary tree.  
    * There is one catch however.  The ordering of the words is not       
    * exactly the order given by strcmp.  It was necessary to             
    * modify the order to make it so that "make" < "make.n" < "make-up"   
    * The problem is that if some other string could  lie between '\0'    
    * and '.' (such as '-' which strcmp would give) then it makes it much 
    * harder to return all the strings that match a given word.           
    * For example, if "make-up" was inserted, then "make" was inserted    
    * the a search was done for "make.n", the obvious algorithm would     
    * not find the match.                                                 
     * <p>
    * verbose version
    *<PRE>
    * int dict_compare(String s, String t) {
    *    int ss, tt;
    *    while (*s != '\0' && *s == *t) {
    *    s++;
    *    t++;
    *    }
    *    if (*s == '.') {
    *    ss = 1;
    *    } else {
    *    ss = (*s)<<1;
    *    }
    *    if (*t == '.') {
    *    tt = 1;
    *    } else {
    *    tt = (*t)<<1;
    *    }
    *    return (ss - tt);
    * }
    * </PRE>
    *        terse version
    *        <PRE>
    *        int dict_compare(String s, String t) {
    *            int i = 0;
    *            while (i < s.length() && i < t.length() && s.charAt(i) == t.charAt(i)) {
    *                i++;
    *            }
    *        return (i >= s.length() ? 0 : (s.charAt(i) == '.' ? 1 : (s.charAt(i) << 1)))
    *                - (i >= t.length() ? 0 : (t.charAt(i) == '.' ? 1 : (t.charAt(i) << 1)));
    *        }
    *        </PRE>
    *       
    */
    int dict_compare(String s, String t) {
        int i = 0;
        while (i < s.length() && i < t.length() && s.charAt(i) == t.charAt(i)) {
            i++;
        }
        return (i >= s.length() ? 0 : (s.charAt(i) == '.' ? 1 : (s.charAt(i) << 1)))
            - (i >= t.length() ? 0 : (t.charAt(i) == '.' ? 1 : (t.charAt(i) << 1)));
    }
    
        /**
         * Insert the new node into the dictionary below node n                 
        * give error message if the new element's string is already there       
        * assumes that the "n" field of new is already set, and the left        
        * and right fields of it are null
         */    
    DictNode insert_dict(DictNode n, DictNode newNode) throws IOException {
                                 
        int comp;

        if (n == null)
            return newNode;
        comp = dict_compare(newNode.string, n.string);
        if (comp < 0) {
            n.left = insert_dict(n.left, newNode);
        } else if (comp > 0) {
            n.right = insert_dict(n.right, newNode);
        } else {
            dict_error("The word \"" + newNode.string + "\" has been multiply defined\n");
            return null;
        }
        return n;
    }

    /**
     * Insert the list into the dictionary.
     * It does the middle one first, then the left half, then the right.
     * 
     * @param p points to a list of dict_nodes connected by their left pointers
     * @param l is the length of this list (the last ptr may not be null)
     * @throws IOException
     */
    void insert_list(DictNode p, int l) throws IOException {
        DictNode dn, dnx, dn_second_half;
        int k, i; /* length of first half */

        if (l == 0)
            return;

        k = (l - 1) / 2;
        dn = p;
        for (i = 0; i < k; i++) {
            dn = dn.left;
        }
        /* dn now points to the middle element */
        dn_second_half = dn.left;
        dn.left = dn.right = null;
        if (contains_underbar(dn.string)) {
            insert_idiom(dn);
        } else if (is_idiom_word(dn.string)) {
//DWL            opts.out.println("*** Word \"" + dn.string + "\" found near line " + line_number + ".");
//DWL            opts.out.println("    Words ending \".Ix\" (x a number) are reserved for idioms.");
//DWL            opts.out.println("    This word will be ignored.");
        } else if ((dnx = abridged_lookup(dn.string)) != null) {
//DWL            opts.out.println("*** The word \"" + dn.string + "\"");
//DWL            opts.out.println(" found near line " + line_number + " matches the following words:");
            for (; dnx != null; dnx = dnx.right) {
//DWL                opts.out.print(" " + dnx.string);
            }
//DWL            opts.out.println("\n    This word will be ignored.");
        } else {
            root = insert_dict(root, dn);
            num_entries++;
        }

        insert_list(p, k);
        insert_list(dn_second_half, l - k - 1);
    }

    /** Starting with the current token parse one dictionary entry.     
     * Add these words to the dictionary                                   
     */
    boolean read_entry() throws IOException {
        

        DictNode dn_new, dnx, dn = null;

        for (; !is_equal(':'); advance()) {
            if (is_special) {
            	dict_error("I expected a word but didn't get it.");
                throw new RuntimeException(GlobalBean.lperrmsg);
            }
            
//DWL            System.out.println("re=" + token.toString());
            if (token.charAt(0) == '/') {
                /* it's a word-file name */
//DWL            	System.out.println("re: Reading word_file");
                dn = read_word_file(dn, token.toString());
                if (dn == null) {
                    throw new RuntimeException("Error reading word file "+token.toString()+", file empty?");
                }
            } else {
                dn_new = new DictNode();
                dn_new.left = dn;
                dn = dn_new;
                dn.file = null;
                dn.string = token.toString();
            }
        }
        if (!advance()) { /* pass the : */
            return false;
        }
        Exp n = expression();
        if (n == null) {
            return false;
        }
        if (!is_equal(';')) {
            dict_error("Expecting \";\" at the end of an entry.");
            throw new RuntimeException(GlobalBean.lperrmsg);
        }
        if (!advance()) { /* pass the ; */
            return false;
        }

        /* at this point, dn points to a list of DictNodes connected by      */
        /* their left pointers.  These are to be inserted into the dictionary */
        int i = 0;
        for (dnx = dn; dnx != null; dnx = dnx.left) {
            dnx.exp = n;
            i++;
        }
        insert_list(dn, i);
        return true;
    }
        /**
         * Takes as input a pointer to a DictNode.
           The string of this DictNode is an idiom string.
           This string is torn apart, and its components are inserted into the
           dictionary as special idiom words (ending in .I*, where * is a number).
           The expression of this DictNode (its node field) has already been
           read and constructed.  This will be used to construct the special idiom
           expressions.
           The given dict node is freed.  The string is also freed.
           */
    void insert_idiom(DictNode dn) throws IOException {

        Exp nc, no, n1;
        ExpList ell, elr;
        String s;
        int s_length;
        DictNode dn_list, xdn, start_dn_list;

        no = dn.exp;
        s = dn.string;
        s_length = s.length();

        if (!is_idiom_string(s)) {
//DWL            opts.out.println(
//DWL            "*** Word \"" + s + "\" on line " + line_number + " is not a correctly formed idiom string.\n");
//DWL            opts.out.println("    This word will be ignored");
            return;
        }
        dn_list = start_dn_list = make_idiom_DictNodes(s);
        if (dn_list.right == null) {
            throw new RuntimeException("Idiom string with only one connector -- should have been caught");
        }

        /* first make the nodes for the base word of the idiom (last word) */
        /* note that the last word of the idiom is first in our list */
        /* ----- this code just sets up the node fields of the dn_list ----*/
        nc = Exp_create();
        nc.string = generate_id_connector();
        nc.dir = '-';
        nc.multi = false;
        nc.type = GlobalBean.CONNECTOR_type;
        nc.cost = 0;

        n1 = Exp_create();
        n1.l = ell = new ExpList();
        ell.next = elr = new ExpList();
        elr.next = null;
        ell.e = nc;
        elr.e = no;
        n1.type = GlobalBean.AND_type;
        n1.cost = 0;

        dn_list.exp = n1;

        dn_list = dn_list.right;

        while (dn_list.right != null) {
            /* generate the expression for a middle idiom word */

            n1 = Exp_create();
            n1.string = null;
            n1.type = GlobalBean.AND_type;
            n1.cost = 0;
            n1.l = ell = new ExpList();
            ell.next = elr = new ExpList();
            elr.next = null;

            nc = Exp_create();
            nc.string = generate_id_connector();
            nc.dir = '+';
            nc.multi = false;
            nc.type = GlobalBean.CONNECTOR_type;
            nc.cost = 0;
            elr.e = nc;

            increment_current_name();

            nc = Exp_create();
            nc.string = generate_id_connector();
            nc.dir = '-';
            nc.multi = false;
            nc.type = GlobalBean.CONNECTOR_type;
            nc.cost = 0;

            ell.e = nc;

            dn_list.exp = n1;

            dn_list = dn_list.right;
        }
        /* now generate the last one */

        nc = Exp_create();
        nc.string = generate_id_connector();
        nc.dir = '+';
        nc.multi = false;
        nc.type = GlobalBean.CONNECTOR_type;
        nc.cost = 0;

        dn_list.exp = nc;

        increment_current_name();

        /* ---- end of the code alluded to above ---- */

        /* now its time to insert them into the dictionary */

        dn_list = start_dn_list;

        while (dn_list != null) {
            xdn = dn_list.right;
            dn_list.left = dn_list.right = null;
            dn_list.string = build_idiom_word_name(dn_list.string);
            root = insert_dict(root, dn_list);
            num_entries++;
            dn_list = xdn;
        }

    }
        /**
         * <ul>
         *
         * <li>  (1) opens the word file and adds it to the word file list
         * <li>  (2) reads in the words
         * <li>  (3) puts each word in a DictNode
         * <li>  (4) links these together by their left pointers at the front of the list pointed to by dn
         * <li>  (5) returns a pointer to the first of this list
         * <ul>
        */
    DictNode read_word_file(DictNode dn, String filename) throws IOException {

        DictNode dn_new;
        WordFile wf;
        Reader fp;
        String s;
        String file_name_copy;
        // Here is a bad hack for bad code. the word files are given as /word/word.x.y
        // but dict open thinks that files given as "/" are abaolute.  Who writes code
        // like this anymore?  where is localfileseperator? How do you know this is UNIX?
        // ARG!!!
        // TODO - clean up file handling
        // So we now add to the bad code by saing that if the filename = /word/...
        // Then we back up on the dictionary and append to it.
        if (filename.length() >= 5 && filename.substring(0, 4) == "word/") {
            int len = name.lastIndexOf("/");
            file_name_copy = name.substring(0,len) + filename.substring(1);
        } else {
            file_name_copy = filename.substring(1);
        }
        
        
//DWL        System.out.println("in read_word_file(), calling dictopen: " + file_name_copy);
        if ((fp = dictopen(opts, name, file_name_copy)) == null) {
//DWL        	System.out.println("error on trying dictopen()");
            throw new RuntimeException("Error opening word file: " + file_name_copy);
        }
//DWL        System.out.println("in read_word_file(), succeeding in opening dictopen" + filename);
        
        /*printf("   Reading \"%s\"\n", file_name_copy);*/
        /*printf("*"); fflush(stdout);*/

        wf = new WordFile();
        wf.file = file_name_copy.toString();
        wf.changed = false;
        wf.next = word_file_header;
        word_file_header = wf;

        while ((s = get_a_word(fp)) != null) {
            dn_new = new DictNode();
            dn_new.left = dn;
            dn = dn_new;
            dn.string = s;
            dn.file = wf;
        }
        fp.close();
        return dn;
    }

    /** Reads in one word from the file, allocates space for it,
           and returns it.
        */
    String get_a_word(Reader fp) throws IOException {
        
        StringBuffer word = new StringBuffer();
        String s;
        int c, j;
        do {
            c = fp.read();
        } while ((c >= 0) && Character.isWhitespace((char)c));
        if (c < 0)
            return null;

        for (j = 0; c >= 0 && !Character.isWhitespace((char)c); j++) {
            word.append((char)c);
            c = fp.read();
        }

        return word.toString();
    }

      /**
        * This function is used to open a dictionary file or a word file,
        *  or any associated data file (like a post process knowledge file).
        * <p>
        *  It works as follows.  If the file name begins with a "/", then
        *  it's assumed to be an absolute file name and it tries to open
        *  that exact file.
        * <p>
        *  If the filename does not begin with a "/", then it uses the
        *  dictpath mechanism to find the right file to open.  This looks
        *  for the file in a sequence of directories until it finds it.  The
        *  sequence of directories is specified in a dictpath string, in
        *  which each directory is followed by a ":".
        *  <p>
        *   The dictpath that it uses is constructed as follows.  If the
        *   dictname is non-null, and is an absolute path name (beginning
        *   with a "/", then the part after the last "/" is removed and this
        *   is the first directory on the dictpath.  After this comes the
        *   DICTPATH environment variable, followed by the DEFAULTPATH
        */
    static Reader dictopen(ParseOptions opts, String dictname, String filename) throws IOException {


        String completename;
        String fulldictpath;
        String dummy1;
        String dp1, dp2;
        int pos, oldpos;
        int filenamelen, len;
        Reader fp;

//        System.out.println("dn1=" + dictname + "; fn1=" + filename);
        
//        if (filename.charAt(0) == '/') {
//            return new FileReader(filename);
//        }

//        dp1 = "";
//        if (dictname != null) {
//            dummy1 = dictname;
//            pos = dummy1.lastIndexOf('/');
//            if (pos >= 0) {
//                dp1 = dummy1.substring(0, pos - 1) + ":";
//            }
//        }
        /* dp1 now points to the part of the dictpath due to dictname */

        dp2 = "";
        /*  We're no longer using the dictpath in the environment
        if (strlen(getenv(DICTPATH)) > 0) {
        sprintf(dummy2, "%s:", getenv(DICTPATH));
        dp2 = dummy2;
        }
        */
        /* dp2 now points to the part of the dictpath due to the environment var */

//        fulldictpath = dp1 + dp2 + GlobalBean.DEFAULTPATH;
        /* now fulldictpath is our dictpath, where each entry is followed by a ":"
           including the last one */
        
        //DWL: ugh! rewrite this all...
        // set up the path: assume that dictname has at least the path; chop off any path-final file that has 4.0.*
        if (dictname != null) {
//        	System.out.println("fixing dictname:" + dictname);
        	dummy1 = dictname;
        	pos = dummy1.lastIndexOf('/');
        	if (pos > 0) {
            	dummy1 = dummy1.substring(pos+1, pos+5);
//            	System.out.println("found slash:" + pos + " dummy1=" + dummy1);
        		if (dummy1.equals("4.0.")) {
        			dictname = dictname.substring(0, pos);
        		}
        	}
        }
        
        // set up the filename: if there's a path-final file that has 4.0.*, chop the rest off
        if (filename != null) {
        	dummy1 = filename;
        	pos = dummy1.lastIndexOf('/');
        	if (pos > 0) {
            	dummy1 = dummy1.substring(pos+1, pos+5);
        		if (dummy1.equals("4.0.")) {
        			filename = filename.substring(pos+1, filename.length());
        		}
        	}
        }
        
//        fulldictpath = "C:/lonz/projects/lgsoar/lgsoar9/lgsoar9x/data/link";
    	fulldictpath = LGSupport.dictionaryPath;
        completename = fulldictpath + "/" + filename;
//        System.out.println("Calling dictopen: " + completename + "\n");
        File f = new File(completename);
        if (f.exists()) {
//        	System.out.println("opening:" + completename);
            if (opts.verbosity >2 ) opts.out.println("   Opening " + completename);
            return new FileReader(f);
        }
        
        return null;
    }

            /**
             * generate a new connector name
           obtained from the current_name
           allocate string space for it.
           return a pointer to it.
         */
    String generate_id_connector() {

        int i, sz;
        StringBuffer s;
        String id;

        for (i = 0; i < current_name.length() - 1 && current_name.charAt(i) == 'A'; i++);
        /* i is now the number of characters of current_name to skip */
        sz = CN_size - i + 2 + 1 + 1;
        s = new StringBuffer("ID");
        s.append(current_name.substring(i));
        return s.toString();
    }
    
    /**
      * Allocates string space and returns a pointer to it.
      *     In this string is placed the idiomized name of the given string s.
      *     This is the same as s, but with a postfix of ".Ix", where x is an
      *     appropriate number.  x is the minimum number that distinguishes
      *     this word from others in the dictionary.
      */
    String build_idiom_word_name(String s) {

        StringBuffer newString;
        int x;
        int count;

        count = max_postfix_found(dictionary_lookup(s)) + 1;

        x = s.indexOf('.');
        if (x >= 0) {
            newString = new StringBuffer(s.substring(0, x));
        } else {
            newString = new StringBuffer(s);
        }
        newString.append(".I");
        newString.append(count);

        return newString.toString();
    }

    int max_postfix_found(DictNode d) {
        /* Look for words that end in ".Ix" where x is a number.
           Return the largest x found.
           */
        int i, j;
        i = 0;
        while (d != null) {
            j = numberfy(d.string);
            if (j > i)
                i = j;
            d = d.right;
        }
        return i;
    }

            /**
             * Tear the idiom string apart.
           Destroys the string s, but does not free it.
           Put the parts into a list of DictNodes (connected by their right pointers)
           Sets the string fields of these DictNodes pointing to the
           fragments of the string s.  Later these will be replaced by
           correct names (with .Ix suffixes).
           The list is reversed from the way they occur in the string.
           A pointer to this list is returned.
           */
    DictNode make_idiom_DictNodes(String string) {

        DictNode dn, dn_new;
        dn = null;
        StringTokenizer tok = new StringTokenizer(string, "_");
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            dn_new = new DictNode();
            dn_new.right = dn;
            dn = dn_new;
            dn.string = word;
            dn.file = null;
        }
        return dn;
    }

    static StringBuffer current_name = new StringBuffer("AAAAAAAA");
    static final int CN_size = current_name.length();

    /*
       This set of 10 characters are the ones defining the syntax of the dictionary.
    */
    private final static String SPECIAL = "(){};[]&|:";

    void increment_current_name() {
        int i, carry;
        i = CN_size - 1;
        carry = 1;
        while (carry == 1) {
            char cc = current_name.charAt(i);
            if (cc == 'Z') {
                current_name.setCharAt(i, 'A');
                carry = 1;
            } else {
                current_name.setCharAt(i, (char) (1 + cc));
                carry = 0;
            }
            i--;
        }
    }

}
