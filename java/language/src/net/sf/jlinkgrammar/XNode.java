package net.sf.jlinkgrammar;

/**
 * This is a hyponym or synonym class the X is for expression. 
 * 
 * Sentence.build_word_expressions(String s)looks up the word s in 
 * the dictionary.  Returns NULL if it's not there.
 * If there, it builds the list of expressions for the word, and returns
 * a pointer to it.
 * 
 * This class is included in the Word class attribute Word.x
 * 
 * TODO - perhaps this could be used to store an english equivilent?
 */
public class XNode {
    /**
     * the word itself
     */
    public String string;
    /**
     * an equivilent expression
     */
    public Exp exp;
    /**
     * link to next equivilent expresion. List terminates with null
     */
    public XNode next;

    /**
     * Destructively catenates the two disjunct lists d1 followed by d2.
      Doesn't change the contents of the disjuncts 
      Traverses the first list, but not the second 
     */
    static XNode catenate_XNodes(XNode d1, XNode d2) {
        
        XNode dis = d1;

        if (d1 == null)
            return d2;
        if (d2 == null)
            return d1;
        while (dis.next != null)
            dis = dis.next;
        dis.next = d2;
        return d1;
    }

}
