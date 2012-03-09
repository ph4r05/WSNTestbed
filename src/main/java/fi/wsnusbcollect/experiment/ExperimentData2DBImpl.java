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
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default message received handler for connected node.
 * Implements thread in order to run independently and ease database access.
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
    
    @Resource(name="experimentInit")
    protected ExperimentInit expInit;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;
    
    /**
     * ExperimentMetadata copy for local optimalization. Should not be written!!
     * It is readonly to optimize fetch time for this information
     */
    protected ExperimentMetadata expMeta;
    
    /**
     * Thread can be gracefully exited by setting this to false
     */
    boolean running=true;
    
    private String sqlFlush="";
    private List<String> sqlQueue = new ArrayList<String>(400);
    private List<Object> objQueue = new ArrayList<Object>(400);
    
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
        this.messageReceived(i, msg);
    }
    
    /**
     * Method handling identification received - store ACK message to database, update
     * last seen counter - node is active
     * @param i
     * @param cMsg
     * @param mili 
     */
    public void identificationReceived(int i, CommandMsg cMsg, long mili){        
        // @extension: for future use here can be converter from i+message+mili to
        // database entity, now directly here...
        // identify message, insert new entity to database
        ExperimentDataAliveCheck experimentDataAliveCheck = new ExperimentDataAliveCheck();
        experimentDataAliveCheck.setCounter(cMsg.get_command_id());
        experimentDataAliveCheck.setExperiment(expMeta);
        experimentDataAliveCheck.setMiliFromStart(mili);
        // @TODO: can be problem in multihop protocol
        experimentDataAliveCheck.setNode(cMsg.getSerialPacket().get_header_src());
//        this.em.persist(experimentDataAliveCheck);
        
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
            //System.out.println("Command message: " + cMsg.toString());
            log.info("Command message: " + cMsg.toString());
            
            // is alive / identification packet?
            if ((cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK) 
                    && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY){
                // notify appropriate method
                this.identificationReceived(i, cMsg, mili);
            }
        }
        
        // noise floor message
        if (NoiseFloorReadingMsg.class.isInstance(msg)){
            final NoiseFloorReadingMsg nMsg = (NoiseFloorReadingMsg) msg;
            log.info("NoiseFloorMessage: " + nMsg.toString());
            
            // store noise floor message to database
            ExperimentDataNoise expDataNoise = new ExperimentDataNoise();
            expDataNoise.setConnectedNode(nMsg.getSerialPacket().get_header_src());
            expDataNoise.setCounter(nMsg.get_counter());
            expDataNoise.setNoise(nMsg.get_noise());
            expDataNoise.setExperiment(expMeta);
            expDataNoise.setMiliFromStart(mili);
            this.em.persist(expDataNoise);
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            //System.out.println("Report message: " + cMsg.toString());
            //log.info("Report message: " + cMsg.toString());
            
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
                
                String toStore="(NULL, "+cMsg.getElement_nodeid(j) +",2,2,"+mili+","+cMsg.getElement_rssi(j)+",2,2,2)";
                this.sqlQueue.add(toStore);
                this.objQueue.add(dataRSSI);
                
//                this.template.execute("INSERT INTO experimentDataRSSI(id,connectedNode,connectedNodeCounter,len,miliFromStart,rssi,sendingNode,sendingNodeCounter,experiment_id) "
//                        + "VALUES(NULL, 2,2,2,2,2,2,2,2)");
//                this.em.persist(dataRSSI);
            }
        }
        
        if (messageFromLastFlush>currentMessageThresholdFlush){
//            this.em.flush();
            this.flushQueues();
        }
    }

    @Transactional
    public void flushQueues(){
        messageFromLastFlush=0;
            log.info("Flushing queue size=" + this.objQueue.size() + "; thread: " + this.getName());
            
//            sqlFlush="INSERT INTO experimentDataRSSI(id,connectedNode,connectedNodeCounter,len,miliFromStart,rssi,sendingNode,sendingNodeCounter,experiment_id) VALUES ";
//            Iterator<String> iterator = this.sqlQueue.iterator();
//            StringBuilder sb = new StringBuilder();
//            sb.append(sqlFlush);
//            
//            for(int k=0; iterator.hasNext(); k++){
//                String sq = iterator.next();
//                if (k>0){
//                    sb.append(", ");
//                }
//                
//                sb.append(sq);
//            }
//            
//            log.info(sb.toString());
//            this.template.execute(sb.toString());
            
            Iterator<Object> iterator2 = this.objQueue.iterator();
            while(iterator2.hasNext()){
                Object obj = iterator2.next();
                this.em.persist(obj);
            }
            
            this.em.flush();
            
            this.objQueue.clear();
            this.sqlQueue.clear();
            
            // randomize message flush
            currentMessageThresholdFlush= maxMessageThresholdFlush==minMessageThresholdFlush ?
                    this.minMessageThresholdFlush :
                    minMessageThresholdFlush + (int)(Math.random() * ((maxMessageThresholdFlush - minMessageThresholdFlush) + 1));
    }
    
    public boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }

    public ExperimentMetadata getExpMeta() {
        return expMeta;
    }

    public void setExpMeta(ExperimentMetadata expMeta) {
        this.expMeta = expMeta;
    }

    public int getCurrentMessageThresholdFlush() {
        return currentMessageThresholdFlush;
    }

    public void setCurrentMessageThresholdFlush(int currentMessageThresholdFlush) {
        this.currentMessageThresholdFlush = currentMessageThresholdFlush;
    }

    public int getMaxMessageThresholdFlush() {
        return maxMessageThresholdFlush;
    }

    public void setMaxMessageThresholdFlush(int maxMessageThresholdFlush) {
        this.maxMessageThresholdFlush = maxMessageThresholdFlush;
    }

    public int getMessageFromLastFlush() {
        return messageFromLastFlush;
    }

    public void setMessageFromLastFlush(int messageFromLastFlush) {
        this.messageFromLastFlush = messageFromLastFlush;
    }

    public int getMinMessageThresholdFlush() {
        return minMessageThresholdFlush;
    }

    public void setMinMessageThresholdFlush(int minMessageThresholdFlush) {
        this.minMessageThresholdFlush = minMessageThresholdFlush;
    }
}
