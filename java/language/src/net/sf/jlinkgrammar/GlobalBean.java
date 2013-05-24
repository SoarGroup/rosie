package net.sf.jlinkgrammar;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * This is a hacked construct.  This bean holds several constructs and  variables
 * needed across the entire link grammar program.  This should be a parent and all
 * link grammar objects should derive from it.  That way we better encapsulate the 
 * variables.
 */
public class GlobalBean {
    /**
     * This is a hack that allows one to discard disjuncts containing
     * connectors whose cost is greater than given a bound. This number plus
     * the cost of any connectors on a disjunct must remain negative, and
     * this number multiplied times the number of costly connectors on any
     * disjunct must fit into an integer.
     */
    public final static int NEGATIVECOST = -1000000;
    /**
     * no connector will have cost this high 
     */
    public final static int NOCUTOFF = 1000; 

    /**
     * the string to use to show the wall 
     */
    public final static String LEFT_WALL_DISPLAY = "LEFT-WALL";

    /**
     * If this connector is used on the wall,
     * then suppress the display of the wall
     * bogus name to prevent ever suppressing
     */
    public final static String LEFT_WALL_SUPPRESS = "Wd";

    /**
     * the string to use to show the wall 
     */
    public final static String RIGHT_WALL_DISPLAY = "RIGHT-WALL";

    /**
     * Supress if this connector is used on the wall
     */
    final static String RIGHT_WALL_SUPPRESS = "RW";

    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String LEFT_WALL_WORD = "LEFT-WALL";
    
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String RIGHT_WALL_WORD = "RIGHT-WALL";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String POSTPROCESS_WORD = "POSTPROCESS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String ANDABLE_CONNECTORS_WORD = "ANDABLE-CONNECTORS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String UNLIMITED_CONNECTORS_WORD = "UNLIMITED-CONNECTORS";

    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String PROPER_WORD = "CAPITALIZED-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String PL_PROPER_WORD = "PL-CAPITALIZED-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String HYPHENATED_WORD = "HYPHENATED-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String NUMBER_WORD = "NUMBERS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String ING_WORD = "ING-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String S_WORD = "S-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String ED_WORD = "ED-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String LY_WORD = "LY-WORDS";
    
    /**
     * Defins the name of a special string in the dictionary.
     */
    public final static String UNKNOWN_WORD = "UNKNOWN-WORD";

    /**
     * file names (including paths)
     * should not be longer than this
     */
    public final static int MAX_PATH_NAME = 400;

    /**
     * Some size definitions.  Reduce these for small machines - Left over from C not needed in Java
     */
    /**
     * maximum number of chars in a word
     */
    public final static int MAX_WORD = 60;
    /**
     * maximum number of chars in a sentence
     */
    public final static int MAX_LINE = 1500;
     /**
      * maximum number of words in a sentence
      */
    public final static int MAX_SENTENCE = 250;

    /**
     * This is the maximum number of links allowed.
     *
     * It cannot be more than 254, because I use word MAX_SENTENCE+1 to
     * indicate that nothing can connect to this connector, and this
     * should fit in one byte (if the word field of a connector is an
     * (unsigned char) 
     */
    public final static int MAX_LINKS = (2 * MAX_SENTENCE - 3);
    /**
     * maximum number of chars in a token
     */
    public final static int MAX_TOKEN_LENGTH = 50;
    /**
     * Max disjunct cost to allow
     */
    public final static int MAX_DISJUNCT_COST = 10000;

    public final static int DOWN_priority = 2;
    public final static int UP_priority = 1;
    public final static int THIN_priority = 0;

    /** the labels >= 0 are used by fat links while -1 is used for normal connectors  */ 
    public final static int NORMAL_LABEL = (-1); 

    public final static int UNLIMITED_LEN = 255;
    public final static int SHORT_LEN = 6;
    public final static int NO_WORD = 255;

    /* Here are the types */
    public final static int OR_type = 0;
    public final static int AND_type = 1;
    public final static int CONNECTOR_type = 2;

    /** These parameters tell power_pruning, to tell whether this is before or after
       generating and disjuncts.  GENTLE is before RUTHLESS is after. */
    public final static int GENTLE = 1;
    public final static int RUTHLESS = 0;

    public static final int PP_LEXER_MAX_LABELS = 512;
    /** CostModel sort by Violations, Disjunct cost, And cost, Link cost */
    public static final int VDAL = 1; 

