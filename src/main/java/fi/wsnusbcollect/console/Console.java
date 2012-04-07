/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.console;

import fi.wsnusbcollect.experiment.ExperimentCoordinator;
import fi.wsnusbcollect.experiment.ExperimentCoordinatorImpl;
import fi.wsnusbcollect.experiment.ExperimentInit;
import fi.wsnusbcollect.usb.USBarbitrator;
import org.python.util.JLineConsole;

/**
 *
 * @author ph4r05
 */
public interface Console {

    /**
     * Interrupts shell
     */
    void exitShell();

    ConsoleHelper getConsoleHelper();

    ExperimentCoordinator getExpCoord();

    ExperimentInit getExpInit();

    JLineConsole getInterp();

    /**
     * Method starts new initialized Jython shell for user
     */
    void getShell();

    USBarbitrator getUsbArbitrator();

    boolean isShellNoExit();

    /**
     * Prepares shell for execution
     */
    void prepareShell();

    void setConsoleHelper(ConsoleHelper consoleHelper);

    void setExpCoord(ExperimentCoordinatorImpl expCoord);

    void setExpInit(ExperimentInit expInit);

    void setInterp(JLineConsole interp);

    void setShellNoExit(boolean shellNoExit);

    /**
     * Sets object to internal shell parameter
     * @param param
     * @param obj
     */
    void setShellParamObject(String param, Object obj);

    void setUsbArbitrator(USBarbitrator usbArbitrator);
    
    public void setShellAlias(String aliasName, Object obj);
    
    void executeCommand(String command);
}
