package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Constituent {
    int left;
    int right;
    String  type;
    int domain_type;
    String  start_link;
    int start_num;
    int subl; 
    int canon;
    int valid;
    /* 0: it's an ordinary VP (or other type); 
       1: it's an AUX, don't print it; 
       2: it's an AUX, and print it */
    int aux;      

}
