/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.db.ExperimentMultiPingRequest;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.MultiPingMsg;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
import fi.wsnusbcollect.messages.RssiMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ph4r05
 */
@Repository
public class ExperimentCoordinatorImpl extends Thread implements ExperimentCoordinator, MessageListener{
    private static final Logger log = LoggerFactory.getLogger(ExperimentCoordinatorImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    //@Autowired
    //@Resource(name="experimentInit")
    protected ExperimentInitImpl expInit;
    
    @Autowired
    protected NodeHandlerRegister nodeReg;
    
    @Autowired
    protected Console console;
    
    protected boolean running=true;
    
    protected boolean suspended=true;
    
    private Message lastMsg;
    
    /**
     * Miliseconds when unsuspended/started
     */
    private Long miliStart;

    public ExperimentCoordinatorImpl(String name) {
        super(name);
    }

    public ExperimentCoordinatorImpl() {
        super("ExperimentCoordinatorImpl");
    }

    @PostConstruct
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public synchronized void interrupt() {
        this.running=false;
        this.nodeReg.shutdownAll();
        super.interrupt();
    }

    // if has shell, needs to spawn new thread
    // without shell it is not necessary
    @Override
    public void work() {
        this.expInit = (ExperimentInitImpl) App.getRunningInstance().getExpInit();
        
        if (App.getRunningInstance().isShell()){
            this.start();
        } else {
            this.run();
        }
    }

    @Override
    public void run() {
        // start suspended?
        if (App.getRunningInstance().isStartSuspended()){
            this.startSuspended();
        }
        
        // main
        this.main();
    }

    /**
     * Holds suspended until script allows execution
     */
    public void startSuspended(){
        System.out.println("Waiting in suspend sleep. To start call unsuspend().");
        try {
            while(suspended){
                Thread.sleep(100L);
                Thread.yield();
            }
            
            System.out.println("Suspend sleep stopped.");
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
    }
    
    public void main() {  
        this.miliStart = System.currentTimeMillis();
        this.expInit.updateExperimentStart(miliStart);
        log.info("Experiment started, miliseconds start: " + miliStart);
        
        // unsuspend all packet listeners to start receiving packets
        log.info("Setting ignore received packets to FALSE to start receiving");
        this.nodeReg.setDropingReceivedPackets(false);
        
        // register node coordinator as message listener
        System.out.println("Register message listener for commands and pings");
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.CommandMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.PingMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.RssiMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.NoiseFloorReadingMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseReportMsg(), this);
        
        // work
        System.out.println("Sleeping in infinite cycle...");
        try {
            while(running){
                Thread.sleep(10L);
                Thread.yield();
            }
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
        
        // shutdown all registered nodes...
        System.out.println("Shutting down all registered nodes");
        this.nodeReg.shutdownAll();
        
        System.out.println("Exiting... Returning controll to main application...");
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInitImpl expInit) {
        this.expInit = expInit;
    }

    public NodeHandlerRegister getNodeReg() {
        return nodeReg;
    }

    public void setNodeReg(NodeHandlerRegister nodeReg) {
        this.nodeReg = nodeReg;
    }

    /**
     * Message received event handler
     * !!! WARNING:
     * Please keep in mind that this method is executed by separate thread - 
     *  - messageListener notifier. Take a caution to avoid race conditions and concurrency 
     * problems.
     * 
     * EntityManager instance is NOT thread-safe, so this method cannot directly use
     * em instance from class attribute. New entityManager is needed.
     * 
     * 
     * @param i
     * @param msg 
     */
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        //System.out.println("Message received: " + i);
        log.info("Message received: " + i + "; type: " + msg.amType()
                + "; dataLen: " + msg.dataLength() 
                + "; hdest: " + msg.getSerialPacket().get_header_dest()
                + "; hsrc: " + msg.getSerialPacket().get_header_src() 
                + "; mili: " + mili);
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            //System.out.println("Command message: " + cMsg.toString());
            log.info("Command message: " + cMsg.toString());
            
            // is alive / identification packet?
            if ((cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK) 
                    && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY){
                // identify message, insert new entity to database
                
                
                
                
            }
            
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            //System.out.println("Report message: " + cMsg.toString());
            log.info("Report message: " + cMsg.toString());
        }
        
        // rssi message
        if (RssiMsg.class.isInstance(msg)){
            final RssiMsg rMsg = (RssiMsg) msg;
            //System.out.println("RSSI message: " + rMsg.toString());
            log.info("RSSI message: " + rMsg.toString());
        }
        
        // noise floor message
        if (NoiseFloorReadingMsg.class.isInstance(msg)){
            final NoiseFloorReadingMsg nMsg = (NoiseFloorReadingMsg) msg;
            log.info("NoiseFloorMessage: " + nMsg.toString());
        }
        
        this.lastMsg = msg;
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg, 0);
    }
    
    /**
     * Sends multi ping request to specified node. Sending packet from this 
     * method is written to db protocol
     * @param nodeId
     * @param txpower
     * @param channel
     * @param packets
     * @param delay
     * @param size
     * @param counterStrategySuccess
     * @param timerStrategyPeriodic 
     */
    @Transactional
    public synchronized void sendMultiPingRequest(int nodeId, int txpower,
            int channel, int packets, int delay, int size, 
            boolean counterStrategySuccess, boolean timerStrategyPeriodic){

	MultiPingMsg msg = new MultiPingMsg();
        msg.set_destination(MessageTypes.AM_BROADCAST_ADDR);
        msg.set_channel((short)channel);
        msg.set_counter(0);
        msg.set_counterStrategySuccess((byte) (counterStrategySuccess ? 1:0));
        msg.set_delay(delay);
        msg.set_packets(packets);
        msg.set_size((short)size);
        msg.set_timerStrategyPeriodic((byte) (timerStrategyPeriodic ? 1:0));
        msg.set_txpower((short)txpower);
        
        // now build database record for this request
        ExperimentMultiPingRequest mpr = new ExperimentMultiPingRequest();
        mpr.setMiliFromStart(System.currentTimeMillis());
        mpr.setExperiment(this.expInit.getExpMeta());
        mpr.loadFromMessage(msg);
        this.em.persist(mpr);
        this.em.flush();
        
        // add message to send
        this.sendMessageToNode(msg, nodeId);
    }
    
    /**
     * Send reset message
     * @param nodeId 
     */
    public synchronized void sendReset(int nodeId){
        CommandMsg msg = new CommandMsg();
        msg.set_command_code((short) MessageTypes.COMMAND_RESET);
        
        this.sendMessageToNode(msg, nodeId);
    }
    
    /**
     * Set noise floor reading packet to node
     * @param nodeId
     * @param delay 
     */
    public synchronized void sendNoiseFloorReading(int nodeId, int delay){
        CommandMsg msg = new CommandMsg();
        msg.set_command_code((short) MessageTypes.COMMAND_SETNOISEFLOORREADING);
        msg.set_command_data(delay);
        
        this.sendMessageToNode(msg, nodeId);
    }
    
    /**
     * Send selected defined packet to node.
     * Another methods may build custom command packet, it is then passed to this method
     * which sends it to all selected nodes
     * 
     * @param CommandMsg payload    data packet to send. Is CommandMessage
     */    
    public synchronized void sendMessageToNode(Message payload, int nodeId){
        try {           
            Integer nId = Integer.valueOf(nodeId);
            // get node from node register
            if (this.nodeReg.containsKey(nId)==false){
                log.error("Cannot send message to node " + nId + "; No such node found in register");
                return;
            }
            
            NodeHandler nh = this.nodeReg.get(nId);
            if (nh.canAddMessage2Send()==false){
                log.error("From some reason message cannot be sent to this node currently, please try again later. NodeId: " + nId);
                return;
            }
            
            // add to send queue
            log.info("CmdMessage to send for node: " + nId + "; Command: " + payload);
            nh.addMessage2Send(payload, null);
        }  catch (Exception ex) {
            log.error("Cannot send CmdMessage to nodeId: " + nodeId, ex);
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Prints node's last seen values
     * @param seconds 
     */
    public void getNodesLastSeen(int seconds){
        Collection<NodeHandler> nhvals = this.nodeReg.values();
        if (nhvals.isEmpty()){
            System.out.println("Node register is empty");
            return;
        }
        
        // human readable date formater
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        
        System.out.println("LastSeen indicators: ");
        Iterator<NodeHandler> iterator = nhvals.iterator();
        
        while(iterator.hasNext()){
            NodeHandler nh = iterator.next();
            long lastSeen = nh.getNodeObj().getLastSeen();
            
            // convert last seen to human readable format
            Date date = new Date(lastSeen);
            System.out.println("NodeID: " + nh.getNodeId() + ";\t LastSeen: " + 
                formatter.format(date) + ";\t type: " + nh.getType());
        }
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public synchronized void unsuspend(){
        this.suspended=false;
    }

    public Message getLastMsg() {
        return lastMsg;
    }
}
