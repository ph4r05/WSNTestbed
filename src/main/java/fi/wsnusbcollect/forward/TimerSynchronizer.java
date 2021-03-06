/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.tinyos.message.LowlvlTimeSyncMsg32;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends single message as fast as possible to all connected nodes synchronously
 *  - used to do time synchronization over cable.
 * 
 * To be used as nearest as possible - in forwarder on VM in senslab. 
 * Thus we cannot connect to the node network binding directly (to avoid corruption of
 * receiving messages to SF), we need to connect to provided SF as new client.
 * 
 * 
 * @author ph4r05
 */
public class TimerSynchronizer extends Thread implements Runnable {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(TimerSynchronizer.class);
    
    /**
     * @deprecated 
     * Message to be send by each thread
     */
    private Message msg2send = null;
    
    /**
     * @deprecated 
     * Number of threads sent message
     */
    private int sentCount = 0;
    
    /**
     * @deprecated
     * List of sender threads
     */
    private List<NodeSender> senders;
    
    /**
     * By this you can temporarily disable time synchronization
     */
    private boolean enabled=true;
    
    /**
     * Time sync counter
     */
    private int syncCoutner=0;
    
    /**
     * Delay for synchronization in milliseconds
     */
    private int synchroDelay=3000;
    
    /**
     * Terminate control
     */
    private boolean terminate=false;
    
    /**
     * Nodes to use - creates new connection with connection string
     */
    private Map<Integer, NodeConfigRecord> nodes = new HashMap<Integer, NodeConfigRecord>();
    private Map<Integer, MoteIF> connectedInterfaces = new HashMap<Integer, MoteIF>();
    
    /**
     * Round trip times for each node measured
     */
    private Map<Integer, Integer> roundTripTimes = new HashMap<Integer, Integer>();
    
    /**
     * Absolute reference point for relative timestamping
     */
    private long absoluteTime=0;

    public TimerSynchronizer(Map<Integer, NodeConfigRecord> nodes) {
        this.nodes = nodes;
        // initialize sender threads
        this.init();
    }
    
    /**
     * Start needed threads 4 sending
     */
    private void init(){
        this.connectedInterfaces = new HashMap<Integer, MoteIF>();
        
        // initialize threads
        for(NodeConfigRecord ncr : this.nodes.values()){
            // build custom error mesenger - store error messages from tinyos to logs directly
            TOSLogMessenger messenger = new TOSLogMessenger();
            // instantiate phoenix source
            PhoenixSource phoenix = BuildSource.makePhoenix(ncr.getConnectionString(), messenger);
            MoteIF moteInterface = null;

            // phoenix is not null, can create packet source and mote interface
            if (phoenix != null) {
                // loading phoenix
                moteInterface = new MoteIF(phoenix);
            }
            
            this.connectedInterfaces.put(ncr.getNodeId(), moteInterface);
            
            // DEPRECATED - synchronous send is not very real since there are only
            // a few processors to process 256 threads at sime time - some delay may
            // occur and thread overhead is big, thus we do it by node-by-node case.
            //
//            // spawn new thread
//            NodeSender tmpSender = new NodeSender(moteInterface, ncr.getNodeId());
//            tmpSender.start();
//            this.senders.add(tmpSender);
        }
    }
    
    /**
     * Sends time sync message to all nodes connected, one-by-one, 
     * using low level time sync messages.
     * 
     * Synchronous send is not very real since there are only
     * a few processors to process 256 threads at some time - some delay may
     * occur and thread overhead is big, thus we do it by node-by-node case.
     * 
     * Thus for every node is generated new fresh message with time sync.
     * Low level time synchronization is used here - time in message is set just
     * before message sending as byte stream.
     */
    public boolean sync(){
        for(Integer nodeId : this.connectedInterfaces.keySet()){
            MoteIF currentInterface = this.connectedInterfaces.get(nodeId);
            
            // special type of message - included in modiffied TinyOS Java SDK
            // for low level time sync - current time will be added 
            // just before escaping packet and sending to ByteSource
            LowlvlTimeSyncMsg32 tMsg = new LowlvlTimeSyncMsg32();
            tMsg.set_counter((short)this.syncCoutner);
            tMsg.set_flags((short)0);
            
            // offset by RTT, will be added just before sending to low|high time
            // by underlying library. 
            tMsg.set_offset(0);
            
            // set time to zero, this should be changed by sending layer
            // thus if is still zero after receiving on sensor node => something 
            // wrong happened.
            long curTime = System.currentTimeMillis();
            if (this.absoluteTime>0){
                tMsg.set_low(curTime - this.absoluteTime);
                tMsg.set_high(0);
            } else {
                tMsg.set_low((curTime & 0xFFFFFFFF));
                tMsg.set_high((curTime >> 32) & 0xFFFFFFFF);
            }
            
            try {
                currentInterface.send(nodeId, tMsg);
                log.info("Sent sync message to node: " + nodeId);
            } catch (IOException ex) {
                log.error("Cannot send timesync message to node: " + nodeId, ex);
            }
        }

        this.syncCoutner+=1;
                
        // now should threads do their jobs - send message to node immediatelly
        return true;
    }
    
    /**
     * Main synchro loop, starts sync() method every synchroDelay seconds.
     * @see {TimerSynchronizer.sync}
     */
    @Override
    public void run(){
        // do in loop forever
        for(;this.terminate==false;){
            try {
                sleep(this.synchroDelay);
                
                if (enabled){
                    this.sync();
                }
            } catch (InterruptedException ex) {
                log.error("Interrupter from sleep at time synchronizer", ex);
            }
        }
        
        // when terminate, disable all running threads
        if (this.senders!=null){
            for(NodeSender ns : this.senders){
                ns.setTerminate(true);
            }
        }
        
        // wait to terminate properly
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public int getSynchroDelay() {
        return synchroDelay;
    }

    public void setSynchroDelay(int synchroDelay) {
        this.synchroDelay = synchroDelay;
    }

    public int getSyncCoutner() {
        return syncCoutner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets absolute time reference point for relative timestamping.
     * If is zero, then relative timestamping is disabled.
     * @return 
     */
    public long getAbsoluteTime() {
        return absoluteTime;
    }

    /**
     * Sets absolute time reference point for relative timestamping.
     * If is zero, then relative timestamping is disabled.
     * @param absoluteTime 
     */
    public void setAbsoluteTime(long absoluteTime) {
        this.absoluteTime = absoluteTime;
    }
    /**
     * Sender thread
     */
    protected class NodeSender extends Thread implements Runnable {
        boolean terminate=false;
        
        Message msgSent = null;
        MoteIF moteif;
        int moteId;

        public NodeSender(MoteIF moteif, int moteId) {
            this.moteif = moteif;
            this.moteId = moteId;
            this.setName("NodeSender for: " + moteId);
        }
        
        @Override
        public void run() {
            for(;terminate!=true;){
                try {
                    // sleep only a small piece of time
                    sleep(0, 10);
                    
                    // is message non-null?
                    if (msg2send==null) continue;
                    
                    // i have already sent this message
                    if (msg2send==this.msgSent) continue;
                    
                    // send message now
                    this.moteif.send(moteId, msg2send);
                    this.msgSent=msg2send;
                    sentCount+=1;
                } catch (IOException ex) {   
                    
                } catch (InterruptedException ex) {
                    
                }
            }
        }

        public boolean isTerminate() {
            return terminate;
        }

        public void setTerminate(boolean terminate) {
            this.terminate = terminate;
        }        
    }
}
