/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentDataAliveCheck;
import fi.wsnusbcollect.db.ExperimentDataNoise;
import fi.wsnusbcollect.db.ExperimentDataRSSI;
import fi.wsnusbcollect.db.ExperimentMetadata;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.Message;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Default message received handler for connected node.
 * Implements thread in order to run independently and ease database access.
 * 
 * Kind of schizophrenic interface. For batch insert is used hibernate directly...
 * 
 * Received messages are stored to database by its type.
 * @author ph4r05
 */
@Repository
//@Transactional
public class ExperimentData2DBImpl extends Thread implements ExperimentData2DB{
    private static final Logger log = LoggerFactory.getLogger(ExperimentData2DBImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    private SessionFactory sf;
    StatelessSession session2;
    
    @Resource(name="experimentInit")
    protected ExperimentInit expInit;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;
    
    /**
     * ExperimentMetadata copy for local optimization. Should not be written!!
     * It is readonly to optimize fetch time for this information
     */
    protected ExperimentMetadata expMeta;
    
    /**
     * Thread can be gracefully exited by setting this to false
     */
    boolean running=true;
    
    private List<String> sqlQueue = new ArrayList<String>(500);
    
    /**
     * Queue of entities to store
     */
    private List<Object> objQueue = new ArrayList<Object>(500);
    
    /**
     * Threshold for queue flush, maximal time when queues can remain in memory.
     */
    private long miliFLushThreshold = 2000;
    
    /**
     * Miliseconds when last queue flush was performed
     */
    private long miliLastFlush = 0;
    
    /**
     * Messages received from last flush
     */
    int messageFromLastFlush=0;
    
    /**
     * Randomized message threshold flush to distribute flush
     * elimitanes spikes in database load when multiple threads wants to flush in 
     * the same time
     */
    int currentMessageThresholdFlush=100;
    
    int maxMessageThresholdFlush=200;
    int minMessageThresholdFlush=40;
    
    
    @PostConstruct
    @Override
    public void init(){
        log.info("Initialized DB manager");
    }

    @Override
    public void run() {
        log.info("Thread running");
        while(running){
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ex) {
                log.warn("Cannot sleep", ex);
            }
        }
        
        this.em.flush();
        log.info("Finishing");
    }   
    
    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg, 0);
    }
    
    /**
     * Method handling identification received - store ACK message to database, update
     * last seen counter - node is active
     * @param i
     * @param cMsg
     * @param mili 
     */
    @Override
    public void identificationReceived(int i, CommandMsg cMsg, long mili){        
        // @extension: for future use here can be converter from i+message+mili to
        // database entity, now directly here...
        // identify message, insert new entity to database
        ExperimentDataAliveCheck experimentDataAliveCheck = new ExperimentDataAliveCheck();
        experimentDataAliveCheck.setCounter(cMsg.get_command_id());
        experimentDataAliveCheck.setExperiment(expMeta);
        experimentDataAliveCheck.setMiliFromStart(mili);
        experimentDataAliveCheck.setRadioQueueFree(cMsg.get_command_data_next()[2] & 0xFF);
        experimentDataAliveCheck.setSerialQueueFree((cMsg.get_command_data_next()[2] & 0xFF00) >> 8);
        experimentDataAliveCheck.setSerialFails(cMsg.get_command_data_next()[3]);
        
        // @TODO: can be problem in multihop protocol
        experimentDataAliveCheck.setNode(cMsg.getSerialPacket().get_header_src());
        this.objQueue.add(experimentDataAliveCheck);
        
        // update last seen record
        synchronized(this.nodeReg){
            this.nodeReg.updateLastSeen(cMsg.getSerialPacket().get_header_src(), mili);
        }
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        //log.info("Message received: " + msg.amType() +  "; time=" + mili + "; " + this.getName());
        
        messageFromLastFlush+=1;
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            
            // is alive / identification packet?
            if ((cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK) 
                    && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY){
                // notify appropriate method
                this.identificationReceived(i, cMsg, mili);
            } else {
                // print only messages different from identity messages
                log.info("Command message: " + cMsg.toString());
            }
        }
        
        // noise floor message
        if (NoiseFloorReadingMsg.class.isInstance(msg)){
            final NoiseFloorReadingMsg nMsg = (NoiseFloorReadingMsg) msg;
            //log.info("NoiseFloorMessage: " + nMsg.toString());
            
            // store noise floor message to database
            ExperimentDataNoise expDataNoise = new ExperimentDataNoise();
            expDataNoise.setConnectedNode(nMsg.getSerialPacket().get_header_src());
            expDataNoise.setCounter(nMsg.get_counter());
            expDataNoise.setNoise(nMsg.get_noise());
            expDataNoise.setExperiment(expMeta);
            expDataNoise.setMiliFromStart(mili);
            this.objQueue.add(expDataNoise);
            
            // update last seen record
            synchronized(this.nodeReg){
                this.nodeReg.updateLastSeen(nMsg.getSerialPacket().get_header_src(), mili);
            }
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            
            // store RSSI measured to database, one report packet may contain multiple
            // measured RSSI values
            for (short j=0, len=cMsg.get_datanum(); j<len; j++){
                // store to database, context has to be derived from protocol/journal
                ExperimentDataRSSI dataRSSI = new ExperimentDataRSSI();
                dataRSSI.setExperiment(expMeta);
                dataRSSI.setConnectedNode(cMsg.getSerialPacket().get_header_src());
                dataRSSI.setConnectedNodeCounter(cMsg.get_counter());
                dataRSSI.setMiliFromStart(mili);
                dataRSSI.setRssi(cMsg.getElement_rssi(j));
                dataRSSI.setSendingNode(cMsg.getElement_nodeid(j));
                dataRSSI.setSendingNodeCounter(cMsg.getElement_nodecounter(j));
                dataRSSI.setLen(cMsg.getElement_len(j));
                
                this.objQueue.add(dataRSSI);
            }
        }
        
        this.checkQueues();
    }
    
    /**
     * Checks if is needed to empty queues, if yes, queues are flushed
     */
    @Override
    public void checkQueues(){
        long curTime = System.currentTimeMillis();
        
        if (messageFromLastFlush>currentMessageThresholdFlush || 
                (curTime-miliLastFlush) > miliFLushThreshold){
            this.flushQueues();
            
            miliLastFlush = curTime;
            
            // randomize message flush
            currentMessageThresholdFlush= maxMessageThresholdFlush==minMessageThresholdFlush ?
                    this.minMessageThresholdFlush :
                    minMessageThresholdFlush + (int)(Math.random() * ((maxMessageThresholdFlush - minMessageThresholdFlush) + 1));
        }
    }

