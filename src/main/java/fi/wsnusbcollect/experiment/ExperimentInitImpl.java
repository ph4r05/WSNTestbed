/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.nodeCom.MessageSender;
import fi.wsnusbcollect.nodeCom.MyMessageListener;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.GenericNode;
import fi.wsnusbcollect.nodes.NodePlatform;
import fi.wsnusbcollect.nodes.NodePlatformFactory;
import fi.wsnusbcollect.nodes.SimpleGenericNode;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import net.tinyos.util.PrintStreamMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author ph4r05
 */
public class ExperimentInitImpl implements ExperimentInit {
    private static final Logger log = LoggerFactory.getLogger(ExperimentInit.class);

    @Autowired
    protected NodeHandlerRegister nodeReg;
    
    @Autowired
    protected ExperimentCoordinatorImpl expCoordinator;
    
    @Autowired
    protected Console console;
    
    private int status=0;
    
    @PostConstruct
    @Override
    public void initClass() {
        log.info("Class initialized");
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
}
