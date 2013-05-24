package net.sf.jlinkgrammar;

/**
 * The first thing we do is we build a data structure to represent the
   result of the entire parse search.  There will be a set of nodes
   built for each call to the count() function that returned a non-zero         
   value, AND which is part of a valid linkage.  Each of these nodes
   represents a valid continuation, and contains pointers to two other
   sets (one for the left continuation and one for the righ
   continuation).

 *
 */
public class ParseSet {
    /** 
     * the number of ways to parse a sentence
     */
    public int count; 
    ParseChoice first;
    /**
     * used to enumerate linkages
     */
    ParseChoice current; 
    
    /**
     * Put this parse_choice into a given set.  The current pointer is always
     * left pointing to the end of the list.
     * @param pc add a ParseCoice() to a set
     * 
     */
    public void put_choice_in_set(ParseChoice pc) {
        
        if (first == null) {
            first = pc;
        } else {
            current.next = pc;
        }
        current = pc;
        pc.next = null;
    }
    
    /**
     * constructor
     * @param count index counter
     * @param first head of the parse choice list
     */
    public ParseSet(int count,ParseChoice first) {
        this.count=count;
        this.first=first;
        current=first;
    }
  
    static ParseSet dummy_set=new ParseSet(1,null);
    
    /**
     * returns an empty set of parses
     * @return Returns an empty set
     */
    public static ParseSet empty_set() {
        
        ParseSet s;
        s = new ParseSet(0,null);
        return s;
    }

}