//    @Transactional
    /**
     * Directly flushes object queues to database.
     */
    public void flushQueues(){
        messageFromLastFlush=0;
        log.debug("Flushing queue size=" + this.objQueue.size() + "; thread: " + this.getName());

        if (session2==null){
            session2 = sf.openStatelessSession();
        }
        
        Transaction tx = null;
        tx = session2.getTransaction();
        if (tx==null){
            tx = session2.beginTransaction();
        }
        
        if (tx.isActive()==false){
            tx.begin();
        }
                
        for (Object entity : objQueue) {
            session2.insert(entity);
        }
        tx.commit();
        
            
            
////            sqlFlush="INSERT INTO experimentDataRSSI(id,connectedNode,connectedNodeCounter,len,miliFromStart,rssi,sendingNode,sendingNodeCounter,experiment_id) VALUES ";
////            Iterator<String> iterator = this.sqlQueue.iterator();
////            StringBuilder sb = new StringBuilder();
////            sb.append(sqlFlush);
////            
////            for(int k=0; iterator.hasNext(); k++){
////                String sq = iterator.next();
////                if (k>0){
////                    sb.append(", ");
////                }
////                
////                sb.append(sq);
////            }
////            
////            log.info(sb.toString());
////            this.template.execute(sb.toString());
//            
//            Iterator<Object> iterator2 = this.objQueue.iterator();
//            while(iterator2.hasNext()){
//                Object obj = iterator2.next();
//                this.em.persist(obj);
//            }
//            
//            this.em.flush();
            
            this.objQueue.clear();
            this.sqlQueue.clear();
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }

    @Override
    public ExperimentMetadata getExpMeta() {
        return expMeta;
    }

    @Override
    public void setExpMeta(ExperimentMetadata expMeta) {
        this.expMeta = expMeta;
    }

    @Override
    public int getCurrentMessageThresholdFlush() {
        return currentMessageThresholdFlush;
    }

    @Override
    public void setCurrentMessageThresholdFlush(int currentMessageThresholdFlush) {
        this.currentMessageThresholdFlush = currentMessageThresholdFlush;
    }

    @Override
    public int getMaxMessageThresholdFlush() {
        return maxMessageThresholdFlush;
    }

    @Override
    public void setMaxMessageThresholdFlush(int maxMessageThresholdFlush) {
        this.maxMessageThresholdFlush = maxMessageThresholdFlush;
    }

    @Override
    public int getMessageFromLastFlush() {
        return messageFromLastFlush;
    }

    @Override
    public void setMessageFromLastFlush(int messageFromLastFlush) {
        this.messageFromLastFlush = messageFromLastFlush;
    }

    @Override
    public int getMinMessageThresholdFlush() {
        return minMessageThresholdFlush;
    }

    @Override
    public void setMinMessageThresholdFlush(int minMessageThresholdFlush) {
        this.minMessageThresholdFlush = minMessageThresholdFlush;
    }

    @Override
    public long getMiliFLushThreshold() {
        return miliFLushThreshold;
    }

    @Override
    public void setMiliFLushThreshold(long miliFLushThreshold) {
        this.miliFLushThreshold = miliFLushThreshold;
    }

    @Override
    public long getMiliLastFlush() {
        return miliLastFlush;
    }

    @Override
    public void setMiliLastFlush(long miliLastFlush) {
        this.miliLastFlush = miliLastFlush;
    }

    public SessionFactory getSf() {
        return sf;
    }

    public void setSf(SessionFactory sf) {
        this.sf = sf;
    }
}
