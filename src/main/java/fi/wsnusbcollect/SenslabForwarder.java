package fi.wsnusbcollect;

import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.console.ConsoleHelperImpl;
import fi.wsnusbcollect.console.ConsoleImpl;
import fi.wsnusbcollect.forward.MassPeriodicMessageTester;
import fi.wsnusbcollect.forward.MassRTTtester;
import fi.wsnusbcollect.forward.PeriodicMessageTester;
import fi.wsnusbcollect.forward.RTTtester;
import fi.wsnusbcollect.forward.RemoteForwarderWork;
import fi.wsnusbcollect.forward.TimeSyncTester;
import fi.wsnusbcollect.forward.TimerSynchronizer;
import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.GenericNode;
import fi.wsnusbcollect.nodes.NodePlatform;
import fi.wsnusbcollect.nodes.NodePlatformFactory;
import fi.wsnusbcollect.nodes.SimpleGenericNode;
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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
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
 * SerialNodeForwarder main class.
 * 
 * Serves as intermediate element for data flow from server connected to sensor nodes.
 * Runs on server connected to sensor nodes, provides proxy (serialForwarder) for each 
 * sensor node. (it needs modified version of TinyOS java SDK - for timestamping of messages)
 * It starts listener for each connected server node and provides serialForwarder for them.
 * Remote user can then connect to nodes not previously accessible via network. 
 * 
 * This application also provides RMI interface for simple tasks that can be done only 
 * on server side and not via serialForwarder - for instance hardware node reset.
 *
 * @author ph4r05
 */
public class SenslabForwarder implements RemoteForwarderWork, AppIntf {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(SenslabForwarder.class);
    
    // file path separator
    public static final String pathSeparator = System.getProperty("file.separator");
    
    // service name registered in RMI registry
    public static final String RMI_SERVICE_NAME = "RemoteForwarder";
    
    // main properties object
    Properties props = null;
    
    // Spring application context - dependency injector
    ApplicationContext appContext = null;
    
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--debug", aliases = {"-d"}, usage = "enables debug output")
    private boolean debug;
    
    @Option(name = "--rmi-server", usage = "starts RemoteMethodInvocation (RMI) server")
    private boolean rmiServer=false;
    
    @Option(name = "--rmi-registry-port", usage = "port for RMI registry service to start")
    private Integer rmiRegistryPort=29998;
    
    @Option(name = "--rmi-server-port", usage = "port for RMI server to start")
    private Integer rmiServerPort=29999;
    
    @Option(name = "--no-shell", usage = "disables python shell. It consumes a lot of memory, usefull for testing, not for production use")
    private boolean noShell=false;
    
    @Option(name = "-c", usage = "read configuration from this config file")
    private File configFile = null;
    
    @Option(name = "--motes", usage = "comma separated list of motes serial numbers to use in experiment. If ALL present, all defined nodes will be used")
    private String useMotesString = null;
    
    @Option(name = "--port", aliases = {"-p"}, usage = "determines start port to start forwarders on - my forwarders from connected nodes")
    private Integer port=30000;
    
    @Option(name = "--connection-type", usage="connection to use with packet listeners to connect to node (serial, network, sf)")
    private String cnType="network";
    
    @Option(name = "--hostname", usage="hostname to connect to - with all nodes connected in testbed (default: experiment)")
    private String host="experiment";
    
    @Option(name="--connectPort", usage="port offset for data source packet listener (on hostname)")
    private Integer connectPort=30000;
    
    @Option(name = "--ignore-motes", usage = "comma separated list of motes serial numbers to ignore in experiment.")
    private String ignoreMotesString = null;
    
    @Option(name = "--senslab", usage = "environment is senslab - specific mote connection")
    private boolean senslab=true;
    
    @Option(name="--timesync", usage="enables serial cable timesync - sets current time to nodes")
    private boolean timesync=false;
    
    @Option(name="--timesync-delay", usage="specify interval in milliseconds of synchronization message send from application to nodes")
    private int timesyncDelay=1000;
    
    @Option(name="--rtt-test", usage="perform RTT test on all connected nodes, how many cycles?")
    private int rttTest=0;
    
    @Option(name="--direct-rtt-test", usage="perform RTT test on all connected nodes, how many cycles? Directly connects to TCP")
    private int directRttTest=0;
    
    @Option(name="--time-sync-test", usage="perform time sync test on all connected nodes, how many?")
    private int timeSyncTest=0;
    
    @Option(name="--periodic-msg-test", usage="perform test on delays between periodically sent messages")
    private boolean periodicMsgTest=false;
    
    @Option(name="--direct-periodic-msg-test", usage="perform test on delays between periodically sent messages, connect directly to TCP socket")
    private boolean directPeriodicMsgTest=false;
    
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
    
    // jython console
    private Console console;
    
