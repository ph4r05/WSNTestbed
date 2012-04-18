/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import fi.wsnusbcollect.nodes.ConnectedNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for listening to multiple nodes at once.
 * Can save RAM to decrease thread count needed for full operation.
 * @author ph4r05
 */
public class MultipleMessageListener extends Thread implements MessageListenerInterface {

    private static final Logger log = LoggerFactory.getLogger(MultipleMessageListener.class);
    
    /**
     * Maximum number of messages in queue to reset queue
     */
    public static final int MAX_QUEUE_SIZE_TO_RESET=5000;

    /**
     * Master message queue for every node
     */
    private final ConcurrentLinkedQueue<MessageReceived> queue;

    /**
     * AMTYPE -> message mapping
     */
    protected static ConcurrentHashMap<Integer, net.tinyos.message.Message> amType2Message = new ConcurrentHashMap<Integer, Message>(8);
    
    /**
     * Queue of message listeners registered for particular AMType
     */
    protected ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>> messageListeners = null;
    
    /**
     * Message filter for each message listener, registers whether particular listener
     * is interested in receiving AMTYPE message from NODEID gateway
     * 
     */
    protected ConcurrentHashMap<MessageListener, HashSet<AMTypeNodeIDComposite>> messageFilter = 
            new ConcurrentHashMap<MessageListener, HashSet<AMTypeNodeIDComposite>>();

    /**
     * time of last sent message
     */
    protected long timeLastMessageSent;

    /**
     * Should I shutdown?
     */
    protected boolean shutdown=false;
    
    /**
     * Am I dropping packets currently?
     */
    protected boolean dropingPackets=true;

    /**
     *
     * @param gateway
     * @param logger
     */
    public MultipleMessageListener(String threadName) {
        // set thread title
        super("MyMessageListener: " + threadName);
        
        // init queues
        queue = new ConcurrentLinkedQueue<MessageReceived>();
        messageListeners = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>>(2);
    }

    /**
     * Pausing thread
     * @param microsecs
     */
    private void pause(int microsecs) {
        try {
            Thread.sleep(microsecs);
        } catch (InterruptedException ie) {
            log.warn("Cannot sleep", ie);
        }
    }
    
    /**
     * Performs shutdown
     */
    @Override
    public synchronized void shutdown(){
        this.reset();
        this.dropingPackets=true;
        this.shutdown=true;
    }
    
    /**
     * Register message listener
     * If same listener (.equals()) is registered to same message type, request
     * is ignored.
     */
    @Override
    public synchronized void registerListener(net.tinyos.message.Message msg, MessageListener listener){
        // null?
        if (msg==null || listener==null){
            log.error("Cannot register listener when message or listener is null");
            throw new NullPointerException("Cannot register listener when message or listener is null");
        }
        
        try {
            // is message in translate queue?
            Integer amtype = msg.amType();

            // if does not contain mapping amtype -> message, create one
            if (amType2Message.containsKey(amtype)==false){
                amType2Message.put(amtype, msg);
                
                // register this in real, on real listening interface for 
                // this message type, myself as listener
                this.gateway.registerListener(msg, this);
            }

            // check if linked list exists
            if (this.messageListeners.containsKey(amtype)==false 
                    || this.messageListeners.get(amtype)==null){
                // none such mapping yet created, create new list of listeners
                ConcurrentLinkedQueue<MessageListener> queueListener = new ConcurrentLinkedQueue<MessageListener>();
                this.messageListeners.put(amtype, queueListener);
            }

            // here is queueListener set, get it now
            ConcurrentLinkedQueue<MessageListener> queueListeners = this.messageListeners.get(amtype);

            // check if is already in linked queue
            if (queueListeners==null){
                // always have queue initialized
                queueListeners = new ConcurrentLinkedQueue<MessageListener>();
            }
            
            Iterator<MessageListener> iterator = queueListeners.iterator();
            while(iterator.hasNext()){
                MessageListener curMessageListener = iterator.next();
                
                // check if is null - remove it
                if (curMessageListener==null){
                    iterator.remove();
                    log.error("Null message listener found in queue, removing");
                    continue;
                }
                
                // equals? already set, not induce redundancy
                if (curMessageListener.equals(listener)){
                    return;
                }
            }

            // if here, listener is not in, set it
            queueListeners.add(listener);

            // update map
            this.messageListeners.put(amtype, queueListeners);
        } catch (Exception e){
            log.error("Exception occurred when registering message listener", e);
        }
    }
    
