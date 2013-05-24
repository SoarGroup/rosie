package net.sf.jlinkgrammar;

/**
 * TODO add javadoc
 *
 */
public class Sublinkage {
    int num_links; /* Number of links in array */
    Link link[]; /* Array of links */
    PPInfo pp_info[]; /* PP info for each link */
    String violation; /* Name of violation, if any */
    PPData pp_data;

    Sublinkage() {
    }

    Sublinkage(ParseInfo pi) {
        int i;
        link = new Link[GlobalBean.MAX_LINKS];
        pp_info = null;
        violation = null;
        for (i = 0; i < GlobalBean.MAX_LINKS; i++)
            link[i] = null;
        num_links = pi.N_links;
        if (!(pi.N_links < GlobalBean.MAX_LINKS)) {
            throw new RuntimeException("Too many links");
        }
    }

}