    // parsed config file
    protected Wini ini;
    protected String configFileContents;
    
    /**
     * application configuration wrapper
     */
    protected AppConfiguration appConfig;
    
    /**
     * RMI registry
     */
    protected Registry registry=null;
    
    /**
     * Timer synchronizer object
     */
    TimerSynchronizer timerSynchronizer;
    
    /**
     * Node config records for nodes to connect to SF created
     */
    Map<Integer, NodeConfigRecord> nodesOutSF;
    
    public static void main(String[] args) {
        log.info("Starting forwarder application");
        try {
            // some inits in static scope
            // ...

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }       
            
            // do main on instance
            SenslabForwarder.runningInstance = new SenslabForwarder();
            RunningApp.setRunningInstance(runningInstance);
            
            // do main work here
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
        
        if (this.noShell==false){
            // initialize console
            ConsoleHelperImpl consoleHelper = new ConsoleHelperImpl();
            console = new ConsoleImpl();
            console.setConsoleHelper(consoleHelper);
            console.setUsbArbitrator(usbArbitrator);
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
        
        // RMI server?
        if (this.rmiServer){
            try {
                // init RMI service, create engine and export it to public
                String name = RMI_SERVICE_NAME;
                RemoteForwarderWork engine = (RemoteForwarderWork) SenslabForwarder.runningInstance;
                RemoteForwarderWork stub = (RemoteForwarderWork) UnicastRemoteObject.exportObject(engine, rmiServerPort);               
                // start RMI registry service - this is diferent service as RMI service, needs separate port
                registry = LocateRegistry.getRegistry(null, rmiRegistryPort);
                // register service name to registry
                registry.rebind(name, stub);
                log.info("RemoteForwarderWork bound, registryPort: " + rmiRegistryPort + "; serverPort: " + rmiServerPort);
            } catch(Exception ex){
                log.error("Exception occurred during RMI server start", ex);
            }
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
        nodesOutSF = new HashMap<Integer, NodeConfigRecord>();
        
        // structure to hold forwarders
        List<SerialForwarder> forwarders = new LinkedList<SerialForwarder>();
        
        // nodeids, now build connection strings
        for(NodeConfigRecord ncr : nodes2connect){
            String connectionString = this.cnType + "@" + this.host + ":" + (this.connectPort + ncr.getNodeId());
            ncr.setConnectionString(connectionString);
            
            nodesOutSF.put(ncr.getNodeId(), ncr);
            log.info("Built connection string info for node: " + ncr.getNodeId() + "; ConnectionString: " + connectionString);
        }
        
        // do we want test speed of serial forwarder or RTT before SF?
        // now is the right time (SF latency determined:))
        if (this.directPeriodicMsgTest){
            log.info("Direct periodic msg test NOW");
            this.periodicMessageTest();
        }
        
        if (this.directRttTest>0){
            log.info("Direct RTT test NOW");
            this.rttTest();
        }
        
        // create serial forwarders and connect to
        // clear nodesOutSF
        this.nodesOutSF.clear();
        // rebuild, now based on serial forwarder
        for(NodeConfigRecord ncr : nodes2connect){    
            String connectionString = ncr.getConnectionString();
            log.info("Going to connect to node: " + ncr.getNodeId() + "; ConnectionString: " + connectionString);
            
            // now we have complete node connect list, it remains only to create corresponding serial forwarders
            SerialForwarder tmpSf = SerialForwarder.newObjInstance(connectionString, this.port + ncr.getNodeId());
            forwarders.add(tmpSf);
            
            log.info("Starting listen server for node on port: " + (this.port + ncr.getNodeId()));
            tmpSf.startListenServer();
            //tmpSf.getListener();
            
            // create new node connection string to connect to created SF
            NodeConfigRecord newNodeCRF = (NodeConfigRecord) ncr.clone();
            newNodeCRF.setConnectionString("sf@127.0.0.1:" + (this.port + ncr.getNodeId()));
            nodesOutSF.put(newNodeCRF.getNodeId(), newNodeCRF);
        }
        
        log.info("Starting is over now, going to run forever");
        
        // wait for server initialization finish
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
        
        // timesync?
        if (this.timesync){
            this.initTimeSync(timesyncDelay);
        }
        
        // tests?
        if (this.rttTest>0){
            this.rttTest();
        }
        
        if (this.timeSyncTest>0){
            this.timeSyncTest();
        }
        
        if (this.periodicMsgTest){
            this.periodicMessageTest();
        }
        
        // shell or block?
        if (noShell){
            // while loop - never ending:)
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    log.error("Cannot sleep here", ex);
                    break;
                }
            }
        } else {
            // prepare shell
            this.console.prepareShell();
            this.console.setShellAlias("_main", this);
            this.console.getShell();
            log.info("Shell terminated, exiting application...");
        }
        
        /**
         * Turn off all senders
         */
        for(SerialForwarder tmpSf : forwarders){
            try {
                tmpSf.stopListenServer();
            } catch(Exception e){
                
            }
        }
       
        log.info("All forwarders stopped its operation");
    }
    