    /**
     * unregister message listener
     */
    @Override
    public synchronized void deregisterListener(net.tinyos.message.Message msg, net.tinyos.message.MessageListener listener){
        if (msg==null || listener==null){
            log.error("Cannot register listener when message or listener is null");
            throw new NullPointerException("Cannot register listener when message or listener is null");
        }
        
        try {
            // is message in translate queue?
            Integer amtype = msg.amType();
            
            // if does not contain mapping amtype -> message, create one
            if (this.amType2Message.containsKey(amtype)==false){                
                // if not here, cannot be registered
                return;
            }
            
            // is amtype established?
            if (this.messageListeners.containsKey(amtype)==false){
                // no, cannot be in underlying list
                return;
            }
            
            // get list
            ConcurrentLinkedQueue<MessageListener> listenersList = this.messageListeners.get(amtype);
            if (listenersList==null || listenersList.isEmpty()){
                // list empty => cannot be in
                return;
            }
            
            Iterator<MessageListener> iterator = listenersList.iterator();
            while(iterator.hasNext()){
                MessageListener curListener = iterator.next();
                if (curListener==null){
                    iterator.remove();
                    continue;
                }
                
                if (curListener.equals(listener)){
                    iterator.remove();
                    break;
                }
            }
            
            // push back to map
            this.messageListeners.put(amtype, listenersList);
            
        } catch (Exception e){
            log.error("Exception occurred when de-registering message listener", e);
        }
    }

    /**
     * perform hard reset to this object = clears entire memory
     */
    @Override
    public synchronized void reset(){
        this.queue.clear();
        
        log.info("MesageListener queues was flushed");
    }
    
    /**
     * After gateway change is needed to register as listener for all registered
     * AMtypes. MyMessageListener is registered as listener to tinyos listener.
     */
    @Override
    public synchronized void reregisterListeners(){
        if (this.amType2Message == null || this.amType2Message.isEmpty()) return;
        
        // if gateway is null - disconnected state
        if (this.gateway==null){
            log.info("Not registering listeners, gateway is empty - disconnected");
            return;
        }
        
        try{
            // register to all registered amtypes to real interface
            Iterator<Integer> iterator = this.amType2Message.keySet().iterator();
            while(iterator.hasNext()){
                Integer amtype = iterator.next();
                Message curMsg = this.amType2Message.get(amtype);

                // register
                this.gateway.registerListener(curMsg, this);
            }
        } catch(Exception e){
            log.error("Exception when re-registering message listeners", e);
        }
    }
    
