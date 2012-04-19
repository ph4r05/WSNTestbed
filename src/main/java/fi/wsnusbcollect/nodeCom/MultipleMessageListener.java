/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import fi.wsnusbcollect.nodes.ConnectedNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.tinyos.message.Message;
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
    private final ConcurrentLinkedQueue<MessageReceived> queue = 
            new ConcurrentLinkedQueue<MessageReceived>();

    /**
     * AMTYPE -> message mapping
     */
    protected static ConcurrentHashMap<Integer, net.tinyos.message.Message> amType2Message = 
            new ConcurrentHashMap<Integer, Message>(2);
    
    /**
     * Queue of message listeners registered for particular AMType
     */
    protected ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>> messageListeners = 
            new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>>(2);
    
    /**
     * Message filter for each message listener, registers whether particular listener
     * is interested in receiving AMTYPE message from NODEID gateway
     * 
     */
    protected ConcurrentHashMap<MessageListener, Set<AMTypeNodeIDComposite>> messageFilter = 
            new ConcurrentHashMap<MessageListener, Set<AMTypeNodeIDComposite>>();

    /**
     * Map NodeID -> AMtype of message 
     * Map of already registered listeners (to be able to re-register)
     */
    protected ConcurrentHashMap<Integer, Set<Integer>> registeredListeners = 
            new ConcurrentHashMap<Integer, Set<Integer>>();
    
    /**
     * NodeID -> Gateway listener
     */
    protected ConcurrentHashMap<Integer, HelperMessageListener> registeredGatewayListeners =
            new ConcurrentHashMap<Integer, HelperMessageListener>();
    
    /**
     * NodeID -> ConnectedNode
     * Internal mapping to be able to obtain connected node
     */
    protected ConcurrentHashMap<Integer, ConnectedNode> connectedNodes = 
            new ConcurrentHashMap<Integer, ConnectedNode>();
    
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
        
        // register to all defined nodes
        for(Integer node : this.connectedNodes.keySet()){
            this.registerListener(node, msg, listener);
        }
    }
    
    @Override
    public void registerListener(int node, Message msg, MessageListener listener) {
        // register for particular node here
        // null?
        if (msg==null || listener==null){
            log.error("Cannot register listener when message or listener is null");
            throw new NullPointerException("Cannot register listener when message or listener is null");
        }
        
        try {
            // node id needs to be in database
            if (this.connectedNodes.containsKey(node)==false){
                log.error("Cannot register listener for node: " + node + "; not found in database");
                return;
            }
            
            // is node OK?
            ConnectedNode cn = this.connectedNodes.get(node);
            if (cn==null || cn.getMoteIf()==null){
                log.error("Cannot register properly, connected node is invalid");
                return;
            }
            
            //
            // Phase - AMTYpe mapping
            //
            
            // amtype convert, register to static to be able to translate by it
            // is message in translate queue?
            Integer amtype = msg.amType();

            // if does not contain mapping amtype -> message, create one
            if (amType2Message.containsKey(amtype)==false){
                amType2Message.put(amtype, msg);
            }
            
            // check if linked list exists
            if (this.messageListeners.containsKey(amtype)==false 
                    || this.messageListeners.get(amtype)==null){
                // none such mapping yet created, create new list of listeners
                ConcurrentLinkedQueue<MessageListener> queueListener = new ConcurrentLinkedQueue<MessageListener>();
                this.messageListeners.put(amtype, queueListener);
            }

            //
            // Phase - list of listeners
            //
            
            // here is queueListener set, get it now
            ConcurrentLinkedQueue<MessageListener> queueListeners = this.messageListeners.get(amtype);
            
            // check if is already in linked queue
            if (queueListeners==null){
                // always have queue initialized
                queueListeners = new ConcurrentLinkedQueue<MessageListener>();
            }
            
            boolean isAlreadyInQueue = false;
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
                    isAlreadyInQueue = true;
                    break;
                }
            }

            // if here, listener is not in, set it
            if (isAlreadyInQueue==false){
                queueListeners.add(listener);
            }

            // update map
            this.messageListeners.put(amtype, queueListeners);
            
            //
            // next phase - update filter
            //
            AMTypeNodeIDComposite filterItem = new AMTypeNodeIDComposite(amtype, node);
            Set<AMTypeNodeIDComposite> filterSet = this.messageFilter.get(listener);
            if(filterSet==null){
                // set is not initialized - create new one from concurent hash map
                filterSet = Collections.newSetFromMap(new ConcurrentHashMap<AMTypeNodeIDComposite, Boolean>());
            }
            
            filterSet.add(filterItem);
            this.messageFilter.put(listener, filterSet);
            
            //
            // next phase - set registered AMtypes and register if is not already
            //
            Set<Integer> registeredAMTypes = this.registeredListeners.get(node);
            if (registeredAMTypes==null){
                registeredAMTypes = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
            }
            
            if (registeredAMTypes.contains(amtype)==false){
                // new helper listener
                HelperMessageListener helperListener = null;
                if (this.registeredGatewayListeners.containsKey(node)){
                    helperListener = this.registeredGatewayListeners.get(node);
                } else {
                    helperListener = new HelperMessageListener(node);
                    this.registeredGatewayListeners.put(node, helperListener);
                }
                
                cn.getMoteIf().registerListener(msg, helperListener);
                registeredAMTypes.add(amtype);
            }
            
            this.registeredListeners.put(node, registeredAMTypes);
        } catch(Exception e){
            log.error("Problem during registering message listener", e);
        }
    }
    
    /**
     * unregister message listener
     */
    @Override
    public synchronized void deregisterListener(net.tinyos.message.Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener){
        if (msg==null || listener==null){
            log.error("Cannot register listener when message or listener is null");
            throw new NullPointerException("Cannot register listener when message or listener is null");
        }
        
        // register to all defined nodes
        for(Integer node : this.connectedNodes.keySet()){
            this.deregisterListener(node, msg, listener);
        }
    }

    
    @Override
    public void deregisterListener(int node, Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener) {
        // register for particular node here
        // null?
        if (msg==null || listener==null){
            log.error("Cannot register listener when message or listener is null");
            throw new NullPointerException("Cannot register listener when message or listener is null");
        }
        
        
        try {
            // node id needs to be in database
            if (this.connectedNodes.containsKey(node)==false){
                log.error("Cannot register listener for node: " + node + "; not found in database");
                return;
            }
            
            // is node OK?
            ConnectedNode cn = this.connectedNodes.get(node);
            if (cn==null || cn.getMoteIf()==null){
                log.error("Cannot register properly, connected node is invalid");
                return;
            }
            
            //
            // Phase - AMTYpe mapping
            //
            
            // amtype convert, register to static to be able to translate by it
            // is message in translate queue?
            Integer amtype = msg.amType();
            
            // if does not contain mapping amtype -> message, create one
            if (amType2Message.containsKey(amtype)==false){                
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
            
            //
            // next phase - update filter
            //
            AMTypeNodeIDComposite filterItem = new AMTypeNodeIDComposite(amtype, node);
            Set<AMTypeNodeIDComposite> filterSet = this.messageFilter.get(listener);
            if(filterSet==null){
                // set is not initialized - create new one from concurent hash map
                filterSet = Collections.newSetFromMap(new ConcurrentHashMap<AMTypeNodeIDComposite, Boolean>());
            }
            
            filterSet.remove(filterItem);
            this.messageFilter.put(listener, filterSet);  
            
            //
            // next phase - deregister from listening to moteif 
            // if there is no active listener in filter
            //
            boolean isContainedInAtLeastOneFilterSet=false;
            for(Set<AMTypeNodeIDComposite> curFilterSet : this.messageFilter.values()){
                if (curFilterSet==null || curFilterSet.isEmpty()) continue;
                if (curFilterSet.contains(filterItem)){
                    isContainedInAtLeastOneFilterSet=true;
                    break;
                }
            }
            
            // filter item is contained in at least one filter set?
            // if no => there is no interest to receive packets of AMTYPE from NODEID -> deregister
            // need to have registered listener in gateway listeners
            if (isContainedInAtLeastOneFilterSet==false){
                HelperMessageListener helperListener = null;
                if (this.registeredGatewayListeners.containsKey(node)==false){
                    log.warn("Inconsistency experienced, want to deregister listener, but "
                            + "there is no gateway listener registered.");
                } else {
                    helperListener = this.registeredGatewayListeners.get(node);
                    
                    // deregister from real physical interface
                    cn.getMoteIf().deregisterListener(msg, helperListener);

                    // delete record from registered listeners
                    Set<Integer> registeredAMTypes = this.registeredListeners.get(node);
                    if (registeredAMTypes==null){
                        registeredAMTypes = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
                    }

                    registeredAMTypes.remove(amtype);
                    this.registeredListeners.put(node, registeredAMTypes);
                }
            }
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
        if (this.registeredListeners==null || this.registeredListeners.isEmpty()) return;
        
        try{
            // register to all registered amtypes to real interface
            for (Integer nodeid : this.registeredListeners.keySet()){
                // exists in database?
                if (this.connectedNodes.containsKey(nodeid)==false){
                    // such node is not contained in register, inconsistent state
                    log.warn("Registered listeners to node: " + nodeid + "; but no such node is in register!");
                    
                    // TODO: 
                    // filter and registered listeners should be cleaned and checked as well
                    // but for now it is ignored, such condition should never hold.
                    // implement cleaning procedure when disconnecting from node?
                    
                    // delete and continue;
                    this.connectedNodes.remove(nodeid);
                    continue;
                }
                
                ConnectedNode cn = this.connectedNodes.get(nodeid);
                if (cn==null || cn.getMoteIf()==null){
                    log.error("Node is invalid or has null MoteIF, nodeId: " + nodeid + "; cannot re-register");
                    continue;
                }
                
                
                Set<Integer> registeredAmtypes = this.registeredListeners.get(nodeid);
                for (Integer curAmtype: registeredAmtypes){
                    // exists am mapping?
                    if (amType2Message.containsKey(curAmtype)==false){
                        log.warn("Inconsistent state, re-registering amtype: " + curAmtype + "; but is not in mapping to message");
                        continue;
                    }
                    
                    cn.getMoteIf().registerListener(amType2Message.get(curAmtype), this);
                }
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
                Set<AMTypeNodeIDComposite> filterSet = this.messageFilter.get(curListener);
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
    @Override
    public synchronized int getQueueLength(){
        return this.queue != null ? this.queue.size() : 0;
    }

    /**
     * Entry method for helper message receivers - contains gateway information
     * For each gateway is registered different helper class
     * 
     * @param gateway
     * @param i
     * @param msg
     * @param mili 
     */
    public void messageReceivedFromHelper(int gateway, int i, Message msg, long mili){
        // blocking?
        if (this.dropingPackets) return;

        MessageReceived msgReceiveed = new MessageReceived(i, msg);
        msgReceiveed.setGateway(gateway);
        msgReceiveed.setTimeReceivedMili(mili);
        this.queue.add(msgReceiveed);
    }
    
    @Override
    public void messageReceived(int i, Message msg) {
        // blocking?
        if (this.dropingPackets) return;
        
        MessageReceived msgReceiveed = new MessageReceived(i, msg);
        msgReceiveed.setTimeReceivedMili(System.currentTimeMillis());
        this.queue.add(msgReceiveed);
    }


    @Override
    public void disconnectNode(ConnectedNode nh, boolean resetQueues) {
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        // exists?
        if (this.connectedNodes.containsKey(nh.getNodeId())==false){
            log.error("Cannot disconnect from nodeId: " + nh.getNodeId() + "; not connected to");
            return;
        }
        
        this.cleanNodeId(nh.getNodeId());
    }
    
    /**
     * Deletes all mappings for given nodeid
     * @param nodeid 
     */
    protected void cleanNodeId(int nodeid){
        ConnectedNode cn = null;
        if (this.connectedNodes.containsKey(nodeid)){
            cn = this.connectedNodes.get(nodeid);
        }
        
        // deregister all listeners for particular node
        if (this.registeredListeners.containsKey(nodeid)){
            Set<Integer> amtypesRegistered = this.registeredListeners.get(nodeid);
            for(Integer registeredAmtype : amtypesRegistered){
                if (amType2Message.containsKey(registeredAmtype)==false){
                    log.warn("Inconsistency experienced - registered AMtype is "
                            + "not found in global static register. NodeId: " + nodeid + "; amtype: " + registeredAmtype);
                    continue;
                }
                
                Message msgRegistered = amType2Message.get(registeredAmtype);
                
                // obtain gateway listener
                if (this.registeredGatewayListeners.containsKey(nodeid)==false){
                    log.warn("Inconsistency experienced - registered listener "
                            + "but node has not gateway listener. NodeID: " + nodeid);
                    continue;
                }
                
                HelperMessageListener helperMessage = this.registeredGatewayListeners.get(nodeid);
                
                // build amfilter 
                AMTypeNodeIDComposite filterItem = new AMTypeNodeIDComposite(registeredAmtype, nodeid);
                
                // deregister properly all listeners that has in filter entry for this
                Set<MessageListener> listenersToDeregister = new HashSet<MessageListener>();
                for(MessageListener cListener : this.messageFilter.keySet()){
                    Set<AMTypeNodeIDComposite> filterSet = this.messageFilter.get(cListener);
                    if (filterSet.contains(filterItem)){
                        listenersToDeregister.add(cListener);
                    }
                }
                
                // deregister all listeners
                for(MessageListener cListener : listenersToDeregister){
                    this.deregisterListener(nodeid, msgRegistered, cListener);
                }
                
                if (cn!=null){
                    // deregister real helper message
                    cn.getMoteIf().deregisterListener(msgRegistered, helperMessage);
                }
            }
            
            // determine if there is any listener wit empty filter
            
            // delete all registered listeners to reflect actual state
            this.registeredListeners.remove(nodeid);
        }
        
        
    }

    @Override
    public void disconnectNode(ConnectedNode nh, Properties props) {
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        boolean reset=false;
        if (props!=null){
            String resetQueues = props.getProperty("reset", "false");
            try {
                reset = Boolean.parseBoolean(resetQueues);
            } catch(Exception e){
                log.warn("Cannot convert resetQueues to boolean", e);
            }
        }
        
        this.disconnectNode(nh, reset);
    }
    
    /**
     * Add node to registered nodes
     * @param nh
     * @param props 
     */
    @Override
    public void connectNode(ConnectedNode nh, Properties props) {
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        // check if already connected to
        if (this.connectedNodes.containsKey(nh.getNodeId())){
            // check if is same as registered version
            ConnectedNode oldCn = this.connectedNodes.get(nh.getNodeId());
            
            if (nh.equals(oldCn)==false){
                // definitely new objetc, register
                this.connectedNodes.put(nh.getNodeId(), nh);
            } else {
                // nothing to do, same object as already registered
                return;
            }
        } else {
            // not in connectedGateways, add new
            this.connectedNodes.put(nh.getNodeId(), nh);
        }
        
        boolean reset=false;
        if (props!=null){
            String resetQueues = props.getProperty("reset", "false");
            try {
                reset = Boolean.parseBoolean(resetQueues);
            } catch(Exception e){
                log.warn("Cannot convert resetQueues to boolean", e);
            }
        }
        
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
        
        log.info("Added nodeid: " + nh.getNodeId() + " to MessageListener");
    }
    
    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */

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
     * Helper class to support message received messages from particular gateways -
     * include base station id
     */
    protected class HelperMessageListener implements MessageListener {
        private int gateway=0;

        public HelperMessageListener() {
        }

        public HelperMessageListener(int gateway) {
            this.gateway = gateway;
        }
        
        public int getGateway() {
            return gateway;
        }

        public void setGateway(int gateway) {
            this.gateway = gateway;
        }
        
        /**
         * Redirector with gateway specified
         * @param i
         * @param msg
         * @param mili 
         */
        @Override
        public void messageReceived(int i, Message msg, long mili) {
            messageReceivedFromHelper(gateway, i, msg, mili);
        }

        /**
         * Entry point from tiny os message listener
         * @param i
         * @param msg 
         */
        @Override
        public void messageReceived(int i, Message msg) {
            this.messageReceived(i, msg, System.currentTimeMillis());
        }
        
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
