/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.messages.TestSerialMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.utils.RingBuffer;
import fi.wsnusbcollect.utils.stats.Statistics;
import java.util.List;
import java.util.logging.Level;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzes periodic messages sent by sensor node - jitter, spread, etc
 * @author ph4r05
 */
public class PeriodicMessageTester implements MessageListener{
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(PeriodicMessageTester.class);
    
    private Integer nodeId;
    private MoteIF moteif;

    /**
     * Determined round trip times history - last 100 records
     */
    private RingBuffer<Long> rtts = null;
    
    private int rttCounter = 0;
    
    /**
     * Successful ping-pong cycle
     */
    private int succRttCoutner=0;
    
    /**
     * Time when was last RTT time measurement started.
     * If null => no measurement in progress...
     */
    private Long lastRttTime = null;
    
    /**
     * Threshold notification for parent signal
     */
    private int threshold = 1000;
    
    // running flag?
    private boolean running=true;
    
    /**
     * Mass periodic message tester - parent notified when threshold reached
     */
    private MassPeriodicMessageTester parent = null;    
    
    public PeriodicMessageTester(Integer nodeId, MoteIF moteif) {
        if (nodeId==null || moteif==null){
            throw new NullPointerException("Cannot init with null parameters");
        }
        
        this.nodeId = nodeId;
        this.moteif = moteif;
    }

    public void init(){
        this.rttCounter = 0;
        this.succRttCoutner=0;
        this.rtts = new RingBuffer<Long>(5000, false);
        
        this.moteif.registerListener(new TestSerialMsg(), this);
    }
    
    public void deinit(){
        this.running=false;
        this.moteif.deregisterListener(new TestSerialMsg(), this);
    }
    
    /**
     * Entry method for testing RTT of serial link. Blocking method holds until RTT measurement is finished.
     * @param cycles 
     */
    public void test(){
        this.rttCounter = 0;
        this.succRttCoutner=0;
        this.lastRttTime=null;
        this.rtts = new RingBuffer<Long>(5000, false);
    }
    
    /**
     * Callable only if test was performed before
     * @return 
     */
    public double getMean(){
        if (this.rtts == null || this.rtts.isEmpty()){
            throw new IllegalStateException("Has to run test first!");
        }
        
        return Statistics.calculateMean(rtts.asList());
    }
    
    /**
     * 
     * Returns raw results
     * @return 
     */
    public List<Long> getRaw(){
        if (this.rtts == null || this.rtts.isEmpty()){
            throw new IllegalStateException("Has to run test first!");
        }
        
        return this.rtts.asList();
    }
    
    /**
     * Returns stdDev of measured RTT times
     * @return 
     */
    public double getStdDev(){
        if (this.rtts == null || this.rtts.isEmpty()){
            throw new IllegalStateException("Has to run test first!");
        }
        
        return Statistics.getStdDev(this.rtts.asList().toArray(new Long[0]));
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        this.rttCounter+=1;
        long timeReceived = System.currentTimeMillis();
        
        /**
         * Watch for replies on request commands
         */
        
        // command message?
        if (TestSerialMsg.class.isInstance(msg)){
            // Command message
            final TestSerialMsg cMsg = (TestSerialMsg) msg;

            // desired situation - RTT received, compute total RTT
            long realTimeReceived = timeReceived;
            long msgMiliTime = msg.getMilliTime();
            boolean timeFromMsg=false;
            // if there is reasonable timestamp in message, prefer it
            if (msgMiliTime > 10){
                realTimeReceived = msgMiliTime;
                timeFromMsg=true;
            }

            // first message is only used to initialize last RttTime, message is dropped
            if (this.lastRttTime==null){
                this.lastRttTime = realTimeReceived;
                return;
            }
            
            // compute real RTT
            long realRTT = realTimeReceived - this.lastRttTime;
            // collect time gaps between messages, total times are not interesting for us
            this.rtts.add(realRTT);

            log.info("MsgReceived["+this.nodeId+"], time: " + timeReceived 
                    + "; MsgReal: " + realTimeReceived 
                    + "; TFM: " + (timeFromMsg ? "Y":"N")
                    + "; rtt: " + realRTT);
            
            // unset lastRttTime to signalize that this RTT round finished
            this.lastRttTime = realTimeReceived;
            this.succRttCoutner+=1;
        }
        
        // check threshold and notufy parent
        if (this.rttCounter > this.threshold && this.parent!=null){
            this.parent.thresholdReachedEvent(this);
            // setup this correctly - not to trigger repeatedly
            this.rttCounter=0;
        }
    }

    @Override
    public void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg, 0);
    }

    public int getRttCounter() {
        return rttCounter;
    }

    public int getSuccRttCoutner() {
        return succRttCoutner;
    }

    public MassPeriodicMessageTester getParent() {
        return parent;
    }

    public void setParent(MassPeriodicMessageTester parent) {
        this.parent = parent;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public MoteIF getMoteif() {
        return moteif;
    }

    public Integer getNodeId() {
        return nodeId;
    }
}