    /**
     * The thread either executes tasks or sleep.
     */
    @Override
    public void run() {
         // do in infitite loop
        MessageReceived tmpMessage = null;            
        Iterator<MessageListener> iterator = null;

         while(true){
            // yield for some time, processor rest
            //this.pause(500);
            Thread.yield();
            try {
                Thread.sleep(0, 200);
            } catch (InterruptedException ex) {
                
            }

            
            // shutdown
            if (this.shutdown == true){
                log.info("MyMessageReceiver shutting down.");
                
                return;
            }

            //  nulltest on queue itself - should not happen
            if (queue==null){
                log.error("Queue is null - should not happen, queue is final, "
                        + "initialized in constructor. Has to exit...");
                
                break;
            }
         

             // yield for some time iff is queue empty
             if (queue == null || queue.isEmpty()) {
                 this.pause(5);
                 continue;
             }

             //  nulltest
             if (queue == null) {
                 continue;
             }

             // check queue size
             if (queue.size() > MAX_QUEUE_SIZE_TO_RESET) {
                 queue.clear();

                 log.warn("Warning! Input queue had to be flushed out!"
                         + "Overflow, size was greater than " + MAX_QUEUE_SIZE_TO_RESET);
                 continue;
             }

             // if is nonempty, select first element
             if (!queue.isEmpty()) {
                 tmpMessage = queue.remove();
             } else {
                 tmpMessage = null;
             }

             // end of synchronization block, check if we have some message
             if (tmpMessage == null) {
                 continue;
             }

             // check listener for existence
             if (!(tmpMessage instanceof MessageReceived)) {
                 log.error("Message is not instance of messageReceived - please inspect it");
                 continue;
             }

             // determine message received
             Message msg = tmpMessage.getMsg();
             if (msg == null) {
                 // empty message, continue
                 log.error("Message is empty in envelope", tmpMessage);
                 continue;
             }

             Integer amtype = Integer.valueOf(msg.amType());

             // is this message registered to anybody?
             if (messageListeners.containsKey(amtype) == false) {
                 // registered to no one, continue - why not 
                 // registered message arrived? Weird
                 log.warn("Message arrived but no message listener "
                         + "registered to it - how it could arrive? "
                         + "AMtype: " + amtype);
                 continue;
             }

             // get list of listeners interested in receiving messages of
             // this AMtype
             ConcurrentLinkedQueue<MessageListener> listenersList = messageListeners.get(amtype);
             if (listenersList == null || listenersList.isEmpty()) {
                 // list is null || empty, cannot forward to anyone
                 continue;
             }

             // iterate over listeners list and notify each listener 
             // in serial manner
             boolean large = queue.size() > 300;

             if (large) {
                 log.info(this.getName() + "; XXqsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
             }
             
             // initialize filter item for next filter decisions
             AMTypeNodeIDComposite filterItem = new AMTypeNodeIDComposite(amtype, tmpMessage.getGateway());
             
             // iterate over all registered listeners
             for(MessageListener curListener : listenersList){
                 if (curListener == null) {
                     continue;
                 }
                 
                // message filtering, is this listener interested in this message?
                HashSet<AMTypeNodeIDComposite> filterSet = this.messageFilter.get(curListener);
                if (filterSet==null){
                    log.warn("Something is wrong with filter settings, cannot find no record for: " + curListener.toString());
                    continue;
                }
                
                // is this listener interested in this message?
                if (filterSet.contains(filterItem)==false){
                    // not interested in this message, continue
                    continue;
                }

                 // notify this listener,, separate try block - exception
                 // can be thrown, this exception affects only one listener
                 try {
                     // notify here
                     curListener.messageReceived(tmpMessage.getI(), msg, tmpMessage.getTimeReceivedMili());

                     if (large) {
                         log.info(this.getName() + "; qsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
                     }
                 } catch (Exception e) {
                     log.error("Exception during notifying listener", e);
                     continue;
                 }
             }

             // set message to null to release it from memory for garbage collector
             tmpMessage = null;
        }
    }

    /**
     * Return size of message queue to send
     *
     * @return
     */
    public synchronized int getQueueLength(){
        return this.queue != null ? this.queue.size() : 0;
    }

    @Override
    public void messageReceived(int i, Message msg) {
        // blocking?
        if (this.dropingPackets) return;
     
        //log.info("PCKT["+System.currentTimeMillis()+"]: " + i + "; AMtype: " + msg.amType() + "; src: " + msg.getSerialPacket().get_header_src());
        // really add message to queue
        MessageReceived msgReceiveed = new MessageReceived(i, msg);
        msgReceiveed.setTimeReceivedMili(System.currentTimeMillis());
        this.queue.add(msgReceiveed);
    }

    @Override
    public void deregisterListener(int node, Message msg, net.tinyos.message.MessageListener listener) {
        this.deregisterListener(msg, listener);
    }

    @Override
    public void registerListener(int node, Message msg, MessageListener listener) {
        this.registerListener(msg, listener);
    }

    @Override
    public void disconnectNode(ConnectedNode nh, boolean resetQueues) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void disconnectNode(ConnectedNode nh, Properties props) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectNode(ConnectedNode nh, Properties props) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */
    
    public synchronized void setGateway(MoteIF gateway, boolean reset) {
        this.gateway = gateway;
        
        // reset internal queues
        if (reset) {
            this.reset();
        }
        
        // re-register listeners here
        this.reregisterListeners();
        
        // reset queues again
        if (reset) {
            this.reset();
        }
        log.info("Gateway changed for MessageListener");
    }

    public ConcurrentLinkedQueue<MessageReceived> getQueue() {
        return queue;
    }

    public long getTimeLastMessageSent() {
        return timeLastMessageSent;
    }

    public void setTimeLastMessageSent(Long timeLastMessageSent) {
        this.timeLastMessageSent = timeLastMessageSent;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public boolean isDropingPackets() {
        return dropingPackets;
    }

    @Override
    public void setDropingPackets(boolean dropingPackets) {
        this.dropingPackets = dropingPackets;
    }

    
    /**
     * Indexing class for decision whether for received note with given AMTYPE
     * current listener accepts this new message based on amtype, nodeid
     */
    protected class AMTypeNodeIDComposite{
        private int amtype;
        private int nodeid;

        public AMTypeNodeIDComposite() {
        }
        
        public AMTypeNodeIDComposite(int amtype, int nodeid) {
            this.amtype = amtype;
            this.nodeid = nodeid;
        }

        public int getAmtype() {
            return amtype;
        }

        public void setAmtype(int amtype) {
            this.amtype = amtype;
        }

        public int getNodeid() {
            return nodeid;
        }

        public void setNodeid(int nodeid) {
            this.nodeid = nodeid;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AMTypeNodeIDComposite other = (AMTypeNodeIDComposite) obj;
            if (this.amtype != other.amtype) {
                return false;
            }
            if (this.nodeid != other.nodeid) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + this.amtype;
            hash = 79 * hash + this.nodeid;
            return hash;
        }
    }
}
