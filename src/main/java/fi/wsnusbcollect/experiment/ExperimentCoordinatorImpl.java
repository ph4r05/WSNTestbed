/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.db.ExperimentDataCommands;
import fi.wsnusbcollect.db.ExperimentDataGenericMessage;
import fi.wsnusbcollect.db.ExperimentMultiPingRequest;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.MultiPingMsg;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
import fi.wsnusbcollect.messages.RssiMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author ph4r05
 */
@Repository
@Transactional
public class ExperimentCoordinatorImpl extends Thread implements ExperimentCoordinator, MessageListener{
    private static final Logger log = LoggerFactory.getLogger(ExperimentCoordinatorImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * Persistence context for event threads - cannot use global em in event handlers.
     * Events are called from another event-watching threads, if main loop works with em
     * at the same time it would cause race condition. Event handlers should SYNCHRONIZE 
     * on this object to avoid same problem with em.
     */
    private EntityManager emThread;
//    
//    @Resource(name="experimentCoordinator")
    private ExperimentCoordinator me;
    
    @PersistenceUnit
    private EntityManagerFactory emf;
    
    @Autowired
    private JdbcTemplate template;
    
    //@Autowired
    //@Resource(name="experimentInit")
    protected ExperimentInit expInit;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;
    
    @Autowired
    protected Console console;
    
    protected boolean running=true;
    
    protected boolean suspended=true;
    
    private Message lastMsg;
    
    private TransactionTemplate transactionTemplate;
    
    /**
     * Miliseconds when unsuspended/started
     */
    private Long miliStart;
    
    private ExperimentState eState;
    
    // nodes received identification packet after reset
    private Set<Integer> nodePrepared;
    
    private HashMap<Integer, Integer> lastNodeAliveCounter;

    public ExperimentCoordinatorImpl(String name) {
        super(name);
    }

    public ExperimentCoordinatorImpl() {
        super("ExperimentCoordinatorImpl");
    }

    @PostConstruct
    public void initClass() {
        log.info("Class initialized");
        
        // create new entitymanager
//        log.info("Creating entityManager for thread from factory: " + this.emf.toString());
        //this.emThread = this.emf.createEntityManager();
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
        this.expInit = (ExperimentInit) App.getRunningInstance().getExpInit();
        log.warn("ThreadID: " + this.getId());
        
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
    
    /**
     * Performs hw reset for all nodes where applicable
     */
    @Override
    public void hwresetAllNodes(){
        this.nodeReg.hwresetAll();
    }
    
    @Override
    public void hwresetNode(int nodeId){
        if (this.nodeReg.containsKey(nodeId)==false) return;
        NodeHandler nh = this.nodeReg.get(nodeId);
        
        if (ConnectedNode.class.isInstance(nh)){
            final ConnectedNode cnh = (ConnectedNode) nh;
            if (cnh.hwresetPossible()){
                cnh.hwreset();
            }
        }
    }
    
    /**
     * Send reset packet to all registered nodes SEQUENTIALY
     */
    @Override
    public void resetAllNodes(){
        Collection<NodeHandler> values = this.nodeReg.values();
        Iterator<NodeHandler> iterator = values.iterator();
        while(iterator.hasNext()){
            NodeHandler nh = iterator.next();
            this.sendReset(nh.getNodeId());
        }
    }
    
    @Override
    public void resetNode(int nodeId){
        if (this.nodeReg.containsKey(nodeId)==false) return;
        this.sendReset(nodeId);
    }
    
    /**
     * Initialize fresh started node - push settings and so ...
     * Current implementation: start again noise floor reading
     * @param nodeId 
     */
    public void nodeStartedFresh(int nodeId){
        if (this.nodeReg.containsKey(nodeId)==false) return;
        this.sendNoiseFloorReading(nodeId, 3000);
    }
    
    @Transactional
    @Override
    public void main() {  
        // get transaction manager - programmaticall transaction management
        this.transactionTemplate = new TransactionTemplate((PlatformTransactionManager)App.getRunningInstance().getAppContext().getBean("transactionManager"));        
        this.me = (ExperimentCoordinator) App.getRunningInstance().getAppContext().getBean("experimentCoordinator");
        this.em = me.getEm();
        
        // at first reset all nodes before experiment count
        System.out.println("Restarting all nodes before experiment, "
                + "waiting nodes to reinit (need to receive IDENTITY message from everyone)");
        // here we should wait to receive identification from all nodes
        this.nodePrepared = new HashSet<Integer>(this.nodeReg.size());
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.CommandMsg(), this);       
        this.nodeReg.setDropingReceivedPackets(false);
        this.lastNodeAliveCounter = new HashMap<Integer, Integer>();
        
        // restart all nodes
        this.resetAllNodes();
        long restartStartedMili = System.currentTimeMillis();
        // check all nodes are prepared
        while(true){
            int preparedCount = 0;
            synchronized(this.nodePrepared){
                preparedCount=this.nodePrepared.size();
            }
            
            // compare size preparedNodes vs. all nodes
            if (preparedCount==this.nodeReg.size()){
                log.info("All nodes prepared, starting experiment");
                System.out.println("All nodes prepared, starting experiment");
                break;
            }
            
            // timeouted?
            long nowMili = System.currentTimeMillis();
            if (nowMili-restartStartedMili > 180000){
                log.warn("Node prepare cycle wait expired");
                System.out.println("Node prepare cycle wait expired");
                break;
            }
            
            // sleep now, wait
            this.pause(500);
        }

        this.miliStart = System.currentTimeMillis();
        this.expInit.updateExperimentStart(miliStart);
        log.info("Experiment started, miliseconds start: " + miliStart);        
        // unsuspend all packet listeners to start receiving packets
        log.info("Setting ignore received packets to FALSE to start receiving");
        // register node coordinator as message listener
        System.out.println("Register message listener for commands and pings");
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.PingMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.RssiMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.NoiseFloorReadingMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingMsg(), this);
//         this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseReportMsg(), this);
        
        // work, inform user that experiment is beginning
        System.out.println("Starting main experiment logic...");       
        //
        // from configuration
        //
        
        // how often to receive noise floor reading?
        int noiseFloorReadingTimeout=3000;
        // how many miliseconds can be node unreachable to be considered as unresponsive
        int nodeAliveThreshold=5000;
        // last node alive check time - not to write warning each miliseconds, reasonable
        // notify intervals
        long nodeAliveLastCheck=0;
        // packets requested
        int packetsRequested = 100;
        // in miliseconds
        int packetDelay = 100;
        // time needed for nodes to transmit (safety zone 1 second)
        long timeNeededForNode = packetsRequested*packetDelay + 1000;
        // init message sizes - should be from config file
        ArrayList<Integer> messageSizes = new ArrayList<Integer>();
        messageSizes.add(0);
        messageSizes.add(8);
        messageSizes.add(16);
        messageSizes.add(24);
        
        // init experiment state
        this.eState = new ExperimentState();
        this.eState.setPacketDelay(packetDelay);
        this.eState.setPacketsRequested(packetsRequested);
        this.eState.setNodeReg(nodeReg);
        this.eState.setMessageSizes(messageSizes);
        this.eState.setTimeNeededForNode(timeNeededForNode);
        
        // instructing all nodes to collect noise floor values
        log.info("Instructing all nodes to do noise floor readings");
        for(NodeHandler nh : this.nodeReg.values()){
            this.sendNoiseFloorReading(nh.getNodeId(), noiseFloorReadingTimeout);
        }
                
        // number of successfully finished cycles from last reset
        // when moving backwards (node freeze) it helps not to repeat older and older 
        // experiments
        int succCyclesFromLastReset=0;
        
        // main running cycle, experiment can be shutted down setting running=false
        log.info("Starting main collecting cycle, one sending block: " + timeNeededForNode + " ms");
        while(running){
            //
            // Experiment state
            //
            
            // next state
            this.eState.next();
            int curNode = this.eState.getCurrentNodeHandler().getNodeId();
            int curTx = this.eState.getCurTxPower();
            int msgSize = this.eState.getCurMsgSize();
                        
            log.info("Sending new ping request for node " + curNode + "; "
                    + "curTx=" + curTx + "; msgSize=" + msgSize + "; succCycles: " + succCyclesFromLastReset);            
            // now send message request
            this.sendMultiPingRequest(curNode, curTx, 0,
                    packetsRequested, packetDelay, msgSize, true, true);
            // wait
            this.pause(timeNeededForNode);
            // send request stopping all transfer
            this.sendMultiPingRequest(curNode, curTx, 0,
                    1, 0, msgSize, true, true);
            this.pause(1000);
            
            //
            // node alive monitor
            //
            long timeNow = System.currentTimeMillis();
            if ((timeNow - nodeAliveLastCheck) > 2000){
                List<Integer> nodesLastResponse = getNodesLastResponse(nodeAliveThreshold);
                if (nodesLastResponse!=null && nodesLastResponse.isEmpty()==false){
                    // some unreachable nodes detected
                    log.warn("There are some unreachable nodes here: (mili="+timeNow+"), restarting");
                    for(Integer unreachableNodeId : nodesLastResponse){
                        NodeHandler nh = this.nodeReg.get(unreachableNodeId);
                        log.warn("NodeID: " + nh.getNodeId() 
                                + "; Obj: " + nh.getNodeObj().toString() 
                                + "; lastSeen: " + nh.getNodeObj().getLastSeen());
                        
                        boolean resetDone=false;
                        
                        // determine adequate way of restart
                        if(ConnectedNode.class.isInstance(nh)){
                            // connected node needs special manipulation
                            final ConnectedNode cn = (ConnectedNode) nh;
                            long nodeDelay = timeNow - nh.getNodeObj().getLastSeen();
                            
                            // decide what to do depending on timeout
                            if (nodeDelay<=6000){
                                // maybe is enought to resync
                                cn.setResetQueues(false);
                                cn.reconnect();
                                log.info("Node was reconnected, timeout is not so big.");
                            } else {
                                // latency increased a lot, do hard reset
                                if (cn.hwresetPossible()){
                                    // try HW reset
                                    cn.hwreset();
                                    resetDone=true;
                                    log.info("HW reset performed");
                                } else {
                                    // HW reset not available, send reset message
                                    this.resetNode(nh.getNodeId());
                                    cn.reconnect();
                                    resetDone=true;
                                    log.info("SW reset performed");
                                }
                            }
                        }
                        
                        // reset failed or not a connected node - reset by old way
                        if (resetDone==false){
                            this.resetNode(nh.getNodeId());
                            log.info("SW reset performed");
                        }
                        
                        // sleep, wait for node initialization
                        this.pause(1000);
                        this.nodeStartedFresh(nh.getNodeId());
                    } // end of foreach nodes
                    
                    // if here, need to repeat last 2 experiments
                    log.info("Need to repeat last 2 experiments, moving backward");
                    this.eState.prev(succCyclesFromLastReset > 3 ? 3 : (succCyclesFromLastReset+1));
                    
                    nodeAliveLastCheck = timeNow;
                    succCyclesFromLastReset=0;
                } // end of non-empty failed nodes
                else {
                    succCyclesFromLastReset++;
                }
            } else {
                succCyclesFromLastReset++;
            }   
        }
        
        // shutdown all registered nodes... Deregister and shutdown listening/sending threads
        System.out.println("Shutting down all registered nodes");
        this.nodeReg.shutdownAll();
        // final message for user
        System.out.println("Exiting... Returning control to main application...");
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }

