/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect;

import fi.wsnusbcollect.forward.RemoteForwarderWork;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

/**
 * Basic RMI testing main class to verify functionality of RMI remote forwarder.
 * @author ph4r05
 */
public class RMIForwarderTester {
    // main logger instance, configured in log4j.properties in resources
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RMIForwarderTester.class);
    
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--rmi-registry-host", usage = "host for RMI registry service to connect to. Default null=localhost (if registry port is forwarder by SSH to localhost)")
    private String rmiRegistryHost=null;
    
    @Option(name = "--rmi-registry-port", usage = "port for RMI registry service to connect to. Default: 29998")
    private Integer rmiRegistryPort=29998;
    
    /**
     * Remote worker class if needed (if using self as forwarder running on different machine)
     */
    protected RemoteForwarderWork remoteWork;
    
    public static void main(String[] args) {
        log.info("Starting application");
        try {
            // some inits in static scope
            // ...

            // do main on instance
            RMIForwarderTester app = new RMIForwarderTester();
            
            // do the main
            app.doMain(args);
        } catch (IOException ex) {
            log.error("Exception thrown: ", ex);
        } catch (CmdLineException ex) {
            log.error("Error in processing command line arguments", ex);
        } catch (RuntimeException ex) {
            log.error("Runtime exception occurred", ex);
        } catch (Exception ex) {
            log.error("Generic exception occurred", ex);
        }
        
        System.out.println("Exiting...");
        log.info("Everything OK, exiting");
        try {
            Thread.yield();
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log.warn("Cannot sleep", ex);
        }
        
        // exit
        System.exit(0);
    }
    
    /**
     * Main entry method for running instance of App
     * After start, execution is passed here
     * 
     * @param args          Startup arguments
     * @throws IOException
     * @throws CmdLineException 
     */
    public void doMain(String[] args) throws IOException, CmdLineException {        
        // command line argument parser
        CmdLineParser parser = new CmdLineParser(this);

        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try {
            // parse the arguments.
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println(" Example: java SampleMain" + parser.printExample(ExampleMode.ALL));

            return;
        }
        
        // initialize RMI
        this.initRMI();
        
        // test rmi call
        String challenge = "MyChallenge: " + System.currentTimeMillis() + "; And yours? ";
        log.info("Going to test RMI interface with challenge:");
        log.info(challenge);
        
        String response = "";
        try{
            response = this.remoteWork.simpleRemoteTest(challenge);
            log.info("RMI invoked without exception");
        } catch(Exception e){
            log.error("Problem during RMI invocation: ", e);
        }
        
        log.info("Result returned by RMI: " + response);
    }
    
    /**
     * Initializes RMI client
     */
    protected void initRMI(){
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            
            // do this after final initialization
            // service name we are looking for - remote forwarder.
            String name = SenslabForwarder.RMI_SERVICE_NAME;
            
            // connect to server RMI registry and lookup given service by svc name
            log.info("Going to allocate registry: " + rmiRegistryHost + ":" + rmiRegistryPort);
            Registry registry = LocateRegistry.getRegistry(rmiRegistryHost, rmiRegistryPort);
            
            log.info("Registry: " + (registry!=null ? "not null":"NULL"));
            log.info("Going to resolve service name: " + name);
            
            this.remoteWork = (RemoteForwarderWork) registry.lookup(name);
            log.info("Resolved remote work: " + (this.remoteWork!=null ? "not null":"NUL"));
            log.info("RMI initialized, no exception thrown yet.");
        } catch (NotBoundException ex) {
            log.error("Cannot init RMI. Not bound exception", ex);
        } catch (AccessException ex) {
            log.error("Cannot init RMI. Accessing exception", ex);
        } catch (RemoteException ex) {
            log.error("Cannot init RMI. Remote exception", ex);
        }
    }
}
