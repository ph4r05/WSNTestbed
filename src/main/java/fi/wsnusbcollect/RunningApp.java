/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect;

/**
 * Static registry class
 * @author ph4r05
 */
public class RunningApp {
    private static Object runningInstance;

    public static Object getRunningInstance() {
        return runningInstance;
    }

    public static void setRunningInstance(Object runningInstance) {
        RunningApp.runningInstance = runningInstance;
    }
}
