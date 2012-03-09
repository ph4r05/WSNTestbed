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
import fi.wsnusbcollect.nodes.NodeHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
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
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
     * Send reset packet to all registered nodes SEQUENTIALY
     */
    public void resetAllNodes(){
        Collection<NodeHandler> values = this.nodeReg.values();
        Iterator<NodeHandler> iterator = values.iterator();
        while(iterator.hasNext()){
            NodeHandler nh = iterator.next();
            this.sendReset(nh.getNodeId());
        }
    }
    
    @Transactional
    @Override
    public void main() {  
        System.out.println("INIT@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        
////        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
////        // explicitly setting the transaction name is something that can only be done programmatically
////        def.setName("SomeTxName");
////        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
////
////        TransactionStatus status = txManager.getTransaction(def);
////        try {
////          // execute your business logic here
////        }
////        catch (MyException ex) {
////          txManager.rollback(status);
////          throw ex;
////        }
////        txManager.commit(status);
//
//        Object holder = TransactionSynchronizationManager.getResource(emf);
//        System.out.println("Holder object from emf: " + holder);
//        EntityManager emx=null;
//        
//        if (holder==null){
//            EntityManagerFactory emfx = (EntityManagerFactory) App.getRunningInstance().getAppContext().getBean("entityManagerFactory");
//            emx = emfx.createEntityManager();
//            
////            emx = emf.createEntityManager();      
//            TransactionSynchronizationManager.bindResource(emfx, new EntityManagerHolder(emx));
//            log.warn("New entityManager bounded + " + emx.toString());
//            log.warn("ThreadID: " + this.getId());
////            
//            this.em = emx;
//        } else {
//            EntityManagerHolder holder2 = (EntityManagerHolder) holder;
//            emx = holder2.getEntityManager();
//            log.warn("Existing entitymanager loaded");
//            log.warn("ThreadID: " + this.getId());
//        }
//       
//        
////        EntityManager emx = holder.getEntityManager();

        this.me = (ExperimentCoordinator) App.getRunningInstance().getAppContext().getBean("experimentCoordinator");
        ExperimentDataGenericMessage edgm = new ExperimentDataGenericMessage();
        edgm.setAmtype(1);
        me.storeData(edgm);
        
        
//        emx.persist(edgm);
//        emx.flush();
        
//        // at first reset all nodes before experiment count
//        this.resetAllNodes();
        System.out.println("All nodes restarted");
        this.em = me.getEm();
        
        
        this.miliStart = System.currentTimeMillis();
        this.expInit.updateExperimentStart(miliStart);
        log.info("Experiment started, miliseconds start: " + miliStart);
        
        // unsuspend all packet listeners to start receiving packets
        log.info("Setting ignore received packets to FALSE to start receiving");
        this.nodeReg.setDropingReceivedPackets(false);
        
        // register node coordinator as message listener
        System.out.println("Register message listener for commands and pings");
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.CommandMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.PingMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.RssiMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.NoiseFloorReadingMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseMsg(), this);
//        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseReportMsg(), this);
        
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
//        
        System.out.println("Exiting... Returning controll to main application...");
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
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
        
        // was this message already handled by specific handler?
        boolean genericMessage=true;
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            //System.out.println("Command message: " + cMsg.toString());
            log.info("Command message: " + cMsg.toString());
            
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
    //@Transactional
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
//        me.getEm().persist(mpr);
//        me.getEm().flush();
        this.em.persist(mpr);
        this.em.flush();
        
        // add message to send
        this.sendMessageToNode(msg, nodeId, false);
    }
    
    /**
     * Sends command message to node. Packet is stored to database
     * @param payload
     * @param nodeId 
     */
    //@Transactional
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
        
        this.storeData(mpr);
//        me.storeData(mpr); 
        
        this.sendMessageToNode(payload, nodeId, false);
    }
    
    /**
     * Send reset message
     * @param nodeId 
     */
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
            log.info("CmdMessage to send for node: " + nId + "; Command: " + payload);
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
    public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external){
        ExperimentDataGenericMessage gmsg = new ExperimentDataGenericMessage();
        gmsg.setMilitime(System.currentTimeMillis());
        gmsg.setExperiment(this.expInit.getExpMeta());
        gmsg.setNode(nodeId);
        gmsg.setNodeBS(nodeId);
        gmsg.setSent(sent);

        gmsg.setAmtype(payload.amType());
        gmsg.setLen(payload.dataLength());
        gmsg.setStringDump(payload.toString());
        
        if (external){
            // entity manager intended for external threads is used
//            synchronized(this.emThread){
//                this.emThread.persist(gmsg);
//                this.emThread.flush();
//            }
        } else {
            synchronized(this.em){
                this.em.persist(gmsg);
                this.em.flush();
            }
        }
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

    public synchronized EntityManager getEm() {
        return em;
    }

    @PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

//    public EntityManager getEmThread() {
//        return emThread;
//    }
//
//    public void setEmThread(EntityManager emThread) {
//        this.emThread = emThread;
//    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }
    
    public void storeData(Object o){
        synchronized(this.em){
            this.em.persist(o);
            this.em.flush();
        }
    }
    
    public synchronized void emRefresh(Object o) {
        em.refresh(o);
    }

    public synchronized void emPersist(Object o) {
        em.persist(o);
    }

    public synchronized void emFlush() {
        em.flush();
    }

//    public EntityManagerFactory getEmf() {
//        return emf;
//    }
//
//    public void setEmf(EntityManagerFactory emf) {
//        this.emf = emf;
//    }
}
