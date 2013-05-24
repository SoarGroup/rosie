package net.sf.jlinkgrammar;

import java.util.Arrays;
import java.util.HashSet;

/**
 *  Routines for creating and destroying processing Sentences. It contains
 *  several its own copy of objects such as Dictionary, ParseInfo, Linkage, LinkageInfo,
 *  ParseOptions, and Postprocessor.  Much of the code still smacks of C rather than
 *  Java.  For instance  the critical ctable is not a collection object and ctable_length
 *  can be modified independently of the actual lenght of the table. 
 *<p>
 *  There are other examples,  many routines take ParseOptions as an argument and yet use 
 *  this.opts for their use.  If the programmer is not careful odd results may occur.
 *<p>
 * One last note: Everything is reference via integer indexes into arrays rather than
 * as objects. A natural object oriented approach would pass the Word  object and the 
 * routine would use Word.id to find the offset in the array.  This would improve type
 * checking and overall program safety. - jlr
 * <p>
 * The most important
 *  routine is sentence_parse()
 *  @see #sentence_parse(ParseOptions) 
 *
 */
public class Sentence {
    /**
     * words are defined from this dictionary
     */
    public Dictionary dict;
    /**
     * number of words in sentence string under evaluation
     */
    public int length;
    /**
     * array of words after tokenization
     */
    public Word word[] = new Word[GlobalBean.MAX_SENTENCE];
    /**
     * true if i'th word is a conjunction, as defined by the program
     * TODO - remove IndoEuropean dependancy
     */
    public boolean is_conjunction[];
    /**
     * deletable regions in a sentence with conjunction
     */
    public boolean deletable[][]; 
    /**
     * created by build_effective_dist()
     * @see Sentence#build_effective_dist(boolean)
     */
    public int effective_dist[][];
    /**
     * total number linkages before postprocessing.  This
       is returned by the count() function
     */
    public int num_linkages_found;
    /**
     * total number of linkages allocated.
       the number post-processed might be fewer
       because some are non-canonical
     */
    public int num_linkages_alloced;
    /**
     * The number of linkages that are actually
       put into the array that was alloced.
       this is not the same as num alloced
       because some may be non-canonical.
     */
    public int num_linkages_post_processed;
    /**
     * number with no pp violations
     */
    public int num_valid_linkages;
    /**
     * number of null links in linkages
     */
    public int null_count;
    /**
     * set of parses for the sentence
     */
    public ParseInfo parse_info;
    /**
     * array of valid and invalid linkages (sorted)
     */
    public LinkageInfo link_info[];
    /**
     * used to keep track of fat disjuncts
     */
    public AndData and_data; 
    /**
     * don't prune rules more than once in p.p.
     */
    public boolean q_pruned_rules; 
    public int post_quote[] = new int[GlobalBean.MAX_SENTENCE];
    public PatchElement patch_array[];

    public static Word local_sent[];
    //static boolean islands_ok;
    public static boolean null_links;



    public Sentence(String input_string, Dictionary dict, ParseOptions opts) {

        dict.lookup_list = null;

        this.dict = dict;
        length = 0;
        num_linkages_found = 0;
        num_linkages_alloced = 0;
        num_linkages_post_processed = 0;
        num_valid_linkages = 0;
        link_info = null;
        deletable = null;
        effective_dist = null;
        num_valid_linkages = 0;
        null_count = 0;
        parse_info = null;
        and_data = new AndData();

        if (!separate_sentence(input_string, opts)) {
            throw new RuntimeException("Problem: can't tokenize");
        }

        q_pruned_rules = false; /* for post processing */
        is_conjunction = new boolean[length];
        set_is_conjunction();
        initialize_conjunction_tables();

        for (int i = 0; i < length; i++) {
            /* in case we free these before they set to anything else */
            word[i].x = null;
            word[i].d = null;
        }

        if (!(dict.unknown_word_defined && dict.use_unknown_word)) {
            if (!sentence_in_dictionary()) {
                throw new RuntimeException("Problem: unknown words");
            }
        }

        build_sentence_expressions(opts);

        patch_array = new PatchElement[GlobalBean.MAX_LINKS];
        for (int i = 0; i < patch_array.length; i++) {
            patch_array[i] = new PatchElement();
        }
    }

    /**
     * This just looks up all the words in the sentence, and builds
       up an appropriate error message in case some are not there.
       It has no side effect on the sentence.  Returns true if all
       went well.
     */
    private boolean sentence_in_dictionary() {
        int w;
        boolean ok_so_far;
        String s;
        StringBuffer temp = new StringBuffer();

        ok_so_far = true;
        for (w = 0; w < length; w++) {
            s = word[w].string;
            if (!dict.boolean_dictionary_lookup(s)
                && !(Character.isUpperCase(s.charAt(0)) && Dictionary.is_s_word(s) && dict.pl_capitalized_word_defined)
                && !(Character.isUpperCase(s.charAt(0)) && dict.capitalized_word_defined)
                && !(Dictionary.ishyphenated(s) && dict.hyphenated_word_defined)
                && !(Dictionary.is_number(s) && dict.number_word_defined)
                && !(Dictionary.is_ing_word(s) && dict.ing_word_defined)
                && !(Dictionary.is_s_word(s) && dict.s_word_defined)
                && !(Dictionary.is_ed_word(s) && dict.ed_word_defined)
                && !(Dictionary.is_ly_word(s) && dict.ly_word_defined)) {
                if (ok_so_far) {
                    temp.append("The following words are not in the dictionary:");
                    ok_so_far = false;
                }
                temp.append(" \"");
                temp.append(word[w].string);
                temp.append("\"");
            }
        }
        if (!ok_so_far) {
            throw new RuntimeException(temp.toString());
        }
        return ok_so_far;
    }

    /**
     * The string s has just been read in from standard input.
     * This function breaks it up into words and stores these words in
     * the sent.word[] array.  
     * Quote marks are treated just like blanks.
     * @param s sentence in String form
     * @param opts passes ParseOptions - In reality these are often kept in global variables. TODO - clean up code
     * @see ParseOptions
     * @return Returns true if all is well, false otherwise.
     */
    public boolean separate_sentence(String s, ParseOptions opts) {

        boolean is_first, quote_found;

        for (int i = 0; i < GlobalBean.MAX_SENTENCE; i++)
            post_quote[i] = 0;
        length = 0;

        if (dict.left_wall_defined)
            if (!issue_sentence_word(GlobalBean.LEFT_WALL_WORD))
                return false;

        is_first = true;
        int j = 0;
        for (;;) {
            quote_found = false;
            while (j < s.length() && (Character.isWhitespace(s.charAt(j)) || s.charAt(j) == '\"')) {
                if (s.charAt(j) == '\"')
                    quote_found = true;
                j++;
            }
            if (j >= s.length())
                break;
            int k = j;
            while (k < s.length() && !(Character.isWhitespace(s.charAt(k)) || s.charAt(k) == '\"'))
                k++;
            if (!separate_word(s.substring(j), k - j, is_first, quote_found, opts))
                return false;
            is_first = false;
            j = k;
            if (j >= s.length())
                break;
        }

        if (dict.right_wall_defined)
            if (!issue_sentence_word(GlobalBean.RIGHT_WALL_WORD))
                return false;

        return (length > (dict.left_wall_defined ? 1 : 0) + (dict.right_wall_defined ? 1 : 0));
    }

    /**
     * w points to a string, wend points to the char one after the end.  The
     * "word" w contains no blanks.  This function splits up the word if
     * necessary, and calls "issue_sentence_word()" on each of the resulting
     * parts.  The process is described above.  returns true of OK, false if
     * too many punctuation marks
     */
    private boolean separate_word(String w, int wend, boolean is_first_word, boolean quote_found, ParseOptions opts) {
        
        int i, j, k, l, len;
        int r_strippable = 0, l_strippable = 0, s_strippable = 0, p_strippable = 0, n_r_stripped, s_stripped;
        boolean word_is_in_dict, s_ok;
        // TODO allocate r_strippable
        int r_stripped[] = new int[GlobalBean.MAX_STRIP]; /* these were stripped from the right */
        String strip_left[] = null;
        String strip_right[] = null;
        String prefix[] = null;
        String suffix[] = null;
        StringBuffer word = new StringBuffer();
        StringBuffer newword = new StringBuffer();
        DictNode dn, dn2, start_dn;
        String rpunc_con = "RPUNC";
        String lpunc_con = "LPUNC";
        String suf_con = "SUF";
        String pre_con = "PRE";

        if (dict.affix_table != null) {
            start_dn = DictNode.list_whole_dictionary(dict.affix_table.root, null);
            for (dn = start_dn; dn != null; dn = dn.right) {
                if (dn.word_has_connector(rpunc_con, 0))
                    r_strippable++;
                if (dn.word_has_connector(lpunc_con, 0))
                    l_strippable++;
                if (dn.word_has_connector(suf_con, 0))
                    s_strippable++;
                if (dn.word_has_connector(pre_con, 0))
                    p_strippable++;
            }
            strip_right = new String[r_strippable];
            strip_left = new String[l_strippable];
            suffix = new String[s_strippable];
            prefix = new String[p_strippable];

            i = 0;
            j = 0;
            k = 0;
            l = 0;
            dn = start_dn;
            while (dn != null) {
                if (dn.word_has_connector(rpunc_con, 0)) {
                    strip_right[i] = dn.string;
                    i++;
                }
                if (dn.word_has_connector(lpunc_con, 0)) {
                    strip_left[j] = dn.string;
                    j++;
                }
                if (dn.word_has_connector(suf_con, 0)) {
                    suffix[k] = dn.string;
                    k++;
                }
                if (dn.word_has_connector(pre_con, 0)) {
                    prefix[l] = dn.string;
                    l++;
                }
                dn2 = dn.right;
                dn = dn2;
            }
        }

        for (;;) {
            for (i = 0; i < l_strippable; i++) {
                if (w.startsWith(strip_left[i])) {
                    if (!issue_sentence_word(strip_left[i]))
                        return false;
                    w = w.substring(strip_left[i].length());
                    wend -= strip_left[i].length();
                    break;
                }
            }
            if (i == l_strippable)
                break;
        }

        /* Now w points to the string starting just to the right of any left-stripped characters. */
        /* stripped[] is an array of numbers, indicating the index numbers (in the strip_right array) of any
           strings stripped off; stripped[0] is the number of the first string stripped off, etc. When it
           breaks out of this loop, n_stripped will be the number of strings stripped off. */

        for (n_r_stripped = 0; n_r_stripped < GlobalBean.MAX_STRIP; n_r_stripped++) {

            word = new StringBuffer(w.substring(0, wend));
            if (word.length() == 0)
                break; /* it will work without this */

            if (dict.boolean_dictionary_lookup(word.toString()) || Dictionary.is_initials_word(word.toString()))
                break;
            if (is_first_word && Character.isUpperCase(word.charAt(0))) {
                /* This should happen if it's a word after a colon, also! */
                word.setCharAt(0, Character.toLowerCase(word.charAt(0)));
                if (dict.boolean_dictionary_lookup(word.toString())) {
                    /* restore word to what it was */
                    word.setCharAt(0, Character.toUpperCase(word.charAt(0)));

                    break;
                }
                word.setCharAt(0, Character.toUpperCase(word.charAt(0)));
            }
            for (i = 0; i < r_strippable; i++) {
                len = strip_right[i].length();
                if (wend < len)
                    continue; /* the remaining w is too short for a possible match */
                if (word.toString().endsWith(strip_right[i])) {
                    r_stripped[n_r_stripped] = i;
                    wend -= len;
                    break;
                }
            }
            if (i == r_strippable)
                break;
        }

        /* Now we strip off suffixes...w points to the remaining word, "wend" to the end of the word. */

        s_stripped = -1;
        word = new StringBuffer(w.substring(0, wend));
        word_is_in_dict = false;

        if (dict.boolean_dictionary_lookup(word.toString()) || Dictionary.is_initials_word(word.toString()))
            word_is_in_dict = true;
        if (is_first_word && Character.isUpperCase(word.charAt(0))) {
            word.setCharAt(0, Character.toLowerCase(word.charAt(0)));
            if (dict.boolean_dictionary_lookup(word.toString())) {
                word_is_in_dict = true;
            }
            word.setCharAt(0, Character.toUpperCase(word.charAt(0)));
        }
        if (!word_is_in_dict) {
            j = 0;
            for (i = 0; i <= s_strippable; i++) {
                s_ok = false;
                /* Go through once for each suffix; then go through one final time for the no-suffix case */
                if (i < s_strippable) {
                    len = suffix[i].length();
                    if (wend < len)
                        continue; /* the remaining w is too short for a possible match */
                    if (word.toString().endsWith(suffix[i]))
                        s_ok = true;
                } else
                    len = 0;

                if (s_ok || i == s_strippable) {

                    newword = new StringBuffer(word.substring(0, wend - len));

                    /* Check if the remainder is in the dictionary; for the no-suffix case, it won't be */
                    if (dict.boolean_dictionary_lookup(newword.toString())) {
                        if (opts.verbosity > 1 && i < s_strippable)
                            opts.out.println("Splitting word into two: " + newword.toString() + "-" + suffix[i]);
                        s_stripped = i;
                        wend -= len;
                        word = newword;
                        break;
                    } else {
                        /* If the remainder isn't in the dictionary, try stripping off prefixes */
                        for (j = 0; j < p_strippable; j++) {
                            if (newword.toString().startsWith(prefix[j])) {
                                newword.delete(0, prefix[j].length());
                                if (dict.boolean_dictionary_lookup(newword.toString())) {
                                    if (opts.verbosity > 1 && i < s_strippable)
                                        opts.out.println(
                                            "Splitting word into three: "
                                                + prefix[j]
                                                + "-"
                                                + newword.toString()
                                                + "-"
                                                + suffix[i]);
                                    if (!issue_sentence_word(prefix[j]))
                                        return false;
                                    if (i < s_strippable)
                                        s_stripped = i;
                                    wend -= len;
                                    word = newword;
                                    break;
                                }
                            }
                        }
                    }
                    if (j != p_strippable)
                        break;
                }
            }
        }

        /* word is now what remains after all the stripping has been done */

        /*    
        if (n_stripped == MAX_STRIP) {
        lperror(SEPARATE, 
            ".\n\"%s\" is followed by too many punctuation marks.\n", word);
        return false;
        } */

        if (quote_found)
            post_quote[length] = 1;

        if (!issue_sentence_word(word.toString()))
            return false;

        if (s_stripped != -1) {
            if (!issue_sentence_word(suffix[s_stripped]))
                return false;
        }

        for (i = n_r_stripped - 1; i >= 0; i--) {
            if (!issue_sentence_word(strip_right[r_stripped[i]]))
                return false;
        }

        return true;
    }
    /**
     * the string s is the next word of the sentence do not issue the empty
     * string.  
     * @return false if too many words or the word is too long.
     */
    private boolean issue_sentence_word(String s) {
        
        if (s.length() == 0)
            return true;
        if (s.length() > GlobalBean.MAX_WORD) {
            throw new RuntimeException(
                ". The word \""
                    + s
                    + "\" is too long.\n"
                    + "A word can have a maximum of "
                    + GlobalBean.MAX_WORD
                    + " characters.\n");
        }

        if (length == GlobalBean.MAX_SENTENCE) {
            throw new RuntimeException(". The sentence has too many words.\n");
        }
        word[length] = new Word();
        word[length].string = s;
        /* Now we record whether the first character of the word is upper-case. 
           (The first character may be made lower-case
           later, but we may want to get at the original version) */
        if (Character.isUpperCase(s.charAt(0)))
            word[length].firstupper = true;
        else
            word[length].firstupper = false;
        length++;
        return true;
    }

    /**
     * Corrects case of first word, fills in other proper nouns, and
     * builds the expression lists for the resulting words.
     * <p>     
     * Algorithm: 
     * <ul>   
     * <li>          Apply the following step to all words w:
     * <li>          if w is in the dictionary, use it.
     * <li>          else if w is upper case use PROPER_WORD disjuncts for w.
     * <li>          else if it's hyphenated, use HYPHENATED_WORD
     * <li>          else if it's a number, use NUMBER_WORD.
     * <li>       
     * <li>          Now, we correct the first word, w.
     * <li>          if w is upper case, let w' be the lower case version of w.
     * <li>          if both w and w' are in the dict, concatenate these disjuncts.
     * <li>          else if w' is in dict, use disjuncts of w'
     * <li>          else leave the disjuncts alone
     * </us>
     *<p>
     * Here's a summary of how subscripts are handled:
     * <p>
     *  Reading the dictionary:
     * <p>
     *    If the last "." in a string is followed by a non-digit character,
     *    then the "." and everything after it is considered to be the subscript
     *    of the word.
     * <p>
     *    The dictionary reader does not allow you to have two words that
     *    match according to the criterion below.  (so you can't have
     *    "dog.n" and "dog")
     * <p>
     *    Quote marks are used to allow you to define words in the dictionary
     *    which would otherwise be considered part of the dictionary, as in
     * <p>
     *     ";": {&#064Xca-} & Xx- & (W+ or Qd+) & {Xx+};
     *     "%" : (ND- & {DD-} & \<noun-sub-x\> & 
     *     (\<noun-main-x\> or B*x+)) or (ND- & (OD- or AN+));
     * <p>
     *  Rules for chopping words from the input sentence:
     * <p>
     *     First the prefix chars are stripped off of the word.  These
     *     characters are "(" and "$" (and now "``")
     * <p>
     *     Now, repeat the following as long as necessary:
     * <p>
     *     Look up the word in the dictionary.
     *     If it's there, the process terminates.
     * <p>
     *     If it's not there and it ends in one of the right strippable
     *     strings (see "right_strip") then remove the strippable string
     *     and make it into a separate word.
     * <p>
     *     If there is no strippable string, then the process terminates. 
     * <p>
     *  Rule for defining subscripts in input words:
     * <p>
     *     The subscript rule is followed just as when reading the dictionary.
     * <p>
     *  When does a word in the sentence match a word in the dictionary?
     * <p>
     *     Matching is done as follows: Two words with subscripts must match
     *     exactly.  If neither has a subscript they must match exactly.  If one
     *     does and one doesn't then they must match when the subscript is
     *     removed.  Notice that this is symmetric.
     * <p>
     *  So, under this system, the dictonary could have the words "Ill" and
     *  also the word "Ill."  It could also have the word "i.e.", which could be
     *  used in a sentence.
     *
     *
     * @param opts - not used everything comes from GlobalBean. TODO - Fix or drop
     */
    public void build_sentence_expressions(ParseOptions opts) {
        
        int i, first_word; /* the index of the first word after the wall */
        String s, u;
        StringBuffer temp_word;
        XNode e;

        if (dict.left_wall_defined) {
            first_word = 1;
        } else {
            first_word = 0;
        }

        /* the following loop treats all words the same 
           (nothing special for 1st word) */
        for (i = 0; i < length; i++) {
            s = word[i].string;
            if (dict.boolean_dictionary_lookup(s)) {
                word[i].x = build_word_expressions(s);
            } else if (
                Character.isUpperCase(s.charAt(0)) && Dictionary.is_s_word(s) && dict.pl_capitalized_word_defined) {
                special_string(i, GlobalBean.PL_PROPER_WORD);
            } else if (Character.isUpperCase(s.charAt(0)) && dict.capitalized_word_defined) {
                special_string(i, GlobalBean.PROPER_WORD);
            } else if (Dictionary.is_number(s) && dict.number_word_defined) {
                /* we know it's a plural number, or 1 */
                /* if the string is 1, we'll only be here if 1's not in the dictionary */
                special_string(i, GlobalBean.NUMBER_WORD);
            } else if (Dictionary.ishyphenated(s) && dict.hyphenated_word_defined) {
                /* singular hyphenated */
                special_string(i, GlobalBean.HYPHENATED_WORD);
            } else if (Dictionary.is_ing_word(s) && dict.ing_word_defined) {
                guessed_string(i, s, GlobalBean.ING_WORD);
            } else if (Dictionary.is_s_word(s) && dict.s_word_defined) {
                guessed_string(i, s, GlobalBean.S_WORD);
            } else if (Dictionary.is_ed_word(s) && dict.ed_word_defined) {
                guessed_string(i, s, GlobalBean.ED_WORD);
            } else if (Dictionary.is_ly_word(s) && dict.ly_word_defined) {
                guessed_string(i, s, GlobalBean.LY_WORD);
            } else if (dict.unknown_word_defined && dict.use_unknown_word) {
                handle_unknown_word(i, s);
            } else {
                /* the reason I can assert this is that the word
                   should have been looked up already if we get here. */
                throw new RuntimeException("I should have found that word.");
            }
        }

        /* Under certain cases--if it's the first word of the sentence, or if it follows a colon
           or a quotation mark--a word that's capitalized has to be looked up as an uncapitalized 
           word (as well as a capitalized word). */

        for (i = 0; i < length; i++) {
            if (!(i == first_word || (i > 0 && ":".equals(word[i - 1].string)) || post_quote[i] == 1))
                continue;
            s = word[i].string;
            if (Character.isUpperCase(s.charAt(0))) {
                temp_word = new StringBuffer(s);
                temp_word.setCharAt(0, Character.toLowerCase(temp_word.charAt(0)));
                u = temp_word.toString();
                /* If the lower-case version is in the dictionary... */
                if (dict.boolean_dictionary_lookup(u)) {
                    /* Then check if the upper-case version is there. If it is, the disjuncts for
                    the upper-case version have been put there already. So add on the disjuncts
                    for the lower-case version. */
                    if (dict.boolean_dictionary_lookup(s)) {
                        e = build_word_expressions(u);
                        word[i].x = XNode.catenate_XNodes(word[i].x, e);
                    } else {
                        /* If the upper-case version isn't there, replace the u.c. disjuncts with l.c. ones */
                        temp_word = new StringBuffer(s);
                        temp_word.setCharAt(0, Character.toLowerCase(temp_word.charAt(0)));
                        word[i].string = temp_word.toString();
                        e = build_word_expressions(word[i].string);
                        word[i].x = e;
                    }
                }
            }
        }
    }

    /**
     * Looks up the word s in the dictionary.  Returns null if it's not there.
     * If there, it builds the list of expressions for the word, and returns
     * a pointer to it.
     * @param s is a word as a String object
     * @return XNode
     * 
     */

    private XNode build_word_expressions(String s) {
        
        DictNode dn;
        XNode x, y;

        //dn = dict.filtered_dictionary_lookup(s);
        dn = dict.dictionary_lookup(s);

        x = null;
        while (dn != null) {
            y = new XNode();
            y.next = x;
            x = y;
            x.exp = Exp.copy_Exp(dn.exp);
            x.string = dn.string;
            dn = dn.right;
        }
        return x;
    }

