/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect;

/**
 * Static registry class, holds currently running main application
 * @author ph4r05
 */
public class RunningApp {
    private static AppIntf runningInstance;

    public static AppIntf getRunningInstance() {
        return runningInstance;
    }

    public static void setRunningInstance(AppIntf runningInstance) {
        RunningApp.runningInstance = runningInstance;
    }
}
