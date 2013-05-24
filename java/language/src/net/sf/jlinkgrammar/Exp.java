package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 * The E_list an Exp structures defined below comprise the expression
 * trees that are stored in the dictionary.  The expression has a type
 * (AND, OR or TERMINAL).  If it is not a terminal it has a list
 * (an EList) of children.
 *
 */
public class Exp {

    /* one of three types, see below */
    int type;
    /* The cost of using this expression.
       Only used for non-terminals */
    int cost;
    /* '-' means to the left, '+' means to right (for connector) */
    char dir;
    /* true if a multi-connector (for connector)  */
    boolean multi;
    /* only needed for non-terminals */
    ExpList l;
    /* only needed if it's a connector */
    String string;
    Exp next;

    Exp(Exp e) {
        type = e.type;
        cost = e.cost;
        dir = e.dir;
        multi = e.multi;
        l = e.l;
        string = e.string;
        next = e.next;
    }

    Exp() {
    }

    Clause build_Clause(int cost_cutoff) {
        /* Build the Clause for the expression e.  Does not change e */
        Clause c = null, c1, c2, c3, c4, c_head;
        ExpList e_list;

        if (type == GlobalBean.AND_type) {
            c1 = new Clause();
            c1.c = null;
            c1.next = null;
            c1.cost = 0;
            c1.maxcost = 0;
            for (e_list = l; e_list != null; e_list = e_list.next) {
                c2 = e_list.e.build_Clause(cost_cutoff);
                c_head = null;
                for (c3 = c1; c3 != null; c3 = c3.next) {
                    for (c4 = c2; c4 != null; c4 = c4.next) {
                        c = new Clause();
                        c.cost = c3.cost + c4.cost;
                        c.maxcost = Math.max(c3.maxcost, c4.maxcost);
                        c.c = TConnector.catenate(c3.c, c4.c);
                        c.next = c_head;
                        c_head = c;
                    }
                }
                c1 = c_head;
            }
            c = c1;
        } else if (type == GlobalBean.OR_type) {
            /* we'll catenate the lists of clauses */
            c = null;
            for (e_list = l; e_list != null; e_list = e_list.next) {
                c1 = e_list.e.build_Clause(cost_cutoff);
                while (c1 != null) {
                    c3 = c1.next;
                    c1.next = c;
                    c = c1;
                    c1 = c3;
                }
            }
        } else if (type == GlobalBean.CONNECTOR_type) {
            c = new Clause();
            c.c = build_terminal();
            c.cost = 0;
            c.maxcost = 0;
            c.next = null;
        } else {
            throw new RuntimeException("an expression node with no type");
        }

        /* c now points to the list of clauses */

        for (c1 = c; c1 != null; c1 = c1.next) {
            c1.cost += cost;
            /* This is how Dennis had it. I prefer the line below */
            /*  c1.maxcost = MAX(c1.maxcost,cost);  */
            c1.maxcost += cost;
        }
        return c;
    }

    TConnector build_terminal() {
        /* build the connector for the terminal node n */
        TConnector c;
        c = new TConnector(this);
        return c;
    }

    int size_of_expression() {
        /* Returns the number of connectors in the expression e */
        int size;
        ExpList el;
        if (type == GlobalBean.CONNECTOR_type)
            return 1;
        size = 0;
        for (el = l; el != null; el = el.next) {
            size += el.e.size_of_expression();
        }
        return size;
    }

    void insert_connectors(int dir) {
        /* Put into the set S all of the dir-pointing connectors still in e.    */
        Connector dummy = new Connector();
        ExpList el;
        dummy.init_connector();
        dummy.label = GlobalBean.NORMAL_LABEL;
        dummy.priority = GlobalBean.THIN_priority;
        /*    dummy.my_word = NO_WORD; */ /* turn off the length part of the matching */

        if (type == GlobalBean.CONNECTOR_type) {
            if (dir == this.dir) {
                dummy.string = string;
                Sentence.insert_S(dummy);
            }
        } else {
            for (el = l; el != null; el = el.next) {
                el.e.insert_connectors(dir);
            }
        }
    }