    private void special_string(int i, String s) {
        XNode e;
        if (dict.boolean_dictionary_lookup(s)) {
            word[i].x = build_word_expressions(s);
            for (e = word[i].x; e != null; e = e.next) {
                e.string = word[i].string;
            }
        } else {
            throw new RuntimeException(
                "Can't build expressions. To process this sentence your dictionary needs the word \"" + s + "\"");
        }
    }

    private void guessed_string(int i, String s, String type) {
        XNode e;
        int t, u;
        if (dict.boolean_dictionary_lookup(type)) {
            word[i].x = build_word_expressions(type);
            e = word[i].x;
            if (Dictionary.is_s_word(s)) {
                for (; e != null; e = e.next) {
                    t = e.string.indexOf('.');
                    if (t >= 0) {
											e.string = s + "[!]." + e.string.substring(t + 1);
                    } else {
                        e.string = s + "[!]";
                    }
                }
            } else {
                if (Dictionary.is_ed_word(s)) {
                    e.string = s + "[!].v";
                } else if (Dictionary.is_ing_word(s)) {
                    e.string = s + "[!].g";
                } else if (Dictionary.is_ly_word(s)) {
                    e.string = s + "[!].e";
                } else {
                    e.string = s + "[!]";
                }
            }
        } else {
            throw new RuntimeException(
                "Can't build expressions. To process this sentence your dictionary needs the word \"" + type + "\"");
        }
    }

    private void handle_unknown_word(int i, String s) {
        /* puts into word[i].x the expression for the unknown word */
        /* the parameter s is the word that was not in the dictionary */
        /* it massages the names to have the corresponding subscripts */
        /* to those of the unknown words */
        /* so "grok" becomes "grok[?].v"  */
        int t, u;
        XNode d;

        word[i].x = build_word_expressions(GlobalBean.UNKNOWN_WORD);
        if (word[i].x == null) {
            throw new RuntimeException("UNKNOWN_WORD should have been there");
        }

        for (d = word[i].x; d != null; d = d.next) {
            t = d.string.indexOf('.');
            StringBuffer str = new StringBuffer();
            if (t >= 0) {
                str.append(s + "[?]." + d.string.substring(t + 1));
            } else {
                str.append(s + "[?]");
            }
            d.string = str.toString();
        }
    }

    /**
     * @see AndData
     */
    
    public void initialize_conjunction_tables() {
        int i;
        and_data.LT_bound = 0;
        and_data.LT_size = 0;
        and_data.label_table = null;
        for (i = 0; i < GlobalBean.HT_SIZE; i++) {
            and_data.hash_table[i] = null;
        }
    }

    /**
     * How is the is_conjunction table initialized?
     * TODO - Remove English dependancy
     * Also what about "yet", "however", "then", "else", "whence", "thus", ...
     * Word word[w] has a list of equivilent expressions that should be followed
     * what is needed here is:
     *
     * if (isConjunct(word[w])||isDisjunct(word[w])||isAdjunct(word[w])) then is_conjunction[w] = true;
     * 
     * where isConjunct(Word word) walks down the list of equivilent expressions 
     */
     public void set_is_conjunction() {
        int w;
        String s;
        for (w = 0; w < length; w++) {
            s = word[w].string;
            is_conjunction[w] = ((s.equals("and")) || (s.equals("or")) || (s.equals("but")) || (s.equals("nor")));
        }
    }

    /**
     * get sentence length in words
     *
     * @return int length
     */
    public int sentence_length() {
        return this.length;  
    }

    /** 
     * assumes that the sentence expression lists have been generated    
     * this does all the necessary pruning and building of and         
     * structures.
     * @param opts parsing options
     */

    public void prepare_to_parse(ParseOptions opts) {
        
        int i;
        boolean has_conjunction;

        build_sentence_disjuncts(opts, opts.disjunct_cost);
        if (opts.verbosity > 2) {
            opts.out.println("After expanding expressions into disjuncts:");
            print_disjunct_counts(opts);
        }
        opts.print_time("Built disjuncts");

        for (i = 0; i < length; i++) {
            word[i].d = Disjunct.eliminate_duplicate_disjuncts(opts, word[i].d);
        }
        opts.print_time("Eliminated duplicate disjuncts");

        if (opts.verbosity > 2) {
            opts.out.println();
            opts.out.println("After expression pruning and duplicate elimination:");
            print_disjunct_counts(opts);
        }

        null_links = (opts.min_null_count > 0);

        has_conjunction = sentence_contains_conjunction();
        set_connector_length_limits(opts);
        build_deletable(has_conjunction);
        build_effective_dist(has_conjunction);
        /**
         * why do we do these here instead of in
           first_prepare_to_parse() only?  The
           reason is that the deletable region
           depends on if null links are in use.
           with null_links everything is deletable 
         */

        if (!has_conjunction) {
            pp_and_power_prune(GlobalBean.RUTHLESS, opts);
        } else {
            pp_and_power_prune(GlobalBean.GENTLE, opts);
            conjunction_prune(opts);
            if (opts.verbosity > 2) {
                opts.out.println();
                opts.out.println("After conjunction pruning:");
                print_disjunct_counts(opts);
            }
            opts.print_time("Done conjunction pruning");
            build_conjunction_tables();
            install_fat_connectors();
            install_special_conjunctive_connectors();
            checkDuplicate("5");
            if (opts.verbosity > 2) {
                opts.out.print("After conjunctions, disjuncts counts:");
                print_disjunct_counts(opts);
            }
            set_connector_length_limits(opts);
            /* have to do this again cause of the
               new fat connectors and disjuncts */

            checkDuplicate("6");
            opts.print_time("Constructed fat disjuncts");

            prune(opts);
            checkDuplicate("7");
            opts.print_time("Pruned fat disjuncts");

            for (i = 0; i < length; i++) {
                word[i].d = Disjunct.eliminate_duplicate_disjuncts(opts, word[i].d);
            }
            if (opts.verbosity > 2) {
                opts.out.println("After pruning and duplicate elimination:");
                print_disjunct_counts(opts);
            }
            opts.print_time("Eliminated duplicate disjuncts (again)");

            if (opts.verbosity > 2)
                print_AND_statistics(opts);

            power_prune(GlobalBean.RUTHLESS, opts);
        }
    }

    private void checkDuplicate(String label) {
        HashSet disj = new HashSet();
        HashSet conn = new HashSet();
        for (int i = 0; i < length; i++) {
            for (Disjunct d = word[i].d; d != null; d = d.next) {
                if (disj.contains(d)) {
                    throw new RuntimeException(label + " dup disj w=" + word[i] + " d=" + d.string);
                } else {
                    disj.add(d);
                    for (Connector c = d.left; c != null; c = c.next) {
                        if (conn.contains(c)) {
                            throw new RuntimeException(
                                label + " dup conn w=" + word[i] + " d=" + d.string + " c=" + c.string + "-");
                        } else {
                            conn.add(c);
                        }
                    }
                    for (Connector c = d.right; c != null; c = c.next) {
                        if (conn.contains(c)) {
                            throw new RuntimeException(
                                label + " dup conn w=" + word[i] + " d=" + d.string + " c=" + c.string + "+");
                        } else {
                            conn.add(c);
                        }
                    }
                }
            }
        }
    }

            /**
           We've already built the sentence disjuncts, and we've pruned them
           and power_pruned(GENTLE) them also.  The sentence contains a
           conjunction.  deletable[][] has been initialized to indicate the
           ranges which may be deleted in the final linkage.
        
           This routine deletes irrelevant disjuncts.  It finds them by first
           marking them all as irrelevant, and then marking the ones that
           might be useable.  Finally, the unmarked ones are removed.
             *
             * @param opts parsing options used to set tolerance for nulls
        
           */
    public void conjunction_prune(ParseOptions opts) {

        Disjunct d;
        int w;

        /* we begin by unmarking all disjuncts.  This would not be necessary if
           whenever we created a disjunct we cleared its marked field.
           I didn't want to search the program for all such places, so
           I did this way.
           */
        for (w = 0; w < length; w++) {
            for (d = word[w].d; d != null; d = d.next) {
                d.marked = false;
            }
        }

        init_fast_matcher();
        init_table();
        local_sent = word;
        null_links = (opts.min_null_count > 0);
        /*
        for (d = sent.word[0].d; d != null; d = d.next) {
        if ((d.left == null) && region_valid(0, sent.length, d.right, null)) {
            mark_region(0, sent.length, d.right, null);
            d.marked = true;
        }
        }
        mark_region(0, sent.length, null, null);
        */

        if (null_links) {
            mark_region(-1, length, null, null);
        } else {
            for (w = 0; w < length; w++) {
                /* consider removing the words [0,w-1] from the beginning
                   of the sentence */
                if (deletable[0][w]) {
                    for (d = word[w].d; d != null; d = d.next) {
                        if ((d.left == null) && region_valid(w, length, d.right, null) > 0) {
                            mark_region(w, length, d.right, null);
                            d.marked = true;
                        }
                    }
                }
            }
        }

        delete_unmarked_disjuncts();
        if (opts.verbosity > 1) {
            opts.out.println("" + match_cost + " Match cost");
        }

    }

    /**
     *    
     *    CONJUNCTION PRUNING.
     *    
     *    The basic idea is this.  Before creating the fat disjuncts,
     *    we run a modified version of the exhaustive search procedure.
     *    Its purpose is to mark the disjuncts that can be used in any
     *    linkage.  It's just like the normal exhaustive search, except that
     *    if a subrange of words are deletable, then we treat them as though
     *    they were not even there.  So, if we call the function in the
     *    situation where the set of words between the left and right one
     *    are deletable, and the left and right connector pointers
     *    are null, then that range is considered to have a solution.
     *    
     *    There are actually two procedures to implement this.  One is
     *    mark_region() and the other is region_valid().  The latter just
     *    checks to see if the given region can be completed (within it).
     *    The former actually marks those disjuncts that can be used in
     *    any valid linkage of the given region.
     *    
     *    As in the standard search procedure, we make use of the fast-match
     *    data structure (which requires power pruning to have been done), and
     *    we also use a hash table.  The table is used differently in this case.
     *    The meaning of values stored in the table are as follows:
     *    
     *    -1  Nothing known (Actually, this is not stored.  It's returned
     *    by table_lookup when nothing is known.)
     *    0  This region can't be completed (marking is therefore irrelevant)
     *    1  This region can be completed, but it's not yet marked
     *    2  This region can be completed, and it's been marked.
     * @param lw integer word number of left wall
     * @param rw integer word number of right wall
     * @param le left expression
     * @param re right expression
     * @return Returns 0 if this range cannot be successfully filled in with links.  
     * Returns 1 if it can, and it's not been marked, and 
     * Returns 2 if it can and it has been marked.
     */

