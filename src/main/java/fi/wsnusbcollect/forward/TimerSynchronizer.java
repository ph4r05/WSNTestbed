/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.messages.TimeSyncMsg;
import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
     * Time sync counter
     */
    private int syncCoutner=0;
    
    /**
     * Delay for synchronization in milliseconds
     */
    private int synchroDelay=1000;
    
    /**
     * Terminate control
     */
    private boolean terminate=false;
    
    /**
     * Nodes to use - creates new connection with connection string
     */
    private Map<Integer, NodeConfigRecord> nodes;
    private List<NodeSender> senders;
    private Map<Integer, MoteIF> connectedInterfaces;

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
     * Synchronize now
     * synchronous send is not very real since there are only
     * a few processors to process 256 threads at some time - some delay may
     * occur and thread overhead is big, thus we do it by node-by-node case.
     * 
     * Thus for every node is generated new fresh message with time sync.
     */
    public boolean sync(){
        for(Integer nodeId : this.connectedInterfaces.keySet()){
            MoteIF currentInterface = this.connectedInterfaces.get(nodeId);
            
            TimeSyncMsg tMsg = new TimeSyncMsg();
            tMsg.set_counter((short)this.syncCoutner);
            tMsg.set_flags((short)0);
            
            long time = System.currentTimeMillis();
            tMsg.set_low((time & 0xFFFFFFFF));
            tMsg.set_high((time >> 32) & 0xFFFFFFFF);
            
            try {
                currentInterface.send(nodeId, tMsg);
            } catch (IOException ex) {
                log.error("Cannot send timesync message to node: " + nodeId, ex);
            }
        }

        this.syncCoutner+=1;
                
        // now should threads do their jobs - send message to node immediatelly
        return true;
    }
    
    /**
     * Main synchro loop
     */
    @Override
    public void run(){
        // do in loop forever
        for(;this.terminate==true;){
            try {
                sleep(this.synchroDelay);
                this.sync();
            } catch (InterruptedException ex) {
                log.error("Interrupter from sleep at time synchronizer", ex);
            }
        }
        
        // when terminate, disable all running threads
        for(NodeSender ns : this.senders){
            ns.setTerminate(true);
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