    @Override
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
////        System.out.println("Message received: " + i);
//        log.info("Message received: " + i + "; type: " + msg.amType()
//                + "; dataLen: " + msg.dataLength() 
//                + "; hdest: " + msg.getSerialPacket().get_header_dest()
//                + "; hsrc: " + msg.getSerialPacket().get_header_src() 
//                + "; mili: " + mili);
        
        // was this message already handled by specific handler?
        boolean genericMessage=true;
        
        // source nodeid
        int nodeIdSrc = msg.getSerialPacket().get_header_src();
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            
            // identification received?
            if (    cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK
                 && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY)
            {               
                // update node last seen
                synchronized(this.nodeReg){
                    this.nodeReg.updateLastSeen(nodeIdSrc, mili);
                }
                
                // check alive sequence for suspicious gaps
                // if gap > 10 and current sequence is under 100, consider node
                // as newly restarted
                synchronized(this.lastNodeAliveCounter){
                    if (this.lastNodeAliveCounter.containsKey(nodeIdSrc)){
                        Integer lastCounter = this.lastNodeAliveCounter.get(nodeIdSrc);
                        
                        if (cMsg.get_command_id() < 100 
                                && ((cMsg.get_command_id()-lastCounter) % 65535) > 10){
                            log.warn("Node " + nodeIdSrc + "; was probably reseted. "
                                    + "Last sequence: " + lastCounter + "; now: " + cMsg.get_command_id());
                            this.nodeStartedFresh(nodeIdSrc);
                        }
                    }
                    
                    this.lastNodeAliveCounter.put(nodeIdSrc, cMsg.get_command_id());
                }
                
                // nodes prepared after reset
                // must synchronize over this Set - different thread from main execution thread
                synchronized(this.nodePrepared){
                    // first node identification after reset? 
                    // add to prepared set - indicates nodes was restarted successfully
                    if  (this.nodePrepared!=null 
                            && cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK
                            && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY
                            && this.nodePrepared.contains(nodeIdSrc)==false
                            ){
                        log.info("NodeId: " + nodeIdSrc + " is now prepared to communicate");
                        this.nodePrepared.add(nodeIdSrc);
                    }
                }
            }
            
