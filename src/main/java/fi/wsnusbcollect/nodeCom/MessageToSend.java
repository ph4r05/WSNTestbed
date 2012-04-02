/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodeCom;

import java.util.LinkedList;
import java.util.List;
import net.tinyos.message.Message;

/**
 * Queue element for each message added to send.
 * Contains string passed to logger when message is sent.
 *
 * Optional fields are event listener which is triggered on message sent.
 * @author ph4r05
 */
public class MessageToSend {
    /**
     * Message to send
     */
    private net.tinyos.message.Message sMsg;
    
    /**
     * Log string - logged after send done
     */
    private String string;
    
    /**
     * source node id to send from - if multiple nodes are managed by single sender
     * If source is null, default gateway will be used to send such message
     */
    private Integer source;
    
    /**
     * destination of message. Node in network
     */
    private int destination;

    /**
     * Message sent listener list. After sending message are called listeners
     * contained in this list.
     */
    private List<MessageSentListener> listener=null;

    /**
     * Listener key for message listener to uniquely determine message
     */
    private String listenerKey=null;
    
    /**
     * If non-null, specified amount of time is waited between next message send
     */
    private Long pauseAfterSend=null;
    
    /**
     * Time of message arrival for sending to message sender
     */
    private long timeAddedToSend;
    
    /**
     * cancel flag. If from some reason sender cannot send message, it can be marked
     * as canceled and is skipped.
     */
    private boolean canceled=false;
    
    /**
     * If true then this sending this message is synchronous - methods ends after 
     * message is successfully sent - good choice when sending control messages
     * to nodes and task is time sensitive. 
     */
    private boolean blockingSend=false;
    
    /**
     * If message should be send synchronously (blockingSend==true) this field
     * gives maximum amount of time for waiting.
     */
    private long blockingTimeout=3000L;

    /**
     * Maximum retry count limit for this message.
     * If 0 packet is dropped, otherwise re-sent and decremented
     */
    private int resendRetryCount=3;

    public MessageToSend(net.tinyos.message.Message sMsg, int destination, String string) {
        this.string = string;
        this.destination = destination;
        this.sMsg = sMsg;
    }

    public MessageToSend(net.tinyos.message.Message sMsg, int destination, String string,
            MessageSentListener listener, String listenerKey) {
        this.sMsg = sMsg;
        this.string = string;
        this.destination = destination;
        this.listener = new LinkedList<MessageSentListener>();
        this.listener.add(listener);
        this.listenerKey = listenerKey;
    }

    /**
     * Adds message sent listener
     *
     * @param listener
     */
    public boolean addListener(MessageSentListener listener){
       if (this.listener == null){
           this.listener = new LinkedList<MessageSentListener>();
       }

       // add only if does not exist
       if (this.listener.contains(listener)){
           return false;
       }

       return this.listener.add(listener);
    }

    /**
     * Removes listener
     *
     * @param listener
     * @return
     */
    public boolean removeListener(MessageSentListener listener){
       if (this.listener == null){
           this.listener = new LinkedList<MessageSentListener>();
           return false;
       }

       return this.listener.remove(listener);
    }

    /**
     * Returns true if object has set some listeners which should be notified 
     * when message is successfully sent. 
     * 
     * @return 
     */
    public boolean useListener(){
        return this.listener!=null && this.listener.isEmpty()==false;
    }
    
    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public Message getsMsg() {
        return sMsg;
    }

    public void setsMsg(Message sMsg) {
        this.sMsg = sMsg;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public List<MessageSentListener> getListener() {
        return listener;
    }

    public void setListener(List<MessageSentListener> listener) {
        this.listener = listener;
    }

    public String getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(String listenerKey) {
        this.listenerKey = listenerKey;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Long getPauseAfterSend() {
        return pauseAfterSend;
    }

    public void setPauseAfterSend(Long pauseAfterSend) {
        this.pauseAfterSend = pauseAfterSend;
    }

    public boolean isBlockingSend() {
        return blockingSend;
    }

    public void setBlockingSend(boolean blockingSend) {
        this.blockingSend = blockingSend;
    }

    public long getBlockingTimeout() {
        return blockingTimeout;
    }

    public void setBlockingTimeout(long blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    public int getResendRetryCount() {
        return resendRetryCount;
    }

    public void setResendRetryCount(int resendRetryCount) {
        this.resendRetryCount = resendRetryCount;
    }

    public long getTimeAddedToSend() {
        return timeAddedToSend;
    }

    public void setTimeAddedToSend(long timeAddedToSend) {
        this.timeAddedToSend = timeAddedToSend;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public String toString() {
        return "MessageToSend{" + "sMsg=" + sMsg + ", string=" + string + ", source=" + source + ", destination=" + destination + ", listener=" + listener + ", listenerKey=" + listenerKey + ", pauseAfterSend=" + pauseAfterSend + ", timeAddedToSend=" + timeAddedToSend + ", canceled=" + canceled + ", blockingSend=" + blockingSend + ", blockingTimeout=" + blockingTimeout + ", resendRetryCount=" + resendRetryCount + '}';
    }
}
