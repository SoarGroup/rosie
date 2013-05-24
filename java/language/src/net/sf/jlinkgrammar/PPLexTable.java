package net.sf.jlinkgrammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * TODO add javadoc
 *
 */
public class PPLexTable {
    /** array of labels (null-terminated) */
    public String labels[] = new String[GlobalBean.PP_LEXER_MAX_LABELS];
    /** str. for each label */
    public PPLabelNode nodes_of_label[] = new PPLabelNode[GlobalBean.PP_LEXER_MAX_LABELS];
    /** efficiency */
    public PPLabelNode last_node_of_label[] = new PPLabelNode[GlobalBean.PP_LEXER_MAX_LABELS];
    /** state: current node of label */
    public PPLabelNode current_node_of_active_label;
    /** read state: current label */
    public int idx_of_active_label; 

    private int yylineno;

    PPLexTable() {
        for (int i = 0; i < GlobalBean.PP_LEXER_MAX_LABELS; i++) {
            nodes_of_label[i] = null;
            last_node_of_label[i] = null;
            labels[i] = null;
        }
    }
    /**
     * post processor set lexer state to first node of this label
     * @param label 
     * @return false if get_index_of_label returns -1
     */
    public boolean pp_lexer_set_label(String label) {
        
        idx_of_active_label = get_index_of_label(label);
        if (idx_of_active_label == -1)
            return false; /* label not found */
        current_node_of_active_label = nodes_of_label[idx_of_active_label];
        return true;
    }

    private int get_index_of_label(String label) {
        int i;
        for (i = 0; labels[i] != null; i++)
            if (labels[i].equals(label))
                return i;
        return -1;
    }
    
    /**
     * counts all tokens, even the commas 
     */
    public int pp_lexer_count_tokens_of_label() {
        
        int n;
        PPLabelNode p;
        if (idx_of_active_label == -1)
            throw new RuntimeException("pp_lexer: current label is invalid");
        for (n = 0, p = nodes_of_label[idx_of_active_label]; p != null; p = p.next, n++);
        return n;
    }

    /**
     * retrieves next token of set label, or null if list exhausted 
     */
    public String pp_lexer_get_next_token_of_label() {
        
        String p;
        if (current_node_of_active_label == null)
            return null;
        p = current_node_of_active_label.str;
        current_node_of_active_label = current_node_of_active_label.next;
        return p;
    }

    /**
     * 
     * @param label 
     */
    public void set_label(String label) {
        int i;
        char c;
        /* have we seen this label already? If so, abort */
        for (i = 0; labels[i] != null && !labels[i].equals(label); i++);
        if (labels[i] != null)
            throw new RuntimeException("pp_lexer: label " + label + " multiply defined!");

        /* new label. Store it */
        if (i == GlobalBean.PP_LEXER_MAX_LABELS - 1)
            throw new RuntimeException("pp_lexer: too many labels. Raise PP_LEXER_MAX_LABELS");
        labels[i] = label;
        idx_of_active_label = i;

    }

    /**
     * add the single string str to the set of strings associated with label 
     */
    public void add_string_to_label(String str) {
        
        PPLabelNode new_node;

        if (idx_of_active_label == -1)
            throw new RuntimeException("pp_lexer: invalid syntax (line " + yylineno + ")");

        if (str.length() > 0 && str.charAt(0) == '@') {
            add_set_of_strings_to_label(str.substring(1));
            return;
        }
        /* make sure string is legal */
        check_string(str);

        /* create a new node in (as yet to be determined) linked list of strings */
        new_node = new PPLabelNode();
        new_node.str = str;
        new_node.next = null;

        /* stick newly-created node at the *end* of the appropriate linked list */
        if (last_node_of_label[idx_of_active_label] == null) {
            /* first entry on linked list */
            nodes_of_label[idx_of_active_label] = new_node;
            last_node_of_label[idx_of_active_label] = new_node;
        } else {
            /* non-first entry on linked list */
            last_node_of_label[idx_of_active_label].next = new_node;
            last_node_of_label[idx_of_active_label] = new_node;
        }
    }

