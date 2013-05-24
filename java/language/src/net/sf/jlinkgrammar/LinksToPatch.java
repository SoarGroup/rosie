package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class LinksToPatch {
    LinksToPatch next;
    int link;
    char dir;   /* this is 'r' or 'l' depending on which end of the link
           is to be patched. */
    int newValue;    /* the new value of the end to be patched */

}
