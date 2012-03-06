/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.db.ExperimentMetadata;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.nodeCom.MessageSender;
import fi.wsnusbcollect.nodeCom.MyMessageListener;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.GenericNode;
import fi.wsnusbcollect.nodes.NodePlatform;
import fi.wsnusbcollect.nodes.NodePlatformFactory;
import fi.wsnusbcollect.nodes.SimpleGenericNode;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import fi.wsnusbcollect.usb.USBarbitrator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import net.tinyos.util.PrintStreamMessenger;
import org.ini4j.Ini;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ph4r05
 */
@Repository
public class ExperimentInitImpl implements ExperimentInit {
    private static final Logger log = LoggerFactory.getLogger(ExperimentInit.class);

    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    protected NodeHandlerRegister nodeReg;
    
    //@Autowired
    protected ExperimentCoordinatorImpl expCoordinator;
    
    @Autowired
    protected Console console;
    
    @Autowired
    protected USBarbitrator usbArbitrator;
    
    // main experiment metadata
    protected ExperimentMetadata expMeta;
    
    // config file prepared by application
    protected Wini config;
    
    private int status=0;
    
    @Autowired
    private ExperimentParameters params;
    
    public static final String INISECTION_MOTELIST="motelist";
    public static final String INISECTION_METADATA="experimentMetadata";
    public static final String INISECTION_PARAMETERS="experimentParameters";
    
    /**
     * Loads configuration to experiment
     */
    public void loadConfig(){
        
    }
    
    @PostConstruct
    @Override
    public void initClass() {
        log.info("Class initialized");
        this.expCoordinator = App.getRunningInstance().getExpCoord();
        
        // default include/exclude motelist from parameters
        // init file has precedense
        String moteInclude = App.getRunningInstance().getUseMotesString();
        String moteExclude = App.getRunningInstance().getIgnoreMotesString();
        
        // stores metadata about experiment
        expMeta = new ExperimentMetadata();
        
        // owner of experiment, determine from system
        String username = System.getProperty("user.name");
        if (username!=null){
            log.info("Found that user running this experiment is: " + username);
            expMeta.setOwner(username);
        }
        
        // read config file
        this.config = App.getRunningInstance().getIni();
        if (this.config!=null){
            // read experiment metadata - to be stored in database
            if (this.config.containsKey(INISECTION_METADATA)==false){
                log.error("Config file has to contain section " + INISECTION_METADATA);
                throw new IllegalArgumentException("Config file has to contain section " + INISECTION_METADATA);
            }
            
            // store config file as raw
            expMeta.setConfigFile(this.config.toString());
            
            // get metadata section from ini file
            Ini.Section metadata = this.config.get("experimentMetadata");
            
            // experiment group
            if (metadata.containsKey("group")){
                expMeta.setExperimentGroup(metadata.get("group"));
            } else {
                expMeta.setExperimentGroup("default");
                log.warn("Cannot found experiment group, using default");
            }
            
            // experiment name
            if (metadata.containsKey("name")){
                expMeta.setName(metadata.get("name"));
            } else {
                log.error("INI file must contain experiment name field");
                throw new IllegalArgumentException("INI file must contain experiment name field");
            }
            
            // annotation
            if (metadata.containsKey("annotation")){
                expMeta.setDescription(metadata.get("annotation"));
            }
            
            // keywords
            if (metadata.containsKey("keywords")){
                expMeta.setKeywords(metadata.get("keywords"));
            }
            
            // read parameters from config file
            this.params.load(this.config);
            
            // include/exclude reading
            if (this.config.containsKey(INISECTION_MOTELIST)){
                Ini.Section motelist = this.config.get(INISECTION_MOTELIST);
                
                if (motelist.containsKey("include")){
                    moteInclude = motelist.get("include");
                }
                
                if (motelist.containsKey("exclude")){
                    moteExclude = motelist.get("exclude");
                }
            }
        }
        
        // config file/arguments parsing, node selectors, get final set of nodes to connect to
        List<NodeConfigRecord> nodes2connect = 
                this.usbArbitrator.getNodes2connect(moteInclude, moteExclude);
        
        // init connected nodes - builds handler and connecto to them
        this.initConnectedNodes(null, nodes2connect);
    }

