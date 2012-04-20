package fi.wsnusbcollect;

import fi.wsnusbcollect.usb.NodeConfigRecord;
import fi.wsnusbcollect.usb.USBarbitrator;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import net.tinyos.sf.SerialForwarder;
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
public class SenslabForwarder {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(SenslabForwarder.class);
    
    // file path separator
    public static final String pathSeparator = System.getProperty("file.separator");
    
    // main properties object
    Properties props = null;
    
    // Spring application context - dependency injector
    ApplicationContext appContext = null;
    
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--debug", aliases = {"-d"}, usage = "enables debug output")
    private boolean debug;
    
    @Option(name = "-c", usage = "read configuration from this config file")
    private File configFile = null;
    
    @Option(name = "--motes", usage = "comma separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private String useMotesString = null;
    
    @Option(name = "--port", aliases = {"-p"}, usage = "determines start port to start forwarders on")
    private Integer port=30000;
    
    @Option(name = "--connection-type", usage="connection to use with packet listeners (serial, network, sf)")
    private String cnType="network";
    
    @Option(name = "--hostname", usage="hostname to connect to (default: experiment)")
    private String host="experiment";
    
    @Option(name="--connectPort", usage="port offset for data source packet listener")
    private Integer connectPort=30000;
    
    @Option(name = "--ignore-motes", usage = "comma separated list of motes serial numbers to ignore in experiment.")
    private String ignoreMotesString = null;
    
    @Option(name = "--senslab", usage = "environment is senslab - specific mote connection")
    private boolean senslab=true;
    
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
    private static SenslabForwarder runningInstance = null;
    
    // USB arbitrator instance
    private USBarbitrator usbArbitrator = null;
    
    // parsed config file
    protected Wini ini;
    protected String configFileContents;
    
    public static void main(String[] args) {
        log.info("Starting forwarder application");
        try {
            // some inits in static scope
            // ...

            // do main on instance
            SenslabForwarder.runningInstance = new SenslabForwarder();
            SenslabForwarder.runningInstance.doMain(args);
            
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
        // dependency initialization according to senslab. If true - no mysql database
        if (senslab){
            log.info("Iniitalizing senslab application context");
            
            // spring application context init, senslab - lightweight
            appContext = new ClassPathXmlApplicationContext("applicationContextSenslabForwarder.xml");
        } else {
            // spring application context init
            throw new IllegalArgumentException("Cannot start in non-senslab mode");
        }
        
        this.usbArbitrator = appContext.getBean("USBarbitrator", USBarbitrator.class);
        if (this.usbArbitrator == null){
            log.error("Dependency injection failed on USB arbitrator bean");
            throw new IllegalStateException("Dependency injection is not working");
        }
        
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
        
        if (configFile != null && (configFile instanceof File)) {
            System.out.println("Config file set: " + configFile.getName());
        }
        
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
        
        // do the work here
        // 1. get list of nodes to connect to, port mapping is by default
        // 30000 + ID of node to work with
        
        // include string is mandatory
        if (this.useMotesString==null || this.useMotesString.isEmpty()){
            System.err.println("--motes option is mandatory");
        }
        
        // use only nodeid, connect by myself
        // connecting to as network@experiment
        List<NodeConfigRecord> nodes2connect = this.usbArbitrator.getNodes2connect(useMotesString, this.ignoreMotesString);
        
        // structure to hold forwarders
        List<SerialForwarder> forwarders = new LinkedList<SerialForwarder>();
        
        // nodeids, now build connection strings
        for(NodeConfigRecord ncr : nodes2connect){
            String connectionString = this.cnType + "@" + this.host + ":" + (this.connectPort + ncr.getNodeId());
            ncr.setConnectionString(connectionString);
            
            log.info("Going to connect to node: " + ncr.getNodeId() + "; ConnectionString: " + connectionString);
            
            // now we have complete node connect list, it remains only to create corresponding serial forwarders
            SerialForwarder tmpSf = SerialForwarder.newObjInstance(connectionString, this.port + ncr.getNodeId());
            forwarders.add(tmpSf);
            
            log.info("Starting listen server for node on port: " + (this.port + ncr.getNodeId()));
            tmpSf.startListenServer();
        }
        
        log.info("Starting is over now, going to run forever");
        
        // while loop - never ending:)
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error("Cannot sleep here", ex);
            }
        }
    }
    
    /**
     * Reads config file to internal ini object and to string object
     */
    public void readConfig(){
        try {
            FileInputStream fstream = new FileInputStream(this.configFile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                sb.append(strLine).append("\n");
            }
            //Close the input stream
            in.close();
            this.configFileContents = sb.toString();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
          
        
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

    public USBarbitrator getUsbArbitrator() {
        return usbArbitrator;
    }

    public void setUsbArbitrator(USBarbitrator usbArbitrator) {
        this.usbArbitrator = usbArbitrator;
    }

    public String getUseMotesString() {
        return useMotesString;
    }

    public void setUseMotesString(String useMotesString) {
        this.useMotesString = useMotesString;
    }

    public Properties getProps() {
        return props;
    }

    public static SenslabForwarder getRunningInstance() {
        return runningInstance;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public Wini getIni() {
        return ini;
    }

    public String getConfigFileContents() {
        return configFileContents;
    }

    public boolean isSenslab() {
        return senslab;
    }

    public void setSenslab(boolean senslab) {
        this.senslab = senslab;
    }
}
