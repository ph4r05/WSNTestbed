/**
 * @author ph4r05
 * 
 * Extended version of MessageSender.
 * Thread-safe, sent-successful-notification-able
 */

package fi.wsnusbcollect.nodeCom;

// $Id: MessageSender.java,v 1.2 2008/03/11 11:18:51 a_barbirato Exp $
/*									tab:4
 * Copyright (c) 2007 University College Dublin.
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL UNIVERSITY COLLEGE DUBLIN BE LIABLE TO ANY
 * PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF 
 * UNIVERSITY COLLEGE DUBLIN HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * UNIVERSITY COLLEGE DUBLIN SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND UNIVERSITY COLLEGE DUBLIN HAS NO
 * OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 *
 * Authors:	Raja Jurdak, Antonio Ruzzelli, and Samuel Boivineau
 * Date created: 2007/09/07
 *
 */
/**
 * @author Raja Jurdak, Antonio Ruzzelli, and Samuel Boivineau
 * @author Dusan Klinec (ph4r05) - extended basic idea
 * 
 */
import fi.wsnusbcollect.nodes.ConnectedNode;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import net.tinyos.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main sending class. Sends messages in send queue to the network.
 * Does not need to know what particular message is sending, uses general interface for
 * messages.
 *
 * Uses another thread pool to notify event listeners that message was sent.
 * (notification does not affect main MsgSender, is isolated)
 *
 * Constraints: should be only one running thread for one gateway node.
 * @todo: multiton generator for this object
 * 
 * Spawns 2 threads, 1 for message sending, another for message-sent-notifier
 *
 * @author ph4r05
 */
public class MultipleMessageSender extends Thread implements MessageSentListener, MessageSenderInterface{
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    
    /**
     * maximum number of notify threads in fixed size thread pool
     */
    private int notifyThreads=1;
    
    /**
     * Number of sender threads
     */
    private int senderThreads=0;

    /**
     * Message queue to send
     */
    private ConcurrentLinkedQueue<MessageToSend> queue;
    private MessageToSend msgToSend;
    
    /**
     * Default gateway
     */
    private Integer gateway;
    
    /**
     * Another connected gateways trough which messages can be send
     */
    private ConcurrentHashMap<Integer, MoteIF> connectedGateways = new ConcurrentHashMap<Integer, MoteIF>();
    
    /**
     * Hashmap GatewayID -> time when is node available for next message (maybe delayed after previous message)
     */
    private ConcurrentHashMap<Integer, Long> preparedTimeGateway = new ConcurrentHashMap<Integer, Long>();

    /**
     * Thread pool
     */
    protected ExecutorService tasksNotifiers;
    
    /**
     * Thread pool for message senders
     */
    protected ExecutorService tasksSenders;

    /**
     * To notify queue for notifier threads
     */
    protected ConcurrentLinkedQueue<MessageToSend> toNotify=null;

    /**
     * time of last sent message
     */
    protected long timeLastMessageSent;

    /**
     * Should I shutdown?
     */
    protected boolean shutdown=false;

    /**
     * Class acknowledging delivery of some messages
     */
    protected MessageDeliveryGuarantor messageDeliveryGuarantor=null;
    
    /**
     * Delay between two sent messages from queue
     */
    private int sentSleepTime=100;
    
    /**
     * Map of messages that are needed to be send synchronously
     */
    private Map<String, MessageToSend> blockingSendMessage;
    
    /**
     * Successfully sent message in blocking mode
     */
    private Set<String> blockingMessageSent;

