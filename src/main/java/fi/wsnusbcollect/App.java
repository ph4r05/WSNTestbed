package fi.wsnusbcollect;

import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.console.ConsoleHelper;
import fi.wsnusbcollect.experiment.ExperimentCoordinator;
import fi.wsnusbcollect.experiment.ExperimentCoordinatorImpl;
import fi.wsnusbcollect.experiment.ExperimentInit;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import fi.wsnusbcollect.usb.USBarbitrator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.ini4j.Ini;
import org.ini4j.Wini;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main run class for WSN USB Collect application
 *
 * @author ph4r05
 */
public class App {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    // main properties object
    Properties props = null;
    
    // Spring application context - dependency injector
    ApplicationContext appContext = null;
    
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--debug", aliases = {"-d"}, usage = "enables debug output")
    private boolean debug;
    
    @Option(name = "--detect-nodes", usage = "performs node detection, read-only operation")
    private boolean detectNodes;
    
    @Option(name = "--show-binding", usage = "returns database binding for connected nodes")
    private boolean showBinding;
    
    @Option(name = "--update-node-database", usage = "updates node connection database info, implies --detect-nodes option")
    private boolean updateNodeDatabase;
    
    @Option(name = "--check-nodes-connection", usage = "checks whether node connection corresponds to DB settings and print warning if not")
    private boolean checkNodesConnection;
    
    @Option(name = "--shell", usage = "should drop to shell after init?")
    private boolean shell=false;
    
    @Option(name = "--start-suspended", usage = "experiment coordinator starts for command from shell")
    private boolean startSuspended=false;
    
    @Option(name = "-c", usage = "read configuration from this config file")
    private File configFile = null;
    
//    @Option(name = "--use-node-id", usage = "switches to NodeID identifier when identifying motes")
//    private boolean useNodeId;
    
    @Option(name = "--motelist", usage = "sets path to motelist command")
    private String motelistCommand = null;
    