    public final static int HT_SIZE = (1 << 10);

    /** size of random table for computing the
       hash functions.  must be a power of 2 */
    public final static int RTSIZE = 256;

    public final static int NODICT = 1;
    public final static int DICTPARSE = 2;
    public final static int WORDFILE = 3;
    public final static int SEPARATE = 4;
    public final static int NOTINDICT = 5;
    public final static int BUILDEXPR = 6;
    public final static int INTERNALERROR = 7;

    public final static int LINKSET_SPARSENESS = 2;
    public final static int LINKSET_MAX_SETS = 512;
    public final static int LINKSET_DEFAULT_SEED = 37;

    public final static int PP_FIRST_PASS = 1;
    public final static int PP_SECOND_PASS = 2;

    public final static int MAXINPUT = 1024;
    public final static int DISPLAY_MAX = 1024;
    /** input lines beginning with this are ignored */
    public final static char COMMENT_CHAR = '%'; 

    public final static char UNGRAMMATICAL = '*';
    public final static char PARSE_WITH_DISJUNCT_COST_GT_0 = ':';
    public final static char NO_LABEL = ' ';

    /** the indiction in a word field that this connector cannot
       be used -- is obsolete.
    */
    public final static int BAD_WORD = (MAX_SENTENCE + 1);

    public final static int PP_MAX_DOMAINS = 128;

    //final static int LINKSET_SPARSENESS=2;
    public final static int LINKSET_SEED_VALUE = 37;
    /** just needs to be approximate */
    public final static int PP_MAX_UNIQUE_LINK_NAMES = 1024; 

    public final static int LINE_LIMIT = 70;

    public static final String DEFAULTPATH = ".:./data:./data/link:/home/liferay/linkgrammar:/home/liferay/linkgrammar/data:/home/liferay/linkgrammar/data/link";

    public final static int MAX_STRIP = 10;

    public final static int MAX_HEIGHT = 30;
    /** to hook the comma to the following "and" */
    public final static int COMMA_LABEL = (-2);
    /** to connect the "either" to the following "or" */
    public final static int EITHER_LABEL = (-3);
    /** to connect the "neither" to the following "nor"*/
    public final static int NEITHER_LABEL = (-4);
    /** to connect the "not" to the following "but"*/
    public final static int NOT_LABEL = (-5);
    /** to connect the "not" to the following "only"*/
    public final static int NOTONLY_LABEL = (-6);
    /** to connect the "both" to the following "and"*/
    public final static int BOTH_LABEL = (-7); 

    public final static int MAXCONSTITUENTS = 1024;
    public final static int MAXSUBL = 16;
    public final static char OPEN_BRACKET = '[';
    public final static char CLOSE_BRACKET = ']';

    public final static int CType_OPEN = 0;
    public final static int CType_CLOSE = 1;
    public final static int CType_WORD = 2;

    public final static int WType_NONE = 0;
    public final static int WType_STYPE = 1;
    public final static int WType_PTYPE = 2;
    public final static int WType_QTYPE = 3;
    public final static int WType_QDTYPE = 4;

    public static int batch_errors = 0;
    public static boolean input_pending = false;
    public static int input_char;
    public static ParseOptions opts;

    public static int lperrno;
    public static String lperrmsg;
    /** keeping statistics */
    public static int STAT_N_disjuncts; 
    public static int STAT_calls_to_equality_test;


    /** Prints s then prints the last |t|-|s| characters of t.
           if s is longer than t, it truncates s.
        */
    public static void left_append_string(StringBuffer string, String s, String t) {
        
        int i, j, k;
        j = t.length();
        k = s.length();
        for (i = 0; i < j; i++) {
            if (i < k) {
                string.append(s.charAt(i));
            } else {
                string.append(t.charAt(i));
            }
        }
    }

    public static int strip_off_label(StringBuffer input_string) {
        int c;

        c = input_string.charAt(0);
        switch (c) {
            case UNGRAMMATICAL :
            case PARSE_WITH_DISJUNCT_COST_GT_0 :
                input_string.setCharAt(0, ' ');
                return c;
            default :
                return NO_LABEL;
        }
    }

    public static boolean special_command(StringBuffer input_string, Dictionary dict) {

        if (input_string.charAt(0) == '\n')
            return true;
        if (input_string.charAt(0) == COMMENT_CHAR)
            return true;
        if (input_string.charAt(0) == '!') {
            opts.issue_special_command(input_string.substring(1), dict);
            return true;
        }
        return false;
    }