    /**
     * Burn in computing
     */
    public void burn(){
        for(int i=0;;i++){
            double log1 = Math.log(i * Math.sin(i));
        }
    }
    
    /**
     * Performs whole time sync test
     */
    public void timeSyncTest(){
        TimeSyncTester timeSyncTester = this.getTimeSyncTester();
        
        log.info("Registering timesync listeners");
        timeSyncTester.init();
        
        for(int i=0; i<this.timeSyncTest; i++){
            log.info("Starting timesync test, cycle: " + i + "/" + this.timeSyncTest);
            timeSyncTester.test();
        }
        
        log.info("TimeSync test finished, deinit...");
        timeSyncTester.deinit();
        
        log.info("TimeSync finished");
    }
    
    /**
     * Returns constructed time sync tester. Not initialized (registered as listener)
     * @return 
     */
    public TimeSyncTester getTimeSyncTester(){
        // connect to every node at first
        Map<Integer, MoteIF> nodeCon = new HashMap<Integer, MoteIF>();
        for(Integer nodeId : this.nodesOutSF.keySet()){
            nodeCon.put(nodeId, this.getMoteIF(nodeId));
        }
        
        TimeSyncTester tester = new TimeSyncTester(nodesOutSF, nodeCon);
        return tester;
    }
    
    /**
     * Perform periodic message receiving and storing gaps between received messages
     * This could help to analyze delays between sending each message, delay induced by
     * network forwarding, delay induced in message sending by sensor node, inaccuracy of 
     * node crystal based timer
     */
    public void periodicMessageTest(){
        Map<Integer, PeriodicMessageTester> testers = new HashMap<Integer, PeriodicMessageTester>();
        
        log.info("Going to initialize RTT testers");
        for(Integer nodeId : this.nodesOutSF.keySet()){
            testers.put(nodeId, this.getPeriodicMessageTester(nodeId));
        }
        
        // build mass tester
        log.info("Going to create mass tester");
        MassPeriodicMessageTester massTester = new MassPeriodicMessageTester(testers);
        massTester.init();
        
        // start collecting
        massTester.test();
        
        //
        massTester.deinit();
    }
    
    public PeriodicMessageTester getPeriodicMessageTester(int nodeid){
        if (this.nodesOutSF.containsKey(nodeid)==false){
            log.error("Given id is not in database");
            return null;
        }
        
        
        MoteIF moteif = this.getMoteIF(nodeid);
        if (moteif==null){
            log.error("Cannot obtain connection to node");
            return null;
        }
        
        PeriodicMessageTester tester = new PeriodicMessageTester(nodeid, moteif);
        return tester;
    }
    
    /**
     * Performs RTT test on all connected nodes
     */
    public void rttTest(){
        Map<Integer, RTTtester> testers = new HashMap<Integer, RTTtester>();
        
        log.info("Going to initialize RTT testers");
        for(Integer nodeId : this.nodesOutSF.keySet()){
            testers.put(nodeId, this.getRttTester(nodeId));
        }
        
        // build mass tester
        log.info("Going to create mass tester");
        MassRTTtester massTester = new MassRTTtester(testers);
        massTester.init();
        
        log.info("Starting mass testing");
        massTester.test(10, rttTest);
        
        log.info("Deinit mass test");
        massTester.deinit();
    }
    
    /**
     * Returns fully initialized RTT tester
     * @param nodeid
     * @return 
     */
    public RTTtester getRttTester(int nodeid){
        if (this.nodesOutSF.containsKey(nodeid)==false){
            log.error("Given id is not in database");
            return null;
        }
        
        
        MoteIF moteif = this.getMoteIF(nodeid);
        if (moteif==null){
            log.error("Cannot obtain connection to node");
            return null;
        }
        
        RTTtester tester = new RTTtester(nodeid, moteif);
        return tester;
    }
    
    /**
     * Initializes new moteif object
     * @param nodeid
     * @return 
     */
    public MoteIF getMoteIF(int nodeid){
        if (this.nodesOutSF.containsKey(nodeid)==false){
            log.error("Given id is not in database");
            return null;
        }
        
        NodeConfigRecord ncr = this.nodesOutSF.get(nodeid);
        
        // build custom error mesenger - store error messages from tinyos to logs directly
        TOSLogMessenger messenger = new TOSLogMessenger();
        // instantiate phoenix source
        PhoenixSource phoenix = BuildSource.makePhoenix(ncr.getConnectionString(), messenger);
        MoteIF moteInterface = null;

        // phoenix is not null, can create packet source and mote interface
        if (phoenix != null) {
            // loading phoenix
            moteInterface = new MoteIF(phoenix);
        }

        return moteInterface;
    }
    
