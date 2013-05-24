package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class AndList {
    AndList next;
    int conjunction;
    int num_elements;
    int element[]=new int[GlobalBean.MAX_SENTENCE];
    int num_outside_words;
    int outside_word[]=new int[GlobalBean.MAX_SENTENCE];
    int cost;

}
