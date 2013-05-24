package net.sf.jlinkgrammar;
/*
 * Parser.java
 *
 * Created on October 20, 2006, 3:02 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import net.sf.jlinkgrammar.Dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.PrintStream;

import com.soartech.bolt.LGSupport;


/**
 * This class is meant to be a bean type interface to link grammar.  All the
 * options are preset and specific actions have to be taken to override them.
 * @author johnryan
 */
public class parser {
    
        private static Dictionary dict;
        private static Sentence sent;
        private static String dictionary_file = null;
        private static String post_process_knowledge_file = null;
        private static String constituent_knowledge_file = null;
        private static String affix_file = null;
        private static boolean pp_on = true;
        private static boolean af_on = true;
        private static boolean cons_on = true;
        private static int num_linkages;
        private static StringBuffer input_string = new StringBuffer();
        private static int label = GlobalBean.NOT_LABEL;
        private static ParseOptions opts;
//        private static String defaultDataDir = "/home/liferay/linkgrammar/data";
    
    /**
     * Creates a new instance of Parser 
     */
    public parser() {
        String[] args = new String[1];
        
        args[0] = new String("parseit");
        InitializeVars(args);

    }
    
    public static void InitializeVars(String arg[]) {
        
        int i = 0;
        if (arg.length > 1 && (arg[0].charAt(0) != '-')) {
            /* the dictionary is the first argument if it doesn't begin with "-" */
            dictionary_file = arg[0];
            i++;
        }
        opts = new ParseOptions();
        GlobalBean.opts = opts;
        // opts = new ParseOptions();

        opts.parse_options_set_max_sentence_length(70);
        opts.parse_options_set_linkage_limit(1000);
        opts.parse_options_set_short_length(10);

        for (; i < arg.length; i++) {
            if (arg[i].charAt(0) == '-') {
                if (arg[i].equals("-pp")) {
                    if ((post_process_knowledge_file != null) || (i + 1 == arg.length))
                        print_usage(arg[0]);
                    post_process_knowledge_file = arg[i + 1];
                    i++;
                } else if (arg[i].equals("-c")) {
                    if ((constituent_knowledge_file != null) || (i + 1 == arg.length))
                        print_usage(arg[0]);
                    constituent_knowledge_file = arg[i + 1];
                    i++;
                } else if (arg[i].equals("-a")) {
                    if ((affix_file != null) || (i + 1 == arg.length))
                        print_usage(arg[0]);
                    affix_file = arg[i + 1];
                    i++;
                } else if (arg[i].equals("-ppoff")) {
                    pp_on = false;
                } else if (arg[i].equals("-coff")) {
                    cons_on = false;
                } else if (arg[i].equals("-aoff")) {
                    af_on = false;
                } else if (arg[i].equals("-batch")) {
                    if ((opts.input != System.in) || (i + 1 == arg.length))
                        print_usage(arg[0]);
                        try {
                           opts.input = new FileInputStream(arg[i + 1]);                            
                        } catch (IOException ex ) {
                            // TODO - Do something
                        }
                    i++;
                    
                } else if (arg[i].equals("-out")) {
                    if ((opts.out != System.out) || (i + 1 == arg.length))
                        print_usage(arg[0]);
                        try {
                            opts.out = new PrintStream(new FileOutputStream(arg[i + 1]));
                        } catch (IOException ex ) {
                            // TODO - Do something
                        }
                    i++;
                } else if (arg[i].charAt(1) == '!') {
                } else {
                    print_usage(arg[0]);
                }
            } else {
                // TODO - print_usage(arg[0]);
            }
        }

        
    	String lgpDataDir = LGSupport.dictionaryPath;
    	
        if (!pp_on && post_process_knowledge_file != null)
            print_usage(arg[0]);

        if (dictionary_file == null) {
             dictionary_file = lgpDataDir + "/4.0.dict";
        }

        if (af_on && affix_file == null) {
             affix_file = lgpDataDir + "/4.0.affix";
        }

        if (pp_on && post_process_knowledge_file == null) {
             post_process_knowledge_file = lgpDataDir + "/4.0.knowledge";
        }

        if (cons_on && constituent_knowledge_file == null) {
             constituent_knowledge_file = lgpDataDir + "/4.0.constituent-knowledge";
        }

        try { dict =
            new Dictionary(opts, dictionary_file, post_process_knowledge_file, 
                constituent_knowledge_file, affix_file);
        } catch (IOException ex ) {
                            // TODO - Do something
        	System.err.println("Failure to open dictionary!");
        }
    	
        /* process the command line like commands */
        for (i = 1; i < arg.length; i++) {
            if (!arg[i].equals("-pp") && !arg[i].equals("-c") && !arg[i].equals("-a")) {
                i++;
            } else if (
                arg[i].charAt(0) == '-'
                    && !arg[i].equals("-ppoff")
                    && !arg[i].equals("-coff")
                    && !arg[i].equals("-aoff")) {
                opts.issue_special_command(arg[i].substring(1), dict);
            }
        }
    }

