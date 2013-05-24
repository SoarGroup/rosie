package net.sf.jlinkgrammar;

/**
 * The basic word class
 * TODO - Create word comparator and internationalization.
 * That is: If the word is in French and we need to know if
 * it is a conjunction we need something like:
 * Jaque et Pierre == John and Peter
 *  Word.compare(english("and")); 
 *
 */
public class Word {

    /**
     * this word as a String object
     */
    public String string;
    /**
     * a linked list of equivilent expressions also a sentence starts out with these
     * @see XNode
     */
    public XNode x;      
    /**
     * holds the disjunct of the word, eventually these get generated.
     * @see Disjunct
     */
    public Disjunct d;    
    /**
      * indicates that the first character is upper case
      * TODO - Remove English (Indo European) dependancy 
      */
    public boolean firstupper;
}
