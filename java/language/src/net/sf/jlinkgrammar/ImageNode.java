package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class ImageNode {
    ImageNode next;
    /* the connector the place on the disjunct must match */
    Connector c;
    /* Indicates the place in the fat disjunct where this
       connector must connect.  If 0 then this is a fat
       connector.  If >0 then go place to the right, if
                      <0 then go -place to the left. */
    int place;
}
