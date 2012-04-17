/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentCTPDebug;
import fi.wsnusbcollect.db.ExperimentCTPInfoStatus;
import fi.wsnusbcollect.db.ExperimentCTPReport;
import fi.wsnusbcollect.db.ExperimentDataAliveCheck;
import fi.wsnusbcollect.db.ExperimentDataNoise;
import fi.wsnusbcollect.db.ExperimentDataRSSI;
import fi.wsnusbcollect.db.ExperimentMetadata;
import fi.wsnusbcollect.messages.CollectionDebugMsg;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.CtpInfoMsg;
import fi.wsnusbcollect.messages.CtpReportDataMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage class does not use database, flushes entities to CSV files
 * @author ph4r05
 */
public class ExperimentData2CSV extends Thread implements ExperimentData2DB {
    private static final Logger log = LoggerFactory.getLogger(ExperimentData2CSV.class);
    
    @Resource(name="experimentInit")
    protected ExperimentInit expInit;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;
    
    @Resource(name="experimentRecords")
    protected ExperimentRecords2DB rec2db;
    
    /**
     * ExperimentMetadata copy for local optimization. Should not be written!!
     * It is readonly to optimize fetch time for this information
     */
    protected ExperimentMetadata expMeta;
    
    /**
     * Thread can be gracefully exited by setting this to false
     */
    boolean running=true;
    
    /**
     * Queue of entities to store
     */
    private Queue<Object> objQueue = new ConcurrentLinkedQueue<Object>();
    
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
    int currentMessageThresholdFlush=1000;
    
    int maxMessageThresholdFlush=2000;
    int minMessageThresholdFlush=400;
    
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
        
        this.rec2db.flush();
        log.info("Finishing");
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
        experimentDataAliveCheck.setAliveFails(cMsg.get_command_data_next()[3] & 0xFF);
        experimentDataAliveCheck.setSerialFails((cMsg.get_command_data_next()[3] & 0xFF00) >> 8);
        
        // @TODO: can be problem in multihop protocol
        experimentDataAliveCheck.setNode(cMsg.getSerialPacket().get_header_src());
        this.objQueue.add(experimentDataAliveCheck);
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        //log.info("Message received: " + msg.amType() +  "; time=" + mili + "; " + this.getName());
        // update last seen record
        int src = msg.getSerialPacket().get_header_src();
        this.nodeReg.updateLastSeen(src, mili);
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
        
        // CTP report message?
        if (CtpReportDataMsg.class.isInstance(msg)){
            final CtpReportDataMsg cMsg = (CtpReportDataMsg) msg;
            
            ExperimentCTPReport report = new ExperimentCTPReport();
            report.loadFromMessage(cMsg);
            report.setMilitime(mili);
            report.setExperiment(expMeta);
            report.setNode(src);
            
            this.objQueue.add(report);
        }
        
        // CTP status info message?
        if (CtpInfoMsg.class.isInstance(msg)){
            final CtpInfoMsg cMsg = (CtpInfoMsg) msg;
            
            // only status info save to db
            if (cMsg.get_type()==0){
                ExperimentCTPInfoStatus status = new ExperimentCTPInfoStatus();
                status.loadFromMessage(cMsg);
                status.setMilitime(mili);
                status.setExperiment(expMeta);
                status.setNode(src);
            
                this.objQueue.add(status);
            }
        }
        
        // CTP debug?
        if (CollectionDebugMsg.class.isInstance(msg)){
            final CollectionDebugMsg cMsg = (CollectionDebugMsg) msg;
            
            // only status info save to db            
            ExperimentCTPDebug dbg = new ExperimentCTPDebug();
            dbg.loadFromMessage(cMsg);
            dbg.setMilitime(mili);
            dbg.setExperiment(expMeta);
            dbg.setNode(src);

            this.objQueue.add(dbg);

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

    /**
     * Directly flushes object queues to database.
     * Need to synchronize on this object - messageReceived is synchronized as well.
     * If no, special case can occur: flush starts, transaction is started. Some delay
     * is occurs in database connection. Meanwhile new messages arrive to queues BUT
     * after queue flush queue can be cleared => error, messages received in time of 
     * queue flush are lost. Two main solutions:
     * 1. synchronize on whole instance
     * 2. + do not call clear on queue, just remove inserted elements from queue
     * 
     * Warning: it is necessary the hibernate bulk insert works properly, otherwise
     * large bottleneck is here (each message inserted by separate sql query=very slow),
     * queues can overflow and congestion may occur. If problem, run database benchmark 
     * and compare JDBC with Hibernate times. Should be in reasonable ratio.
     */
    public synchronized void flushQueues(){
        log.debug("Flushing queue size=" + this.objQueue.size() + "; thread: " + this.getName());    
        try {
            // iterate over and save all objects with data records
            // queue here => insert all elements from queue until there is any
            Object entity = null;
            while(objQueue.isEmpty()==false){
                entity = objQueue.poll();
                if (entity==null){
                    // null element in queue - probably empty
                    continue;
                }
                
                // bulk insert should work here!
                // otherwise this is large bottleneck, see benchmark if problems
                // occur
                try {
                    this.rec2db.storeEntity(entity);
                } catch(Exception e){
                    log.warn("Cannot flush particular entity to database", e);
                }
            }
            
            messageFromLastFlush=0;
        } catch (Exception e) {
            log.warn("Exception when starting transaction", e);
            
        }
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
}