    /**
     * Time of last cleanup performed - costly operation + has to be valid 
     * for some amount of time.
     */
    private long lastBlockingCleanup;
    /**
     *
     * @param gateway
     * @param logger
     */
    public MultipleMessageSender(Integer gatewayId, MoteIF gateway) {
        super("MessageSender");
        this.queue = new ConcurrentLinkedQueue<MessageToSend>();
        this.toNotify = new ConcurrentLinkedQueue<MessageToSend>();
        this.blockingSendMessage = new ConcurrentHashMap<String, MessageToSend>();
        this.blockingMessageSent = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        this.preparedTimeGateway = new ConcurrentHashMap<Integer, Long>();
        this.connectedGateways = new ConcurrentHashMap<Integer, MoteIF>();
        
        if (gatewayId!=null){
            this.connectedGateways.put(gatewayId, gateway);
        }
        
        this.gateway = gatewayId;

        // delivery guarantor
        //this.messageDeliveryGuarantor = new MessageDeliveryGuarantor(this);

        // instantiate thread pool
        if (senderThreads>0){
            tasksSenders = Executors.newFixedThreadPool(senderThreads);
        }
        
        tasksNotifiers = Executors.newFixedThreadPool(notifyThreads);
        // create notify threads
        for(int i=0; i<notifyThreads; i++){
            tasksNotifiers.execute(new MessageSenderNotifyWorker());
        }
    }

    /**
     * Performs shutdown
     */
    @Override
    public synchronized void shutdown(){
        this.shutdown = true;
        this.reset();
        this.tasksNotifiers.shutdown();
    }
    
    /**
     * Pausing thread
     * @param milisecs
     */
    private void pause(int milisecs) {
        try {
            Thread.yield();
            Thread.sleep(milisecs);
        } catch (InterruptedException ie) {
            log.warn("Cannot sleep", ie);
        }
    }
    
    /**
     * perform hard reset to this object = clears entire memory
     */
    @Override
    public synchronized void reset(){
        this.queue.clear();
        this.msgToSend = null;
        this.toNotify.clear();
        
        log.info("MesageSender queues was flushed");
    }