    @Option(name = "--use-motes", usage = "comma separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private String useMotesString = null;
    
    @Option(name = "--use-motes-from-file", usage = "newline separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private File useMotesFile = null;
    
    @Option(name = "--ignore-motes", usage = "comma separated list of motes serial numbers to ignore in experiment.")
    private String ignoreMotesString = null;
    
    @Option(name = "--ignore-motes-from-file", usage = "newline separated list of motes serial numbers to ignore in experiment.")
    private File ignoreMotesFile = null;
    
    /**
     * Real parsed list of motes to use - uses 
     * --use-motes SERIAL1,SERIAL2,SERIAL3
     * --use-motes-from-file includeMotesList.txt
     * --ingore-motes SERIAL1,SERIAL2,SERIAL3
     * --ignore-motes-from-file ignoreMotesList.txt
     */
    private List<String> moteList2use;
    
    // Running instance of application.
    // To be reachable from another modules for configuration purposes
    private static App runningInstance = null;
    
    // USB arbitrator instance
    private USBarbitrator usbArbitrator = null;
    
    // ConsoleHandler for Jython interface
    private ConsoleHelper consoleHelper = null;
    
    // Console
    private Console console = null;
    
    // experiment objects
    private ExperimentCoordinatorImpl expCoord;
    private ExperimentInit expInit;
    
    // parsed config file
    protected Wini ini;
    
    public static void main(String[] args) {
        log.info("Starting application");
        try {
            // some inits in static scope
            // ...

            // do main on instance
            App.runningInstance = new App();
            App.runningInstance.doMain(args);
            
            // ending application
            // ... 

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
     * Initializes dependencies (instantiates components) for application
     * USBArbitrator
     */
    public void initDependencies(){        
        // spring application context init
        appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        
        this.usbArbitrator = appContext.getBean("USBarbitrator", USBarbitrator.class);
        if (this.usbArbitrator == null){
            log.error("Dependency injection failed on USB arbitrator bean");
            throw new IllegalStateException("Dependency injection is not working");
        }
        
        this.consoleHelper = appContext.getBean("consoleHelper", ConsoleHelper.class);
        this.console = appContext.getBean("console", Console.class);
        this.expInit = appContext.getBean("experimentInit", ExperimentInit.class);
        this.expCoord = appContext.getBean("experimentCoordinator", ExperimentCoordinatorImpl.class);
        
        // reconnect between
        log.info("All dependencies initialized");
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
        // load default properties
        props = new Properties();
        props.load(getClass().getResourceAsStream("/application.properties"));
        // init default properties
        this.motelistCommand = props.getProperty("motelistCmd", "motelist");
        
        // command line argument parser
        CmdLineParser parser = new CmdLineParser(this);

        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            //parser.parseArgument("include","mote");

            // after parsing arguments, you should check
            // if enough arguments are given.
//            if (arguments.isEmpty()) {
//                throw new CmdLineException("No argument is given");
//            }

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
        
        if (configFile != null && (configFile instanceof File)) {
            System.out.println("Config file set: " + configFile.getName());
        }
        
        // parameters implications
        detectNodes=updateNodeDatabase || detectNodes || checkNodesConnection || showBinding;
        
        // init dependencies here - arguments and properties loaded
        // application context loading
        log.info("Initializing depencencies");
        this.initDependencies();
        
        // main logic starting
        log.info("Arguments parsed, can start logic");
        
        // read config file if applicable
        if (this.configFile!=null){
            this.readConfig();
        }
        
        // new nodes detection - discovery USB connected nodes and update database
        if (detectNodes){
            log.info("Detecting new nodes");
            usbArbitrator.detectConnectedNodes();
            
            // if update node database or check nodes connection were choosen
            // perform just this single actions
            if (updateNodeDatabase){
                log.info("Ending execution.");
                return;
            }
        }
        
        // prepare shell
        if (shell){
            this.console.prepareShell();
        }
        
        // initialize experiment init class
        this.expInit.initClass();
        // init, prepare for experiment
        this.expInit.initEnvironment();
        // pass controll to experiment coordinator, spawn new thread/blocking run
        this.expCoord.work();
        
        // drop to shell?
        if (shell){
            this.console.getShell();
        }
    }
    
    /**
     * Reads config file to internal ini object
     */
    public void readConfig(){
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(this.configFile));
            this.ini = new Wini(in);
        } catch (FileNotFoundException ex) {
            log.error("Config file not found");
        } catch (IOException ex){
            log.error("Config file not found");
        }
        
        finally {
            try {
                in.close();
            } catch (IOException ex) {
                log.error("Problem with closing config file");
            }
        }
    }
    
    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public boolean isCheckNodesConnection() {
        return checkNodesConnection;
    }

    public void setCheckNodesConnection(boolean checkNodesConnection) {
        this.checkNodesConnection = checkNodesConnection;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDetectNodes() {
        return detectNodes;
    }

    public void setDetectNodes(boolean detectNodes) {
        this.detectNodes = detectNodes;
    }

    public String getMotelistCommand() {
        return motelistCommand;
    }

    public void setMotelistCommand(String motelistCommand) {
        this.motelistCommand = motelistCommand;
    }

    public File getIgnoreMotesFile() {
        return ignoreMotesFile;
    }

    public void setIgnoreMotesFile(File ignoreMotesFile) {
        this.ignoreMotesFile = ignoreMotesFile;
    }

    public String getIgnoreMotesString() {
        return ignoreMotesString;
    }

    public void setIgnoreMotesString(String ignoreMotesString) {
        this.ignoreMotesString = ignoreMotesString;
    }

    public List<String> getMoteList2use() {
        return moteList2use;
    }

    public void setMoteList2use(List<String> moteList2use) {
        this.moteList2use = moteList2use;
    }

    public boolean isUpdateNodeDatabase() {
        return updateNodeDatabase;
    }

    public void setUpdateNodeDatabase(boolean updateNodeDatabase) {
        this.updateNodeDatabase = updateNodeDatabase;
    }

    public USBarbitrator getUsbArbitrator() {
        return usbArbitrator;
    }

    public void setUsbArbitrator(USBarbitrator usbArbitrator) {
        this.usbArbitrator = usbArbitrator;
    }

    public File getUseMotesFile() {
        return useMotesFile;
    }

    public void setUseMotesFile(File useMotesFile) {
        this.useMotesFile = useMotesFile;
    }

    public String getUseMotesString() {
        return useMotesString;
    }

    public void setUseMotesString(String useMotesString) {
        this.useMotesString = useMotesString;
    }

//    public boolean isUseNodeId() {
//        return useNodeId;
//    }
//
//    public void setUseNodeId(boolean useNodeId) {
//        this.useNodeId = useNodeId;
//    }

    public Properties getProps() {
        return props;
    }

    public static App getRunningInstance() {
        return runningInstance;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public boolean isShell() {
        return shell;
    }

    public boolean isShowBinding() {
        return showBinding;
    }

    public boolean isStartSuspended() {
        return startSuspended;
    }

    public void setStartSuspended(boolean startSuspended) {
        this.startSuspended = startSuspended;
    }

    public Wini getIni() {
        return ini;
    }

    public ExperimentCoordinatorImpl getExpCoord() {
        return expCoord;
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }
}
