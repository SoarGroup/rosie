package net.sf.jlinkgrammar;

import java.util.Date;

/**
 * TODO add javadoc
 *
 */
public class Resources {
    private long startTime;
    private long lastTime;
    private long cumulativeTime;

    Resources() {
        startTime = lastTime = new Date().getTime();
        cumulativeTime = 0;
    }

    /**
     * Reset start time for a new parsing run.
     */
    void reset() {
        lastTime = startTime = new Date().getTime();
    }

    /**
     * Print out the elapsed time since this was last called.
     * @param opts control verbosity and output destination
     * @param s Label to print
     */
    void printTime(ParseOptions opts, String s) {
        /*  */
        long currentTime = new Date().getTime();
        if (opts.verbosity > 1) {
            opts.out.print("++++");
            opts.left_print_string(s, "                                           ");
            opts.out.println("" + ((currentTime - lastTime) / 1000.0) + " seconds");
        }
        lastTime = currentTime;
    }

    /**
     * Print out time since last reset and total cumulative time.
     * @param opts control verbosity and output destination
     */
    void printTotalTime(ParseOptions opts) {
        /*  */
        long currentTime = new Date().getTime();
        cumulativeTime += (currentTime - startTime);
        if (opts.verbosity > 0) {
            opts.out.print("++++");
            opts.left_print_string("Time", "                                           ");
            opts.out.println(
                "" + ((currentTime - startTime) / 1000.0) + " seconds (" + (cumulativeTime / 1000.0) + " total)");
        }
        startTime = lastTime = currentTime;
    }

}