    /**
     * The thread either executes tasksNotifiers or sleep.
     */
    @Override
    public void run() {
         // do in infitite loop
         while(true){
            // yield for some time, avoid CPU hogging
            this.pause(10);
            long currTime = System.currentTimeMillis();
            
            // cleaning for blocking operaions?
            if (currTime-this.lastBlockingCleanup > 60000){
                this.blockingMessageCleanup();
                this.lastBlockingCleanup = currTime;
            }
            
            // shutdown
            if (this.shutdown == true){
                log.info("Message sender shutdown");
                
                this.tasksNotifiers.shutdown();
                return;
            }

            //  nulltest on queue itself - should not happen
            if (queue==null){
                log.warn("Queue is null, reinitializing. This should not happen, please check it.");
                queue = new ConcurrentLinkedQueue<MessageToSend>();
                
                continue;
            }
            
            // perform tick on DeliveryGuarantor
            if (this.messageDeliveryGuarantor!=null){
                try{
                    this.messageDeliveryGuarantor.timerTick();
                 } catch(Exception e){
                     log.error("Exception when calling message delivery guarantor tick", e);
                 }
            }

            // test queue to send
            if (this.queue.isEmpty()){
                // nothing to do, queue is empty
                msgToSend=null;
                continue;
            }
            
            // now we need to select next message 4 send.
            // we need to take under consideration timeout limit after message sent 
            // (previous message could be reset for particular gateway thus we don't want
            // to send message to same node again very quickly)
            
            // now examine queue elements and delete those canceled
            while(queue.isEmpty()==false){
                MessageToSend peekedMessage = queue.peek();
                if (peekedMessage.isCanceled()==false){
                    // regular message, continue
                    break;
                }
                
                // throw away, canceled message
                queue.poll();
            }
            
            Integer gateway2SendId = null;
            MoteIF gateway2Send = null;
            
            // determine which message to use, can be send only if gateway is prepared now
            int index=0;
            for(Iterator<MessageToSend> iterator = queue.iterator(); iterator.hasNext(); index++){
                // iterate over whole send queue and find message to send
                // has to be able to send message - source of message (gateway) need
                // to be prepared - time is OK
                MessageToSend nextMessage = iterator.next();
                
                // determine wanted gateway for current message - if not specified, use default gateway
                Integer wantedGateway = nextMessage.getSource() != null ? nextMessage.getSource() : this.gateway;
                
                // is wanted gateway in set of gateways I am managing?
                if (wantedGateway!=null && this.connectedGateways.containsKey(wantedGateway)==false){
                    // no -> need to remove message
                    log.warn("Need to throw message away - gateway " + wantedGateway + " is not available here.", nextMessage);
                    iterator.remove();
                    continue;
                }
                
                // is wanted gateway prepared4sending? check times when will be prepared
                // if no record in map -> is prepared immediatelly, can continue 2 send
                if (this.preparedTimeGateway.containsKey(wantedGateway)){
                    // here prepared time is in map, check if is greater than current time
                    // if yes -> will be prepared in future -> cannot send via this gateway
                    Long preparedTime = this.preparedTimeGateway.get(wantedGateway);
                    if (preparedTime > currTime){
                        continue;
                    }
                }
                
                // time is OK here or no limitation is set for wanted gateway
                gateway2SendId = wantedGateway;
                msgToSend = nextMessage;
                break;
            }
            
            // check if we have something to send, if no, continue
            if (gateway2SendId==null || msgToSend==null){
                continue;
            }
            
            // gateway is choosen as well as msgToSend
            gateway2Send = this.connectedGateways.get(gateway2SendId);
            // remove from queue
            this.queue.remove(msgToSend);

            synchronized(this){
                // if message was null, continue to sleep
                if (msgToSend==null) continue;

                try {
                    // send message
                    gateway2Send.send(msgToSend.getDestination(), msgToSend.getsMsg());

                    // message was sent here, notify listener if exists
                    // QUESTION to think about: When I now call listener, how does it
                    // affect current thread?
                    // Pos. 1: execution will execute eventhandler, execution could take some time
                    // so sending will be blocked, in worst scenario this thread crashes and no more
                    // sending will occur
                    //
                    // Solution: fork another thread which will execute this particular
                    // messageSent method
                    // DarkSide of this idea: too many threads after some time?
                    // Solution2: add fixed size pool for threads
                    // If pool full, then a) ignore sending notifications; b) wait => back on the beginning
                    // main thread starvation
                    //
                    // Solution3, best: fixedThreadPool with notificators in loop, checking toNotify queue and execute
                    // notifications to listeners; notificator = define subclass in this class, to be able to access queue toNotify
    //
    //                // now dummy solution, just call
    //                if (msgToSend.listener != null){
    //                    msgToSend.listener.messageSent(msgToSend.getListenerKey(), msgToSend.getsMsg(), msgToSend.getDestination());
    //                }

                    // add message to tonotify queue if needed
                    if (msgToSend.useListener()){
                        // do it in sycnhronized block not to interfere with reading
                        // threads
                        this.toNotify.add(msgToSend);
                    }
                    
                    // basic message sent log
                    log.info("Sending message: NodeID: " +  msgToSend.getDestination() + "; StringID: " + msgToSend.getString());

                    // store last sent messages, global for every connected gateway
                    this.timeLastMessageSent = System.currentTimeMillis();
                    
                    // sleep exact amount of time requested by particular message
                    // for particular gateway
                    if (msgToSend.getPauseAfterSend()!=null){
                        this.preparedTimeGateway.put(gateway2SendId, currTime + msgToSend.getPauseAfterSend());
                    } else {
                        this.preparedTimeGateway.put(gateway2SendId, currTime + this.sentSleepTime);
                    }
                } catch (Exception e) {
                    log.error("Exception during message sending. "
                            + "Message for: " + msgToSend.getDestination()
                            + "; AMType: " + msgToSend.getsMsg().amType()
                            + "; currentTime: " + System.currentTimeMillis(), e);
                }
            }
        }
    }

    /**
     * Return TRUE if is possible to add new message to send, FALSE otherwise
     * (moteInterface may be NULL => cannot add message to send)
     * 
     * @return booleans
     */
    @Override
    public boolean canAdd(){
        return (this.getGateway()!=null && this.shutdown==false);
    }

