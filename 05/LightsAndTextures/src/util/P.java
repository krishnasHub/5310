package util;

/**
 * Created by krish on 11/10/2017.
 *
 * Simple wrapper to Print on console.
 */
public class P {
    private static final boolean SHOW_LOGS = true;

    public static void P() {
        if(SHOW_LOGS) System.out.println();
    }

    public static void P(Object o) {
        if(SHOW_LOGS) System.out.println(o);
    }
}
