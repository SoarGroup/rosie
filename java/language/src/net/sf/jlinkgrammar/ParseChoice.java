package net.sf.jlinkgrammar;

/**
 * One possible parsing choice of a ParseSet
 *
 */
public class ParseChoice {
    ParseChoice next;
    ParseSet set[] = new ParseSet[2];
    Link link[] = new Link[2]; /* the lc fields of these is null if there is no link used */
    Disjunct ld, md, rd; /* the chosen disjuncts for the relevant three words */

    /**
     * Constructor
     * @see ParseInfo
     * @param lset  sets left ParseSet
     * @param llw   sets the index of the left word in left set
     * @param lrw   sets the index of the rightword in the left set
     * @param llc   sets the left Connector in the left set
     * @param lrc   sets the right Connector in the left set
     * @param rset  sets right ParseSet
     * @param rlw   sets the index of the left word in right set
     * @param rrw   sets the index of the rightword in the right set
     * @param rlc   sets the left Connector in the right set
     * @param rrc   sets the right Connector in the right set
     * @param ld    sets the left Disjunct
     * @param md    sets the middle Disjunct
     * @param rd    sets the right Disjunct
     * @see ParseSet
     * @see Connector
     * @see Disjunct
     * 
     */
    public ParseChoice(
        ParseSet lset,
        int llw,
        int lrw,
        Connector llc,
        Connector lrc,
        ParseSet rset,
        int rlw,
        int rrw,
        Connector rlc,
        Connector rrc,
        Disjunct ld,
        Disjunct md,
        Disjunct rd) {
        next = null;
        set[0] = lset;
        link[0] = new Link();
        link[0].l = llw;
        link[0].r = lrw;
        link[0].lc = llc;
        link[0].rc = lrc;
        set[1] = rset;
        link[1] = new Link();
        link[1].l = rlw;
        link[1].r = rrw;
        link[1].lc = rlc;
        link[1].rc = rrc;
        this.ld = ld;
        this.md = md;
        this.rd = rd;
    }

}