    int mark_dead_connectors(Sentence sent,char d) {
        /* Mark as dead all of the dir-pointing connectors
           in e that are not matched by anything in the current set.
           Returns the number of connectors so marked.
        */
        Connector dummy = new Connector();
        int count;
        ExpList el;
        dummy.init_connector();
        dummy.label = GlobalBean.NORMAL_LABEL;
        dummy.priority = GlobalBean.THIN_priority;
        /*    dummy.my_word = NO_WORD; */ /* turn off the length part of the matching */
        count = 0;
        if (type == GlobalBean.CONNECTOR_type) {
            if (dir == d) {
                dummy.string = string;
                if (!sent.matches_S(dummy, d)) {
                    string = null;
                    count++;
                }
            }
        } else {
            for (el = l; el != null; el = el.next) {
                count += el.e.mark_dead_connectors(sent,d);
            }
        }
        return count;
    }

    /* The purge operations remove all irrelevant stuff from the expression,    */
    /* and free the purged stuff.  A connector is deemed irrelevant if its      */
    /* string pointer has been set to null.  The passes through the sentence    */
    /* have the job of doing this.                                              */

    /* If an OR or AND type expression node has one child, we can replace it by */
    /* its child.  This, of course, is not really necessary                     */

    Exp purge_Exp() {
        /* Must be called with a non-null expression                                */
        /* Return null iff the expression has no disjuncts.                         */
        /*  Exp * ne; */

        if (type == GlobalBean.CONNECTOR_type) {
            if (string == null) {
                return null;
            } else {
                return this;
            }
        }
        if (type == GlobalBean.AND_type) {
            if (!and_purge_ExpList(l)) {
                return null;
            }
        } else {
            l = or_purge_ExpList(l);
            if (l == null) {
                return null;
            }
        }
        return this;
    }

    static boolean and_purge_ExpList(ExpList l) {
        /* Returns 0 iff the length of the disjunct list is 0.          */
        /* If this is the case, it frees the structure rooted at l.     */
        if (l == null)
            return true;
        if ((l.e = l.e.purge_Exp()) == null) {
            
            return false;
        }
        if (!and_purge_ExpList(l.next)) {
            return false;
        }
        return true;
    }

    ExpList or_purge_ExpList(ExpList l) {
        /* get rid of the elements with null expressions */
        ExpList el;
        if (l == null)
            return null;
        if ((l.e = l.e.purge_Exp()) == null) {
            el = or_purge_ExpList(l.next);
            return el;
        }
        l.next = or_purge_ExpList(l.next);
        return l;
    }

    static Exp copy_Exp(Exp e) {
        Exp n;
        if (e == null)
            return null;
        n = new Exp(e);
        if (e.type != GlobalBean.CONNECTOR_type) {
            n.l = ExpList.copy_ExpList(e.l);
        }
        return n;
    }

    ConnectorSet connector_set_create() {
        int i;
        ConnectorSet conset;

        conset = new ConnectorSet();
        conset.table_size = MyRandom.next_power_of_two_up(size_of_expression());
        conset.hash_table = new Connector[conset.table_size];
        for (i = 0; i < conset.table_size; i++)
            conset.hash_table[i] = null;
        build_connector_set_from_expression(conset);
        return conset;
    }

    void build_connector_set_from_expression(ConnectorSet conset) {
        ExpList el;
        Connector c;
        int h;
        if (type == GlobalBean.CONNECTOR_type) {
            c = new Connector();
            c.init_connector();
            c.string = string;
            c.label = GlobalBean.NORMAL_LABEL; /* so we can use match() */
            c.priority = GlobalBean.THIN_priority;
            c.word = dir; /* just use the word field to give the dir */
            h = conset.connector_set_hash(c.string, c.word);
            c.next = conset.hash_table[h];
            conset.hash_table[h] = c;
        } else {
            for (el = l; el != null; el = el.next) {
                el.e.build_connector_set_from_expression(conset);
            }
        }
    }

}
