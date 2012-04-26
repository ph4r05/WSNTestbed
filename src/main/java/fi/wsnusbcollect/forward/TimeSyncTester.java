/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.db.DataCSVWritable;
import fi.wsnusbcollect.db.FileWritableTypes;
import fi.wsnusbcollect.experiment.ExperimentRecords2CSV;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.TimeSyncMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests time synchronization accuracy
 * @author ph4r05
 */
public class TimeSyncTester implements MessageListener{
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(TimeSyncTester.class);

    /**
     * Nodes to connect to and to use
     */
    protected Map<Integer, NodeConfigRecord> nodes;
    
    /**
     * Connection to nodes
     */
    protected Map<Integer, MoteIF> nodeCon;
    
    /**
     * Queue of arrived messages
     */
    protected Queue<TimeSyncMsg> msgQueueReceived = new ConcurrentLinkedQueue<TimeSyncMsg>();
    
    protected int coutner=0;
    
    protected ExperimentRecords2CSV dataWriter=null;
    
    protected long expStart=0;
    
    /**
     * If true then ignore messages -> no message expected
     */
    protected boolean ignoreMsgs=true;

    public TimeSyncTester(Map<Integer, NodeConfigRecord> nodes, Map<Integer, MoteIF> nodeCon) {
        this.nodes = nodes;
        this.nodeCon = nodeCon;
        
        dataWriter = new ExperimentRecords2CSV();
        dataWriter.postConstruct();
        dataWriter.setMainExperiment(null);
    }

    /**
     * Register listener
     */
    public void init(){
        for(Integer nodeId : this.nodeCon.keySet()){
            MoteIF moteif = this.nodeCon.get(nodeId);
            if (moteif==null){
                log.error("Moteif is null for nodeid: " + nodeId);
                continue;
            }
            
            // register listener here
            moteif.registerListener(new TimeSyncMsg(), this);
        }
    }
    
    /**
     * Deregister listener
     */
    public void deinit(){
        for(Integer nodeId : this.nodeCon.keySet()){
            MoteIF moteif = this.nodeCon.get(nodeId);
            if (moteif==null){
                log.error("Moteif is null for nodeid: " + nodeId);
                continue;
            }
            
            // register listener here
            moteif.deregisterListener(new TimeSyncMsg(), this);
        }
    }
    
    /**
     * Entry method for testing RTT of serial link. Blocking method holds until RTT measurement is finished.
     * @param cycles 
     */
    public void test(){
        this.coutner = 0;
        this.expStart = System.currentTimeMillis();
        
        for(Integer nodeId : this.nodes.keySet()){
            MoteIF moteif = this.nodeCon.get(nodeId);
            if(moteif==null){
                log.error("Cannot obtain moteIF for nodeid: " + nodeId);
                continue;
            }
            
            // clear timesync message queue
            this.msgQueueReceived.clear();
            
            // in cycle send requests for response, then wait until finished
            CommandMsg msg = new CommandMsg();
            msg.set_command_code((short)MessageTypes.COMMAND_TIMESYNC_GETGLOBAL_BCAST);
            msg.set_command_data(this.coutner++);
            
            // when was time obtained?
            long startTime = System.currentTimeMillis();
            this.ignoreMsgs = false;
            
            try {
                moteif.send(nodeId, msg);
                log.info("Sent bcast request for timesync to node: " + nodeId + "; counter: " + this.coutner);
            } catch (IOException ex) {
                log.info("Cannot send message, counter: " + this.coutner + "; nodeid: " + nodeId , ex);
            }
            
            // wait for responses, sleep for 3 seconds for each node
            // should be large enough to obtain results from all nodes            
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                log.error("Cannot sleep", ex);
                return;
            }
            
            // analyze collected messages from queue
            log.info("Finished receiving timesync messages, nodeID broadcast req: " + nodeId + "; answers: " + this.msgQueueReceived.size());
            ignoreMsgs=true;
            
            // analyze and store to CSV
            for (TimeSyncMsg tMsg : this.msgQueueReceived){
                TimeSyncRecord rec = new TimeSyncRecord();
                rec.setExpStart(expStart);
                
                rec.setBcastNodeId(nodeId);
                rec.setResponseNodeId(tMsg.getSerialPacket().get_header_src());
                rec.setMiliMessageReceived(tMsg.getMilliTime());
                rec.setMiliBcast(startTime);
                
                rec.setCounter(tMsg.get_counter());
                rec.setFlags(tMsg.get_flags());
                rec.setLow(tMsg.get_low());
                rec.setHigh(tMsg.get_high());
                
                this.dataWriter.storeEntityCSV(rec);
                this.dataWriter.flush();
            }
        }
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        /**
         * Wait only for timesync messages from nodes, should be response on radio broadcast
         */
        if (ignoreMsgs || TimeSyncMsg.class.isInstance(msg)==false) return;
        final TimeSyncMsg tMsg = (TimeSyncMsg) msg;
        
        // add timesync message to queue
        this.msgQueueReceived.add(tMsg);
    }

    @Override
    public void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg, 0);
    }
}