    /**
     * Stores experiment configuration to database
     */
    public void storeConfig(){
        // store experiment metadata to database
        
        
        // store experiment parameters to database
        this.params.storeToDatabase(expMeta);
    }
    
    @Override
    public void initEnvironment() {
        log.info("Environment initialized");
        
        // here can init shell console
    }

    /**
     * Initialize connected nodes here
     * @param props
     * @param ncr 
     */
    @Override
    public void initConnectedNodes(Properties props, List<NodeConfigRecord> ncr) {
        log.info("initializing connected nodes here");
        if (ncr==null){
            throw new NullPointerException("NCR is null");
        }
        
        Iterator<NodeConfigRecord> iterator = ncr.iterator();
        while(iterator.hasNext()){
            NodeConfigRecord nextncr = iterator.next();
            System.out.println("Node to connect to: " + nextncr.toString());
            
            if (nextncr.getNodeId()==null){
                log.warn("Cannot work with node without defined node ID, please "
                        + "define its node id in database: " + nextncr.toString());
                continue;
            }
            
            // determine platform
            NodePlatform platform = NodePlatformFactory.getPlatform(nextncr);
            // try to connect to node
            MoteIF connectToNode = this.connectToNode(nextncr.getConnectionString());
            if (connectToNode==null){
                log.warn("Cannot connect to node: " + nextncr.toString());
                continue;
            }
            
            // build generic node info
            GenericNode gn = new SimpleGenericNode(true, nextncr.getNodeId());
            gn.setPlatform(platform);
            
            ConnectedNode cn = new ConnectedNode();
            cn.setNodeObj(gn);
            cn.setNodeConfig(nextncr);
            cn.setMoteIf(connectToNode);
            
            // message listener
            MyMessageListener listener = new MyMessageListener(connectToNode);
            listener.setDropingPackets(false);
            
            // message sender
            MessageSender sender = new MessageSender(connectToNode);
            
            cn.setMsgListener(listener);
            cn.setMsgSender(sender);
            
            // add listening to packets here to separate DB listener
            ExperimentData2DB dbForNode = App.getRunningInstance().getAppContext().getBean("experimentData2DB", ExperimentData2DB.class);
            log.info("DB for node is running: " + dbForNode.isRunning());
            cn.registerMessageListener(new CommandMsg(), dbForNode);
            
            // add to map
            this.nodeReg.put(cn);
            
            System.out.println("Initialized connected node: " + cn.toString());
        }
        
        // starting all threads
        System.out.println("Starting all threads");
        this.nodeReg.startAll();
        
        System.out.println("Initialized");
        status=1;
    }
    
    /**
     * Connects to given source and if OK returns mote interface
     * @param source
     * @return 
     */
    public MoteIF connectToNode(String source){
        PhoenixSource phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
        MoteIF moteInterface = null;
        
        // phoenix is not null, can create packet source and mote interface
        if (phoenix != null) {
            // loading phoenix
            moteInterface = new MoteIF(phoenix);
        }
        
        return moteInterface;
    }

    @Override
    public String toString() {
        return "ExperimentInitImpl{" + "status=" + status + '}';
    }

    public NodeHandlerRegister getNodeReg() {
        return nodeReg;
    }

    public void setNodeReg(NodeHandlerRegister nodeReg) {
        this.nodeReg = nodeReg;
    }

    public ExperimentCoordinatorImpl getExpCoordinator() {
        return expCoordinator;
    }

    public void setExpCoordinator(ExperimentCoordinatorImpl expCoordinator) {
        this.expCoordinator = expCoordinator;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public ExperimentMetadata getExpMeta() {
        return expMeta;
    }

    public void setExpMeta(ExperimentMetadata expMeta) {
        this.expMeta = expMeta;
    }

    public ExperimentParameters getParams() {
        return params;
    }

    public void setParams(ExperimentParameters params) {
        this.params = params;
    }   
    
    
}
