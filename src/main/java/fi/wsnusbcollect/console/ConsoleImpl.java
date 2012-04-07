/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.console;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.experiment.ExperimentCoordinator;
import fi.wsnusbcollect.experiment.ExperimentCoordinatorImpl;
import fi.wsnusbcollect.experiment.ExperimentInit;
import fi.wsnusbcollect.usb.USBarbitrator;
import javax.annotation.Resource;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.python.util.JLineConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class ConsoleImpl implements Console {
    private static final Logger log = LoggerFactory.getLogger(ConsoleImpl.class);
        
    @Resource(name="consoleHelper")
    private ConsoleHelper consoleHelper = null;
    
    @Resource(name="USBarbitrator")
    private USBarbitrator usbArbitrator = null;
    
    @Resource(name="experimentCoordinator")
    private ExperimentCoordinator expCoord;
    
    @Resource(name="experimentInit")
    private ExperimentInit expInit;    

    // interactive jython interface
    protected JLineConsole interp;
    
    // python shell does not exit on ctrl+d
    private boolean shellNoExit=true;    
    
    /**
     * Prepares shell for execution
     */
    @Override
    public void prepareShell(){
        log.info("Dropping to shell now...");
        
        // set Properties 
        if (System.getProperty("python.home") == null) {
            System.setProperty("python.home", "~/");
        }

        // initialize python shell
        PySystemState.initialize(PySystemState.getBaseProperties(), null, new String[0]);

        // no postProps, registry values used 
        JLineConsole.initialize(System.getProperties(), null, new String[0]);

        interp = new JLineConsole();
        // important line, set JLineConsole to internal python variable to be able to 
        // acces console from python interface
        setShellParamObject("_jy_interpreter", Py.java2py(interp));

        // usb arbitrator set
        setShellParamObject("_jy_usbartibtrator", Py.java2py(this.usbArbitrator));

        // console helper
        setShellParamObject("_jy_ch", Py.java2py(this.consoleHelper));
        
        // console object
        setShellParamObject("_jy_console", Py.java2py(this));
        
        // experiment init
        setShellParamObject("_jy_expInit", Py.java2py(this.expInit));
        
        // experiment coord
        setShellParamObject("_jy_expCoord", Py.java2py(this.expCoord));

        // this instance
        setShellParamObject("_jy_main", Py.java2py(App.getRunningInstance()));

        // enable autocomplete, sigint handler by default
        this.consoleHelper.prepareConsoleBeforeStart(interp);
    }
    
    /**
     * Sets object to internal shell parameter
     * @param param
     * @param obj 
     */
    @Override
    public void setShellParamObject(String param, Object obj){
        interp.getSystemState().__setattr__(param, Py.java2py(obj));
    }
    
    /**
     * 
     * @param aliasName
     * @param obj 
     */
    @Override
    public void setShellAlias(String aliasName, Object obj){
        setShellParamObject(aliasName, Py.java2py(obj));
    }
    
    @Override
    public void executeCommand(String command) {
        if (interp==null){
            throw new NullPointerException("Passed empty jython console instance");
        }
        
        if (command==null){
            throw new NullPointerException("Null command cannot be executed");
        }
        
        // enable autocomplete by default:)
        interp.exec(command);
    }
    
    /**
     * Method starts new initialized Jython shell for user
     */
    @Override
    public void getShell(){
        while (this.shellNoExit) {
            log.info("Starting shell");
            this.consoleHelper.consoleRestarted();

            try {
                interp.interact();
            } catch (Error e) {
                // interrupted shell error
                if (interp!=null){
                    interp.cleanup();
                    interp.resetbuffer();
                }

                System.out.println("Shell was interrupted...");
            } catch (Throwable e) {
                log.warn("Exception occured during jython interaction", e);
                interp.cleanup();
            }
            
            // is applicable only if shell should start again
            if (this.shellNoExit){
                System.out.println("If you want to exit shell, please call: exitNow()");
                if (interp!=null){
                    interp.cleanup();
                    interp.resetbuffer();
                }
            }
        }
        
        log.info("Shel terminating");
        // next command is used to start telnet jython server
        // not properly implemented yet
        //JythonShellServer.run_server(7000, new HashMap());
    }

    /**
     * Interrupts shell
     */
    @Override
    public void exitShell(){
        this.shellNoExit=false;
        this.interp.interrupt(this.consoleHelper.getTs());
        this.interp = null;
    }
    
    @Override
    public ConsoleHelper getConsoleHelper() {
        return consoleHelper;
    }

    @Override
    public void setConsoleHelper(ConsoleHelper consoleHelper) {
        this.consoleHelper = consoleHelper;
    }

    @Override
    public JLineConsole getInterp() {
        return interp;
    }

    @Override
    public void setInterp(JLineConsole interp) {
        this.interp = interp;
    }

    @Override
    public USBarbitrator getUsbArbitrator() {
        return usbArbitrator;
    }

    @Override
    public void setUsbArbitrator(USBarbitrator usbArbitrator) {
        this.usbArbitrator = usbArbitrator;
    }

    @Override
    public boolean isShellNoExit() {
        return shellNoExit;
    }

    @Override
    public void setShellNoExit(boolean shellNoExit) {
        this.shellNoExit = shellNoExit;
    }

    @Override
    public ExperimentCoordinator getExpCoord() {
        return expCoord;
    }

    @Override
    public void setExpCoord(ExperimentCoordinatorImpl expCoord) {
        this.expCoord = expCoord;
    }

    @Override
    public ExperimentInit getExpInit() {
        return expInit;
    }

    @Override
    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }
}