    public int region_valid(int lw, int rw, Connector le, Connector re) {
        /* Returns 0 if this range cannot be successfully filled in with           */
        /* links.  Returns 1 if it can, and it's not been marked, and returns      */
        /* 2 if it can and it has been marked.                                     */

        Disjunct d;
        boolean left_valid = false, right_valid = false;
        int found;
        int i, start_word, end_word;
        int w;
        MatchNode m, m1;

        i = table_lookup(lw, rw, le, re, 0);
        if (i >= 0) {
            return i;
        }

        if ((le == null) && (re == null) && deletable[lw + 1][rw]) {
            table_store(lw, rw, le, re, 0, 1);
            return 1;
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

        found = 0;

        for (w = start_word; w <= end_word; w++) {
            m1 = m = form_match_list(w, le, lw, re, rw);
            for (; m != null; m = m.next) {
                d = m.d;
                /* mark_cost++;*/
                /* in the following expressions we use the fact that 0=false. Could eliminate
                   by always saying "region_valid(...) != 0"  */
                left_valid =
                    (((le != null) && (d.left != null) && Connector.prune_match(this, le, d.left, lw, w))
                        && ((region_valid(lw, w, le.next, d.left.next) > 0)
                            || ((le.multi) && region_valid(lw, w, le, d.left.next) > 0)
                            || ((d.left.multi) && region_valid(lw, w, le.next, d.left) > 0)
                            || ((le.multi && d.left.multi) && region_valid(lw, w, le, d.left) > 0)));
                if (left_valid && region_valid(w, rw, d.right, re) > 0) {
                    found = 1;
                    break;
                }
                right_valid =
                    (((d.right != null) && (re != null) && Connector.prune_match(this, d.right, re, w, rw))
                        && ((region_valid(w, rw, d.right.next, re.next) > 0)
                            || ((d.right.multi) && region_valid(w, rw, d.right, re.next) > 0)
                            || ((re.multi) && region_valid(w, rw, d.right.next, re) > 0)
                            || ((d.right.multi && re.multi) && region_valid(w, rw, d.right, re) > 0)));
                if ((left_valid && right_valid) || (right_valid && region_valid(lw, w, le, d.left) > 0)) {
                    found = 1;
                    break;
                }
            }
            if (found != 0)
                break;
        }
        table_store(lw, rw, le, re, 0, found);
        return found;
    }

    /**
     * Forms and returns a list of disjuncts that might match lc or rc or both.
     * lw and rw are the words from which lc and rc came respectively.
     * The list is formed by the link pointers of MatchNodes.
     * The list contains no duplicates.  A quadratic algorithm is used to
     * eliminate duplicates.  In practice the match_cost is less than the
     * parse_cost (and the loop is tiny), so there's no reason to bother
     * to fix this.
     * @param w array index of word to match
     * @param lc left Connector
     * @param lw index into word array of left word
     * @param rc right Connector
     * @param rw index into word array of right word
     * @return the right match
     */
    public static MatchNode form_match_list(int w, Connector lc, int lw, Connector rc, int rw) {
        /* 
        */
        MatchNode ml, mr, mx, my, mz, front, free_later;

        if (lc != null) {
            ml = match_l_table[w][fast_match_hash(lc) & (match_l_table_size[w] - 1)];
        } else {
            ml = null;
        }
        if (rc != null) {
            mr = match_r_table[w][fast_match_hash(rc) & (match_r_table_size[w] - 1)];
        } else {
            mr = null;
        }

        front = null;
        for (mx = ml; mx != null; mx = mx.next) {
            if (mx.d.left.word < lw)
                break;
            my = get_match_node();
            my.d = mx.d;
            my.next = front;
            front = my;
        }
        ml = front; /* ml is now the list of things that could match the left */

        front = null;
        for (mx = mr; mx != null; mx = mx.next) {
            if (mx.d.right.word > rw)
                break;
            my = get_match_node();
            my.d = mx.d;
            my.next = front;
            front = my;
        }
        mr = front; /* mr is now the list of things that could match the right */

        /* now we want to eliminate duplicates from the lists */

        free_later = null;
        front = null;
        for (mx = mr; mx != null; mx = mz) {
            /* see if mx in first list, put it in if its not */
            mz = mx.next;
            match_cost++;
            for (my = ml; my != null; my = my.next) {
                match_cost++;
                if (mx.d == my.d)
                    break;
            }
            if (my != null) { /* mx was in the l list */
                mx.next = free_later;
                free_later = mx;
            }
            if (my == null) { /* it was not there */
                mx.next = front;
                front = mx;
            }
        }
        mr = front; /* mr is now the abbreviated right list */

        /* now catenate the two lists */
        if (mr == null)
            return ml;
        for (mx = mr; mx.next != null; mx = mx.next);
        mx.next = ml;
        return mr;
    }

    static MatchNode get_match_node() {
        /* return a match node to be used by the caller */
        return new MatchNode();
    }

    void table_update(int lw, int rw, Connector le, Connector re, int cost, int count) {
        /* Stores the value in the table.  Unlike table_store, it assumes it's already there */
        TableConnector t = table_pointer(lw, rw, le, re, cost);

        if (t == null) {
            throw new RuntimeException("This entry is supposed to be in the table.");
        }
        t.count = count;
    }

    void mark_region(int lw, int rw, Connector le, Connector re) {
        /* Mark as useful all disjuncts involved in some way to complete the structure  */
        /* within the current region.  Note that only disjuncts strictly between        */
        /* lw and rw will be marked.  If it so happens that this region itself is not   */
        /* valid, then this fact will be recorded in the table, and nothing else happens*/

        Disjunct d;
        boolean left_valid, right_valid;
        int i;
        int start_word, end_word;
        int w;
        MatchNode m, m1;

        i = region_valid(lw, rw, le, re);
        if ((i == 0) || (i == 2))
            return;
        /* we only reach this point if it's a valid unmarked region, i=1 */
        table_update(lw, rw, le, re, 0, 2);

        if ((le == null) && (re == null) && (null_links) && (rw != 1 + lw)) {
            w = lw + 1;
            for (d = local_sent[w].d; d != null; d = d.next) {
                if ((d.left == null) && region_valid(w, rw, d.right, null) > 0) {
                    d.marked = true;
                    mark_region(w, rw, d.right, null);
                }
            }
            mark_region(w, rw, null, null);
            return;
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
            m1 = m = form_match_list(w, le, lw, re, rw);
            for (; m != null; m = m.next) {
                d = m.d;
                /* mark_cost++;*/
                left_valid =
                    (((le != null) && (d.left != null) && Connector.prune_match(this, le, d.left, lw, w))
                        && ((region_valid(lw, w, le.next, d.left.next) > 0)
                            || ((le.multi) && region_valid(lw, w, le, d.left.next) > 0)
                            || ((d.left.multi) && region_valid(lw, w, le.next, d.left) > 0)
                            || ((le.multi && d.left.multi) && region_valid(lw, w, le, d.left) > 0)));
                right_valid =
                    (((d.right != null) && (re != null) && Connector.prune_match(this, d.right, re, w, rw))
                        && ((region_valid(w, rw, d.right.next, re.next) > 0)
                            || ((d.right.multi) && region_valid(w, rw, d.right, re.next) > 0)
                            || ((re.multi) && region_valid(w, rw, d.right.next, re) > 0)
                            || ((d.right.multi && re.multi) && region_valid(w, rw, d.right, re) > 0)));

                /* The following if statements could be restructured to avoid superfluous calls
                   to mark_region.  It didn't seem a high priority, so I didn't optimize this.
                   */

                if (left_valid && region_valid(w, rw, d.right, re) > 0) {
                    d.marked = true;
                    mark_region(w, rw, d.right, re);
                    mark_region(lw, w, le.next, d.left.next);
                    if (le.multi)
                        mark_region(lw, w, le, d.left.next);
                    if (d.left.multi)
                        mark_region(lw, w, le.next, d.left);
                    if (le.multi && d.left.multi)
                        mark_region(lw, w, le, d.left);
                }

                if (right_valid && region_valid(lw, w, le, d.left) > 0) {
                    d.marked = true;
                    mark_region(lw, w, le, d.left);
                    mark_region(w, rw, d.right.next, re.next);
                    if (d.right.multi)
                        mark_region(w, rw, d.right, re.next);
                    if (re.multi)
                        mark_region(w, rw, d.right.next, re);
                    if (d.right.multi && re.multi)
                        mark_region(w, rw, d.right, re);
                }

                if (left_valid && right_valid) {
                    d.marked = true;
                    mark_region(lw, w, le.next, d.left.next);
                    if (le.multi)
                        mark_region(lw, w, le, d.left.next);
                    if (d.left.multi)
                        mark_region(lw, w, le.next, d.left);
                    if (le.multi && d.left.multi)
                        mark_region(lw, w, le, d.left);
                    mark_region(w, rw, d.right.next, re.next);
                    if (d.right.multi)
                        mark_region(w, rw, d.right, re.next);
                    if (re.multi)
                        mark_region(w, rw, d.right.next, re);
                    if (d.right.multi && re.multi)
                        mark_region(w, rw, d.right, re);
                }
            }
        }
    }

    Disjunct explode_disjunct_list(Disjunct d) {
        /*  This is basically a "map" function for build_fat_link_substitutions.
             It's applied to the disjuncts for all regular words of the sentence.
        */
        Disjunct d1;

        d1 = null;

        for (; d != null; d = d.next) {
            d1 = Disjunct.catenate_disjuncts(d1, build_fat_link_substitutions(d));
        }
        return d1;
    }

    Disjunct build_COMMA_disjunct_list() {
        /*  Builds and returns a disjunct list for the comma.  These are the
            disjuncts that are used when "," operates in conjunction with "and".
            Does not deal with the ", and" issue, nor the other uses
            of comma.
        */
        int lab;
        Disjunct d1, d2, d, wd;
        Connector work_connector1 = new Connector(), work_connector2 = new Connector(), c1, c2;
        Connector work_connector3 = new Connector(), c3;
        c1 = work_connector1;
        c1.init_connector();
        c2 = work_connector2;
        c2.init_connector();
        c3 = work_connector3;
        c3.init_connector();
        wd = new Disjunct();

        d1 = null; /* where we put the list we're building */

        c1.next = null;
        c2.next = c3;
        c3.next = null;
        c1.priority = c3.priority = GlobalBean.DOWN_priority;
        c2.priority = GlobalBean.UP_priority;
        c1.multi = c2.multi = c3.multi = false;
        wd.left = c1;
        wd.right = c2;
        wd.string = ","; /* *** fix this later?? */
        wd.next = null;
        wd.cost = 0;
        for (lab = 0; lab < and_data.LT_size; lab++) {
            for (d = and_data.label_table[lab]; d != null; d = d.next) {
                c1.string = c2.string = c3.string = d.string;
                c1.label = c2.label = c3.label = lab;
                d2 = Disjunct.copy_disjunct(wd);
                d2.next = d1;
                d1 = d2;
            }
        }
        return d1;
    }

    void install_fat_connectors() {
        /* Installs all the special fat disjuncts on all of the words of the   */
        /* sentence */
        int i;
        for (i = 0; i < length; i++) {
            if (is_conjunction[i]) {
                word[i].d = Disjunct.catenate_disjuncts(word[i].d, build_AND_disjunct_list(word[i].string));
            } else {
                word[i].d = Disjunct.catenate_disjuncts(word[i].d, explode_disjunct_list(word[i].d));
                if (word[i].string.equals(",")) {
                    word[i].d = Disjunct.catenate_disjuncts(word[i].d, build_COMMA_disjunct_list());
                }
            }
        }
    }

    Disjunct build_fat_link_substitutions(Disjunct d) {
        /* This function allocates and returns a list of disjuncts.
           This is the one obtained by substituting each contiguous
           non-empty subrange of d (incident on the center) by an appropriate
           fat link, in two possible positions.  Does not effect d.
           The cost of d is inherited by all of the disjuncts in the result.
        */
        Connector cl, cr, tl, tr, wc, work_connector = new Connector();
        Disjunct d1, wd, work_disjunct = new Disjunct(), d_list;
        if (d == null)
            return null;
        wd = work_disjunct;
        wc = work_connector;
        wc.init_connector();
        d_list = null;

        // TODO: * wd = * d;
        wd.next = d.next;
        wd.cost = d.cost;
        wd.marked = d.marked;
        wd.string = d.string;
        wd.left = d.left;
        wd.right = d.right;

        tl = d.left;
        d.left = null;
        for (cr = d.right; cr != null; cr = cr.next) {
            tr = cr.next;
            cr.next = null;
            if (is_appropriate(d)) {
                connector_for_disjunct(d, wc);
                wd.left = tl;
                wd.right = wc;
                wc.next = tr;
                d1 = Disjunct.copy_disjunct(wd);
                d1.next = d_list;
                d_list = d1;
                wd.left = wc;
                wc.next = tl;
                wd.right = tr;
                d1 = Disjunct.copy_disjunct(wd);
                d1.next = d_list;
                d_list = d1;
            }
            cr.next = tr;
        }
        d.left = tl;

        tr = d.right;
        d.right = null;
        for (cl = d.left; cl != null; cl = cl.next) {
            tl = cl.next;
            cl.next = null;
            if (is_appropriate(d)) {
                connector_for_disjunct(d, wc);
                wd.left = tl;
                wd.right = wc;
                wc.next = tr;
                d1 = Disjunct.copy_disjunct(wd);
                d1.next = d_list;
                d_list = d1;
                wd.left = wc;
                wc.next = tl;
                wd.right = tr;
                d1 = Disjunct.copy_disjunct(wd);
                d1.next = d_list;
                d_list = d1;
            }
            cl.next = tl;
        }
        d.right = tr;

        for (cl = d.left; cl != null; cl = cl.next) {
            for (cr = d.right; cr != null; cr = cr.next) {
                tl = cl.next;
                tr = cr.next;
                cl.next = cr.next = null;
                if (is_appropriate(d)) {
                    connector_for_disjunct(d, wc);
                    wd.left = tl;
                    wd.right = wc;
                    wc.next = tr;
                    d1 = Disjunct.copy_disjunct(wd);
                    d1.next = d_list;
                    d_list = d1;
                    wd.left = wc;
                    wc.next = tl;
                    wd.right = tr;
                    d1 = Disjunct.copy_disjunct(wd);
                    d1.next = d_list;
                    d_list = d1;
                }
                cl.next = tl;
                cr.next = tr;
            }
        }
        return d_list;
    }

    /**
     * 
     * @param d 
     * @param c 
     */
    public void connector_for_disjunct(Disjunct d, Connector c) {
        /* Fill in the fields of c for the disjunct.  This must be in
           the table data structures.  The label field and the string field
           are filled in appropriately.  Priority is set to UP_priority.
        */
        int h;
        Disjunct d1 = null;
        LabelNode lp;

        h = d.and_hash_disjunct();

        for (lp = and_data.hash_table[h]; lp != null; lp = lp.next) {
            d1 = and_data.label_table[lp.label];
            if (d.disjunct_types_equal(d1))
                break;
        }
        if (lp == null) {
            throw new RuntimeException("A disjunct I inserted was not there. (1)");
        }
        /*
          I don't know what these lines were for.  I replaced them by
          the above assertion.
            if (lp == null) {
            printf("error: A disjunct I inserted was not there\n");
                lp = lp.next;  (to force an error)
            }
        */
        while (d1 != null) {
            if (d1.disjuncts_equal_AND(d))
                break;
            d1 = d1.next;
        }

        if (!(d1 != null)) {
            throw new RuntimeException("A disjunct I inserted was not there. (2)");
        }

        c.label = lp.label;
        c.string = d1.string;
        c.priority = GlobalBean.UP_priority;
        c.multi = false;
    }

    /**
     * 
     * Builds and returns a disjunct list for "and", "or" and "nor" 
     * for each disjunct in the label_table, we build three disjuncts 
     * this means that "Danny and Tycho and Billy" will be parsable in
     * two ways.  I don't know an easy way to avoid this 
     * the string is either "and", or "or", or "nor" at the moment
     * <p>
     * must accommodate "he and I are good", "Davy and I are good"
     *      "Danny and Davy are good", and reject all of these with "is"
     *      instead of "are".
     * <p>  
     *      The SI connectors must also be modified to accommodate "are John
     *      and Dave here", but kill "is John and Dave here"
     * <p>
     *    Then we consider "a cat or a dog is here"  vs  "a cat or a dog are here"
     *     The first seems right, the second seems wrong.  I'll stick with this.
     *   
     *     That is, "or" has the property that if both parts are the same in
     *     number,  we use that but if they differ, we use plural.
     * <p>  
     *     The connectors on "I" must be handled specially.  We accept
     *     "I or the dogs are here" but reject "I or the dogs is here"
     * <p>
     * TODO - the code here still does now work "right", rejecting "is John or I invited"
     *      and accepting "I or my friend know what happened"
     *   
     *      The more generous code for "nor" has been used instead
     * <p>
     * It appears that the "nor" of two things can be either singular or
     *      plural. 
     * <p>
     * "neither she nor John likes dogs"
     *<p>
     * "neither she nor John like dogs"
     *
     * @param s 
     * @return head of a disjunct list
     * @see Connector
     */
    public Disjunct build_AND_disjunct_list(String s) {
        

        int lab;
        Disjunct d_list, d1, d3, d, d_copy;
        Connector c1, c2, c3;

        d_list = null; /* where we put the list we're building */

        for (lab = 0; lab < and_data.LT_size; lab++) {
            for (d = and_data.label_table[lab]; d != null; d = d.next) {
                d1 = build_fat_link_substitutions(d);
                d_copy = Disjunct.copy_disjunct(d); /* also include the thing itself! */
                d_copy.next = d1;
                d1 = d_copy;
                for (; d1 != null; d1 = d3) {
                    d3 = d1.next;

                    c1 = new Connector();
                    c1.init_connector();
                    c2 = new Connector();
                    c2.init_connector();
                    c1.next = null;
                    c2.next = null;
                    c1.priority = c2.priority = GlobalBean.DOWN_priority;
                    c1.multi = c2.multi = false;
                    c1.string = c2.string = d.string;
                    c1.label = c2.label = lab;

                    d1.string = s;

                    if (d1.right == null) {
                        d1.right = c2;
                    } else {
                        for (c3 = d1.right; c3.next != null; c3 = c3.next);
                        c3.next = c2;
                    }
                    if (d1.left == null) {
                        d1.left = c1;
                    } else {
                        for (c3 = d1.left; c3.next != null; c3 = c3.next);
                        c3.next = c1;
                    }
                    d1.next = d_list;
                    d_list = d1;
                }
            }
        }
        //    if defined(PLURALIZATION)
        /* here is where "and" makes singular into plural. */
        /* must accommodate "he and I are good", "Davy and I are good"
           "Danny and Davy are good", and reject all of these with "is"
           instead of "are".
        
           The SI connectors must also be modified to accommodate "are John
           and Dave here", but kill "is John and Dave here"
        */
        if (s.equals("and")) {
            for (d1 = d_list; d1 != null; d1 = d1.next) {
                for (c1 = d1.right; c1 != null; c1 = c1.next) {
                    if (c1.string.length() >= 1
                        && (c1.string.charAt(0) == 'S')
                        && ((c1.string.length() == 1)
                            || (c1.string.charAt(1) == '^')
                            || (c1.string.charAt(1) == 's')
                            || (c1.string.charAt(1) == 'p'))) {
                        c1.string = "Sp";
                    }
                }
                for (c1 = d1.left; c1 != null; c1 = c1.next) {
                    if (c1.string.length() >= 2
                        && (c1.string.charAt(0) == 'S')
                        && (c1.string.charAt(1) == 'I')
                        && ((c1.string.length() == 2)
                            || (c1.string.charAt(2) == '^')
                            || (c1.string.charAt(2) == 's')
                            || (c1.string.charAt(2) == 'p'))) {
                        c1.string = "SIp";
                    }
                }
            }
        }
        /*
          "a cat or a dog is here"  vs  "a cat or a dog are here"
          The first seems right, the second seems wrong.  I'll stick with this.
        
          That is, "or" has the property that if both parts are the same in
          number,  we use that but if they differ, we use plural.
        
          The connectors on "I" must be handled specially.  We accept
          "I or the dogs are here" but reject "I or the dogs is here"
        */

        /* the code here still does now work "right", rejecting "is John or I invited"
           and accepting "I or my friend know what happened"
        
           The more generous code for "nor" has been used instead
        */
        /*
            else if (s.equals("or")) {
            for (d1 = d_list; d1!=null; d1=d1.next) {
                for (c1=d1.right; c1!=null; c1=c1.next) {
                if (c1.string[0] == 'S') {
                    if (c1.string[1]=='^') {
                    if (c1.string[2]=='a') {
                        c1.string = "Ss"; 
                    } else {
                        c1.string = "Sp";
                    }
                    } else if ((c1.string[1]=='p') && (c1.string[2]=='a')){
                    c1.string = "Sp";
                    }
                }
                }
                for (c1=d1.left; c1!=null; c1=c1.next) {
                if ((c1.string[0] == 'S') && (c1.string[1] == 'I')) {
                    if (c1.string[2]=='^') {
                    if (c1.string[3]=='a') {
                        c1.string = "Ss"; 
                    } else {
                        c1.string = "Sp";
                    }
                    } else if ((c1.string[2]=='p') && (c1.string[3]=='a')){
                    c1.string = "Sp";
                    }
                }
                }
            }
            }
        */
        /*
            It appears that the "nor" of two things can be either singular or
            plural.  "neither she nor John likes dogs"
                     "neither she nor John like dogs"
        
        */
        else if ((s.equals("nor")) || (s.equals("or"))) {
            for (d1 = d_list; d1 != null; d1 = d1.next) {
                for (c1 = d1.right; c1 != null; c1 = c1.next) {
                    if (c1.string.length() >= 2
                        && (c1.string.charAt(0) == 'S')
                        && ((c1.string.charAt(1) == '^')
                            || (c1.string.charAt(1) == 's')
                            || (c1.string.charAt(1) == 'p'))) {
                        c1.string = "S";
                    }
                }
                for (c1 = d1.left; c1 != null; c1 = c1.next) {
                    if (c1.string.length() >= 3
                        && (c1.string.charAt(0) == 'S')
                        && (c1.string.charAt(1) == 'I')
                        && ((c1.string.charAt(2) == '^')
                            || (c1.string.charAt(2) == 's')
                            || (c1.string.charAt(2) == 'p'))) {
                        c1.string = "SI";
                    }
                }
            }
        }

        //     endif    PLURALIZATION
        return d_list;
    }

    public void build_conjunction_tables() {
        /* Goes through the entire sentence and builds the fat link tables
           for all the disjuncts of all the words.
        */
        int w;
        int k;
        Disjunct d;

        init_HT();
        init_LT();
        GlobalBean.STAT_N_disjuncts = GlobalBean.STAT_calls_to_equality_test = 0;

        for (w = 0; w < length; w++) {
            for (d = word[w].d; d != null; d = d.next) {
                extract_all_fat_links(d);
            }
        }

        for (k = 0; k < and_data.LT_size; k++) {
            compute_matchers_for_a_label(k);
        }
    }

    /**
     * 
     * @param k 
     */
    public void compute_matchers_for_a_label(int k) {
        /* This takes a label k, modifies the list of disjuncts with that
           label.  For each such disjunct, it computes the string that
           will be used in the fat connector that represents it.
        
           The only hard part is finding the length of each of the strings
           so that "*" can be put in.  A better explanation will have to wait.
        */

        int lengths[];
        int N_connectors, i, j, tot_len;
        Connector c;
        Disjunct d;
        StringBuffer os;
        String s;

        d = and_data.label_table[k];

        N_connectors = 0;
        for (c = d.left; c != null; c = c.next)
            N_connectors++;
        for (c = d.right; c != null; c = c.next)
            N_connectors++;

        lengths = new int[N_connectors];
        for (i = 0; i < N_connectors; i++)
            lengths[i] = 0;
        while (d != null) {
            i = 0;
            for (c = d.left; c != null; c = c.next) {
                s = c.string;
                j = 0;
                while (j < s.length() && Character.isUpperCase(s.charAt(j)))
                    j++;
                j = s.length() - j;
                if (j > lengths[i])
                    lengths[i] = j;
                i++;
            }
            for (c = d.right; c != null; c = c.next) {
                s = c.string;
                j = 0;
                while (j < s.length() && Character.isUpperCase(s.charAt(j)))
                    j++;
                j = s.length() - j;
                if (j > lengths[i])
                    lengths[i] = j;
                i++;
            }
            d = d.next;
        }

        tot_len = 0;
        for (i = 0; i < N_connectors; i++)
            tot_len += lengths[i] + 1;
        /* +1 is for the multi-match character */
        for (d = and_data.label_table[k]; d != null; d = d.next) {
            i = 0;
            os = new StringBuffer(tot_len);
            for (c = d.left; c != null; c = c.next) {
                stick_in_one_connector(os, c, lengths[i]);
                i++;
            }
            for (c = d.right; c != null; c = c.next) {
                stick_in_one_connector(os, c, lengths[i]);
                i++;
            }
            d.string = os.toString();
        }
    }

    /**
     * 
     * @param s 
     * @param c 
     * @param len 
     */
    public void stick_in_one_connector(StringBuffer s, Connector c, int len) {
        /* put the next len characters from c.string (skipping upper
           case ones) into s.  If there are fewer than this, pad with '*'s.
           Then put in a character for the multi match bit of c.
           Then put in a '\0', and return a pointer to this place.
        */
        int i = 0;
        while (i < c.string.length() && Character.isUpperCase(c.string.charAt(i))) {
            i++;
        }
        while (i < c.string.length()) {
            s.append(c.string.charAt(i));
            i++;
            len--;
        }
        while (len > 0) {
            s.append('*');
            len--;
        }
        if (c.multi)
            s.append('*');
        else
            s.append('^');
        /* check this sometime */
    }

    /**
     * 
     * @param d 
     */
    public void extract_all_fat_links(Disjunct d) {
        /*  A sub disjuct of d is any disjunct obtained by killing the tail
            of either connector list at any point.
            Here we go through each sub-disjunct of d, and put it into our
            table data structure.
                                                                     
            The function has no side effects on d.
        */
        Connector cl, cr, tl, tr;
        tl = d.left;
        d.left = null;
        for (cr = d.right; cr != null; cr = cr.next) {
            tr = cr.next;
            cr.next = null;
            if (is_appropriate(d))
                put_disjunct_into_table(d);
            cr.next = tr;
        }
        d.left = tl;
        tr = d.right;
        d.right = null;
        for (cl = d.left; cl != null; cl = cl.next) {
            tl = cl.next;
            cl.next = null;
            if (is_appropriate(d))
                put_disjunct_into_table(d);
            cl.next = tl;
        }
        d.right = tr;
        for (cl = d.left; cl != null; cl = cl.next) {
            for (cr = d.right; cr != null; cr = cr.next) {
                tl = cl.next;
                tr = cr.next;
                cl.next = cr.next = null;
                if (is_appropriate(d))
                    put_disjunct_into_table(d);
                cl.next = tl;
                cr.next = tr;
            }
        }
    }

    /**
     * 
     * @param d 
     */
    public void put_disjunct_into_table(Disjunct d) {
        /* (1) look for the given disjunct in the table structures
               if it's already in the table structures, do nothing
           (2) otherwise make a copy of it, and put it into the table structures
           (3) also put all of the GCDs of this disjunct with all of the
               other matching disjuncts into the table.
              
           The costs are set to zero.
           Note that this has no effect on disjunct d.
        */
        Disjunct d1 = null, d2, di, d_copy;
        LabelNode lp;
        int h, k;
        h = d.and_hash_disjunct();
        for (lp = and_data.hash_table[h]; lp != null; lp = lp.next) {
            d1 = and_data.label_table[lp.label];
            if (d.disjunct_types_equal(d1))
                break;
        }
        if (lp != null) { /* there is already a label for disjuncts of this type */
            /* d1 points to the list of disjuncts of this type already there */
            while (d1 != null) {
                if (d1.disjuncts_equal_AND(d))
                    return;
                d1 = d1.next;
            }
            /* now we must put the d disjunct in there, and all of the GCDs of
               it with the ones already there.
            
               This is done as follows.  We scan through the list of disjuncts
               computing the gcd of the new one with each of the others, putting
               the resulting disjuncts onto another list rooted at d2.
               Now insert d into the the list already there.  Now for each
               one on the d2 list, put it in if it isn't already there.
               
               Here we're making use of the following theorem: Given a
               collection of sets s1, s2 ... sn closed under intersection,
               to if we add a new set s to the collection and also add
               all the intersections between s and s1...sn to the collection,
               then the collection is still closed under intersection.
               
               Use a Venn diagram to prove this theorem.
               
               */
            d_copy = Disjunct.copy_disjunct(d);
            d_copy.cost = 0;
            k = lp.label;
            d2 = null;
            for (d1 = and_data.label_table[k]; d1 != null; d1 = d1.next) {
                di = d_copy.intersect_disjuncts(d1);
                di.next = d2;
                d2 = di;
            }
            d_copy.next = and_data.label_table[k];
            and_data.label_table[k] = d_copy;
            for (; d2 != null; d2 = di) {
                di = d2.next;
                for (d1 = and_data.label_table[k]; d1 != null; d1 = d1.next) {
                    if (d1.disjuncts_equal_AND(d2))
                        break;
                }
                if (d1 == null) {
                    GlobalBean.STAT_N_disjuncts++;
                    d2.next = and_data.label_table[k];
                    and_data.label_table[k] = d2;
                } else {
                    d2.next = null;
                }
            }
        } else { /* create a new label for disjuncts of this type */
            d_copy = Disjunct.copy_disjunct(d);
            d_copy.cost = 0;
            d_copy.next = null;
            if (and_data.LT_size == and_data.LT_bound)
                grow_LT();
            lp = new LabelNode();
            lp.next = and_data.hash_table[h];
            and_data.hash_table[h] = lp;
            lp.label = and_data.LT_size;
            and_data.label_table[and_data.LT_size] = d_copy;
            and_data.LT_size++;
            GlobalBean.STAT_N_disjuncts++;
        }
    }

    void grow_LT() {
        and_data.LT_bound = (3 * and_data.LT_bound) / 2;
        Disjunct old[] = and_data.label_table;
        and_data.label_table = new Disjunct[and_data.LT_bound];
        System.arraycopy(old, 0, and_data.label_table, 0, old.length);
    }

    //  
    /**
     * returns true if the disjunct is appropriate to be made into fat links.
           Check here that the connectors are from some small set.
           This will disallow, for example "the and their dog ran".
     *<p>
     * TODO: move to dict
     *<p>
     * @param d 
     * @return true if the disjunct is appropriate to be made into fat links
     */
    public boolean is_appropriate(Disjunct d) {
        Connector c;
        if (dict.andable_connector_set == null)
            return true; /* if no set, then everything is considered andable */
        for (c = d.right; c != null; c = c.next) {
            if (!c.match_in_connector_set(this, dict.andable_connector_set, '+'))
                return false;
        }
        for (c = d.left; c != null; c = c.next) {
            if (!c.match_in_connector_set(this, dict.andable_connector_set, '-'))
                return false;
        }
        return true;
    }

    public void init_HT() {
        int i;
        for (i = 0; i < GlobalBean.HT_SIZE; i++) {
            and_data.hash_table[i] = null;
        }
    }

    public void init_LT() {
        and_data.LT_bound = 20;
        and_data.LT_size = 0;
        and_data.label_table = new Disjunct[and_data.LT_bound];
    }

    /**
     * 
     * @param opts 
     */
    public void print_AND_statistics(ParseOptions opts) {
        opts.out.println("Number of disjunct types (labels): " + and_data.LT_size);
        opts.out.println("Number of disjuncts in the table: " + GlobalBean.STAT_N_disjuncts);
        if (and_data.LT_size != 0) {
            opts.out.println("average list length: " + ((float)GlobalBean.STAT_N_disjuncts / and_data.LT_size));
        }
        opts.out.println("Number of equality tests: " + GlobalBean.STAT_calls_to_equality_test);
    }

    /**
     * 
     * @param has_conjunction 
     */
    public void build_effective_dist(boolean has_conjunction) {
        /*
           The "effective distance" between two words is the actual distance minus
           the largest deletable region strictly between the two words.  If the
           effective distance between two words is greater than a connector's max
           link length, then that connector cannot be satisfied by linking these
           two words.
        
           [Note: The effective distance is not monotically increasing as you move
           away from a word.]
           
           This function creates effective_dist[][].  It assumes that deleteble[][]
           has already been computed.
           
           Dynamic programming is used to compute this.  The order used is smallest
           region to largest.
           
           Just as deletable[i][j] is constructed for j=N_words (which is one
           off the end of the sentence) we do that for effective_dist[][].
         */

        int i, j, diff;
        effective_dist = new int[length][];
        for (i = 0; i < length; i++) {
            effective_dist[i] = new int[length + 1];
        }
        for (i = 0; i < length; i++) {
            /* Fill in the silly part */
            for (j = 0; j <= i; j++) {
                effective_dist[i][j] = j - i;
            }
        }

        /* what is the rationale for ignoring the effective_dist
           if null links are allowed? */
        if (null_links) {
            for (i = 0; i < length; i++) {
                for (j = 0; j <= length; j++) {
                    effective_dist[i][j] = j - i;
                }
            }
        } else {
            for (diff = 1; diff < length; diff++) {
                for (i = 0; i + diff <= length; i++) {
                    j = i + diff;
                    if (deletable[i + 1][j]) {
                        /* note that deletable[x][x+1] is true */
                        effective_dist[i][j] = 1;
                    } else {
                        effective_dist[i][j] = 1 + Math.min(effective_dist[i][j - 1], effective_dist[i + 1][j]);
                    }
                }
            }

            /* now when you link to a conjunction, your effective length is 1 */
            for (i = 0; i < length; i++) {
                for (j = i + 1; j < length; j++) {
                    if (is_conjunction[i] || is_conjunction[j])
                        effective_dist[i][j] = 1;
                }
            }
        } /* effective_dist[i][i] should be 0 */
        /*for (j=0; j<=length; j++) {
          printf("%4d", j);
          }
          printf("\n");
          for (i=0; i<length; i++) {  
          for (j=0; j<=length; j++) {
          printf("%4d", effective_dist[i][j]);
          }
          printf("\n");
          }
          */
    }

    /**
     * 
     * Initialize the array deletable[i][j] to indicate if the words          
     * i+1...j-1 could be non existant in one of the multiple linkages.  This 
     * array is used in conjunction_prune and power_prune.  Regions of length 
     * 0 are always deletable.  A region of length two with a conjunction at   
     * one end is always deletable.  Another observation is that for the       
     * comma to form the right end of a deletable region, it must be the case  
     * that there is a conjunction to the right of the comma.  Also, when      
     * considering deletable regions with a comma on their left sides, there   
     * must be a conjunction inside the region to be deleted. Finally, the     
     * words "either", "neither", "both", "not" and "not only" are all         
     * deletable.
     *<p>
     * TODO - This is awfully ethnocentric. What about other languages, or words like
     * thus, thence, whence etc.  This should be a loadable array!                                                              
     * @param has_conjunction 
     */
    public void build_deletable(boolean has_conjunction) {
        

        int i, j, k;
        if (length >= GlobalBean.MAX_SENTENCE) {
            throw new RuntimeException("sent.length too big");
        }

        deletable = new boolean[length + 1][];
        for (i = -1; i < length; i++) {
            deletable[i + 1] = new boolean[length + 1];
            /* the +1 is to allow us to have the info for the last word
               read the comment above */
            for (j = 0; j <= length; j++) {
                if (j == i + 1) {
                    deletable[i + 1][j] = true;
                } else if (null_links) {
                    deletable[i + 1][j] = true;
                } else if (!has_conjunction) {
                    deletable[i + 1][j] = false;
                } else if (
                    j > i + 2
                        && (is_conjunction[i + 1]
                            || is_conjunction[j - 1]
                            || (word[i + 1].string.equals(",") && conj_in_range(i + 2, j - 1))
                            || (word[j - 1].string.equals(",") && conj_in_range(j, length - 1)))) {
                    deletable[i + 1][j] = true;
                } else if (j > i) {
                    for (k = i + 1; k < j; k++) {
                        if ("either".equals(word[k].string)
                            || "neither".equals(word[k].string)
                            || "both".equals(word[k].string)
                            || "not".equals(word[k].string))
                            continue;
                        if (("only".equals(word[k].string)) && (k > i + 1) && ("not".equals(word[k - 1].string)))
                            continue;
                        break;
                    }
                    deletable[i + 1][j] = (k == j);
                } else {
                    deletable[i + 1][j] = false;
                }
            }
        }
    }

    /**
     * Determin if there is a conjunction between the suppled right and
     * left words. 
     * @param lw integer index of left word
     * @param rw integer index of right word
     * @return true if the range lw...rw inclusive contains a conjunction
     */
    public boolean conj_in_range(
        int lw,
        int rw) { 
        for (; lw <= rw; lw++) {
            if (is_conjunction[lw])
                return true;
        }
        return false;
    }

    private void set_connector_length_limits(ParseOptions opts) {
        int i;
        int len;
        Disjunct d;
        len = opts.short_length;
        if (len > GlobalBean.UNLIMITED_LEN)
            len = GlobalBean.UNLIMITED_LEN;
        for (i = 0; i < length; i++) {
            for (d = word[i].d; d != null; d = d.next) {
                set_connector_list_length_limit(d.left, dict.unlimited_connector_set, len, opts);
                set_connector_list_length_limit(d.right, dict.unlimited_connector_set, len, opts);
            }
        }
    }

    private void set_connector_list_length_limit(Connector c, ConnectorSet conset, int short_len, ParseOptions opts) {
        for (; c != null; c = c.next) {
            if (opts.parse_options_get_all_short_connectors()) {
                c.length_limit = short_len;
            } else if (conset == null || c.match_in_connector_set(this, conset, '+')) {
                c.length_limit = GlobalBean.UNLIMITED_LEN;
            } else {
                c.length_limit = short_len;
            }
        }
    }

    /**
     * Step three in parsing a sentence. First you must create a dictionary, then a sentence, then call this
     * method to generate a parse tree.  This is really the heart of the system.  There are several things 
     * done when a sentence is parsed.
     *<ul>
     *<li>  Word expressions are extracted from the dictionary and pruned
     *<li> Disjuncts are built
     *<li>  Aseries of pruning operations are carried out.
     *<li>  The linkages having the minimum number of null links are counted.
     *<li>  A "parse set" of linkages is built.
     *<li>  The linkages are post processed
     *</ul>
     *The "parse set" is attached to the sentence, and this is one of the key reasons that
     *the API is flexible and modular.  All of the necessary information for building linkages
     *is stored in the parse set.  This means that other sentences can be parsed, possibly using 
     *different dictionaries, without disturbing the information obtained from a call to sentence_parse.
     *If another call to sentence_parse is made on the same sentence, the parsing information
     *for the previous call is deleted.
     *<p>
     *O.K. that may be true of the C code version but in this code a lot of information
     *from ParseOptions is held in GlobalBean. 
     *<p>
     * TODO - Make the dictionary and ParseInfo private 
     *to the sentence.  Then add getter and setter methods.
     *
     * @param opts 
     * @return the number of valid linkages.
     * @see #num_linkages_found
     */
    public int sentence_parse(ParseOptions opts) {
        int nl;


        
        expression_prune(opts);
        opts.print_time("Finished expression pruning");
        prepare_to_parse(opts);
        init_fast_matcher();
        init_table();
        opts.print_time("Initialized fast matcher and hash table");

        /* A parse set may have been already been built for this sentence,
           if it was previously parsed.  If so we free it up before building another.  */
        free_parse_set();
        init_x_table();
        for (nl = opts.min_null_count; nl <= opts.max_null_count; ++nl) {
            null_count = nl;
            num_linkages_found = parse(null_count, opts);
            opts.print_time("Counted parses");
            post_process_linkages(opts);
            if (num_valid_linkages > 0)
                break;
        }
        if (opts.verbosity > 1) {
            opts.out.println("" + match_cost + " Match cost");
        }
        opts.print_time("Finished parse");
        return num_valid_linkages;
    }

    /**
     * Returns the number of null links the sentence can be parsed with the
           specified cost Assumes that the hash table this.ctable has already been
           initialized, and is freed later.
     * @param cost 
     * @param opts 
     * @return the number of null links in this.ctable
     */
    public int parse(int cost, ParseOptions opts) {

        int total;

        local_sent = word;

        total = count(-1, length, null, null, cost + 1, opts);
        if (opts.verbosity > 1) {
            opts.out.println("Total count with " + cost + " null links:   " + total);
        }
        if (total < 0) {
            opts.out.println("WARNING: Overflow in count!");
        }

        local_sent = null;
        return total;
    }

    /**
     * 
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost 
     * @param opts 
     * @return the total number of links in a TableConnector this.ctable matching input parameters
     */
    public int count(int lw, int rw, Connector le, Connector re, int cost, ParseOptions opts) {
        Disjunct d;
        int total, pseudototal;
        int start_word, end_word, w;
        int leftcount, rightcount;
        int lcost, rcost;
        boolean Lmatch, Rmatch;

        MatchNode m, m1;
        TableConnector t;

        if (cost < 0)
            return 0; /* will we ever call it with cost<0 ? */

        t = table_pointer(lw, rw, le, re, cost);

        if (t == null) {
            /* create the table entry with a tentative cost of 0 */
            /* this cost must be updated before we return */
            t = table_store(lw, rw, le, re, cost, 0);
        } else {
            return t.count;
        }

        if (rw == 1 + lw) {
            /* lw and rw are neighboring words */
            /* you can't have a linkage here with cost > 0 */
            if ((le == null) && (re == null) && (cost == 0)) {
                t.count = 1;
            } else {
                t.count = 0;
            }
            return t.count;
        }

        if ((le == null) && (re == null)) {
            if (!opts.islands_ok && (lw != -1)) {
                /* if we don't allow islands (a set of words linked together but
                   separate from the rest of the sentence) then  the cost of skipping
                   n words is just n */
                if (cost == ((rw - lw - 1) + opts.null_block - 1) / opts.null_block) {
                    /* if null_block=4 then the cost of 
                       1,2,3,4 nulls is 1, 5,6,7,8 is 2 etc. */
                    t.count = 1;
                } else {
                    t.count = 0;
                }
                return t.count;
            }
            if (cost == 0) {
                /* there is no zero-cost solution in this case */
                /* slight efficiency hack to separate this cost=0 case out */
                /* but not necessary for correctness */
                t.count = 0;
            } else {
                total = 0;
                w = lw + 1;
                for (d = local_sent[w].d; d != null; d = d.next) {
                    if (d.left == null) {
                        total += count(w, rw, d.right, null, cost - 1, opts);
                    }
                }
                total += count(w, rw, null, null, cost - 1, opts);
                t.count = total;
            }
            return t.count;
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

        total = 0;

        for (w = start_word; w <= end_word; w++) {
            m1 = m = form_match_list(w, le, lw, re, rw);
            for (; m != null; m = m.next) {
                d = m.d;
                for (lcost = 0; lcost <= cost; lcost++) {
                    rcost = cost - lcost;
                    /* now lcost and rcost are the costs we're assigning to those parts respectively */

                    /* Now, we determine if (based on table only) we can see that
                       the current range is not parsable. */

                    Lmatch = (le != null) && (d.left != null) && Connector.match(this, le, d.left, lw, w);
                    Rmatch = (d.right != null) && (re != null) && Connector.match(this, d.right, re, w, rw);

                    rightcount = leftcount = 0;
                    if (Lmatch) {
                        leftcount = pseudocount(lw, w, le.next, d.left.next, lcost);
                        if (le.multi)
                            leftcount += pseudocount(lw, w, le, d.left.next, lcost);
                        if (d.left.multi)
                            leftcount += pseudocount(lw, w, le.next, d.left, lcost);
                        if (le.multi && d.left.multi)
                            leftcount += pseudocount(lw, w, le, d.left, lcost);
                    }

                    if (Rmatch) {
                        rightcount = pseudocount(w, rw, d.right.next, re.next, rcost);
                        if (d.right.multi)
                            rightcount += pseudocount(w, rw, d.right, re.next, rcost);
                        if (re.multi)
                            rightcount += pseudocount(w, rw, d.right.next, re, rcost);
                        if (d.right.multi && re.multi)
                            rightcount += pseudocount(w, rw, d.right, re, rcost);
                    }

                    pseudototal = leftcount * rightcount; /* total number where links are used on both sides */

                    if (leftcount > 0) {
                        /* evaluate using the left match, but not the right */
                        pseudototal += leftcount * pseudocount(w, rw, d.right, re, rcost);
                    }
                    if ((le == null) && (rightcount > 0)) {
                        /* evaluate using the right match, but not the left */
                        pseudototal += rightcount * pseudocount(lw, w, le, d.left, lcost);
                    }

                    /* now pseudototal is 0 implies that we know that the true total is 0 */
                    if (pseudototal != 0) {
                        rightcount = leftcount = 0;
                        if (Lmatch) {
                            leftcount = count(lw, w, le.next, d.left.next, lcost, opts);
                            if (le.multi)
                                leftcount += count(lw, w, le, d.left.next, lcost, opts);
                            if (d.left.multi)
                                leftcount += count(lw, w, le.next, d.left, lcost, opts);
                            if (le.multi && d.left.multi)
                                leftcount += count(lw, w, le, d.left, lcost, opts);
                        }

                        if (Rmatch) {
                            rightcount = count(w, rw, d.right.next, re.next, rcost, opts);
                            if (d.right.multi)
                                rightcount += count(w, rw, d.right, re.next, rcost, opts);
                            if (re.multi)
                                rightcount += count(w, rw, d.right.next, re, rcost, opts);
                            if (d.right.multi && re.multi)
                                rightcount += count(w, rw, d.right, re, rcost, opts);
                        }

                        total += leftcount * rightcount; /* total number where links are used on both sides */

                        if (leftcount > 0) {
                            /* evaluate using the left match, but not the right */
                            total += leftcount * count(w, rw, d.right, re, rcost, opts);
                        }
                        if ((le == null) && (rightcount > 0)) {
                            /* evaluate using the right match, but not the left */
                            total += rightcount * count(lw, w, le, d.left, lcost, opts);
                        }
                    }
                }
            }
        }
        t.count = total;
        return total;
    }

    /**
     * 
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost 
     * @return 0 if and only if this entry is in the hash table with a count value of 0
     */
    public int pseudocount(int lw, int rw, Connector le, Connector re, int cost) {
        int count;
        count = table_lookup(lw, rw, le, re, cost);
        if (count == 0)
            return 0;
        else
            return 1;
    }

    /**
     * A piecewise exponential function determines the size of the hash table.      
     * Probably should make use of the actual number of disjuncts, rather than just
     * the number of words 
     */
    public void init_x_table() {
        
        int i, x_table_size;
        ParseInfo pi;

        if (!(parse_info == null)) {
            throw new RuntimeException("ParseInfo is not null");
        }

        pi = parse_info = new ParseInfo();
        pi.N_words = length;

        if (pi.N_words >= 10) {
            x_table_size = (1 << 14);
        } else if (pi.N_words >= 4) {
            x_table_size = (1 << (pi.N_words));
        } else {
            x_table_size = (1 << 4);
        }

        /*printf("Allocating x_table of size %d\n", x_table_size);*/
        pi.x_table_size = x_table_size;
        pi.x_table = new XTableConnector[x_table_size];
        for (i = 0; i < x_table_size; i++) {
            pi.x_table[i] = null;
        }
    }
    /**
     * The size of this.ctable
     * TODO - make this Java, not C, and use the collection object stuff!
     * sp that ctable size can not be modified independently of ctable.
     */
    public static int ctable_size;
    /**
     * The TableConnector table associated with this sentence instance object
     */
    public static TableConnector ctable[];
    /**
     * A piecewise exponential function determines the size of the hash table.      
     * Probably should make use of the actual number of disjuncts, rather than just
     * the number of words 
     */
    public void init_table() {
        /* A piecewise exponential function determines the size of the hash table.      */
        /* Probably should make use of the actual number of disjuncts, rather than just */
        /* the number of words                                                          */
        int i;
        if (length >= 10) {
            ctable_size = (1 << 16);
            /*  } else if (sent.length >= 10) {
                table_size = (1 << (((6*(sent.length-10))/30) + 10)); */
        } else if (length >= 4) {
            ctable_size = (1 << (((6 * (length - 4)) / 6) + 4));
        } else {
            ctable_size = (1 << 4);
        }
        ctable = new TableConnector[ctable_size];
        for (i = 0; i < ctable_size; i++) {
            ctable[i] = null;
        }
    }

    /**
     * 
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost 
     * @see TableConnector#lw
     * @see TableConnector#rw
     * @see TableConnector#le
     * @see TableConnector#re
     * @see TableConnector#cost
     * @return the count for this quintuple if there, -1 otherwise
     */
    public static int table_lookup(int lw, int rw, Connector le, Connector re, int cost) {
        /* returns the count for this quintuple if there, -1 otherwise */
        TableConnector t = table_pointer(lw, rw, le, re, cost);

        if (t == null)
            return -1;
        else
            return t.count;
    }

    /**
     * 
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost
     * @see TableConnector#lw
     * @see TableConnector#rw
     * @see TableConnector#le
     * @see TableConnector#re
     * @see TableConnector#cost 
     * @return hash used in this.ctable
     */
    public static int hash(int lw, int rw, Connector le, Connector re, int cost) {
        int i;
        i = 0;

        i = i + (i << 1) + MyRandom.randtable[(lw + i) & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(rw + i) & (GlobalBean.RTSIZE - 1)];
        i =
            i
                + (i << 1)
                + MyRandom.randtable[(((le == null ? 0 : le.hashCode()) + i) % (Sentence.ctable_size + 1))
                    & (GlobalBean.RTSIZE - 1)];
        i =
            i
                + (i << 1)
                + MyRandom.randtable[(((re == null ? 0 : re.hashCode()) + i) % (Sentence.ctable_size + 1))
                    & (GlobalBean.RTSIZE - 1)];
        i = i + (i << 1) + MyRandom.randtable[(cost + i) & (GlobalBean.RTSIZE - 1)];
        return i & (Sentence.ctable_size - 1);
    }

    /**
     * 
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost
     * @see TableConnector#lw
     * @see TableConnector#rw
     * @see TableConnector#le
     * @see TableConnector#re
     * @see TableConnector#cost 
     * @return the pointer to this info, null if not there
     */
    public static TableConnector table_pointer(int lw, int rw, Connector le, Connector re, int cost) {
        /* returns the pointer to this info, null if not there */
        TableConnector t;
        t = Sentence.ctable[hash(lw, rw, le, re, cost)];
        for (; t != null; t = t.next) {
            if ((t.lw == lw) && (t.rw == rw) && (t.le == le) && (t.re == re) && (t.cost == cost))
                return t;
        }

        return null;
    }

    /**
     * Stores the value in the table this.ctable.  Assumes it's not already there
     * @param lw 
     * @param rw 
     * @param le 
     * @param re 
     * @param cost 
     * @param count 
     * @return a new TableConnector
     * @see TableConnector#lw
     * @see TableConnector#rw
     * @see TableConnector#le
     * @see TableConnector#re
     * @see TableConnector#cost
     * @see TableConnector#next
     * @see #init_table()
     *
     *
     */
    public static TableConnector table_store(int lw, int rw, Connector le, Connector re, int cost, int count) {
        /* Stores the value in the table.  Assumes it's not already there */
        TableConnector t, n;
        int h;

        n = new TableConnector();
        n.count = count;
        n.lw = lw;
        n.rw = rw;
        n.le = le;
        n.re = re;
        n.cost = cost;
        h = hash(lw, rw, le, re, cost);
        t = ctable[h];
        n.next = t;
        ctable[h] = n;
        return n;
    }

    public static int match_cost;

    public static int match_l_table_size[] = new int[GlobalBean.MAX_SENTENCE]; /* the sizes of the hash tables */
    public static int match_r_table_size[] = new int[GlobalBean.MAX_SENTENCE];

    /* the beginnings of the hash tables */
    public static MatchNode match_l_table[][] = new MatchNode[GlobalBean.MAX_SENTENCE][];
    public static MatchNode match_r_table[][] = new MatchNode[GlobalBean.MAX_SENTENCE][];

    public void init_fast_matcher() {
        int w, len, size, i;
        MatchNode t[];
        Disjunct d;
        match_cost = 0;
        for (w = 0; w < length; w++) {
            len = left_disjunct_list_length(word[w].d);
            size = MyRandom.next_power_of_two_up(len);
            match_l_table_size[w] = size;
            t = match_l_table[w] = new MatchNode[size];
            for (i = 0; i < size; i++)
                t[i] = null;

            for (d = word[w].d; d != null; d = d.next) {
                if (d.left != null) {
                    put_into_match_table(size, t, d, d.left, -1);
                }
            }

            len = right_disjunct_list_length(word[w].d);
            size = MyRandom.next_power_of_two_up(len);
            match_r_table_size[w] = size;
            t = match_r_table[w] = new MatchNode[size];
            for (i = 0; i < size; i++)
                t[i] = null;

            for (d = word[w].d; d != null; d = d.next) {
                if (d.right != null) {
                    put_into_match_table(size, t, d, d.right, 1);
                }
            }
        }
    }

    /**
     * 
     * @param d 
     * @return the number of disjuncts in the list that have non-null
           left connector lists
     */
    public static int left_disjunct_list_length(Disjunct d) {
        /* returns the number of disjuncts in the list that have non-null
           left connector lists */
        int i;
        for (i = 0; d != null; d = d.next) {
            if (d.left != null)
                i++;
        }
        return i;
    }

    /**
     * the number of disjuncts in the list that have non-null
           right connector lists
     * @param d 
     * @return the number of disjuncts in the list that have non-null
           right connector lists
     */
    public static int right_disjunct_list_length(Disjunct d) {
        int i;
        for (i = 0; d != null; d = d.next) {
            if (d.right != null)
                i++;
        }
        return i;
    }

    /**
     * The disjunct d (whose left or right pointer points to c) is put
           into the appropriate hash table
     *<p>
           dir =  1, we're putting this into a right table.
     *<p>
           dir = -1, we're putting this into a left table.
     *
     * @param size 
     * @param t 
     * @param d 
     * @param c 
     * @param dir 
     */
    public static void put_into_match_table(int size, MatchNode t[], Disjunct d, Connector c, int dir) {
        /* The disjunct d (whose left or right pointer points to c) is put
           into the appropriate hash table
           dir =  1, we're putting this into a right table.
           dir = -1, we're putting this into a left table.
        */
        int h;
        MatchNode m;
        h = fast_match_hash(c) & (size - 1);
        m = new MatchNode();
        m.next = null;
        m.d = d;
        if (dir == 1) {
            t[h] = add_to_right_table_list(m, t[h]);
        } else {
            t[h] = add_to_left_table_list(m, t[h]);
        }
    }

    /**
     * This hash function only looks at the leading upper case letters of
           the connector string, and the label fields.  This ensures that if two
           strings match (formally), then they must hash to the same place.
           The answer must be masked to the appropriate table size.
     * @param c 
     * @return the index into the hash table
     */
    public static int fast_match_hash(Connector c) {
        /* This hash function only looks at the leading upper case letters of
           the connector string, and the label fields.  This ensures that if two
           strings match (formally), then they must hash to the same place.
           The answer must be masked to the appropriate table size.
        */
        String s;
        int i;
        i = MyRandom.randtable[c.label & (GlobalBean.RTSIZE - 1)];
        s = c.string;
        int j = 0;
        while (j < s.length() && Character.isUpperCase(s.charAt(j))) {
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
            j++;
        }
        return i;
    }

    /**
     * Adds the match node m to the sorted list of match nodes l.
           The parameter dir determines the order of the sorting to be used.
           Makes the list sorted from smallest to largest.
     * @param m the node to add
     * @param l the node to which we are to add m on the right
     * @return The matched node's right index
     */
    public static MatchNode add_to_right_table_list(MatchNode m, MatchNode l) {
        if (l == null)
            return m;
        if ((m.d.right.word) <= (l.d.right.word)) {
            m.next = l;
            return m;
        } else {
            l.next = add_to_right_table_list(m, l.next);
            return l;
        }
    }

    /**
     * Adds the match node m to the sorted list of match nodes l.
           The parameter dir determines the order of the sorting to be used.
           Makes the list sorted from largest to smallest
     * @param m the node to add
     * @param l the node to which we are to add m on the right
     * @return The matched node's left index
     */
    public static MatchNode add_to_left_table_list(MatchNode m, MatchNode l) {
        if (l == null)
            return m;
        if ((m.d.left.word) >= (l.d.left.word)) {
            m.next = l;
            return m;
        } else {
            l.next = add_to_left_table_list(m, l.next);
            return l;
        }
    }

    /**
     * This is the top level call that computes the whole parse_set.  It
     *      points whole_set at the result.  It creates the necessary hash
     *      table (x_table) which will be freed at the same time the
     *      whole_set is freed.
     * <p>                                                      
     *      It also assumes that count() has been run, and that hash table is
     *      filled with the values thus computed.  Therefore this function
     *      must be structured just like parse() (the main function for
     *      count()).  
     * <p>      
     *      If the number of linkages gets huge, then the counts can overflow.
     *      We check if this has happened when verifying the parse set.
     *      This routine returns true iff overflowed occurred.
     * <p>
     * This method modifies this.loca-sent, this.parse_info
     * @param cost 
     * @param opts 
     * @return the result of ParseInfo.verify_set
     * @see ParseInfo#verify_set()
     * @see Word
     * @see #local_sent
     * @see #parse_info
     */
    public boolean build_parse_set(int cost, ParseOptions opts) {
        ParseSet whole_set;
        local_sent = word;
        whole_set = parse_info.parse_set(this, null, null, -1, length, null, null, cost + 1, opts);

        if ((whole_set != null) && (whole_set.current != null)) {
            whole_set.current = whole_set.first;
        }

        parse_info.parse_set = whole_set;
        local_sent = null;
        return parse_info.verify_set();
    }

    /**
     * We've already built the sentence expressions.  This turns them into
           disjuncts.  The method modifies this.word by adding the disjuncts
     *     to word[index].d
     * @param opts - refers to this.cost_cutoff that is set from ParseInfo pi
     *                      at object creation. TODO - Fix where ParseInfo is kept. 
     * @param cost_cutoff
     * @see #word
     * @see Word#d
     */
    public void build_sentence_disjuncts(ParseOptions opts, int cost_cutoff) {

        /* We've already built the sentence expressions.  This turns them into
           disjuncts.  
        */

        Disjunct d;
        XNode x;
        int w;
        for (w = 0; w < length; w++) {
            d = null;
            for (x = word[w].x; x != null; x = x.next) {
                Disjunct dd = build_disjuncts_for_XNode(opts, x, cost_cutoff);
                d = Disjunct.catenate_disjuncts(dd, d);
            }
            word[w].d = d;
        }
    }

    /**
     * 
     * @param opts unused - refers to this.cost_cutoff that is set from ParseInfo pi
     *                      at object creation. TODO - Fix where ParseInfo is kept.
     * @param x is the Word  expression list node
     * @param cost_cutoff 
     * @return a linked list of dijuncts for the named node
     * @see Word#x
     */
    public Disjunct build_disjuncts_for_XNode(ParseOptions opts, XNode x, int cost_cutoff) {
        Clause c;
        Disjunct dis;
        c = x.exp.build_Clause(cost_cutoff);
        dis = Clause.build_disjunct(c, x.string, cost_cutoff);
        return dis;
    }

    /**
     * We've already built the sentence expressions.  This turns them into
           disjuncts.  Assumes
           is_conjunction[] has been initialized.
     * @return true if there are conjunctions
     */
    public boolean sentence_contains_conjunction() {

        /* Return We've already built the sentence expressions.  This turns them into
           disjuncts.  Assumes
           is_conjunction[] has been initialized.
        */

        int w;
        for (w = 0; w < length; w++) {
            if (is_conjunction[w])
                return true;
        }
        return false;
    }

    /**
     * 
     * @param opts 
     */
    public void print_disjunct_counts(ParseOptions opts) {
        int i;
        int c;
        Disjunct d;
        for (i = 0; i < length; i++) {
            c = 0;
            for (d = word[i].d; d != null; d = d.next) {
                c++;
            }
            opts.out.print(word[i].string + "(" + c + ") ");
        }
        opts.out.println();
        opts.out.println();
    }

    /**
     * This is another top level call.  It is called by default when you create
     * a new Linkage object.  If you want another postprocessor this is the method
     * to call.
     * @param opts
     * @see Linkage#Linkage(int, Sentence, ParseOptions) 
     */
    public void post_process_linkages(ParseOptions opts) {

        int indices[];
        int in, block_bottom, block_top;
        int N_linkages_found, N_linkages_alloced;
        int N_linkages_post_processed, N_valid_linkages;
        boolean overflowed, only_canonical_allowed;
        double denom;
        boolean canonical;
        link_info = null;
        overflowed = build_parse_set(null_count, opts);
        opts.print_time("Built parse set");
        if (overflowed) {
            /* We know that sent.num_linkages_found is bogus, possibly negative */
            num_linkages_found = opts.linkage_limit;
            if (opts.verbosity > 1)
                opts.out.println(
                    "Warning: Count overflow.\n"
                        + "Considering a random subset of "
                        + opts.linkage_limit
                        + " of an unknown and large number of linkages");
        }
        N_linkages_found = num_linkages_found;
        if (num_linkages_found == 0) {
            num_linkages_alloced = 0;
            num_linkages_post_processed = 0;
            num_valid_linkages = 0;
            link_info = null;
            return;
        }

        if (N_linkages_found > opts.linkage_limit) {
            N_linkages_alloced = opts.linkage_limit;
            if (opts.verbosity > 1)
                opts.out.println(
                    "Warning: Considering a random subset of "
                        + N_linkages_alloced
                        + " of "
                        + N_linkages_found
                        + " linkages");
        } else
            N_linkages_alloced = N_linkages_found;
        link_info = new LinkageInfo[N_linkages_alloced];
        N_linkages_post_processed = N_valid_linkages = 0;
        /* generate an array of linkage indices to examine */
        indices = new int[N_linkages_alloced];
        if (overflowed) {
            for (in = 0; in < N_linkages_alloced; in++) {
                indices[in] = - (in + 1);
            }
        } else {
            MyRandom.my_random_initialize(N_linkages_found + length);
            for (in = 0; in < N_linkages_alloced; in++) {
                denom = (double)N_linkages_alloced;
                block_bottom = (int) (((double)in * (double)N_linkages_found) / denom);
                block_top = (int) (((double) (in + 1) * (double)N_linkages_found) / denom);
                indices[in] = block_bottom + (MyRandom.my_random() % (block_top - block_bottom));
            }
            MyRandom.my_random_finalize();
        }

        only_canonical_allowed = (!(overflowed || (N_linkages_found > 2 * opts.linkage_limit)));
        /* When we're processing only a small subset of the linkages, don't worry
           about restricting the set we consider to be canonical ones.  In the extreme
           case where we are only generating 1 in a million linkages, it's very unlikely
           that we'll hit two symmetric variants of the same linkage anyway. */

        /* (optional) first pass: just visit the linkages */

        /* The purpose of these two passes is to make the post-processing more
           efficient.  Because (hopefully) by the time you do the real work
           in the 2nd pass you've pruned the relevant rule set in the first pass. */
        if (length >= opts.twopass_length) {
            for (in = 0; in < N_linkages_alloced; in++) {
                Linkage.extract_links(indices[in], null_count, parse_info);
                if (set_has_fat_down()) {
                    if (only_canonical_allowed && !is_canonical_linkage())
                        continue;
                    analyze_fat_linkage(opts, GlobalBean.PP_FIRST_PASS);
                } else {
                    analyze_thin_linkage(opts, GlobalBean.PP_FIRST_PASS);
                }
            }
        } /* second pass: actually perform post-processing */
        for (in = 0; in < N_linkages_alloced; in++) {
            Linkage.extract_links(indices[in], null_count, parse_info);
            if (set_has_fat_down()) {
                canonical = is_canonical_linkage();
                if (only_canonical_allowed && !canonical)
                    continue;
                link_info[N_linkages_post_processed] = analyze_fat_linkage(opts, GlobalBean.PP_SECOND_PASS);
                link_info[N_linkages_post_processed].fat = true;
                link_info[N_linkages_post_processed].canonical = canonical;
            } else {
                link_info[N_linkages_post_processed] = analyze_thin_linkage(opts, GlobalBean.PP_SECOND_PASS);
                link_info[N_linkages_post_processed].fat = false;
                link_info[N_linkages_post_processed].canonical = true;
            }
            if (link_info[N_linkages_post_processed].N_violations == 0)
                N_valid_linkages++;
            link_info[N_linkages_post_processed].index = indices[in];
            N_linkages_post_processed++;
        }

        opts.print_time("Postprocessed all linkages");
        Arrays.sort(this.link_info, 0, N_linkages_post_processed, opts.cost_model);
        if ((N_linkages_post_processed == 0) && (N_linkages_found > 0) && (N_linkages_found < opts.linkage_limit)) {
            throw new RuntimeException("None of the linkages is canonical");
        }

        if (opts.verbosity > 1) {
            opts.out.println(
                "" + N_valid_linkages + " of " + N_linkages_post_processed + " linkages with no P.P. violations");
        }

        opts.print_time("Sorted all linkages");
        num_linkages_alloced = N_linkages_alloced;
        num_linkages_post_processed = N_linkages_post_processed;
        num_valid_linkages = N_valid_linkages;
    }

    public static boolean structure_violation;
    /* The following three functions are all for computing the cost of and lists */
    public static boolean visited[] = new boolean[GlobalBean.MAX_SENTENCE];
    public static int and_element_sizes[] = new int[GlobalBean.MAX_SENTENCE];
    public static int and_element[] = new int[GlobalBean.MAX_SENTENCE];
    public static int N_and_elements;
    public static int outside_word[] = new int[GlobalBean.MAX_SENTENCE];
    public static int N_outside_words;

    /**
     * Patches up appropriate links in the patch_array for this DISNode
     * and this patch list.
     * @param dn 
     * @param ltp 
     */
    public void fill_patch_array_DIS(DISNode dn, LinksToPatch ltp) {
        /* Patches up appropriate links in the patch_array for this DISNode     */
        /* and this patch list.                                                  */
        CONList cl;
        ListOfLinks lol;
        LinksToPatch ltpx;
        for (lol = dn.lol; lol != null; lol = lol.next) {
            patch_array[lol.link].used = true;
        }
        if ((dn.cl == null) || (dn.cl.cn.word != dn.word)) {
            for (; ltp != null; ltp = ltpx) {
                ltpx = ltp.next;
                patch_array[ltp.link].changed = true;
                if (ltp.dir == 'l') {
                    patch_array[ltp.link].newl = dn.word;
                } else {
                    patch_array[ltp.link].newr = dn.word;
                }

            }
        }
        /* ltp != null at this point means that dn has child which is a cn
           which is the same word */
        for (cl = dn.cl; cl != null; cl = cl.next) {
            fill_patch_array_CON(cl.cn, ltp);
            ltp = null;
        }
    }

    /**
     * 
     * @param cn 
     * @param ltp 
     */
    public void fill_patch_array_CON(CONNode cn, LinksToPatch ltp) {
        ListOfLinks lol;
        LinksToPatch ltpx;
        for (lol = ParseInfo.word_links[cn.word]; lol != null; lol = lol.next) {
            if (lol.dir == 0) {
                ltpx = new LinksToPatch();
                ltpx.next = ltp;
                ltp = ltpx;
                ltp.newValue = cn.word;
                ltp.link = lol.link;
                if (lol.word > cn.word) {
                    ltp.dir = 'l';
                } else {
                    ltp.dir = 'r';
                }
            }
        }
        fill_patch_array_DIS(cn.current.dn, ltp);
    }

    /**
     * This uses link_array.  It enumerates and post-processes
           all the linkages represented by this one.  We know this contains
           at least one fat link.
     * @param opts 
     * @param analyze_pass 
     * @return a new LinkageInfo object based on this.parse_info
     * @see ParseInfo
     */
    public LinkageInfo analyze_fat_linkage(ParseOptions opts, int analyze_pass) {
        /* This uses link_array.  It enumerates and post-processes
           all the linkages represented by this one.  We know this contains
           at least one fat link. */
        int i;
        LinkageInfo li = new LinkageInfo();
        DISNode d_root;
        PPNode pp;
        Postprocessor postprocessor;
        Sublinkage sublinkage;
        ParseInfo pi = parse_info;
        PPNode accum = new PPNode(); /* for domain ancestry check */
        DTypeList dtl0, dtl1; /* for domain ancestry check */

        sublinkage = new Sublinkage(pi);
        postprocessor = dict.postprocessor;
        pi.build_digraph();
        structure_violation = false;
        d_root = pi.build_DIS_CON_tree(); /* may set structure_violation to true */

        li.N_violations = 0;
        li.improper_fat_linkage = structure_violation;
        li.inconsistent_domains = false;
        li.unused_word_cost = pi.unused_word_cost();
        li.disjunct_cost = pi.disjunct_cost();
        li.null_cost = pi.null_cost();
        li.link_cost = pi.link_cost();

        if (structure_violation) {
            li.N_violations++;
            li.and_cost = 0; /* ? */
            li.andlist = null;
            return li;
        }

        if (analyze_pass == GlobalBean.PP_SECOND_PASS) {
            li.andlist = build_andlist();
            li.and_cost = li.andlist.cost;
        } else
            li.and_cost = 0;

        compute_link_names();

        for (i = 0; i < pi.N_links; i++)
            accum.d_type_array[i] = null;

        for (;;) { /* loop through all the sub linkages */
            for (i = 0; i < pi.N_links; i++) {
                patch_array[i].used = patch_array[i].changed = false;
                patch_array[i].newl = pi.link_array[i].l;
                patch_array[i].newr = pi.link_array[i].r;
                // TODO: copy_full_link(& sublinkage.link[i], & (pi.link_array[i]));
                // sublinkage.link[i] = pi.link_array[i];
                sublinkage.link[i] = new Link();
                sublinkage.link[i].l = pi.link_array[i].l;
                sublinkage.link[i].lc = pi.link_array[i].lc;
                sublinkage.link[i].r = pi.link_array[i].r;
                sublinkage.link[i].rc = pi.link_array[i].rc;
                sublinkage.link[i].name = pi.link_array[i].name;
            }
            fill_patch_array_DIS(d_root, null);

            for (i = 0; i < pi.N_links; i++) {
                if (patch_array[i].changed || patch_array[i].used) {
                    sublinkage.link[i].l = patch_array[i].newl;
                    sublinkage.link[i].r = patch_array[i].newr;
                } else {
                    if ((ParseInfo.dfs_root_word[pi.link_array[i].l] != -1)
                        && (ParseInfo.dfs_root_word[pi.link_array[i].r] != -1)) {
                        sublinkage.link[i].l = -1;
                    }
                }
            }

            compute_pp_link_array_connectors(sublinkage);
            compute_pp_link_names(sublinkage);

            /* 'analyze_pass' logic added ALB 1/97 */
            if (analyze_pass == GlobalBean.PP_FIRST_PASS) {
                post_process_scan_linkage(postprocessor, opts, sublinkage);
                if (!d_root.advance_DIS())
                    break;
                else
                    continue;
            }

            pp = post_process(postprocessor, opts, sublinkage, true);

            if (pp == null) {
                if (postprocessor != null)
                    li.N_violations = 1;
            } else if (pp.violation == null) {
                /* the purpose of this stuff is to make sure the domain
                   ancestry for a link in each of its sentences is consistent. */

                for (i = 0; i < pi.N_links; i++) {
                    if (sublinkage.link[i].l == -1)
                        continue;
                    if (accum.d_type_array[i] == null) {
                        accum.d_type_array[i] = copy_d_type(pp.d_type_array[i]);
                    } else {
                        dtl0 = pp.d_type_array[i];
                        dtl1 = accum.d_type_array[i];
                        while ((dtl0 != null) && (dtl1 != null) && (dtl0.type == dtl1.type)) {
                            dtl0 = dtl0.next;
                            dtl1 = dtl1.next;
                        }
                        if ((dtl0 != null) || (dtl1 != null))
                            break;
                    }
                }
                if (i != pi.N_links) {
                    li.N_violations++;
                    li.inconsistent_domains = true;
                }
            } else if (pp.violation != null) {
                li.N_violations++;
            }

            if (!d_root.advance_DIS())
                break;
        }

        /* if (display_on && (li.N_violations != 0) && 
           (verbosity > 3) && should_print_messages) 
           printf("P.P. violation in one part of conjunction.\n"); */
        return li;
    }

    /**
     * During a first pass (prior to actual post-processing of the linkages 
           of a sentence), call this once for every generated linkage. Here we
           simply maintain a set of "seen" link names for rule pruning later on
     * @param pp 
     * @param opts 
     * @param sublinkage 
     */
    public void post_process_scan_linkage(Postprocessor pp, ParseOptions opts, Sublinkage sublinkage) {
        /* During a first pass (prior to actual post-processing of the linkages 
           of a sentence), call this once for every generated linkage. Here we
           simply maintain a set of "seen" link names for rule pruning later on */
        String p;
        int i;
        if (pp == null)
            return;
        if (length < opts.twopass_length)
            return;
        for (i = 0; i < sublinkage.num_links; i++) {
            if (sublinkage.link[i].l == -1)
                continue;
            p = sublinkage.link[i].name;
            PPLinkset.PPLinkset_add(pp.set_of_links_of_sentence, p);
        }
    }

    /**
     * call this (a) after having called post_process_scan_linkage() on all
           generated linkages, but (b) before calling post_process() on any
           particular linkage. Here we mark all rules which we know (from having
           accumulated a set of link names appearing in *any* linkage) won't
           ever be needed.
     * @param opts 
     * @param pp 
     */
    public void prune_irrelevant_rules(ParseOptions opts, Postprocessor pp) {
        /* call this (a) after having called post_process_scan_linkage() on all
           generated linkages, but (b) before calling post_process() on any
           particular linkage. Here we mark all rules which we know (from having
           accumulated a set of link names appearing in *any* linkage) won't
           ever be needed. */
        PPRule rule;
        int coIDX, cnIDX, rcoIDX = 0, rcnIDX = 0;

        /* If we didn't scan any linkages, there's no pruning to be done. */
        if (PPLinkset.PPLinkset_population(pp.set_of_links_of_sentence) == 0)
            return;

        for (coIDX = 0;; coIDX++) {
            rule = pp.knowledge.contains_one_rules[coIDX];
            if (rule.msg == null)
                break;
            if (PPLinkset.PPLinkset_match_bw(pp.set_of_links_of_sentence, rule.selector)) {
                /* mark rule as being relevant to this sentence */
                pp.relevant_contains_one_rules[rcoIDX++] = coIDX;
                PPLinkset.PPLinkset_add(pp.set_of_links_in_an_active_rule, rule.selector);
            }
        }
        pp.relevant_contains_one_rules[rcoIDX] = -1; /* end sentinel */

        for (cnIDX = 0;; cnIDX++) {
            rule = pp.knowledge.contains_none_rules[cnIDX];
            if (rule.msg == null)
                break;
            if (PPLinkset.PPLinkset_match_bw(pp.set_of_links_of_sentence, rule.selector)) {
                pp.relevant_contains_none_rules[rcnIDX++] = cnIDX;
                PPLinkset.PPLinkset_add(pp.set_of_links_in_an_active_rule, rule.selector);
            }
        }
        pp.relevant_contains_none_rules[rcnIDX] = -1;

        if (opts.verbosity > 1) {
            opts.out.println(
                "Saw "
                    + PPLinkset.PPLinkset_population(pp.set_of_links_of_sentence)
                    + " unique link names in all linkages.");
            opts.out.println("Using " + rcoIDX + " 'contains one' rules and " + rcnIDX + " 'contains none' rules");
        }
    }

    /**
     * Takes a sublinkage and returns:
     *<ul>
     * <li>      . for each link, the domain structure of that link
     * <li>      . a list of the violation strings
     * </ul>
     * <p>
           NB: sublinkage.link[i].l=-1 means that this connector is to be ignored
     * @param pp 
     * @param opts 
     * @param sublinkage 
     * @param cleanup 
     * @return a PPNode for the linkage in the given postprocessor or null if none or the postprocessor is not valid
     */
    public PPNode post_process(Postprocessor pp, ParseOptions opts, Sublinkage sublinkage, boolean cleanup) {
        /* Takes a sublinkage and returns:
           . for each link, the domain structure of that link
           . a list of the violation strings
           NB: sublinkage.link[i].l=-1 means that this connector is to be ignored*/

        String msg[] = new String[1];

        if (pp == null)
            return null;

        pp.pp_data.links_to_ignore = null;
        pp.pp_data.length = length;

        /* In the name of responsible memory management, we retain a copy of the 
           returned data structure pp_node as a field in pp, so that we can clear
           it out after every call, without relying on the user to do so. */
        Postprocessor.reset_pp_node(pp);

        /* The first time we see a sentence, prune the rules which we won't be 
           needing during postprocessing the linkages of this sentence */
        if (q_pruned_rules == false && length >= opts.twopass_length)
            prune_irrelevant_rules(opts, pp);
        q_pruned_rules = true;

        switch (Postprocessor.internal_process(pp, sublinkage, msg)) {
            case -1 :
                /* some global test failed even before we had to build the domains */
                pp.n_global_rules_firing++;
                pp.pp_node.violation = msg[0];
                return pp.pp_node;

            case 1 :
                /* one of the "normal" post processing tests failed */
                pp.n_local_rules_firing++;
                pp.pp_node.violation = msg[0];
                break;
            case 0 :
                /* This linkage is legal according to the post processing rules */
                pp.pp_node.violation = null;
                break;
        }

        Postprocessor.build_type_array(pp);
        if (cleanup)
            Postprocessor.post_process_free_data(pp.pp_data);
        return pp.pp_node;
    }

    /**
     * This takes as input link_array[], sublinkage.link[].l and
           sublinkage.link[].r (and also has_fat_down[word], which has been
           computed in a prior call to is_canonical()), and from these
           computes sublinkage.link[].lc and .rc.  We assume these have
           been initialized with the values from link_array.  We also assume
           that there are fat links.
     * @param sublinkage 
     */
    public void compute_pp_link_array_connectors(Sublinkage sublinkage) {
        /*
           This takes as input link_array[], sublinkage.link[].l and
           sublinkage.link[].r (and also has_fat_down[word], which has been
           computed in a prior call to is_canonical()), and from these
           computes sublinkage.link[].lc and .rc.  We assume these have
           been initialized with the values from link_array.  We also assume
           that there are fat links.
        */
        int link, end, word, place;
        Connector this_end_con, upcon, updiscon, clist, con, mycon;
        Disjunct dis, updis, mydis;
        ParseInfo pi = parse_info;

        for (end = -1; end <= 1; end += 2) {
            for (link = 0; link < pi.N_links; link++) {
                if (sublinkage.link[link].l == -1)
                    continue;
                if (end < 0) {
                    word = pi.link_array[link].l;
                    if (!has_fat_down[word])
                        continue;
                    this_end_con = pi.link_array[link].lc;
                    dis = pi.chosen_disjuncts[word];
                    mydis = pi.chosen_disjuncts[sublinkage.link[link].l];
                    clist = dis.right;
                } else {
                    word = pi.link_array[link].r;
                    if (!has_fat_down[word])
                        continue;
                    this_end_con = pi.link_array[link].rc;
                    dis = pi.chosen_disjuncts[word];
                    mydis = pi.chosen_disjuncts[sublinkage.link[link].r];
                    clist = dis.left;
                }

                if (this_end_con.label != GlobalBean.NORMAL_LABEL)
                    continue;
                /* no need to construct a connector for up links,
                   or commas links or either/neither links */

                /* Now compute the place */
                place = 0;
                if ((dis.left != null) && (dis.left.priority == GlobalBean.UP_priority)) {
                    upcon = dis.left;
                } else if ((dis.right != null) && (dis.right.priority == GlobalBean.UP_priority)) {
                    upcon = dis.right;
                } else {
                    upcon = null;
                }
                if (upcon != null) { /* add on extra for a fat up link */
                    updis = and_data.label_table[upcon.label];
                    if (end > 0) {
                        updiscon = updis.left;
                    } else {
                        updiscon = updis.right;
                    }
                    for (; updiscon != null; updiscon = updiscon.next) {
                        place++;
                    }
                }
                for (; clist != this_end_con; clist = clist.next) {
                    if (clist.label < 0)
                        place++;
                }
                /* place has just been computed */

                /* now find the right disjunct in the table */
                if ((mydis.left != null) && (mydis.left.priority == GlobalBean.UP_priority)) {
                    mycon = mydis.left;
                } else if ((mydis.right != null) && (mydis.right.priority == GlobalBean.UP_priority)) {
                    mycon = mydis.right;
                } else {
                    throw new RuntimeException(
                        "There should be a fat UP link here. "
                            + "word = "
                            + word
                            + " fat link: ["
                            + pi.link_array[link].l
                            + ", "
                            + pi.link_array[link].r
                            + "]"
                            + " thin link: ["
                            + sublinkage.link[link].l
                            + ", "
                            + sublinkage.link[link].r
                            + "]");
                }

                for (dis = and_data.label_table[mycon.label]; dis != null; dis = dis.next) {
                    if (dis.string == mycon.string)
                        break;
                }
                if (!(dis != null)) {
                    throw new RuntimeException("Should have found this connector string");
                }
                /* the disjunct in the table has just been found */

                if (end < 0) {
                    for (con = dis.right; place > 0; place--, con = con.next);
                    /* sublinkage.link[link].lc = con; OLD CODE */
                    sublinkage.link[link].lc = Connector.copy_connectors(con);
                } else {
                    for (con = dis.left; place > 0; place--, con = con.next);
                    /* sublinkage.link[link].rc = con; OLD CODE */
                    sublinkage.link[link].rc = Connector.copy_connectors(con);
                }
            }
        }
    }

    /**
     * This fills in the sublinkage.link[].name field.  We assume that
           link_array[].name have already been filled in.  As above, in the
           standard case, the name is just the GCD of the two end points.
           If pluralization has occurred, then we want to use the name
           already in link_array[].name.  We detect this in two ways.
           If the endpoints don't match, then we know pluralization
           has occured.  If they do, but the name in link_array[].name
           is *less* restrictive, then pluralization must have occured.
     * @param sublinkage 
     */
    public void compute_pp_link_names(Sublinkage sublinkage) {
        /*
           This fills in the sublinkage.link[].name field.  We assume that
           link_array[].name have already been filled in.  As above, in the
           standard case, the name is just the GCD of the two end points.
           If pluralization has occurred, then we want to use the name
           already in link_array[].name.  We detect this in two ways.
           If the endpoints don't match, then we know pluralization
           has occured.  If they do, but the name in link_array[].name
           is *less* restrictive, then pluralization must have occured.
        */
        int i;
        String s;
        ParseInfo pi = parse_info;

        for (i = 0; i < pi.N_links; i++) {
            if (sublinkage.link[i].l == -1)
                continue;
            if (!Connector.x_match(this, sublinkage.link[i].lc, sublinkage.link[i].rc))
                sublinkage.link[i].replace_link_name(pi.link_array[i].name);
            else {
                s = Sentence.intersect_strings(sublinkage.link[i].lc.string, sublinkage.link[i].rc.string);
                if (Sentence.strictly_smaller_name(s, pi.link_array[i].name))
                    sublinkage.link[i].replace_link_name(pi.link_array[i].name);
                else
                    sublinkage.link[i].replace_link_name(s);
            }
        }
    }

    /**
     * Copy the named Domain Type List and return a copy
     * @param dtl 
     * @return head of the DTypeList
     */
    public static DTypeList copy_d_type(DTypeList dtl) {
        DTypeList dtlx, dtlcurr = null, dtlhead = null;
        for (; dtl != null; dtl = dtl.next) {
            dtlx = new DTypeList(dtl);
            if (dtlhead == null) {
                dtlhead = dtlx;
                dtlcurr = dtlx;
            } else {
                dtlcurr.next = dtlx;
                dtlcurr = dtlx;
            }
        }
        return dtlhead;
    }

    /**
     * get down the tree past all the commas 
     */
    private void and_dfs_commas(int w) {
        
        ListOfLinks lol;
        if (visited[w])
            return;
        visited[w] = true;
        for (lol = ParseInfo.word_links[w]; lol != null; lol = lol.next) {
            if (lol.dir == 1) {
                /* we only consider UP or DOWN priority links here */

                if (word[lol.word].string.equals(",")) {
                    /* pointing to a comma */
                    and_dfs_commas(lol.word);
                } else {
                    and_element[N_and_elements] = lol.word;
                    and_dfs_full(lol.word);
                    N_and_elements++;
                }
            }
            if (lol.dir == 0) {
                outside_word[N_outside_words] = lol.word;
                N_outside_words++;
            }
        }
    }

    private void and_dfs_full(int w) {
        /* scope out this and element */
        ListOfLinks lol;
        if (visited[w])
            return;
        visited[w] = true;
        and_element_sizes[N_and_elements]++;

        for (lol = ParseInfo.word_links[w]; lol != null; lol = lol.next) {
            if (lol.dir >= 0) {
                and_dfs_full(lol.word);
            }
        }
    }

    /**
     * This function computes the "and cost", resulting from inequalities in the length of 
           and-list elements. It also computes other information used to construct the "andlist"
           structure of linkage_info.
     * @return list of conunctions
     * @see AndData for a detailed explanation of And
     */
    public AndList build_andlist() {
        /* This function computes the "and cost", resulting from inequalities in the length of 
           and-list elements. It also computes other information used to construct the "andlist"
           structure of linkage_info. */

        int w, i, min, max, j, cost;
        String s;
        AndList new_andlist, old_andlist;
        ParseInfo pi = parse_info;

        old_andlist = null;
        cost = 0;

        for (w = 0; w < pi.N_words; w++) {
            s = word[w].string;
            if (is_conjunction[w]) {
                N_and_elements = 0;
                N_outside_words = 0;
                for (i = 0; i < pi.N_words; i++) {
                    visited[i] = false;
                    and_element_sizes[i] = 0;
                }
                if (dict.left_wall_defined)
                    visited[0] = true;
                and_dfs_commas(w);
                if (N_and_elements == 0)
                    continue;
                new_andlist = new AndList();
                new_andlist.num_elements = N_and_elements;
                new_andlist.num_outside_words = N_outside_words;
                for (i = 0; i < N_and_elements; i++) {
                    new_andlist.element[i] = and_element[i];
                }
                for (i = 0; i < N_outside_words; i++) {
                    new_andlist.outside_word[i] = outside_word[i];
                }
                new_andlist.conjunction = w;
                new_andlist.next = old_andlist;
                old_andlist = new_andlist;

                if (N_and_elements > 0) {
                    min = GlobalBean.MAX_SENTENCE;
                    max = 0;
                    for (i = 0; i < N_and_elements; i++) {
                        j = and_element_sizes[i];
                        if (j < min)
                            min = j;
                        if (j > max)
                            max = j;
                    }
                    cost += max - min;
                }
            }
        }
        old_andlist.cost = cost;
        return old_andlist;
    }

    /**
     * This uses link_array.  It post-processes
           this linkage, and prints the appropriate thing.  There are no fat
           links in it.
     * <p>
     * The code can be used to generate the "islands" array. For this to work,
           however, you have to call "build_digraph" first (as in analyze_fat_linkage).
           and then "free_digraph". For some reason this causes a space leak.
     * @param opts 
     * @param analyze_pass 
     * @return a valid LinkageInfo 
     */
    public LinkageInfo analyze_thin_linkage(ParseOptions opts, int analyze_pass) {
        /* This uses link_array.  It post-processes
           this linkage, and prints the appropriate thing.  There are no fat
           links in it. */
        int i;
        LinkageInfo li = new LinkageInfo();
        PPNode pp;
        Postprocessor postprocessor;
        Sublinkage sublinkage;
        ParseInfo pi = parse_info;
        pi.build_digraph();

        sublinkage = new Sublinkage(pi);
        postprocessor = dict.postprocessor;

        compute_link_names();
        for (i = 0; i < pi.N_links; i++) {
            //TODO: copy_full_link(& (sublinkage.link[i]), & (pi.link_array[i]));
            sublinkage.link[i] = pi.link_array[i];
        }

        if (analyze_pass == GlobalBean.PP_FIRST_PASS) {
            post_process_scan_linkage(postprocessor, opts, sublinkage);
            return li;
        }

        li.N_violations = 0;
        li.and_cost = 0;

        /* The code below can be used to generate the "islands" array. For this to work,
           however, you have to call "build_digraph" first (as in analyze_fat_linkage).
           and then "free_digraph". For some reason this causes a space leak. */

        pp = post_process(postprocessor, opts, sublinkage, true);

        li.unused_word_cost = pi.unused_word_cost();
        li.improper_fat_linkage = false;
        li.inconsistent_domains = false;
        li.disjunct_cost = pi.disjunct_cost();
        li.null_cost = pi.null_cost();
        li.link_cost = pi.link_cost();
        li.andlist = null;

        if (pp == null) {
            if (postprocessor != null)
                li.N_violations = 1;
        } else if (pp.violation != null) {
            li.N_violations++;
        }

        return li;
    }

    /**
     * uses link_array[], chosen_disjuncts[], has_fat_down[].
           It assumes that there is a fat link in the current linkage.
           See AndData() for more information about how it works
     * @return true if it is cannonical
     * @see AndData
     */
    public boolean is_canonical_linkage() {
        /*
           This uses link_array[], chosen_disjuncts[], has_fat_down[].
           It assumes that there is a fat link in the current linkage.
           See the comments above for more information about how it works
        */

        int w, d_label = 0, place;
        Connector d_c, c, dummy_connector, upcon;
        Disjunct dis, chosen_d;
        ImageNode in;
        ParseInfo pi = parse_info;

        dummy_connector = new Connector();
        dummy_connector.priority = GlobalBean.UP_priority;
        dummy_connector.init_connector();

        build_image_array();

        for (w = 0; w < pi.N_words; w++) {
            if (!has_fat_down[w])
                continue;
            chosen_d = pi.chosen_disjuncts[w];

            /* there must be a down connector in both the left and right list */
            for (d_c = chosen_d.left; d_c != null; d_c = d_c.next) {
                if (d_c.priority == GlobalBean.DOWN_priority) {
                    d_label = d_c.label;
                    break;
                }
            }
            if (d_c == null) {
                throw new RuntimeException("Should have found the down link.");
            }

            if ((chosen_d.left != null) && (chosen_d.left.priority == GlobalBean.UP_priority)) {
                upcon = chosen_d.left;
            } else if ((chosen_d.right != null) && (chosen_d.right.priority == GlobalBean.UP_priority)) {
                upcon = chosen_d.right;
            } else {
                upcon = null;
            }

            /* check that the disjunct on w is minimal (canonical) */
            for (dis = and_data.label_table[d_label]; dis != null; dis = dis.next) {
                /* now, reject a disjunct if it's not strictly below the old */
                if (!strictly_smaller(dis.string, d_c.string))
                    continue;

                /* Now, it has to match the image connectors */

                for (in = image_array[w]; in != null; in = in.next) {

                    place = in.place;
                    if (place == 0) {

                        if (!(upcon != null)) {
                            throw new RuntimeException("Should have found an up link");
                        }
                        dummy_connector.label = upcon.label;

                        /* now we have to compute the string of the
                           disjunct with upcon.label that corresponds
                           to dis  */

                        if (upcon.label == d_label) {
                            dummy_connector.string = dis.string;
                        } else {
                            dummy_connector.string = find_subdisjunct(dis, upcon.label).string;
                        }
                        if (!Connector.x_match(this, dummy_connector, in.c))
                            break; /* I hope using x_match here is right */
                    } else if (place > 0) {
                        for (c = dis.right; place > 1; place--) {
                            c = c.next;
                        }
                        if (!Connector.x_match(this, c, in.c))
                            break; /* Ditto above comment  --DS 07/97*/
                    } else {
                        for (c = dis.left; place < -1; place++) {
                            c = c.next;
                        }
                        if (!Connector.x_match(this, c, in.c))
                            break; /* Ditto Ditto */
                    }
                }

                if (in == null)
                    break;
            }
            if (dis != null)
                break;
            /* there is a better disjunct that the one we're using, so this
               word is bad, so we're done */
        }
        return (w == pi.N_words);
    }

    /**
     * 
     * @param s 
     * @param t 
     * @return true if string s represents a strictly smaller match set
           than does t
     */
    public boolean strictly_smaller(String s, String t) {
        /* returns true if string s represents a strictly smaller match set
           than does t
        */
        int strictness;
        strictness = 0;
        int i = 0;
        while (i < s.length() && i < t.length()) {
            if (s.charAt(i) == t.charAt(i))
                continue;
            if (t.charAt(i) == '*' || s.charAt(i) == '^') {
                strictness++;
            } else {
                return false;
            }
            i++;
        }
        if (i != s.length() || i != t.length()) {
            throw new RuntimeException("s and t should be the same length!");
        }
        return (strictness > 0);
    }

    /**
     * Find the specific disjunct of in label_table[label]
           which corresponds to dis.
     * @param dis a disjunct in the label_table
     * @param label lable_table containing a disjunct
     * @return first disjunct
     */
    public Disjunct find_subdisjunct(Disjunct dis, int label) {
        /* dis points to a disjunct in the label_table.  label is the label
           of a different set of disjuncts.  These can be derived from the label
           of dis.  Find the specific disjunct of in label_table[label]
           which corresponds to dis.
        */
        Disjunct d;
        Connector cx, cy;
        for (d = and_data.label_table[label]; d != null; d = d.next) {
            for (cx = d.left, cy = dis.left; cx != null; cx = cx.next, cy = cy.next) {
                /*      if ((cx.string != cy.string) || */
                if ((!cx.string.equals(cy.string)) || (cx.multi != cy.multi))
                    break; /* have to check multi? */
            }
            if (cx != null)
                continue;
            for (cx = d.right, cy = dis.right; cx != null; cx = cx.next, cy = cy.next) {
                /*      if ((cx.string != cy.string) || */
                if (!cx.string.equals(cy.string) || cx.multi != cy.multi)
                    break;
            }
            if (cx == null)
                break;
        }
        if (d == null) {
            throw new RuntimeException("Never found subdisjunct");
        }
        return d;
    }

    /* TRUE if this word has a fat down link, FALSE otherise */
    public static boolean has_fat_down[] = new boolean[GlobalBean.MAX_SENTENCE];

    /* points to the image structure for each word.  null if not a fat word. */
    public static ImageNode image_array[] = new ImageNode[GlobalBean.MAX_SENTENCE];

    /* The following routines' purpose is to eliminate all but the
       canonical linkage (of a collection of linkages that are identical
       except for fat links).  An example of the problem is
       "I went to a talk and ate lunch".  Without the canonical checker
       this has two linkages with identical structure.
    <p>
       We restrict our attention to a collection of linkages that are all
       isomorphic.  Consider the set of all disjuncts that are used on one
       word (over the collection of linkages).  This set is closed under GCD,
       since two linkages could both be used in that position, then so could
       their GCD.  The GCD has been constructed and put in the label table.
    <p>
       The canonical linkage is the one in which the minimal disjunct that
       ever occurrs in a position is used in that position.  It is easy to
       prove that a disjunct is not canonical -- just find one of it's fat
       disjuncts that can be replaced by a smaller one.  If this can not be
       done, then the linkage is canonical.
    <p>
       The algorithm uses link_array[] and chosen_disjuncts[] as input to
       describe the linkage, and also uses the label_table.
    <p>
       (1) find all the words with fat disjuncts
     *<p>
       (2) scan all links and build, for each fat disjucnt used,
            an "image" structure that contains what this disjunct must
        connect to in the rest of the linkage.
     *<p>
       (3) For each fat disjunct, run through the label_table for disjuncts
            with the same label, considering only those with strictly more
            restricted match sets (this uses the string fields of the disjuncts
        from the table).
     *<p>
       (4) For each that passes this test, we see if it can replace the chosen
            disjunct.  This is performed by examining how this disjunct
            compares with the image structure for this word.
    */

    public void build_image_array() {
        /* uses link_array, chosen_disjuncts, and down_label to construct
           image_array */
        int link, end, word;
        Connector this_end_con, other_end_con, upcon, updiscon, clist;
        Disjunct dis, updis;
        ImageNode in;
        ParseInfo pi = parse_info;

        for (word = 0; word < pi.N_words; word++) {
            image_array[word] = null;
        }

        for (end = -1; end <= 1; end += 2) {
            for (link = 0; link < pi.N_links; link++) {
                if (end < 0) {
                    word = pi.link_array[link].l;
                    if (!has_fat_down[word])
                        continue;
                    this_end_con = pi.link_array[link].lc;
                    other_end_con = pi.link_array[link].rc;
                    dis = pi.chosen_disjuncts[word];
                    clist = dis.right;
                } else {
                    word = pi.link_array[link].r;
                    if (!has_fat_down[word])
                        continue;
                    this_end_con = pi.link_array[link].rc;
                    other_end_con = pi.link_array[link].lc;
                    dis = pi.chosen_disjuncts[word];
                    clist = dis.left;
                }

                if (this_end_con.priority == GlobalBean.DOWN_priority)
                    continue;
                if ((this_end_con.label != GlobalBean.NORMAL_LABEL) && (this_end_con.label < 0))
                    continue;
                /* no need to construct an image node for down links,
                   or commas links or either/neither links */

                in = new ImageNode();
                in.next = image_array[word];
                image_array[word] = in;
                in.c = other_end_con;
                /* the rest of this code is for computing in.place */
                if (this_end_con.priority == GlobalBean.UP_priority) {
                    in.place = 0;
                } else {
                    in.place = 1;
                    if ((dis.left != null) && (dis.left.priority == GlobalBean.UP_priority)) {
                        upcon = dis.left;
                    } else if ((dis.right != null) && (dis.right.priority == GlobalBean.UP_priority)) {
                        upcon = dis.right;
                    } else {
                        upcon = null;
                    }
                    if (upcon != null) { /* add on extra for a fat up link */
                        updis = and_data.label_table[upcon.label];
                        if (end > 0) {
                            updiscon = updis.left;
                        } else {
                            updiscon = updis.right;
                        }
                        for (; updiscon != null; updiscon = updiscon.next) {
                            in.place++;
                        }
                    }
                    for (; clist != this_end_con; clist = clist.next) {
                        if (clist.label < 0)
                            in.place++;
                    }
                    in.place = in.place * (-end);
                }
            }
        }
    }

    /**
     * Computes and returns the number of connectors in all of the expressions 
           of the sentence.
     * @return the number of connectors in all of the expressions
     */
    public int size_of_sentence_expressions() {

        /* 
        */
        XNode x;
        int w, size;
        size = 0;
        for (w = 0; w < length; w++) {
            for (x = word[w].x; x != null; x = x.next) {
                size += x.exp.size_of_expression();
            }
        }
        return size;
    }

    /**
     * This removes the expressions that are empty from the list corresponding
         *  to word w of the sentence.
     * @param w 
     */
    public void clean_up_expressions(int w) {

        /* 
         */
        XNode head_node = new XNode(), d, d1;
        d = head_node;
        d.next = word[w].x;
        while (d.next != null) {
            if (d.next.exp == null) {
                d1 = d.next;
                d.next = d1.next;
            } else {
                d = d.next;
            }
        }
        word[w].x = head_node.next;
    }

    /**
     * 
     * @param opts 
     */
    public void expression_prune(ParseOptions opts) {
        int N_deleted;
        XNode x;
        int w;
        s_table_size = MyRandom.next_power_of_two_up(size_of_sentence_expressions());
        table = new Connector[s_table_size];
        zero_S();
        N_deleted = 1;
        /* a lie to make it always do at least 2 passes */

        for (;;) { /* left-to-right pass */
            for (w = 0; w < length; w++) {
                for (x = word[w].x; x != null; x = x.next) {
                    N_deleted += x.exp.mark_dead_connectors(this, '-');
                }
                for (x = word[w].x; x != null; x = x.next) {
                    x.exp = x.exp.purge_Exp();
                }
                clean_up_expressions(w);
                /* gets rid of XNodes with null exp */
                for (x = word[w].x; x != null; x = x.next) {
                    x.exp.insert_connectors('+');
                }
            }

            if (opts.verbosity > 2) {
                opts.out.println("l->r pass removed " + N_deleted);
                print_expression_sizes(opts);
            }

            free_S();
            if (N_deleted == 0)
                break; /* right-to-left pass */
            N_deleted = 0;
            for (w = length - 1; w >= 0; w--) {
                for (x = word[w].x; x != null; x = x.next) {
                    N_deleted += x.exp.mark_dead_connectors(this, '+');
                }
                for (x = word[w].x; x != null; x = x.next) {
                    x.exp = x.exp.purge_Exp();
                }
                clean_up_expressions(w);
                /* gets rid of XNodes with null exp */
                for (x = word[w].x; x != null; x = x.next) {
                    x.exp.insert_connectors('-');
                }
            }

            if (opts.verbosity > 2) {
                opts.out.println("r->l pass removed " + N_deleted);
                print_expression_sizes(opts);
            }
            free_S();
            if (N_deleted == 0)
                break;
            N_deleted = 0;
        }
    }

    /**
     * 
     * @param opts 
     */
    public void print_expression_sizes(ParseOptions opts) {
        XNode x;
        int w, size;
        for (w = 0; w < length; w++) {
            size = 0;
            for (x = word[w].x; x != null; x = x.next) {
                size += x.exp.size_of_expression();
            }
            opts.out.print(word[w].string + "[" + size + "] ");
        }
        opts.out.println();
        opts.out.println();
    }

    public static int s_table_size;
    public static Connector table[];

    public static void zero_S() {
        int i;
        for (i = 0; i < s_table_size; i++) {
            table[i] = null;
        }
    }

    public static void free_S() {
        /* This function removes all connectors from the set S */
        int i;
        for (i = 0; i < s_table_size; i++) {
            table[i] = null;
        }
    }

    /**
     * 
     * @param c 
     */
    public static void insert_S(Connector c) {
        /* this function puts a copy of c into S if one like it isn't already there */
        int h;
        Connector e;

        h = hash_S(c);

        for (e = table[h]; e != null; e = e.next) {
            if (c.string.equals(e.string) && c.label == e.label && c.priority == e.priority) {
                return;
            }
        }
        e = new Connector(c);
        e.next = table[h];
        table[h] = e;
    }

    /**
     * This hash function only looks at the leading upper case letters of
           the connector string, and the label fields.  This ensures that if two
           strings match (formally), then they must hash to the same place.
     * @param c 
     * @return the hash  of the connector
     */
    public static int hash_S(Connector c) {
        /* 
        */
        String s;
        int i;
        i = c.label;
        s = c.string;
        int j = 0;
        while (j < s.length() && Character.isUpperCase(s.charAt(j))) {
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
            j++;
        }
        return (i & (s_table_size - 1));
    }

    /**
     * 
     * @param opts 
     */
    public void print_parse_statistics(ParseOptions opts) {
        if (sentence_num_linkages_found() > 0) {
            if (sentence_num_linkages_found() > opts.parse_options_get_linkage_limit()) {
                opts.out.print(
                    "Found "
                        + sentence_num_linkages_found()
                        + " linkage"
                        + (sentence_num_linkages_found() == 1 ? "" : "s")
                        + " ("
                        + sentence_num_valid_linkages()
                        + " of "
                        + sentence_num_linkages_post_processed()
                        + " random linkages had no P.P. violations)");
            } else {
                opts.out.print(
                    "Found "
                        + sentence_num_linkages_post_processed()
                        + " linkage"
                        + (sentence_num_linkages_found() == 1 ? "" : "s")
                        + " ("
                        + sentence_num_valid_linkages()
                        + " had no P.P. violations)");
            }
            if (sentence_null_count() > 0) {
                opts.out.print(" at null count " + sentence_null_count());
            }
            opts.out.println();
        }
    }

    /**
     * returns true if c can match anything in the set S 
     * because of the horrible kludge, prune match is assymetric, and  
     * direction is '-' if this is an l.r pass, and '+' if an r.l pass.
     * @param c 
     * @param dir 
     * @return returns true if c can match anything in the set S
     */
    public boolean matches_S(Connector c, int dir) {
        /*    */

        int h;
        Connector e;

        h = hash_S(c);
        if (dir == '-') {
            for (e = table[h]; e != null; e = e.next) {
                if (Connector.x_prune_match(this, e, c))
                    return true;
            }
        } else {
            for (e = table[h]; e != null; e = e.next) {
                if (Connector.x_prune_match(this, c, e))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param index 
     * @return the String form of the word at the named index in this.word
     * @see #word
     */
    public String sentence_get_word(int index) {
        return word[index].string;
    }

    /**
     * 
     * @return the number of null linkages?
     */
    public int sentence_null_count() {
        return null_count;
    }

    /**
     * 
     * @return the number of linkages found
     */
    public int sentence_num_linkages_found() {
        return num_linkages_found;
    }


    /**
     * 
     * @return the number of valid linkages
     */
    public int sentence_num_valid_linkages() {

      return num_valid_linkages;
    }

    public int sentence_num_linkages_post_processed() {
        return num_linkages_post_processed;
    }

    public int sentence_num_violations(int i) {
        return link_info[i].N_violations;
    }

    public int sentence_disjunct_cost(int i) {
        return link_info[i].disjunct_cost;
    }

    public boolean set_has_fat_down() { /* Fill in the has_fat_down array.  Uses link_array[].
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             Returns true if there exists at least one word with a
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             fat down label.
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          */ /* true if this word has a fat down link false otherise */
        boolean has_fat_down[] = new boolean[GlobalBean.MAX_SENTENCE];
        int link, w, N_fat;
        ParseInfo pi = parse_info;
        N_fat = 0;
        for (w = 0; w < pi.N_words; w++) {
            has_fat_down[w] = false;
        }

        for (link = 0; link < pi.N_links; link++) {
            if (pi.link_array[link].lc.priority == GlobalBean.DOWN_priority) {
                N_fat++;
                has_fat_down[pi.link_array[link].l] = true;
            } else if (pi.link_array[link].rc.priority == GlobalBean.DOWN_priority) {
                N_fat++;
                has_fat_down[pi.link_array[link].r] = true;
            }
        }
        return N_fat > 0;
    }

    public void compute_link_names() {
        /*
        The name of the link is set to be the GCD of the names of
        its two endpoints.
        */
        int i;
        ParseInfo pi = parse_info;
        for (i = 0; i < pi.N_links; i++) {
            pi.link_array[i].name = intersect_strings(pi.link_array[i].lc.string, pi.link_array[i].rc.string);
        }
    }

    public static boolean strictly_smaller_name(String s, String t) {
        /*
         Returns true if string s represents a strictly smaller match set
         than does t.  An almost identical function appears in and.c.
         The difference is that here we don't require s and t to be the
         same length.
        */
        int strictness, ss, tt;
        strictness = 0;
        int i = 0;
        int j = 0;
        while (i < s.length() || j < t.length()) {
            if (i >= s.length()) {
                ss = '*';
            } else {
                ss = s.charAt(i);
                i++;
            }
            if (j >= t.length()) {
                tt = '*';
            } else {
                tt = t.charAt(j);
                j++;
            }
            if (ss == tt)
                continue;
            if ((tt == '*') || (ss == '^')) {
                strictness++;
            } else {
                return false;
            }
        }
        return (strictness > 0);
    }

    public static String intersect_strings(String s, String t) {
        /* This returns a string that is the the GCD of the two given strings.
         If the GCD is equal to one of them, a pointer to it is returned.
         Otherwise a new string for the GCD is xalloced and put on the
         free later" list.
         */
        int len, i, j, d = 0;
        StringBuffer u;
        String u0;
        if (s.equals(t))
            return s; /* would work without this */
        i = s.length();
        j = t.length();
        if (j > i) {
            u0 = s;
            s = t;
            t = u0;
            len = j;
        } else {
            len = i;
        } /* s is now the longer (at least not the shorter) string */ /* and len is its length */
        u = new StringBuffer(len);
        i = 0;
        while (i < t.length()) {
            if (s.charAt(i) == t.charAt(i) || t.charAt(i) == '*') {
                u.append(s.charAt(i));
            } else {
                d++;
                if (s.charAt(i) == '*')
                    u.append(t.charAt(i));
                else
                    u.append('^');
            }
            i++;
        }
        if (d == 0) {
            return s;
        } else {
            if (i < s.length()) {
                u.append(s.substring(i));
            }
            return u.toString();
        }
    }

    public void free_sentence_disjuncts() {
        int i;
        for (i = 0; i < length; ++i) {
            word[i].d = null;
        }
        if (sentence_contains_conjunction())
            free_AND_tables();
    }

    public void free_HT() {
        int i;
        for (i = 0; i < GlobalBean.HT_SIZE; i++) {
            and_data.hash_table[i] = null;
        }
    }

    public void free_LT() {
        int i;
        and_data.LT_bound = 0;
        and_data.LT_size = 0;
        and_data.label_table = null;
    }

    public void free_AND_tables() {
        free_LT();
        free_HT();
    }

    public void free_parse_set() {
        parse_info = null;
    }

  private void construct_comma() {
        /* Finds all places in the sentence where a comma is followed by
           a conjunction ("and", "or", "but", or "nor").  It modifies these comma
           disjuncts, and those of the following word, to allow the following
           word to absorb the comma (if used as a conjunction).
        */
        int w;
        for (w = 0; w < length - 1; w++) {
            if ((word[w].string.equals(",")) && is_conjunction[w + 1]) {
                word[w].d = Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.COMMA_LABEL, '+', "", ","), word[w].d);
                word[w + 1].d = glom_comma_connector(word[w + 1].d);
            }
        }
    }

    /* The functions below put the special connectors on certain auxilliary
       words to be used with conjunctions.  Examples: either, neither, 
       both...and..., not only...but...
    */
    private void construct_either() {
        int w;
        if (!sentence_contains("either"))
            return;
        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("either"))
                continue;
            word[w].d =
                Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.EITHER_LABEL, '+', "", "either"), word[w].d);
        }

        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("or"))
                continue;
            word[w].d = glom_aux_connector(word[w].d, GlobalBean.EITHER_LABEL, false);
        }
    }

    private void construct_neither() {
        int w;
        if (!sentence_contains("neither")) {
            /* I don't see the point removing disjuncts on "nor".  I
               Don't know why I did this.  What's the problem keeping the
               stuff explicitely defined for "nor" in the dictionary?  --DS 3/98 */
            return;
        }
        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("neither"))
                continue;
            word[w].d =
                Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.NEITHER_LABEL, '+', "", "neither"), word[w].d);
        }

        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("nor"))
                continue;
            word[w].d = glom_aux_connector(word[w].d, GlobalBean.NEITHER_LABEL, true);
        }
    }

    private void construct_notonlybut() {
        int w;
        Disjunct d;
        if (!sentence_contains("not")) {
            return;
        }
        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("not"))
                continue;
            word[w].d = Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.NOT_LABEL, '+', "", "not"), word[w].d);
            if (w < length - 1 && word[w + 1].string.equals("only")) {
                word[w + 1].d =
                    Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.NOTONLY_LABEL, '-', "", "only"), word[w + 1].d);
                d = special_disjunct(GlobalBean.NOTONLY_LABEL, '+', "", "not");
                d = add_one_connector(GlobalBean.NOT_LABEL, '+', "", d);
                word[w].d = Disjunct.catenate_disjuncts(d, word[w].d);
            }
        }
        /* The code below prevents sentences such as the following from
           parsing:
           it was not carried out by Serbs but by Croats */

        /* We decided that this is a silly thing to.  Here's the bug report
           caused by this:
        
          Bug with conjunctions.  Some that work with "and" but they don't work
          with "but".  "He was not hit by John and by Fred".
          (Try replacing "and" by "but" and it does not work.
          It's getting confused by the "not".)
         */
        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("but"))
                continue;
            word[w].d = glom_aux_connector(word[w].d, GlobalBean.NOT_LABEL, false);
            /* The above line use to have a true in it */
        }
    }

    private void construct_both() {
        int w;
        if (!sentence_contains("both"))
            return;
        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("both"))
                continue;
            word[w].d = Disjunct.catenate_disjuncts(special_disjunct(GlobalBean.BOTH_LABEL, '+', "", "both"), word[w].d);
        }

        for (w = 0; w < length; w++) {
            if (!word[w].string.equals("and"))
                continue;
            word[w].d = glom_aux_connector(word[w].d, GlobalBean.BOTH_LABEL, false);
        }
    }

    public void install_special_conjunctive_connectors() {
        construct_either(); /* special connectors for "either" */
        checkDuplicate("A");
        construct_neither(); /* special connectors for "neither" */
        checkDuplicate("B");
        construct_notonlybut(); /* special connectors for "not..but.." */
        /* and               "not only..but.." */
        checkDuplicate("C");
        construct_both(); /* special connectors for "both..and.." */
        checkDuplicate("D");
        construct_comma(); /* special connectors for extra comma */
        checkDuplicate("E");
    }

    public boolean sentence_contains(String s) {
        /* Returns true if one of the words in the sentence is s */
        int w;
        for (w = 0; w < length; w++) {
            if (word[w].string.equals(s))
                return true;
        }
        return false;
    }

    /**
     * This file contains the functions for massaging disjuncts of the 
    *   sentence in special ways having to do with conjunctions.
    *   The only function called from the outside world is 
    *   install_special_conjunctive_connectors()
    * <p>
    *   It would be nice if this code was written more transparently.  In
    *   other words, there should be some fairly general functions that
    *   manipulate disjuncts, and take words like "neither" etc as input
    *   parameters, so as to encapsulate the changes being made for special
    *   words.  This would not be too hard to do, but it's not a high priority.  
    *       -DS 3/98
    *<p>
    *
    * There's a problem with installing "...but...", "not only...but...", and
    *   "not...but...", which is that the current comma mechanism will allow
    *   a list separated by commas.  "Not only John, Mary but Jim came"
    *   The best way to prevent this is to make it impossible for the comma
    *   to attach to the "but", via some sort of additional subscript on commas.
    *<p>
    *   I can't think of a good way to prevent this.
    *<p>
    *<p>
    *
    * The following functions all do slightly different variants of the
    *   following thing:
    *<p>
    *   Catenate to the disjunct list pointed to by d, a new disjunct list.
    *   The new list is formed by copying the old list, and adding the new
    *   connector somewhere in the old disjunct, for disjuncts that satisfy
    *   certain conditions
    */

    public static Disjunct glom_comma_connector(Disjunct d) {
        /* In this case the connector is to connect to the comma to the
           left of an "and" or an "or".  Only gets added next to a fat link
        */
        Disjunct d_list, d1, d2;
        Connector c, c1;
        d_list = null;
        for (d1 = d; d1 != null; d1 = d1.next) {
            if (d1.left == null)
                continue;
            for (c = d1.left; c.next != null; c = c.next);
            if (c.label < 0)
                continue; /* last one must be a fat link */

            d2 = Disjunct.copy_disjunct(d1);
            d2.next = d_list;
            d_list = d2;

            c1 = new Connector();
            c1.init_connector();
            c1.string = "";
            c1.label = GlobalBean.COMMA_LABEL;
            c1.priority = GlobalBean.THIN_priority;
            c1.multi = false;
            c1.next = null;

            c.next = c1;
        }
        return Disjunct.catenate_disjuncts(d, d_list);
    }
    /**
     * In this case the connector is to connect to the "either", "neither",
           "not", or some auxilliary d to the current which is a conjunction.
           Only gets added next to a fat link, but before it (not after it)
           In the case of "nor", we don't create new disjuncts, we merely modify
           existing ones.  This forces the fat link uses of "nor" to
           use a neither.  (Not the case with "or".)  If necessary=false, then
           duplication is done, otherwise it isn't
        */
    public static Disjunct glom_aux_connector(Disjunct d, int label, boolean necessary) {
        
        Disjunct d_list, d1, d2;
        Connector c, c1, c2;
        d_list = null;
        for (d1 = d; d1 != null; d1 = d1.next) {
            if (d1.left == null)
                continue;
            for (c = d1.left; c.next != null; c = c.next);
            if (c.label < 0)
                continue; /* last one must be a fat link */

            if (!necessary) {
                d2 = Disjunct.copy_disjunct(d1);
                d2.next = d_list;
                d_list = d2;
            }

            c1 = new Connector();
            c1.init_connector();
            c1.string = "";
            c1.label = label;
            c1.priority = GlobalBean.THIN_priority;
            c1.multi = false;
            c1.next = c;

            if (d1.left == c) {
                d1.left = c1;
            } else {
                for (c2 = d1.left; c2.next != c; c2 = c2.next);
                c2.next = c1;
            }
        }
        return Disjunct.catenate_disjuncts(d, d_list);
    }
    /**
     * This adds one connector onto the beginning of the left (or right)
           connector list of d.  The label and string of the connector are
           specified
        */
    public static Disjunct add_one_connector(int label, int dir, String cs, Disjunct d) {
        
        Connector c = new Connector();

        c.init_connector();
        c.string = cs;
        c.label = label;
        c.priority = GlobalBean.THIN_priority;
        c.multi = false;
        c.next = null;

        if (dir == '+') {
            c.next = d.right;
            d.right = c;
        } else {
            c.next = d.left;
            d.left = c;
        }
        return d;
    }

    /**
     * Builds a new disjunct with one connector pointing in direction dir
           (which is '+' or '-').  The label and string of the connector
           are specified, as well as the string of the disjunct.
           The next pointer of the new disjunct set to null, so it can be
           regarded as a list.
        */
    public static Disjunct special_disjunct(int label, int dir, String cs, String ds) {
        
        Disjunct d1;
        Connector c = new Connector();
        d1 = new Disjunct();
        d1.cost = 0;
        d1.string = ds;
        d1.next = null;

        c.init_connector();
        c.string = cs;
        c.label = label;
        c.priority = GlobalBean.THIN_priority;
        c.multi = false;
        c.next = null;

        if (dir == '+') {
            d1.left = null;
            d1.right = c;
        } else {
            d1.right = null;
            d1.left = c;
        }
        return d1;
    }

    public int pp_prune(ParseOptions opts) {
        PPKnowledge knowledge;
        PPRule rule;
        String selector;
        PPLinkset link_set;
        int i, w, dir;
        Disjunct d;
        Connector c;
        int change, total_deleted, N_deleted;
        boolean deleteme;

        if (dict.postprocessor == null)
            return 0;

        knowledge = dict.postprocessor.knowledge;

        init_cms_table();

        for (w = 0; w < length; w++) {
            for (d = word[w].d; d != null; d = d.next) {
                d.marked = true;
                for (dir = 0; dir < 2; dir++) {
                    for (c = (dir == 1 ? d.left : d.right); c != null; c = c.next) {
                        insert_in_cms_table(c.string);
                    }
                }
            }
        }

        total_deleted = 0;
        change = 1;
        while (change > 0) {
            change = 0;
            N_deleted = 0;
            for (w = 0; w < length; w++) {
                for (d = word[w].d; d != null; d = d.next) {
                    if (!d.marked)
                        continue;
                    deleteme = false;
                    for (dir = 0; dir < 2; dir++) {
                        for (c = (dir == 1 ? d.left : d.right); c != null; c = c.next) {
                            for (i = 0; i < knowledge.n_contains_one_rules; i++) {

                                rule = knowledge.contains_one_rules[i]; /* the ith rule */
                                selector = rule.selector; /* selector string for this rule */
                                link_set = rule.link_set; /* the set of criterion links */

                                if (selector.indexOf('*') >= 0)
                                    continue; /* If it has a * forget it */

                                if (!Postprocessor.post_process_match(selector, c.string))
                                    continue;

                                /*
                                printf("pp_prune: trigger ok.  selector = %s  c.string = %s\n", selector, c.string);
                                */

                                /* We know c matches the trigger link of the rule. */
                                /* Now check the criterion links */

                                if (!rule_satisfiable(link_set)) {
                                    deleteme = true;
                                }
                                if (deleteme)
                                    break;
                            }
                            if (deleteme)
                                break;
                        }
                        if (deleteme)
                            break;
                    }

                    if (deleteme) { /* now we delete this disjunct */
                        N_deleted++;
                        total_deleted++;
                        d.marked = false; /* mark for deletion later */
                        for (dir = 0; dir < 2; dir++) {
                            for (c = ((dir == 1) ? (d.left) : (d.right)); c != null; c = c.next) {
                                change += delete_from_cms_table(c.string);
                            }
                        }
                    }
                }
            }

            if (opts.verbosity > 2) {
                opts.out.println("pp_prune pass deleted " + N_deleted);
            }

        }
        delete_unmarked_disjuncts();

        if (opts.verbosity > 2) {
            opts.out.println();
            opts.out.println("After pp_pruning:");
            print_disjunct_counts(opts);
        }

        opts.print_time("pp pruning");

        return total_deleted;
    }

    public void pp_and_power_prune(int mode, ParseOptions opts) {
        /* do the following pruning steps until nothing happens:
           power pp power pp power pp....
           Make sure you do them both at least once.
           */
        power_prune(mode, opts);
        for (;;) {
            if (pp_prune(opts) == 0)
                break;
            if (power_prune(mode, opts) == 0)
                break;
        }
    }

    public void delete_unmarked_disjuncts() {
        int w;
        Disjunct d_head, d, dx;

        for (w = 0; w < length; w++) {
            d_head = null;
            for (d = word[w].d; d != null; d = dx) {
                dx = d.next;
                if (d.marked) {
                    d.next = d_head;
                    d_head = d;
                } else {
                    d.next = null;
                }
            }
            word[w].d = d_head;
        }
    }

    /**
     * Step three of the sentence_parse operation - pruning
 <p>   
      The algorithms in this file prune disjuncts from the disjunct list
      of the sentence that can be elimininated by a simple checks.  The first
      check works as follows:
 <p>    
      A series of passes are made through the sentence, alternating
      left-to-right and right-to-left.  Consider the left-to-right pass (the
      other is symmetric).  A set S of connectors is maintained (initialized
      to be empty).  Now the disjuncts of the current word are processed.
      If a given disjunct's left pointing connectors have the property that
      at least one of them has no connector in S to which it can be matched,
      then that disjunct is deleted. Now the set S is augmented by the right
      connectors of the remaining disjuncts of that word.  This completes
      one word.  The process continues through the words from left to right.
      Alternate passes are made until no disjunct is deleted.
  <p>   
      It worries me a little that if there are some really huge disjuncts lists,
      then this process will probably do nothing.  (This fear turns out to be 
      unfounded.)
  <p>   
      Notes:  Power pruning will not work if applied before generating the
      "and" disjuncts.  This is because certain of it's tricks don't work.
      Think about this, and finish this note later....
      Also, currently I use the standard connector match procedure instead
      of the pruning one, since I know power pruning will not be used before 
      and generation.  Replace this to allow power pruning to work before
      generating and disjuncts.
   <p>  
      Currently it seems that normal pruning, power pruning, and generation,
      pruning, and power pruning (after "and" generation) and parsing take
      about the same amount of time.  This is why doing power pruning before
      "and" generation might be a very good idea.
  <p>   
      New idea:  Suppose all the disjuncts of a word have a connector of type
      c pointing to the right.  And further, suppose that there is exactly one
      word to its right containing that type of connector pointing to the left.
      Then all the other disjuncts on the latter word can be deleted.
      (This situation is created by the processing of "either...or", and by
      the extra disjuncts added to a "," neighboring a conjunction.)
   <p>  
     * see AndData()
    */

    public void clean_up(int w) {
        /* This removes the disjuncts that are empty from the list corresponding
           to word w of the sentence.
        */
        Disjunct head_disjunct = new Disjunct(), d, d1;

        d = head_disjunct;

        d.next = word[w].d;

        while (d.next != null) {
            if ((d.next.left == null) && (d.next.right == null)) {
                d1 = d.next;
                d.next = d1.next;
            } else {
                d = d.next;
            }
        }
        word[w].d = head_disjunct.next;
    }

    public int count_disjuncts_in_sentence() {
        /* returns the total number of disjuncts in the sentence */
        int w, count;
        count = 0;
        for (w = 0; w < length; w++) {
            count += Disjunct.count_disjuncts(word[w].d);
        }
        return count;
    }



    public static int power_cost;
    /* either GENTLE or RUTHLESS */
    /* obviates excessive paramater passing */
    public static int power_prune_mode;
    /* counts the number of changes
       of c.word fields in a pass */
    public static int N_changed;

    /* the sizes of the hash tables */
    public static int power_l_table_size[] = new int[GlobalBean.MAX_SENTENCE];
    public static int power_r_table_size[] = new int[GlobalBean.MAX_SENTENCE];

    /* the beginnings of the hash tables */
    public static CList power_l_table[][] = new CList[GlobalBean.MAX_SENTENCE][];
    public static CList power_r_table[][] = new CList[GlobalBean.MAX_SENTENCE][];

        /**
     *
 *   Here is what you've been waiting for: POWER-PRUNE
 * <p>  
 *   The kinds of constraints it checks for are the following:
 * <p>   
 *   1) successive connectors on the same disjunct have to go to
 *      nearer and nearer words.
 * <p>   
 *   2) two deep connectors cannot attach to eachother
 *      (A connectors is deep if it is not the first in its list, it
 *      is shallow if it is the first in its list, it is deepest if it
 *      is the last on its list.)
 * <p>   
 *   3) on two adjacent words, a pair of connectors can be used
 *      only if they're the deepest ones on their disjuncts
 * <p>   
 *   4) on two non-adjacent words, a pair of connectors can be used only
 *      if not [both of them are the deepest].
 * <p>   
 *   The data structure consists of a pair of hash tables on every word.
 *   Each bucket of a hash table has a list of pointers to connectors.
 *   These nodes also store if the chosen connector is shallow.
 * <p>   
 * <p>   
 *      As with normal pruning, we make alternate left.right and right.left
 *      passes.  In the R.L pass, when we're on a word w, we make use of
 *      all the left-pointing hash tables on the words to the right of w.
 *      After the pruning on this word, we build the left-pointing hash table
 *      this word.  This guarantees idempotence of the pass -- after doing an
 *      L.R, doing another would change nothing.
 * <p>   
 *      Each connector has an integer c_word field.  This refers to the closest
 *      word that it could be connected to.  These are initially determined by
 *      how deep the connector is.  For example, a deepest connector can connect
 *      to the neighboring word, so its c_word field is w+1 (w-1 if this is a left
 *      pointing connector).  It's neighboring shallow connector has a c_word
 *      value of w+2, etc.
 * <p>   
 *      The pruning process adjusts these c_word values as it goes along,
 *      accumulating information about any way of linking this sentence.
 *      The pruning process stops only after no disjunct is deleted and no
 *      c_word values change.
 *   
 * <p>
     * The difference between RUTHLESS and GENTLE power pruning is simply
 *      that GENTLE uses the deletable region array, and RUTHLESS does not.
 *      So we can get the effect of these two different methods simply by
 *      always unsuring that deletable[][] has been defined.  With nothing
 *      deletable, this is equivalent to RUTHLESS.   --DS, 7/97
    */
    public int power_prune(int mode, ParseOptions opts) {
        /* The return value is the number of disjuncts deleted */
        Disjunct d, free_later, dx, nd;
        Connector c;
        int w, N_deleted, total_deleted;

        power_prune_mode = mode;
        /* this global variable avoids lots of
                                         parameter passing */
        null_links = (opts.min_null_count > 0);

        init_power();
        power_cost = 0;
        free_later = null;
        N_changed = 1; /* forces it always to make at least two passes */
        N_deleted = 0;

        total_deleted = 0;

        while (true) {

            /* left-to-right pass */
            for (w = 0; w < length; w++) {
                for (d = word[w].d; d != null; d = d.next) {
                    if (d.left == null)
                        continue;
                    if (left_connector_list_update(d.left, w, w, true) < 0) {
                        for (c = d.left; c != null; c = c.next) {
                            c.word = GlobalBean.BAD_WORD;
                        }
                        for (c = d.right; c != null; c = c.next) {
                            c.word = GlobalBean.BAD_WORD;
                        }
                        N_deleted++;
                        total_deleted++;
                    }
                }

                clean_table(power_r_table_size[w], power_r_table[w]);
                nd = null;
                for (d = word[w].d; d != null; d = dx) {
                    dx = d.next;
                    if ((d.left != null) && (d.left.word == GlobalBean.BAD_WORD)) {
                        d.next = free_later;
                        free_later = d;
                    } else {
                        d.next = nd;
                        nd = d;
                    }
                }
                word[w].d = nd;
            }
            if (opts.verbosity > 2) {
                opts.out.println("l->r pass changed " + N_changed + " and deleted " + N_deleted);
            }

            if (N_changed == 0)
                break;

            N_changed = N_deleted = 0;
            /* right-to-left pass */

            for (w = length - 1; w >= 0; w--) {
                for (d = word[w].d; d != null; d = d.next) {
                    if (d.right == null)
                        continue;
                    if (right_connector_list_update(d.right, w, w, true) >= length) {
                        for (c = d.right; c != null; c = c.next)
                            c.word = GlobalBean.BAD_WORD;
                        for (c = d.left; c != null; c = c.next)
                            c.word = GlobalBean.BAD_WORD;
                        N_deleted++;
                        total_deleted++;
                    }
                }
                clean_table(power_l_table_size[w], power_l_table[w]);
                nd = null;
                for (d = word[w].d; d != null; d = dx) {
                    dx = d.next;
                    if ((d.right != null) && (d.right.word == GlobalBean.BAD_WORD)) {
                        d.next = free_later;
                        free_later = d;
                    } else {
                        d.next = nd;
                        nd = d;
                    }
                }
                word[w].d = nd;
            }

            if (opts.verbosity > 2) {
                opts.out.println("r->l pass changed " + N_changed + " and deleted " + N_deleted);
            }

            if (N_changed == 0)
                break;
            N_changed = N_deleted = 0;
        }

        if (opts.verbosity > 2)
            opts.out.println("" + power_cost + " power prune cost:");

        if (mode == GlobalBean.RUTHLESS) {
            opts.print_time("power pruned (ruthless)");
        } else {
            opts.print_time("power pruned (gentle)");
        }

        if (opts.verbosity > 2) {
            if (mode == GlobalBean.RUTHLESS) {
                opts.out.println("\nAfter power_pruning (ruthless):");
            } else {
                opts.out.println("\nAfter power_pruning (gentle):");
            }
            print_disjunct_counts(opts);
        }

        return total_deleted;
    }

    /**
     * This runs through all the connectors in this table, and eliminates those
           who are obsolete.  The word fields of an obsolete one has been set to
           BAD_WORD.
        */
    public void clean_table(int size, CList t[]) {
        
        int i;
        CList m, xm, head;
        for (i = 0; i < size; i++) {
            head = null;
            for (m = t[i]; m != null; m = xm) {
                xm = m.next;
                if (m.c.word != GlobalBean.BAD_WORD) {
                    m.next = head;
                    head = m;
                }
            }
            t[i] = head;
        }
    }
    /**
    * take this connector list, and try to match it with the words
           w-1, w-2, w-3...Returns the word to which the first connector of the
           list could possibly be matched.  If c is null, returns w.  If there
           is no way to match this list, it returns a negative number.
           If it does find a way to match it, it updates the c.word fields
           correctly.
        */
    public int left_connector_list_update(Connector c, int word_c, int w, boolean shallow) {
        
        int n;
        boolean foundmatch;

        if (c == null)
            return w;
        n = left_connector_list_update(c.next, word_c, w, false) - 1;
        if (c.word < n)
            n = c.word;

        /* n is now the rightmost word we need to check */
        foundmatch = false;
        for (;(n >= 0) && ((w - n) <= GlobalBean.MAX_SENTENCE); n--) {
            power_cost++;
            if (right_table_search(n, c, shallow, word_c)) {
                foundmatch = true;
                break;
            }
        }
        if (n < c.word) {
            c.word = n;
            N_changed++;
        }
        return (foundmatch ? n : -1);
    }

    /**
     * take this connector list, and try to match it with the words
           w+1, w+2, w+3...Returns the word to which the first connector of the
           list could possibly be matched.  If c is null, returns w.  If there
           is no way to match this list, it returns a number greater than N_words-1
           If it does find a way to match it, it updates the c.word fields
           correctly.
        */
    public int right_connector_list_update(Connector c, int word_c, int w, boolean shallow) {
        
        int n;
        boolean foundmatch;

        if (c == null)
            return w;
        n = right_connector_list_update(c.next, word_c, w, false) + 1;
        if (c.word > n)
            n = c.word;

        /* n is now the leftmost word we need to check */
        foundmatch = false;
        for (;(n < length) && ((n - w) <= GlobalBean.MAX_SENTENCE); n++) {
            power_cost++;
            if (left_table_search(n, c, shallow, word_c)) {
                foundmatch = true;
                break;
            }
        }
        if (n > c.word) {
            c.word = n;
            N_changed++;
        }
        return (foundmatch ? n : length);
    }

    public void prune(ParseOptions opts) {
        int N_deleted;
        Disjunct d;
        Connector e;
        int w;

        s_table_size = MyRandom.next_power_of_two_up(count_disjuncts_in_sentence());
        table = new Connector[s_table_size];
        /* You know, I don't think this makes much sense.  This is probably much  */
        /* too big.  There are many fewer connectors than disjuncts.              */

        zero_S();
        N_deleted = 1; /* a lie to make it always do at least 2 passes */
        // TODO: count_set_effective_distance();

        for (;;) {
            /* left-to-right pass */

            for (w = 0; w < length; w++) {
                for (d = word[w].d; d != null; d = d.next) {
                    for (e = d.left; e != null; e = e.next) {
                        if (!matches_S(e, '-'))
                            break;
                    }
                    if (e != null) {
                        /* we know this disjunct is dead */
                        N_deleted++;
                        d.left = d.right = null;
                    }
                }
                clean_up(w);
                for (d = word[w].d; d != null; d = d.next) {
                    for (e = d.right; e != null; e = e.next) {
                        insert_S(e);
                    }
                }
            }

            if (opts.verbosity > 2) {
                opts.out.println("l.r pass removed " + N_deleted);
                print_disjunct_counts(opts);
            }
            free_S();
            if (N_deleted == 0)
                break;

            /* right-to-left pass */
            N_deleted = 0;
            for (w = length - 1; w >= 0; w--) {
                for (d = word[w].d; d != null; d = d.next) {
                    for (e = d.right; e != null; e = e.next) {
                        if (!matches_S(e, '+'))
                            break;
                    }
                    if (e != null) {
                        /* we know this disjunct is dead */
                        N_deleted++;
                        d.left = d.right = null;
                    }
                }
                clean_up(w);
                for (d = word[w].d; d != null; d = d.next) {
                    for (e = d.left; e != null; e = e.next) {
                        insert_S(e);
                    }
                }
            }
            if (opts.verbosity > 2) {
                opts.out.println("r.l pass removed " + N_deleted);
                print_disjunct_counts(opts);
            }
            free_S();
            if (N_deleted == 0)
                break;
            N_deleted = 0;
        }
    }

    public int set_dist_fields(Connector c, int w, int delta) {
        int i;
        if (c == null)
            return w;
        i = set_dist_fields(c.next, w, delta) + delta;
        c.word = i;
        return i;
    }

    /**
     * this takes two connectors (and whether these are shallow or not)
           (and the two words that these came from) and returns true if it is
           possible for these two to match based on local considerations.
        */
    public boolean possible_connection(Connector lc, Connector rc, boolean lshallow, boolean rshallow, int lword, int rword) {
        
        if ((!lshallow) && (!rshallow))
            return false;
        /* two deep connectors can't work */
        if ((lc.word > rword) || (rc.word < lword))
            return false;
        /* word range constraints */

        /* Now, notice that the only differences between the following two
           cases is that (1) ruthless uses match and gentle uses prune_match.
           and (2) ruthless doesn't use deletable[][].  This latter fact is
           irrelevant, since deletable[][] is now guaranteed to have been
           created. */

        if (power_prune_mode == GlobalBean.RUTHLESS) {
            if (lword == rword - 1) {
                if (!((lc.next == null) && (rc.next == null)))
                    return false;
            } else {
                if ((!null_links) && (lc.next == null) && (rc.next == null) && (!lc.multi) && (!rc.multi)) {
                    return false;
                }
            }
            return Connector.match(this, lc, rc, lword, rword);
        } else {
            if (lword == rword - 1) {
                if (!((lc.next == null) && (rc.next == null)))
                    return false;
            } else {
                if ((!null_links)
                    && (lc.next == null)
                    && (rc.next == null)
                    && (!lc.multi)
                    && (!rc.multi)
                    && !deletable[lword + 1][rword]) {
                    return false;
                }
            }
            return Connector.prune_match(this, lc, rc, lword, rword);
        }
    }

    /* this returns true if the right table of word w contains
           a connector that can match to c.  shallow tells if c is shallow */
    public boolean right_table_search(int w, Connector c, boolean shallow, int word_c) {
        
        int size, h;
        CList cl;
        size = power_r_table_size[w];
        h = power_hash(c) & (size - 1);
        for (cl = power_r_table[w][h]; cl != null; cl = cl.next) {
            if (possible_connection(cl.c, c, cl.shallow, shallow, w, word_c)) {
                return true;
            }
        }
        return false;
    }

    /* this returns true if the right table of word w contains
           a connector that can match to c.  shallows tells if c is shallow
        */
    public boolean left_table_search(int w, Connector c, boolean shallow, int word_c) {
        
        int size, h;
        CList cl;
        size = power_l_table_size[w];
        h = power_hash(c) & (size - 1);
        for (cl = power_l_table[w][h]; cl != null; cl = cl.next) {
            if (possible_connection(c, cl.c, shallow, cl.shallow, word_c, w)) {
                return true;
            }
        }
        return false;
    }

    /**
     * allocates and builds the initial power hash tables 
     */
    public void init_power() {
        
        int w, len, size, i;
        CList t[];
        Disjunct d, xd, head;
        Connector c;

        /* first we initialize the word fields of the connectors, and
           eliminate those disjuncts with illegal connectors */
        for (w = 0; w < length; w++) {
            head = null;
            for (d = word[w].d; d != null; d = xd) {
                xd = d.next;
                if ((set_dist_fields(d.left, w, -1) < 0) || (set_dist_fields(d.right, w, 1) >= length)) {
                    d.next = null;
                } else {
                    d.next = head;
                    head = d;
                }
            }
            word[w].d = head;
        }

        for (w = 0; w < length; w++) {
            len = left_connector_count(word[w].d);
            size = MyRandom.next_power_of_two_up(len);
            power_l_table_size[w] = size;
            t = power_l_table[w] = new CList[size];
            for (i = 0; i < size; i++)
                t[i] = null;

            for (d = word[w].d; d != null; d = d.next) {
                c = d.left;
                if (c != null) {
                    put_into_power_table(size, t, c, true);
                    for (c = c.next; c != null; c = c.next) {
                        put_into_power_table(size, t, c, false);
                    }
                }
            }

            len = right_connector_count(word[w].d);
            size = MyRandom.next_power_of_two_up(len);
            power_r_table_size[w] = size;
            t = power_r_table[w] = new CList[size];
            for (i = 0; i < size; i++)
                t[i] = null;

            for (d = word[w].d; d != null; d = d.next) {
                c = d.right;
                if (c != null) {
                    put_into_power_table(size, t, c, true);
                    for (c = c.next; c != null; c = c.next) {
                        put_into_power_table(size, t, c, false);
                    }
                }
            }
        }
    }

    /**
     * returns the number of connectors in the left lists of the disjuncts. 
     */
    public int left_connector_count(Disjunct d) {
        
        Connector c;
        int i = 0;
        for (; d != null; d = d.next) {
            for (c = d.left; c != null; c = c.next)
                i++;
        }
        return i;
    }

    /**
     * returns the number of connectors in the right lists of the disjuncts. 
     */
    public int right_connector_count(Disjunct d) {
        Connector c;
        int i = 0;
        for (; d != null; d = d.next) {
            for (c = d.right; c != null; c = c.next)
                i++;
        }
        return i;
    }

    /**
     * This hash function only looks at the leading upper case letters of
           the connector string, and the label fields.  This ensures that if two
           strings match (formally), then they must hash to the same place.
           The answer must be masked to the appropriate table size.
        
           This is exactly the same hash function used in fast-match.
        */
    public int power_hash(Connector c) {
        
        String s;
        int i;
        i = MyRandom.randtable[c.label & (GlobalBean.RTSIZE - 1)];
        s = c.string;
        int j = 0;
        while (j < s.length() && Character.isUpperCase(s.charAt(j))) {
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
            j++;
        }
        return i;
    }

    /**
     * The disjunct d (whose left or right pointer points to c) is put
           into the appropriate hash table
        */
    public void put_into_power_table(int size, CList t[], Connector c, boolean shal) {
        
        int h;
        CList m;
        h = power_hash(c) & (size - 1);
        m = new CList();
        m.next = t[h];
        t[h] = m;
        m.c = c;
        m.shallow = shal;
    }

    /*
       PP Pruning
    
       The "contains one" post-processing constraints give us a new way to
       prune.  Suppose there's a rule that says "a group that contains foo
       must contain a bar or a baz."  Here foo, bar, and baz are connector
       types.  foo is the trigger link, bar and baz are called the criterion
       links.  If, after considering the disjuncts we find that there is is
       a foo, but neither a bar, nor a baz, then we can eliminte the disjuct
       containing bar.
    
       Things are actually a bit more complex, because of the matching rules
       and subscripts.  The problem is that post-processing deals with link
       names, while at this point all we have to work with is connector
       names.  Consider the foo part.  Consider a connector C.  When does
       foo match C for our purposes?  It matches it if every possible link
       name L (that can result from C being at one end of that link) results
       in post_process_match(foo,L) being true.  Suppose foo contains a "*".
       Then there is no C that has this property.  This is because the *s in
       C may be replaced by some other subscripts in the construction of L.
       And the non-* chars in L will not post_process_match the * in foo.
    
       So let's assume that foo has no *.  Now the result we want is simply
       given by post_process_match(foo, C).  Proof: L is the same as C but
       with some *s replaced by some other letters.  Since foo contains no *
       the replacement in C of some * by some other letter could change
       post_process_match from false to true, but not vice versa.  Therefore
       it's conservative to use this test.
    
       For the criterion parts, we need to determine if there is a
       collection of connectors C1, C2,... such that by combining them you
       can get a link name L that post_process_matches bar or baz.  Here's a
       way to do this.  Say bar="Xabc".  Then we see if there are connector
       names that post_process_match "Xa##", "X#b#", and "X##c".  They must
       all be there in order for it to be possible to create a link name
       "Xabc".  A "*" in the criterion part is a little different.  In this
       case we can simply skip the * (treat it like an upper case letter)
       for this purpose.  So if bar="X*ab" then we look for "X*#b" and
       "X*a#".  (The * in this case could be treated the same as another
       subscript without breaking it.)  Note also that it's only necessary
       to find a way to match one of the many criterion links that may be in
       the rule.  If none can match, then we can delete the disjunct
       containing C.
    
       Here's how we're going to implement this.  We'll maintain a multiset
       of connector names.  We'll represent them in a hash table, where the
       hash function uses only the upper case letters of the connector name.
       We'll insert all the connectors into the multiset.  The multiset will
       support the operation of deletion (probably simplest to just
       decrement the count).  Here's the algorithm.
    
       Insert all the connectors into M.
    
       While the previous pass caused a count to go to 0 do:
          For each connector C do
             For each rule R do
                if C is a trigger for R and the criterion links
            of the rule cannot be satisfied by the connectors in
            M, Then:
                   We delete C's disjunct.  But before we do, 
                   we remove all the connectors of this disjunct
               from the multiset.  Keep tabs on whether or not
               any of the counts went to 0.
    
    
    
        Efficiency hacks to be added later:
            Note for a given rule can become less and less satisfiable.
            That is, rule_satisfiable(r) for a given rule r can change from
            true to false, but not vice versa.  So once it's false, we can just
        remember that.
    
            Consider the effect of a pass p on the set of rules that are
        satisfiable.  Suppose this set does not change.  Then pass p+1
        will do nothing.  This is true even if pass p caused some
        disjuncts to be deleted.  (This observation will only obviate
        the need for the last pass.)
    
      */

    public final static int CMS_SIZE = (2 << 10);
    public static Cms cms_table[] = new Cms[CMS_SIZE];

    public void init_cms_table() {
        int i;
        for (i = 0; i < CMS_SIZE; i++) {
            cms_table[i] = null;
        }
    }

    public int cms_hash(String s) {
        int i = 0;
        int j = 0;
        while (j < s.length() && Character.isUpperCase(s.charAt(j))) {
            i = i + (i << 1) + MyRandom.randtable[(s.charAt(j) + i) & (GlobalBean.RTSIZE - 1)];
            j++;
        }
        return (i & (CMS_SIZE - 1));
    }

    public boolean match_in_cms_table(String pp_match_name) {
        /* This returns true if there is a connector name C in the table
           such that post_process_match(pp_match_name, C) is true */
        Cms cms;
        for (cms = cms_table[cms_hash(pp_match_name)]; cms != null; cms = cms.next) {
            if (Postprocessor.post_process_match(pp_match_name, cms.name))
                return true;
        }
        return false;
    }

    public Cms lookup_in_cms_table(String str) {
        Cms cms;
        for (cms = cms_table[cms_hash(str)]; cms != null; cms = cms.next) {
            if (str.equals(cms.name))
                return cms;
        }
        return null;
    }

    /*  This is not used currently */
    /*
    int is_in_cms_table(String  str) {
        Cms * cms = lookup_in_cms_table(str);
        return (cms != null && cms.count > 0);
    }
    */

    public void insert_in_cms_table(String str) {
        Cms cms;
        int h;
        cms = lookup_in_cms_table(str);
        if (cms != null) {
            cms.count++;
        } else {
            cms = new Cms();
            cms.name = str;
            /* don't copy the string...just keep a pointer to it.
                               we won't free these later */
            cms.count = 1;
            h = cms_hash(str);
            cms.next = cms_table[h];
            cms_table[h] = cms;
        }
    }

    public int delete_from_cms_table(String str) {
        /* Delete the given string from the table.  Return true if
           this caused a count to go to 0, return false otherwise */
        Cms cms;
        cms = lookup_in_cms_table(str);
        if (cms != null && cms.count > 0) {
            cms.count--;
            return (cms.count == 0) ? 1 : 0;
        }
        return 0;
    }

    public boolean rule_satisfiable(PPLinkset ls) {
        int hashval;
        StringBuffer name = new StringBuffer();
        int s, t;
        PPLinksetNode p;
        int bad, n_subscripts;
        for (hashval = 0; hashval < ls.hash_table_size; hashval++) {
            for (p = ls.hash_table[hashval]; p != null; p = p.next) {

                /* ok, we've got our hands on one of the criterion links */
                name.setLength(0);
                name.append(p.str);
                /* now we want to see if we can satisfy this criterion link */
                /* with a collection of the links in the cms table */

                for (s = 0; s < name.length() && Character.isUpperCase(name.charAt(s)); s++);
                for (; s < name.length(); s++)
                    if (name.charAt(s) != '*')
                        name.setCharAt(s, '#');
                t = 0;
                for (s = 0; s < name.length() && Character.isUpperCase(name.charAt(s)); s++) {
                    t++;
                }

                /* s and t remain in lockstep */
                bad = 0;
                n_subscripts = 0;
                for (; s < name.length() && bad == 0; s++, t++) {
                    if (name.charAt(s) == '*')
                        continue;
                    n_subscripts++;
                    /* after the upper case part, and is not a * so must be a regular subscript */
                    name.setCharAt(s, p.str.charAt(t));
                    if (!match_in_cms_table(name.toString()))
                        bad++;
                    name.setCharAt(s, '#');
                }

                if (n_subscripts == 0) {
                    /* now we handle the special case which occurs if there
                       were 0 subscripts */
                    if (!match_in_cms_table(name.toString()))
                        bad++;
                }

                /* now if bad==0 this criterion link does the job
                   to satisfy the needs of the trigger link */

                if (bad == 0)
                    return true;
            }
        }
        return false;
    }

}
