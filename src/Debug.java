import javax.swing.*;

import static java.lang.System.err;

// This class is for printing out information used for debugging (specifically, whether code is excecuted in or outside of the event dispatch thread compared to where it should be excecuting). Shouldn't be used at all in the final product.

public class Debug {

    public static void threadCheck(String info, boolean desired) {
        String output = info + " ";
        boolean isOnEDT = SwingUtilities.isEventDispatchThread();
        if (isOnEDT) {
            output += "is running in the EDT";
        } else {
            output += "is not running in the EDT";
        }

        if (isOnEDT != desired) {
            err.println(output);
        }
    }

}
