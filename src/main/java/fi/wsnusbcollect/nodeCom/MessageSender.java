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
 */
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * @author ph4r05
 */
public class MessageSender extends Thread {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    
    /**
     * maximum number of notify threads in fixed size thread pool
     */
    private static final int MAX_NOTIFY_THREADS=1;

    private ConcurrentLinkedQueue<MessageToSend> queue;
    private MessageToSend msgToSend;
    private MoteIF gateway;

    /**
     * Thread pool
     */
    protected ExecutorService tasks;

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
     *
     * @param gateway
     * @param logger
     */
    public MessageSender(MoteIF gateway) {
        super("MessageSender");
        queue = new ConcurrentLinkedQueue<MessageToSend>();
        toNotify = new ConcurrentLinkedQueue<MessageToSend>();
        this.gateway = gateway;

        // delivery guarantor
        this.messageDeliveryGuarantor = new MessageDeliveryGuarantor(this);

        // instantiate thread pool
        tasks = Executors.newFixedThreadPool(MAX_NOTIFY_THREADS);
        // create notify threads
        for(int i=0; i<MAX_NOTIFY_THREADS; i++){
            tasks.execute(new MessageSenderNotifyWorker());
        }
    }

    /**
     * Performs shutdown
     */
    public synchronized void shutdown(){
        this.shutdown = true;
        this.reset();
        this.tasks.shutdown();
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
    public synchronized void reset(){
        this.queue.clear();
        this.msgToSend = null;
        this.toNotify.clear();
        
        log.info("MesageSender queues was flushed");
    }

    /*
    The thread either executes tasks or sleep.
     */
    @Override
    public void run() {
         // do in infitite loop
         while(true){
            // yield for some time
            this.pause(30);

            // shutdown
            if (this.shutdown == true){
                log.info("Message sender shutdown");
                
                this.tasks.shutdown();
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
            synchronized(queue){
                if (this.queue.isEmpty()){
                    msgToSend=null;
                }
                else {
                    msgToSend=queue.remove();
                }
            }

            synchronized(this){
                // if message was null, continue to sleep
                if (msgToSend==null) continue;

                try {
                    // send message
                    gateway.send(msgToSend.getDestination(), msgToSend.getsMsg());

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
                    if (msgToSend.listener != null && msgToSend.listener.isEmpty()==false){
                        // do it in sycnhronized block not to interfere with reading
                        // threads
                        synchronized(this.toNotify){
                            this.toNotify.add(msgToSend);
                        }
                    }

//                    if (this.logger!=null && msgToSend.string!=null){
//                        this.logger.addLogEntry(msgToSend.string, 56, "MsgSender", 0, 1, JPannelLoggerLogElement.SEVERITY_INFO);
//                    }
                    log.info("Sending message: NodeID: " +  msgToSend.getDestination() + "; StringID: " + msgToSend.string);

                    // store last sent messages
                    this.timeLastMessageSent = System.currentTimeMillis();
                } catch (Exception e) {
                    log.error("Exception during message sending", e);
                }
            }
            
            // sleep now - give connected node some time
            try {
                Thread.sleep(this.sentSleepTime);
            } catch (InterruptedException ex) {
                log.error("Cannot sleep", ex);
            }
        }
    }

    /**
     * Return TRUE if is possible to add new message to send, FALSE otherwise
     * (moteInterface may be NULL => cannot add message to send)
     * 
     * @return booleans
     */
    public boolean canAdd(){
        return (this.getGateway()!=null && this.shutdown==false);
    }

    /**
     * Adds message to send to send queue
     * @param target
     * @param msg
     * @param text
     */
    public void add(int target, net.tinyos.message.Message msg, String text){
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }

        MessageToSend msgRecord = new MessageToSend(msg, target, text);

        synchronized(this.queue){
            this.queue.add(msgRecord);
        }
    }

    /**
     * Adds message to send to send queue, with wanted notification after sent
     * @param target
     * @param msg
     * @param text
     */
    public void add(int target, net.tinyos.message.Message msg, String text,
            MessageSentListener listener, String listenerKey){
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        MessageToSend msgRecord = new MessageToSend(msg, target, text, listener, listenerKey);

        synchronized(this.queue){
            this.queue.add(msgRecord);
        }
    }

    /**
     * Adds initialized message to send to send queue.
     * @param msg
     */
    public void add(MessageToSend msg){
        if (this.canAdd()==false){
            throw new NullPointerException("Cannot add message to send queue since gateway is null");
        }
        
        synchronized(this.queue){
            this.queue.add(msg);
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
                this.pause(150);
                
                //  nulltest
                if (toNotify==null) continue;

                // select new message from queue in synchronized block
                synchronized(toNotify){
                    // if is nonempty, select first element
                    if (!toNotify.isEmpty()){
                        tmpMessage = toNotify.remove();
                    }
                    else {
                        tmpMessage = null;
                    }
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
        return gateway;
    }

    public synchronized void setGateway(MoteIF gateway) {
        this.gateway = gateway;
        this.reset();
        
        if (this.messageDeliveryGuarantor!=null){
            this.messageDeliveryGuarantor.registerListeners();
        }
        
        log.info("Gateway changed for MessageSender, queues flushed");
    }

    public ConcurrentLinkedQueue<MessageToSend> getQueue() {
        return queue;
    }

    public ExecutorService getTasks() {
        return tasks;
    }

    public long getTimeLastMessageSent() {
        return timeLastMessageSent;
    }

    public void setTimeLastMessageSent(Long timeLastMessageSent) {
        this.timeLastMessageSent = timeLastMessageSent;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public MessageDeliveryGuarantor getMessageDeliveryGuarantor() {
        return messageDeliveryGuarantor;
    }

    public int getSentSleepTime() {
        return sentSleepTime;
    }

    public void setSentSleepTime(int sentSleepTime) {
        this.sentSleepTime = sentSleepTime;
    }
}
