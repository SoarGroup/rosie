package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class PPRule {
    /* Holds a single post-processing rule. Since rules come in many 
       flavors, not all fields of the following are always relevant  */
    String selector;       /* name of link to which rule applies      */
    int   domain;         /* type of domain to which rule applies    */
    PPLinkset link_set; /* handle to set of links relevant to rule */
    int   link_set_size;  /* size of this set                        */
    String link_array[];   /* array containing the spelled-out names  */
    String msg;           /* explanation (null=end sentinel in array)*/

}
