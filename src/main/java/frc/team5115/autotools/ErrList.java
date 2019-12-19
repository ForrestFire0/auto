package frc.team5115.autotools;

import java.util.ArrayList;

public class ErrList {
    static ArrayList<Throwable> errList = new ArrayList<Throwable>();
    static ArrayList<Integer> count = new ArrayList<Integer>();

    public static void reportError(Throwable err) {
        int index = find(err);

        count.set(index, count.get(index) + 1);
    }

    public static void printErrorList() {
        //runAuto/IMUCalc: Accelerometer value doesn't make sense. (20 x)
        System.out.println("Error List:");
        for (int i = 0; i < errList.size(); i++) {
            Throwable x = errList.get(i);
            int numTries = count.get(i);
            System.out.println(printErr(x, numTries));
        }
    }

    static int find(Throwable err) {
        for (int i = 0; i < errList.size(); i++) {
            if(err.getMessage() //check if the messages are equal.
                    .equals(errList.get(i).getMessage()))
            return i;
        }

        errList.add(err); //otherwise add the error to the end
        count.add(0);
        return errList.size()-1; //return that location.
    }

    static String printErr(Throwable err, int numTries) {
        StackTraceElement[] st = err.getStackTrace();
        return st[1].getMethodName() + "/" + st[0].getMethodName() + ": " + err.getMessage() + " (x" + numTries + ")";
    }
}