    /**
     * add the set of strings, defined earlier by label_of_set, to the set of 
     *      strings associated with the current label 
     */
    public void add_set_of_strings_to_label(String label_of_set) {
        
        PPLabelNode p;
        int idx_of_label_of_set;
        if (idx_of_active_label == -1)
            throw new RuntimeException("pp_lexer: invalid syntax (line " + yylineno + ")");
        if ((idx_of_label_of_set = get_index_of_label(label_of_set)) == -1)
            throw new RuntimeException(
                "pp_lexer: label "
                    + label_of_set
                    + " must be defined before it's referred to (line "
                    + yylineno
                    + ")");
        for (p = nodes_of_label[idx_of_label_of_set]; p != null; p = p.next)
            add_string_to_label(p.str);
    }

    static void check_string(String str) {
        if (str.length() > 1 && str.indexOf(',') >= 0)
            throw new RuntimeException("pp_lexer: string " + str + " contains a comma, which is a no-no.");
    }

    /**
     * all tokens until next comma, null-terminated 
     */
    public String[] pp_lexer_get_next_group_of_tokens_of_label() {
        
        int n;
        String tokens[];
        PPLabelNode p;

        p = current_node_of_active_label;
        for (n = 0; p != null && !p.str.equals(","); n++, p = p.next);
        tokens = new String[n];
        p = current_node_of_active_label;
        for (n = 0; p != null && !p.str.equals(","); n++, p = p.next)
            tokens[n] = p.str;
        /* advance "current node of label" state */
        current_node_of_active_label = p;
        if (p != null)
            current_node_of_active_label = p.next;
        return tokens;
    }

    /**
     * 
     * @return 
     */
    public int pp_lexer_count_commas_of_label() {
        int n;
        PPLabelNode p;
        if (idx_of_active_label == -1)
            throw new RuntimeException("pp_lexer: current label is invalid");
        for (n = 0, p = nodes_of_label[idx_of_active_label]; p != null; p = p.next)
            if (p.str.equals(","))
                n++;
        return n;
    }

    /**
     * Reads a knowledge object into a table for use by other methods.
     * Called by PPKnowledge.
     * @param ppknowledge 
     * @param f 
     * @throws java.io.IOException 
     */
    public void yylex(PPKnowledge ppknowledge, Reader f) throws IOException {
        BufferedReader r = new BufferedReader(f);
        String line = r.readLine();
        yylineno = 0;
        StringBuffer token = new StringBuffer();
        while (line != null) {
    
            int i = 0;
            boolean quoteMode = false;
            while (i < line.length()) {
                if (quoteMode) {
                    if (line.charAt(i) == '"') {
                        quoteMode = false;
                        add_string_to_label(token.toString());
                        token.setLength(0);
                    } else {
                        token.append(line.charAt(i));
                    }
                } else {
                    if (line.charAt(i) == ';') {
                        if (token.length() > 0) {
                            add_string_to_label(token.toString());
                            token.setLength(0);
                        }
                        break;
                    } else if (line.charAt(i) == ':') {
                        set_label(token.toString());
                        token.setLength(0);
                    } else if (line.charAt(i) == ',') {
                        if (token.length() > 0) {
                            add_string_to_label(token.toString());
                            token.setLength(0);
                        }
                        add_string_to_label(token.toString());
                    } else if (line.charAt(i) == '"') {
                        if (token.length() > 0) {
                            add_string_to_label(token.toString());
                            token.setLength(0);
                        }
                        quoteMode = true;
                    } else if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
                        if (token.length() > 0) {
                            add_string_to_label(token.toString());
                            token.setLength(0);
                        }
                    } else {
                        token.append(line.charAt(i));
                    }
                }
                i++;
            }
            if (quoteMode) {
                throw new RuntimeException("post_process: open string at line " + yylineno);
            }
            if (token.length() > 0) {
                add_string_to_label(token.toString());
                token.setLength(0);
            }
            line = r.readLine();
            yylineno++;
        }
    }

}
