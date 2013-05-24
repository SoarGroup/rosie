package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class WordFile {
    /* The structure below stores a list of dictionary word files. */

        String file;   /* the file name */
        boolean changed;             /* true if this file has been changed */
        WordFile  next;

}