    public static void batch_process_some_linkages(int label, Sentence sent, ParseOptions opts) {
        Linkage linkage;

        if (there_was_an_error(label, sent, opts) != 0) {
            if (sent.sentence_num_linkages_found() > 0) {
                linkage = new Linkage(0, sent, opts);
                linkage.process_linkage(opts);
            }
            opts.out.println("+++++ error " + batch_errors);
        }
    }

    public static int there_was_an_error(int label, Sentence sent, ParseOptions opts) {

        if (sent.sentence_num_valid_linkages() > 0) {
            if (label == UNGRAMMATICAL) {
                opts.out.println("error: parsed ungrammatical sentence");
                batch_errors++;
                return UNGRAMMATICAL;
            }
            if ((sent.sentence_disjunct_cost(0) == 0) && (label == PARSE_WITH_DISJUNCT_COST_GT_0)) {
                opts.out.println("error: cost=0");
                batch_errors++;
                return PARSE_WITH_DISJUNCT_COST_GT_0;
            }
        } else {
            if (label != UNGRAMMATICAL) {
                opts.out.println("error: failed");
                batch_errors++;
                return UNGRAMMATICAL;
            }
        }
        return 0;
    }

    public static void process_some_linkages(Sentence sent, ParseOptions opts) throws IOException {
        int i, c, num_displayed, num_to_query;
        Linkage linkage;

        if (opts.verbosity > 0)
            sent.print_parse_statistics(opts);
        if (!opts.parse_options_get_display_bad()) {
            num_to_query = Math.min(sent.sentence_num_valid_linkages(), DISPLAY_MAX);
        } else {
            num_to_query = Math.min(sent.sentence_num_linkages_post_processed(), DISPLAY_MAX);
        }

        for (i = 0, num_displayed = 0; i < num_to_query; ++i) {

            if ((sent.sentence_num_violations(i) > 0) && (!opts.parse_options_get_display_bad())) {
                continue;
            }

            linkage = new Linkage(i, sent, opts);  

            if (opts.verbosity > 0) {
                if (sent.sentence_num_valid_linkages() == 1 && (!opts.parse_options_get_display_bad())) {
                    opts.out.print("  Unique linkage, ");
                } else if ((opts.parse_options_get_display_bad()) && (sent.sentence_num_violations(i) > 0)) {
                    opts.out.print("  Linkage " + (i + 1) + " (bad), ");
                } else {
                    opts.out.print("  Linkage " + (i + 1) + ", ");
                }

                if (!linkage.linkage_is_canonical()) {
                    opts.out.print("non-canonical, ");
                }
                if (linkage.linkage_is_improper()) {
                    opts.out.print("improper fat linkage, ");
                }
                if (linkage.linkage_has_inconsistent_domains()) {
                    opts.out.print("inconsistent domains, ");
                }
                opts.out.println(
                    "cost vector = (UNUSED="
                        + linkage.linkage_unused_word_cost()
                        + " DIS="
                        + linkage.linkage_disjunct_cost()
                        + " AND="
                        + linkage.linkage_and_cost()
                        + " LEN="
                        + linkage.linkage_link_cost()
                        + ")");
            }

            linkage.process_linkage(opts);

            if (++num_displayed < num_to_query) {
                if (opts.verbosity > 0) {
                    opts.out.println("Press RETURN for the next linkage.");
                }
                c = fget_input_char(System.in, opts);
                if (c != '\n' && c != '\r') {
                    input_char = c;
                    input_pending = true;
                    break;
                }
            }
        }
    }

    public static int fget_input_char(InputStream in, ParseOptions opts) throws IOException {
        if (!opts.parse_options_get_batch_mode() && (opts.verbosity > 0))
            opts.out.print("linkparser> ");
        opts.out.flush();
        return in.read();
    }

    public static boolean fget_input_string(StringBuffer input_string, InputStream in, PrintStream out, ParseOptions opts)
        throws IOException {
        int c;
        input_string.setLength(0);
        if (input_pending) {
            input_pending = false;
            c = input_char;
        } else {
            if (!opts.parse_options_get_batch_mode() && opts.verbosity > 0)
                out.println("linkparser> ");
            out.flush();
            c = in.read();
        }
        while (c != '\n') {
            if (c < 0) {
                return false;
            }
            input_string.append((char)c);
            c = in.read();
        }
        return true;
    }

}
