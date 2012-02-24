package fi.wsnusbcollect;

import fi.wsnusbcollect.usb.USBarbitrator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main run class for WSN USB Collect application
 *
 * @author ph4r05
 */
public class App {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    // main properties object
    Properties props;
    
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--debug", usage = "enables debug output")
    private boolean debug;
    
    @Option(name = "--detect-nodes", usage = "performs node detection")
    private boolean detectNodes;
    
    @Option(name = "--update-node-database", usage = "updates node connection database info, implies --detect-nodes option")
    private boolean updateNodeDatabase;
    
    @Option(name = "--check-nodes-connection", usage = "checks whether node connection corresponds to DB settings and print warning if not")
    private boolean checkNodesConnection;
    
    @Option(name = "-c", usage = "read configuration from this config file")
    private File configFile = null;
    
    @Option(name = "--use-node-id", usage = "switches to NodeID identifier when identifying motes")
    private boolean useNodeId;
    
    @Option(name = "--motelist", usage = "sets path to motelist command")
    private String motelistCommand = null;
    
    @Option(name = "--use-motes", usage = "comma separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private String useMotesString = null;
    
    @Option(name = "--use-motes-from-file", usage = "newline separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private File useMotesFile = null;
    
    @Option(name = "--ingore-motes", usage = "comma separated list of motes serial numbers to ignore in experiment.")
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

        log.info("Everything OK, exiting");
    }

    /**
     * Initializes dependencies (instantiates components) for application
     * USBArbitrator
     */
    public void initDependencies(){
        this.usbArbitrator = new USBarbitrator();
        
        // here initialize database connection
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

        // access non-option arguments
        System.out.println("other arguments are:");
        for (String s : arguments) {
            System.out.println(s);
        }
        
        // parameters implications
        detectNodes=updateNodeDatabase || detectNodes;
        
        // init dependencies here - arguments and properties loaded
        log.info("Initializing depencencies");
        this.initDependencies();
        
        // main logic starting
        log.info("Arguments parsed, can start logic");
        
        // new nodes detection - discovery USB connected nodes and update database
        if (detectNodes){
            log.info("Detecting new nodes");
            usbArbitrator.detectConnectedNodes();
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

    public boolean isUseNodeId() {
        return useNodeId;
    }

    public void setUseNodeId(boolean useNodeId) {
        this.useNodeId = useNodeId;
    }

    public Properties getProps() {
        return props;
    }

    public static App getRunningInstance() {
        return runningInstance;
    }
}