    /**
     * Adds message to send to send queue
     * @param target
     * @param msg
     * @param text
     */
    @Override
    public void add(int target, net.tinyos.message.Message msg, String text){
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }

        MessageToSend msgRecord = new MessageToSend(msg, target, text);
        this.queue.add(msgRecord);
    }

    /**
     * Adds message to send to send queue, with wanted notification after sent
     * @param target
     * @param msg
     * @param text
     */
    @Override
    public void add(int target, net.tinyos.message.Message msg, String text,
            MessageSentListener listener, String listenerKey){
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        MessageToSend msgRecord = new MessageToSend(msg, target, text, listener, listenerKey);

        this.queue.add(msgRecord);
    }
    
    /**
     * Adds more messages at time. Can be blocking...
     * All messages are added to sending queue + if there are blocking messages
     * method waits for finishing it.
     * 
     * @param msgs
     * @return  list of failed blocking messages 
     * @throws TimeoutException 
     */
    @Override
    public Collection<MessageToSend> add(Collection<MessageToSend> msgs) throws TimeoutException {
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        
        List<MessageToSend> failedMessages = new LinkedList<MessageToSend>();
        // at first add non-blocking messages and remove them from collection
        for(Iterator<MessageToSend> iterator = msgs.iterator();iterator.hasNext();){
            MessageToSend nextMessage = iterator.next();
            // skip blocking messages - will be processed later
            if (nextMessage.isBlockingSend()){
                // has message correct listener key? if no, remove it and send alert
                // means that message is already waiting to be sent
                if (this.blockingSendMessage.containsKey(nextMessage.getListenerKey())){
                    log.warn("Such message is already in "
                            + "wait queue, listener key: " + nextMessage.getListenerKey());
                    iterator.remove();
                    failedMessages.add(nextMessage);
                    continue;
                }
                
                continue;
            }
            
            // nonblocking message here, just add to queue and remove from collection
            this.queue.add(nextMessage);
            iterator.remove();
        }
        
        // now he have only blocking messages in collection
        // if empty nothing to do next
        if (msgs.isEmpty()){
            return failedMessages;
        }
        
        // message listener key is now unique - set time of message sending
        long timeStart = System.currentTimeMillis();
        
        // iterate over messages, init and add 2 send
        for(MessageToSend msg : msgs){
            msg.setTimeAddedToSend(timeStart);
            String msgKey = msg.getListenerKey();

            // set myself as message watcher for successfull sending event
            msg.addListener(this);

            // add to blocking send message queue
            this.blockingSendMessage.put(msgKey, msg);
            this.queue.add(msg);
        }

        // repeatedly check if messages were already sent - 
        // do it until there is any unsent message
        while(msgs.isEmpty()==false){
            long currentTime = System.currentTimeMillis();
            // iterate all messages and removed send ones
            for(Iterator<MessageToSend> iterator = msgs.iterator(); iterator.hasNext();){
                MessageToSend msg = iterator.next();
                String msgKey = msg.getListenerKey();
                
                if ((currentTime-timeStart) > msg.getBlockingTimeout()){
                    // timeout, message was not sent...
                    log.warn("Message cannot be send, timeouted. Key: " + msgKey);
                    log.debug("Failed message: " + msg.toString());
                    
                    failedMessages.add(msg);
                    iterator.remove();
                    continue;
                }
                
                 // message was sent - check set queue?
                if (this.blockingMessageSent.contains(msgKey)){
                    // key was found => message was sent successfully, cleanup records.
                    // and return from blocking operation
                    this.blockingMessageSent.remove(msgKey);
                    this.blockingSendMessage.remove(msgKey);
                    iterator.remove();
                    continue;
                }
            }
            
            // all mesages cleared?
            if (msgs.isEmpty()) return failedMessages;

            // messages were not found...                
            // sleep small amount of time - CPU
            this.pause(10);
        }
        
        return failedMessages;
    }
    
    /**
     * Adds initialized message to send to send queue.
     * @param msg
     */
    @Override
    public void add(MessageToSend msg) throws TimeoutException{
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        
        // if message should be send in bocking way, handle it
        if (msg.isBlockingSend()){
            // message should be sent synchronously
            // unique message identifier expected here, do not send if 
            // is already waiting for send
            if (this.blockingSendMessage.containsKey(msg.getListenerKey())){
                throw new IllegalArgumentException("Such message is already in "
                        + "wait queue, listener key: " + msg.getListenerKey());
            }
            
            // message listener key is now unique - set time of message sending
            long timeStart = System.currentTimeMillis();
            msg.setTimeAddedToSend(timeStart);
            String msgKey = msg.getListenerKey();
            
            // set myself as message watcher for successfull sending event
            msg.addListener(this);
            
            // add to blocking send message queue
            this.blockingSendMessage.put(msgKey, msg);
            this.queue.add(msg);
            
            // repeatedly check if is message already sent - 
            while(true){
                long currentTime = System.currentTimeMillis();
                if ((currentTime-timeStart) > msg.getBlockingTimeout()){
                    // timeout, message was not sent...
                    log.warn("Message cannot be send, timeouted. Key: " + msgKey);
                    log.debug("Failed message: " + msg.toString());
                    throw new TimeoutException("Message was not send - timeout");
                }
                
                // message was sent - check set queue?
                if (this.blockingMessageSent.contains(msgKey)){
                    // key was found => message was sent successfully, cleanup records.
                    // and return from blocking operation
                    this.blockingMessageSent.remove(msgKey);
                    this.blockingSendMessage.remove(msgKey);
                    return;
                }
                
                // message was not found...
                // sleep small amount of time - CPU
                this.pause(10);
            }
        } else {
            this.queue.add(msg);
        }
    }

    /**
     * Watch message sent event for blocking message sending
     * @param listenerKey
     * @param msg
     * @param destination 
     */
    @Override
    public void messageSent(String listenerKey, Message msg, int destination) {
        // just add to set...
        if (this.blockingSendMessage.containsKey(listenerKey)){
            this.blockingMessageSent.add(listenerKey);
        }
    }
    
    /**
     * Iterates over map values and cleans unused values, then set is flushed 
     * according to map keys
     */
    protected void blockingMessageCleanup(){
        long currTime = System.currentTimeMillis();
        Iterator<MessageToSend> iterator = this.blockingSendMessage.values().iterator();
        while(iterator.hasNext()){
            MessageToSend m2s = iterator.next();
            
            // is over limit?
            if ((currTime - (m2s.getTimeAddedToSend() + m2s.getBlockingTimeout())) > 60000){
                this.blockingMessageSent.remove(m2s.getListenerKey());
                iterator.remove();
            }
        }
        
        // iterate over orphaned entities in set
        Iterator<String> iterator1 = this.blockingMessageSent.iterator();
        while(iterator1.hasNext()){
            String key = iterator1.next();
            if (this.blockingSendMessage.containsKey(key)) continue;
            
            // remove only orphaned messages
            iterator1.remove();
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
    public synchronized void disconnectNode(ConnectedNode nh, boolean resetQueues) {
        // disconnect given node from this listener
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        if (this.connectedGateways.containsKey(nh.getNodeId()) == false){
            // no such node in register, nothing to do
            return;
        }
        
        boolean gatewayRemoval = false;
        Integer nodeId = nh.getNodeId();
        if (nodeId.equals(this.gateway)){
            // ou shit, gateway removal, assign next random gateway from queue
            gatewayRemoval=true;
        }
        
        // remove from queue, prune
        Iterator<MessageToSend> iterator = this.queue.iterator();
        while(iterator.hasNext()){
            MessageToSend m2s = iterator.next();
            if (m2s.getSource()==null){
                if (gatewayRemoval==false) continue;
            } else if (m2s.getSource().equals(nh.getNodeId())==false){
                // not an interesting id
                continue;
            }
            
            iterator.remove();
        }
        
        // remove moteif from queue
        this.connectedGateways.remove(nh.getNodeId());
       
        // new gateway
        if (gatewayRemoval){
            Set<Integer> keySet = this.connectedGateways.keySet();
            if (keySet.size()>0){
                this.gateway = keySet.iterator().next();
            }
        }
    }

    @Override
    public synchronized void disconnectNode(ConnectedNode nh, Properties props) {
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        boolean reset=false;
        if (props!=null){
            String resetQueues = props.getProperty("resetQueues", "true");
            try {
                reset = Boolean.parseBoolean(resetQueues);
            } catch(Exception e){
                log.warn("Cannot convert resetQueues to boolean", e);
            }
        }
        
        this.disconnectNode(nh, reset);
    }

    @Override
    public synchronized void connectNode(ConnectedNode nh, Properties props) {
        if (nh==null){
            throw new NullPointerException("Cannot manipulate with null node");
        }
        
        // check if already connected to
        if (this.connectedGateways.containsKey(nh.getNodeId())){
            // check if is same as registered version
            MoteIF oldMoteIF = this.connectedGateways.get(nh.getNodeId());
            MoteIF newMoteIF = nh.getMoteIf();
            
            if (      (oldMoteIF==null && newMoteIF!=null)
                    ||(oldMoteIF!=null && newMoteIF==null)
                    ||(oldMoteIF.equals(newMoteIF)==false)){
                // definitely new objetc, register
                this.connectedGateways.put(nh.getNodeId(), newMoteIF);
            } else {
                // nothing to do, same object as already registered
                return;
            }
        } else {
            // not in connectedGateways, add new
            this.connectedGateways.put(nh.getNodeId(), nh.getMoteIf());
        }
        
        // if here, new node was added => if no gateway is assigned, pick this one as new gateway
        // node sender should definitely has some default gateway
        if (this.gateway==null){
            this.gateway = nh.getNodeId();
        }
    }

    @Override
    public void add(int source, int target, Message msg, String text) {
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        MessageToSend msgRecord = new MessageToSend(msg, target, text);
        msgRecord.setSource(source);

        this.queue.add(msgRecord);
    }

    @Override
    public void add(int source, int target, Message msg, String text, MessageSentListener listener, String listenerKey) {
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        MessageToSend msgRecord = new MessageToSend(msg, target, text, listener, listenerKey);
        msgRecord.setSource(source);

        this.queue.add(msgRecord);
    }

    /**
     * =========================================================================
     *
     * INTERNAL SUBCLASS NOTIFICATOR WORKER
     *
     * =========================================================================
     */

    /**
     * Perform notifications to listeners
     * Isolated from message sender not to block sending during event notification.
     */
    private class MessageSenderNotifyWorker extends Thread implements Runnable {
        public MessageSenderNotifyWorker() {
            ;
        }

        /**
         * Pausing thread
         * @param microsecs
         */
        private void pause(int microsecs) {
            try {
                Thread.yield();
                Thread.sleep(microsecs);
            } catch (InterruptedException ie) {
                log.warn("Cannot sleep: ", ie);
            }
        }

        /**
         * Log if there is some logger. Uses main thread logger
         * 
         * @param s
         * @param subtype
         * @param code
         * @param severity
         */
        private void log(String s, int subtype, int code, int severity){
                log.info(s);
        }

        /**
         * Main run method
         */
        @Override
        public void run() {
            // new message to be notified
            MessageToSend tmpMessage = null;

            // do in infitite loop
            while(true){
                // shutting down
                if (shutdown){
                    log.info("MessageSenderNotifyWorker shutdown");
                    return;
                }
                
                // yield for some time
                this.pause(10);
                
                //  nulltest
                if (toNotify==null) continue;
                
                // if is nonempty, select first element
                if (!toNotify.isEmpty()){
                    tmpMessage = toNotify.remove();
                }
                else {
                    tmpMessage = null;
                }

                // end of synchronization block, check if we have some message
                if (tmpMessage==null) continue;

                // check listener for existence
                if (!(tmpMessage instanceof MessageToSend)){
                    log.error("Message is not instance of messageToSend");
//                    this.log("Message is not instance of messageToSend", 1, 1, JPannelLoggerLogElement.SEVERITY_ERROR);
                    continue;
                }

                if (tmpMessage.getListener() == null || tmpMessage.getListener().isEmpty()) continue;

                // perform notification in try-catch to avoid
                // unexpected conditions
                try {
                    // here perform notification
                    List<MessageSentListener> listeners = tmpMessage.getListener();
                    Iterator<MessageSentListener> listenerIt = listeners.iterator();
                    while(listenerIt.hasNext()){
                        MessageSentListener curListener = listenerIt.next();
                        if (curListener == null) continue;
                        
                        // inner try block - if message sent throws exception, 
                        // not to fail other notifications in list
                        try {
                            curListener.messageSent(tmpMessage.getListenerKey(), tmpMessage.getsMsg(), tmpMessage.getDestination());
                        } catch(Exception e){
                            log.error("Cannot notify listener, excaption thrown", e);
                        }
                    }
                } catch(Exception e){
                    log.error("Exception happened during message listener notification; Exception: " + e.toString(), e);
//                    this.log("Exception happened during message listener notification; Exception: " + e.toString(),
//                            1, 2, JPannelLoggerLogElement.SEVERITY_ERROR);
                    continue;
                } finally {
                    // set message to null to release it from memory for garbage collector
                    tmpMessage = null;
                }
            } //end while(true)
        } // end run()
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */
    
    public MoteIF getGateway() {        
        return this.connectedGateways!=null && this.connectedGateways.containsKey(gateway) ? this.connectedGateways.get(gateway) : null;
    }

    public synchronized void setGateway(Integer gatewayId, MoteIF gateway){
        this.setGateway(gatewayId, gateway, true);
    }
    
    public synchronized void setGateway(Integer gatewayId, MoteIF gateway, boolean reset) {
        this.connectedGateways.put(gatewayId, gateway);
        this.gateway = gatewayId;
        if (reset){
            this.reset();
        }
        
        if (this.messageDeliveryGuarantor!=null){
            this.messageDeliveryGuarantor.registerListeners();
        }
        
        log.info("Gateway changed for MessageSender, queues flushed");
    }

    /**
     * Sets all gateways maintained by this class
     * 
     * @param gateways
     * @param defaultGateway
     * @param reset 
     */
    public synchronized void setAllGateways(Map<Integer, MoteIF> gateways, Integer defaultGateway, boolean reset){
        // need to reset state maps & variables
        this.preparedTimeGateway.clear();
        this.connectedGateways.clear();
        this.lastBlockingCleanup=0;
        
        this.connectedGateways.putAll(gateways);
        this.gateway = defaultGateway;

        if (reset){
            this.reset();
        }
        
        if (this.messageDeliveryGuarantor!=null){
            this.messageDeliveryGuarantor.registerListeners();
        }
    }
    
    public ConcurrentLinkedQueue<MessageToSend> getQueue() {
        return queue;
    }

    public ExecutorService getTasks() {
        return tasksNotifiers;
    }

    @Override
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

    public MessageDeliveryGuarantor getMessageDeliveryGuarantor() {
        return messageDeliveryGuarantor;
    }

    @Override
    public int getSentSleepTime() {
        return sentSleepTime;
    }

    public void setSentSleepTime(int sentSleepTime) {
        this.sentSleepTime = sentSleepTime;
    }

    public int getNotifyThreads() {
        return notifyThreads;
    }

    public void setNotifyThreads(int notifyThreads) {
        this.notifyThreads = notifyThreads;
    }

    public int getSenderThreads() {
        return senderThreads;
    }

    public void setSenderThreads(int senderThreads) {
        this.senderThreads = senderThreads;
    }

    public ConcurrentHashMap<Integer, MoteIF> getConnectedGateways() {
        return connectedGateways;
    }

    public void setConnectedGateways(ConcurrentHashMap<Integer, MoteIF> connectedGateways) {
        this.connectedGateways = connectedGateways;
    }
}
