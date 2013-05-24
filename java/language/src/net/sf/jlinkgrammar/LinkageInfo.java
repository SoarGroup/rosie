package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class LinkageInfo {
    /* This is for building the graphs of links in post-processing and          */
    /* fat link extraction.                                                     */

    int index;
    boolean fat;
    boolean canonical;
    boolean improper_fat_linkage;
    boolean inconsistent_domains;
    int N_violations, null_cost, unused_word_cost, disjunct_cost, and_cost, link_cost;
    AndList andlist;
    int island[] = new int[GlobalBean.MAX_SENTENCE];

}