            genericMessage=false;
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            //System.out.println("Report message: " + cMsg.toString());
            log.info("Report message: " + cMsg.toString());
            
            genericMessage=false;
        }
        
        // noise floor message
        if (NoiseFloorReadingMsg.class.isInstance(msg)){
            final NoiseFloorReadingMsg nMsg = (NoiseFloorReadingMsg) msg;
            log.info("NoiseFloorMessage: " + nMsg.toString());
            
            genericMessage=false;
        }
        
        // rssi message
        if (RssiMsg.class.isInstance(msg)){
            final RssiMsg rMsg = (RssiMsg) msg;
            //System.out.println("RSSI message: " + rMsg.toString());
            log.info("RSSI message: " + rMsg.toString());
        }
        
        // generic message was probably not really handled -> store to protocol
        if (genericMessage){
            this.storeGenericMessageToProtocol(msg, i, false, true);            
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
    @Override
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
        mpr.setNode(nodeId);
        mpr.setNodeBS(nodeId);
        mpr.loadFromMessage(msg);
        me.storeData(mpr);
        
        // add message to send
        this.sendMessageToNode(msg, nodeId, false);
    }
    
    /**
     * Sends command message to node. Packet is stored to database
     * @param payload
     * @param nodeId 
     */
    @Transactional
    @Override
    public synchronized void sendCommand(CommandMsg payload, int nodeId){
        // store to database here
        // now build database record for this request
        ExperimentDataCommands mpr = new ExperimentDataCommands();
        mpr.setMilitime(System.currentTimeMillis());
        mpr.setExperiment(this.expInit.getExpMeta());
        mpr.setNode(nodeId);
        mpr.setNodeBS(nodeId);
        mpr.setSent(true);
        mpr.loadFromMessage(payload);
        me.storeData(mpr);
        
        this.sendMessageToNode(payload, nodeId, false);
    }
    
    /**
     * Send reset message
     * @param nodeId 
     */
    @Override
    public synchronized void sendReset(int nodeId){
        CommandMsg msg = new CommandMsg();
        msg.set_command_code((short) MessageTypes.COMMAND_RESET);
        
        //this.sendCommand(msg, nodeId);
        this.sendCommand(msg, nodeId);
    }
    
    /**
     * Set noise floor reading packet to node
     * @param nodeId
     * @param delay 
     */
    @Override
    public synchronized void sendNoiseFloorReading(int nodeId, int delay){
        CommandMsg msg = new CommandMsg();
        msg.set_command_code((short) MessageTypes.COMMAND_SETNOISEFLOORREADING);
        msg.set_command_data(delay);
        
        this.sendCommand(msg, nodeId);
    }
    
    /**
     * Send selected defined packet to node.
     * Another methods may build custom command packet, it is then passed to this method
     * which sends it to all selected nodes
     * 
     * @param payload    data packet to send. Is CommandMessage
     * @param nodeId     nodeId to send message to
     * @param protocol   if yes then message is written to generic message protocol
     */    
    @Override
    public synchronized void sendMessageToNode(Message payload, int nodeId, boolean protocol){
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
            
            // store to protocol?
            if (protocol){
                this.storeGenericMessageToProtocol(payload, nodeId, true, false);
            }
            
            // add to send queue
            log.debug("Message to send for node: " + nId + "; Command: " + payload);
            nh.addMessage2Send(payload, null);
        }  catch (Exception ex) {
            log.error("Cannot send CmdMessage to nodeId: " + nodeId, ex);
        }
    }
    
    /**
     * Stores generic message to protocol log
     * @param payload
     * @param nodeId 
     * @param sent  if true message is marked as sent from application, otherwise 
     *              message is received to application
     * @param external if true => method was invoked from different thread from main, then is used emThread
     */
    //@Transactional
    @Override
    public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external){
        final ExperimentDataGenericMessage gmsg = new ExperimentDataGenericMessage();
        gmsg.setMilitime(System.currentTimeMillis());
        gmsg.setExperiment(this.expInit.getExpMeta());
        gmsg.setNode(nodeId);
        gmsg.setNodeBS(nodeId);
        gmsg.setSent(sent);

        gmsg.setAmtype(payload.amType());
        gmsg.setLen(payload.dataLength());
        gmsg.setStringDump(payload.toString());

        synchronized (this.em) {
            if (true || external) {
//                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                    // the code in this method executes in a transactional context
//                    @Override
//                    protected void doInTransactionWithoutResult(TransactionStatus status) {
//                        em.persist(gmsg);
//                        em.flush();
//                    }
//                });
                me.storeData(gmsg);
            } else {
                this.em.persist(gmsg);
                this.em.flush();
            }
        }
    }
    
    /**
     * Returns nodes that have (NOW()-lastSeen) > mili. Time from last seen is 
     * greater than mili
     * 
     * @param mili 
     */
    @Override
    public List<Integer> getNodesLastResponse(long mili){
       long currTime = System.currentTimeMillis();
       List<Integer> lnodeList = new LinkedList<Integer>();
       
       Collection<NodeHandler> nhvals = this.nodeReg.values();
        if (nhvals.isEmpty()){
            System.out.println("Node register is empty");
            return lnodeList;
        }

        for(NodeHandler nh : nhvals){
            long lastSeen = nh.getNodeObj().getLastSeen();
            if ((currTime-lastSeen) > mili){
                lnodeList.add(nh.getNodeId());
            }
        }
        
        return lnodeList;
    }
    
    /**
     * Prints node's last seen values
     * @param seconds 
     */
    @Override
    public void getNodesLastSeen(){
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
    
    /**
     * Pause execution of this thread for specified time
     * @param mili 
     */
    public void pause(long mili){
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep " + ex);
        }
    }
    
    /**
     * Pause execution of this thread for specified time
     * @param mili
     * @param nano 
     */
    public void pause(long mili, int nano){
        try {
            Thread.sleep(mili, nano);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep " + ex);
        }
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

    /**
     * Unsuspends experiment. If specified, experiment is suspended before main
     * logic start to prepare environment and update some settings via console. 
     * When is everything prepared, unsuspend is called. As a consequence suspend 
     * sleep cycle is ended and main experiment method is called.
     */
    @Override
    public synchronized void unsuspend(){
        this.suspended=false;
    }

    /**
     * Gets last received message - for console use. 
     * Has no special purpose in main logic, only informative output
     */
    public Message getLastMsg() {
        return lastMsg;
    }
    
    /**
     * Setting this to false will exit main loop
     * @param running 
     */
    public synchronized void setRunning(boolean running) {
        this.running = running;
    }
    
    /**
     * Returns state of running flag. If false => thread probably exited
     * If true thread my or may not be running
     * @return 
     */
    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized EntityManager getEm() {
        return em;
    }

    @PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }
    
    @Override
    public void storeData(Object o){
        synchronized(this.em){
            try {
                this.em.persist(o);
                this.em.flush();
            } catch(Exception e){
                log.error("Cannot persist object, exception thrown", e);
            }
        }
    }
    
    @Override
    public synchronized void emRefresh(Object o) {
        em.refresh(o);
    }

    @Override
    public synchronized void emPersist(Object o) {
        em.persist(o);
    }

    @Override
    public synchronized void emFlush() {
        em.flush();
    }

    @Override
    public ExperimentState geteState() {
        return eState;
    }

    @Override
    public void seteState(ExperimentState eState) {
        this.eState = eState;
    }
    
    
}