    /**
     * Initializes timesync object
     * @param delay 
     */
    protected void initTimeSync(int delay){
        // initialize time synchtonizer and start its execution
        timerSynchronizer = new TimerSynchronizer(nodesOutSF);
        timerSynchronizer.setSynchroDelay(delay);
        timerSynchronizer.start();
    }
    
    /**
     * Reads config file to internal ini object and to string object
     */
    protected void readConfig(){
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
            this.appConfig = new AppConfiguration(ini);
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
    
    public File getConfigFile() {
        return configFile;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getIgnoreMotesString() {
        return ignoreMotesString;
    }

    public List<String> getMoteList2use() {
        return moteList2use;
    }

    public USBarbitrator getUsbArbitrator() {
        return usbArbitrator;
    }

    public String getUseMotesString() {
        return useMotesString;
    }

    public void setUseMotesString(String useMotesString) {
        this.useMotesString = useMotesString;
    }

    @Override
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

    public Registry getRegistry() {
        return registry;
    }

    public String getCnType() {
        return cnType;
    }

    public Integer getConnectPort() {
        return connectPort;
    }

    public Console getConsole() {
        return console;
    }

    public String getHost() {
        return host;
    }

    public static Logger getLog() {
        return log;
    }

    public boolean isNoShell() {
        return noShell;
    }

    public Map<Integer, NodeConfigRecord> getNodesOutSF() {
        return nodesOutSF;
    }

    public Integer getPort() {
        return port;
    }

    public boolean isRmiServer() {
        return rmiServer;
    }

    public int isRttTest() {
        return rttTest;
    }

    public int isTimeSyncTest() {
        return timeSyncTest;
    }

    public TimerSynchronizer getTimerSynchronizer() {
        return timerSynchronizer;
    }

    public boolean isTimesync() {
        return timesync;
    }

    public int getTimesyncDelay() {
        return timesyncDelay;
    }

    /**
     * @param nodes2reset
     * @return
     * @throws RemoteException 
     */
    @Override
    public List<Integer> resetNodes(List<Integer> nodes2reset) throws RemoteException {
        if (nodes2reset==null){
            throw new RemoteException("Cannot be null", new NullPointerException("Cannot reset null list"));
        }
        
        List<Integer> failed = new LinkedList<Integer>();
        log.debug("Reset nodes request arrived");
        
        for(Integer nodeId : nodes2reset){
            if (this.nodesOutSF.containsKey(nodeId)==false){
                failed.add(nodeId);
                continue;
            }
            
            NodeConfigRecord ncr = this.nodesOutSF.get(nodeId);
            Properties properties = new Properties();
            boolean ok = false;
            
            // determine platform
            NodePlatform platform = NodePlatformFactory.getPlatform(ncr.getPlatformId());
            
            // build generic node info
            GenericNode gn = new SimpleGenericNode(true, nodeId);
            gn.setPlatform(platform);
            
            ConnectedNode cn = new ConnectedNode();
            cn.setNodeObj(gn);
            cn.setNodeConfig(ncr);
            cn.setMoteIf(null);
            
            if (senslab){
                // reset node with USB arbitrator - handles environment differences
                ok = usbArbitrator.resetNode(cn, properties);
            } else {
                if (ncr.getDeviceAlias()!=null && ncr.getDeviceAlias().isEmpty()==false){
                    properties.setProperty("preferDevice", ncr.getDeviceAlias());
                }
                
                // reset node with USB arbitrator - handles environment differences
                ok = usbArbitrator.resetNode(cn, properties);
            }
            
            if (ok==false){
                failed.add(nodeId);
            }
        }
        
        return null;
    }

    @Override
    public void enableTimeSync(boolean enable, int timeInterval) throws RemoteException {
        // decide what to do depending on actual state
        if (enable==this.timesync){
            // nothing to do, actual state is that one requested
            this.timerSynchronizer.setSynchroDelay(timeInterval);
            log.info("Changed timeout for timesync from remote procedure");
            
            return;
        }
        
        // want to enable/disable?
        if (enable){
            // init new timesync
            this.initTimeSync(timeInterval);
            log.info("Initialized time syncronizer from remote procedure");
        } else {
            // destruct
            this.timerSynchronizer.setTerminate(true);
            this.timerSynchronizer = null;
            log.info("Timesynchronizer disabled from remote procedure");
        }
    }
    
    @Override
    public String simpleRemoteTest(String src) throws RemoteException {
        if (src==null){
            return ":"+System.currentTimeMillis();
        }
        
        return src+":"+System.currentTimeMillis();
    }
    
    @Override
    public AppConfiguration getConfig() {
        return this.appConfig;
    }
}