    public void parseSentence(String sentence) {
    	opts.parse_options_set_verbosity(1);
        
        sent = new Sentence(sentence, dict, opts);
        
        // First parse with cost 0 or 1 and no null links            
        opts.parse_options_set_disjunct_cost(2);
        opts.parse_options_set_min_null_count(0);
        opts.parse_options_set_max_null_count(0);
        opts.parse_options_reset_resources();
                    
        num_linkages = sent.sentence_parse(opts);
        if ( num_linkages == 0) { 
            // O.K. we have a null link (i.e. word without a link)
            // so allow one and try again
            opts.parse_options_set_min_null_count(1);
            opts.parse_options_set_max_null_count(sent.sentence_length());
            num_linkages = sent.sentence_parse(opts);
        }
        
        
        for (int i=0; i<num_linkages; i++) {
        	Linkage myLinkage = new Linkage(i, sent, opts);
        
        	LGSupport.loadLinkage(myLinkage, i, sent);
        }
        return;
    }
    
    
    /**
     * Instead of printing a link diagram print an XML tree
     *
     * @param sent  the sentence to print.
     */
    public void printWordsLabelsAndLinks(Sentence sent) {
        CNode root, current, next, previous;
        Linkage linkage;
        int numLinkages;
        int num_to_query;
        int i;
        
        if (sent.sentence_num_linkages_found() > 0) {
            // We have to walk all the linakges throwing away the bad ones.
            num_to_query = Math.min(sent.sentence_num_linkages_post_processed(), 1000);
            
            
            for (i = 0; i < num_to_query; ++i) {
                
                if ((sent.sentence_num_violations(i) > 0) && (!opts.parse_options_get_display_bad())) {
                    continue;
                }
                
                // O.K. we have our fisrt valid linkage.  Do we want to print them all? No just one.
                // TODO - optimize this somehow
                linkage = new Linkage(i, sent, opts);
                // linkage = new Linkage(0, sent, opts);
                int j, mode, first_sublinkage;
                
                // In effect we are saying display sublinkages
                linkage.linkage_compute_union();
                numLinkages = linkage.linkage_get_num_sublinkages();
                first_sublinkage = numLinkages - 1;
                
                
                for (j = first_sublinkage; j < numLinkages; ++j) {
                    linkage.linkage_set_current_sublinkage(j);
                    root = linkage.linkage_constituent_tree();
                    // Now we can walk the linkage and print the structure
                    current = root;
                    int w = 0;
                    do {
                        opts.out.println(linkage.word[w++].toString());
                        displayCNode(current);
                    } while (current.next != null);
                }
                
            }
        }
    }
           
    public void displayCNode(CNode n) {
        opts.out.println(n.toString());
    }


    static void print_usage(String str) {
        System.err.println(
            "Usage: <class name> [dict_file] [-pp PPKnowledge_file]\n"
                + "          [-c constituent_knowledge_file] [-a affix_file]\n"
                + "          [-ppoff] [-coff] [-aoff] [-batch] [-<special \"!\" command>]");
        System.exit(1);
    }
}    
