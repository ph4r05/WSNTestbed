/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.utils.stats.Statistics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler RTT measurements for channel
 * @author ph4r05
 */
public class RTTtester implements MessageListener{
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(RTTtester.class);
    
    private Integer nodeId;
    private MoteIF moteif;

    /**
     * Determined round trip times history - last 100 records
     */
    private List<Long> rtts = null;
    
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
    
    public RTTtester(Integer nodeId, MoteIF moteif) {
        if (nodeId==null || moteif==null){
            throw new NullPointerException("Cannot init with null parameters");
        }
        
        this.nodeId = nodeId;
        this.moteif = moteif;
    }

    /**
     * Entry method for testing RTT of serial link. Blocking method holds until RTT measurement is finished.
     * @param cycles 
     */
    public void testRTT(int cycles){
        this.rtts = new ArrayList<Long>(cycles);
        for(int i=0; i<cycles; i++){
            // in cycle send requests for response, then wait until finished
            
            CommandMsg msg = new CommandMsg();
            msg.set_command_code((short)MessageTypes.COMMAND_PING);
            msg.set_command_data(this.rttCounter++);
            long startTime = System.currentTimeMillis();
            
            try {
                this.moteif.send(this.nodeId, msg);
                this.lastRttTime = startTime;
            } catch (IOException ex) {
                log.info("Cannot send message, counte: " + this.rttCounter, ex);
            }
            
            // wait for response
            while(this.lastRttTime!=null){
                try {
                    Thread.sleep(2);
                    long totalTime = System.currentTimeMillis() - startTime;
                    if (totalTime>100){
                        // expire this
                        this.lastRttTime=null;
                        // wait
                        Thread.sleep(1000);
                        break;
                    }
                } catch (InterruptedException ex) {
                    log.error("Cannot sleep", ex);
                }
            }
        }
    }
    
    /**
     * Callable only if test was performed before
     * @return 
     */
    public double getMeanRTT(){
        if (this.rtts == null || this.rtts.isEmpty()){
            throw new IllegalStateException("Has to run test first!");
        }
        
        return Statistics.calculateMean(rtts);
    }
    
    /**
     * Returns stdDev of measured RTT times
     * @return 
     */
    public double getStdDev(){
        if (this.rtts == null || this.rtts.isEmpty()){
            throw new IllegalStateException("Has to run test first!");
        }
        
        return Statistics.getStdDev(this.rtts.toArray(new Integer[0]));
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        long timeReceived = System.currentTimeMillis();
        
        // ignore message if no RTT measurement is in progress
        if (this.lastRttTime==null){
            return;
        }
        
        /**
         * Watch for replies on request commands
         */
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            
            // is alive / identification packet?
            if ((cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK) && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_PING){
                
                // desired situation - RTT received, compute total RTT
                long realTimeReceived = timeReceived;
                long msgMiliTime = msg.getMilliTime();
                // if there is reasonable timestamp in message, prefer it
                if (msgMiliTime > 10){
                    realTimeReceived = msgMiliTime;
                }
                
                // compute real RTT
                long realRTT = realTimeReceived - this.lastRttTime;
                this.rtts.add(realRTT);
                
                // unset lastRttTime to signalize that this RTT round finished
                this.lastRttTime = null;
                this.succRttCoutner+=1;
            }
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
}
