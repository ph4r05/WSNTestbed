/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.db.PrintfEntity;
import fi.wsnusbcollect.experiment.ExperimentRecords2CSV;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.messages.PrintfMsg;
import fi.wsnusbcollect.messages.TestSerialMsg;
import fi.wsnusbcollect.messages.TimeSyncMsg;
import fi.wsnusbcollect.messages.TimeSyncReportMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.PacketSource;
import net.tinyos.sf.SerialForwarder;
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
    protected Queue<TimeSyncRecord> msgQueueReceived = new ConcurrentLinkedQueue<TimeSyncRecord>();
    
    protected int coutner=0;
    
    protected ExperimentRecords2CSV dataWriter=null;
    
    /**
     * If true then ignore messages -> no message expected
     */
    protected boolean ignoreMsgs=true;
    
    /**
     * Absolute time value when experiment started
     */
    protected long experimentStart=0;
    
    /**
     * Number of milliseconds to wait nodes to reply on broadcast
     */
    protected long timePauseBetweenBcast=5000;
    
    /**
     * How many broadcast should do one node during test?
     */
    protected int oneNodeBroadcastCount=20;
    
    /**
     * Time synchronizer object
     */
    protected TimerSynchronizer timeSynchronizer=null;

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
    public void init(Map<Integer, SerialForwarder> sfmap){
        // set experiment absolute time point
        this.experimentStart = System.currentTimeMillis();
        
        if (this.timeSynchronizer!=null){
            this.timeSynchronizer.setAbsoluteTime(experimentStart);
        }
        
        for(Integer nodeId : this.nodeCon.keySet()){
            MoteIF moteif = this.nodeCon.get(nodeId);
            if (moteif==null){
                log.error("Moteif is null for nodeid: " + nodeId);
                continue;
            }
            
            // register listener here
            moteif.registerListener(new TimeSyncMsg(), this);
            moteif.registerListener(new TimeSyncReportMsg(), this);
            moteif.registerListener(new TestSerialMsg(), this);
            moteif.registerListener(new PrintfMsg(), this);
            moteif.registerListener(new CommandMsg(), this);
            log.info("Registered listeners for moteif: " + nodeId);
            log.info("Source name: " + moteif.getSource().getName());
            log.info("Packet Source name: " + moteif.getSource().getPacketSource().getName());
                    
            // set experiment start relative time - reference point
            // for further time stamps.
            PacketSource packetSource = moteif.getSource().getPacketSource();
            if (packetSource instanceof net.tinyos.packet.Packetizer){
                net.tinyos.packet.Packetizer packetizer = (net.tinyos.packet.Packetizer) packetSource;
                packetizer.setAbsoluteTime(experimentStart);
                log.info("Absolute reference time point set for packetizer: " + packetSource.getName());
                continue;
            } else {
                log.info("packetSource is not packetizer!: " + packetSource.getName());
                log.info(packetSource.toString());
            }
            
            // If reached here, nodes are probably not connected directly.
            // Try sf map (contains serial forwarder server) if not null.
            if (sfmap==null) continue;
            if (sfmap.containsKey(nodeId)==false) continue;
            log.info("SFmap is here, nodeId["+nodeId+"] is in map");
            SerialForwarder sfserver = sfmap.get(nodeId);
            
            try {
                PacketSource ps = sfserver.getListenServer().getSource().getPacketSource();
                 if (ps instanceof net.tinyos.packet.Packetizer){
                    net.tinyos.packet.Packetizer packetizer = (net.tinyos.packet.Packetizer) ps;
                    packetizer.setAbsoluteTime(experimentStart);
                    log.info("Absolute reference time point set for packetizer: " + packetSource.getName());
                    continue;
                } else {
                    log.info("packetSource is not packetizer!: " + packetSource.getName());
                    log.info(packetSource.toString());
                }
            }
            catch(Exception ex){
                 log.warn("Exception during setting absolute time to SFserver for time stamps", ex);
            }
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
            moteif.deregisterListener(new TimeSyncReportMsg(), this);
            moteif.deregisterListener(new TestSerialMsg(), this);
            moteif.deregisterListener(new PrintfMsg(), this);
            moteif.deregisterListener(new CommandMsg(), this);
        }
    }
    
    /**
     * Entry method for testing RTT of serial link. Blocking method holds until RTT measurement is finished.
     * @param cycles 
     */
    public void test(){
        this.coutner = 0;
        
        for(Integer nodeId : this.nodes.keySet()){
            MoteIF moteif = this.nodeCon.get(nodeId);
            if(moteif==null){
                log.error("Cannot obtain moteIF for nodeid: " + nodeId);
                continue;
            }
            
            // iterate more than one for given node - statistical measurement, we
            // want more samples to obtain estimate for accuracy of time sync with
            // same conditions = same broadcast node
            for (int nodeIter=0; nodeIter < this.oneNodeBroadcastCount; nodeIter++){
            
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

                // wait for responses, sleep for 5 seconds for each node
                // should be large enough to obtain results from all nodes            
                try {
                    Thread.sleep(this.timePauseBetweenBcast);
                } catch (InterruptedException ex) {
                    log.error("Cannot sleep", ex);
                    return;
                }

                // analyze collected messages from queue
                log.info("Finished receiving timesync messages, nodeID broadcast req: " + nodeId + "; answers: " + this.msgQueueReceived.size());
                ignoreMsgs=true;

                // analyze and store to CSV
                for (TimeSyncRecord rec : this.msgQueueReceived){
                    rec.setBcastNodeId(nodeId);
                    rec.setMiliBcast(startTime - this.experimentStart);
                    this.dataWriter.storeEntityCSV(rec);
                    this.dataWriter.flush();
                }
            }
        }
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        /**
         * Wait only for timesync messages from nodes, should be response on radio broadcast
         */
        //if (ignoreMsgs) return;
        // change i - I want source here
        i=msg.getSerialPacket().get_header_src();
        
        if (TimeSyncMsg.class.isInstance(msg)){
            final TimeSyncMsg tMsg = (TimeSyncMsg) msg;
            log.info("TimeSync Recvd ["+i+"]");
        }
        
        if (TimeSyncReportMsg.class.isInstance(msg)){
            final TimeSyncReportMsg tsyncrep = (TimeSyncReportMsg) msg;
            
            // create record from this message - we want accurate time stamp now,
            // thus record is created right now from message, later, broadcast node
            // will be added, just before saving to file/database
            TimeSyncRecord rec = new TimeSyncRecord();
            rec.loadFromMessage(tsyncrep);
            rec.setMiliMessageReceived(System.currentTimeMillis() - this.experimentStart);
            rec.setMiliMessageReceivedTOS(tsyncrep.getMilliTime() - this.experimentStart);
            rec.setCounter(this.coutner);
            rec.setExpStart(this.experimentStart);
            this.msgQueueReceived.add(rec);
            
            log.info("TimeSyncReportMsg arrived, now["+System.currentTimeMillis()+"] "
                    + "msgtime["+msg.getMilliTime()+"] from node ["+i+"]");
            log.info(dumpTimeSyncReportMsg(tsyncrep));
        }
        
        if (TestSerialMsg.class.isInstance(msg)){
            log.info("TestSerial: " + msg.toString());
        }
        
        if (CommandMsg.class.isInstance(msg)){
            CommandMsg cmsg = (CommandMsg) msg;
            log.info("CmdMsg: " + cmsg.get_command_code());
        }
        
        // printf support - dump printf message to out
        if (PrintfMsg.class.isInstance(msg)){
            final PrintfMsg pmsg = (PrintfMsg) msg;
            PrintfEntity entity = new PrintfEntity();
            entity.loadFromMessage(pmsg);
            
            String[] split = entity.getBuff().split("\n");
            for(String line: split){
                log.info("PrintfMessage["+i+"]: " + line);
            }
        }
    }
    
    /**
     * Build dump of time sync report message to string
     * @param msg
     * @return 
     */
    public static String dumpTimeSyncReportMsg(TimeSyncReportMsg msg){
        StringBuilder sb = new StringBuilder();
        sb.append("[LocTime: ").append(msg.get_localTime())
                .append("; globTime: ").append(msg.get_globalTime())
                .append(";\n lastSync").append(msg.get_lastSync())
                .append("; entries").append(msg.get_entries())
                .append(";\n hbeats").append(msg.get_hbeats())
                .append("; offset").append(msg.get_offset())
                .append("; skew").append(msg.get_skew())
                .append(" ]");
        return sb.toString();
    }
    
    @Override
    public void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg, 0);
    }
}
