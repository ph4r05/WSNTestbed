/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import net.tinyos.message.Message;

/**
 * Message received record for MyMessageListener. 
 * Is queue element for each received message. Stores message itself, 
 * destination node, timestamp of arrival to application. 
 * 
 * @author ph4r05
 */
public class MessageReceived {
    private int gateway=0;
    private int i;
    private long timeReceivedMili;
    private net.tinyos.message.Message msg;

    public MessageReceived(int i, Message msg) {
        this.i = i;
        this.msg = msg;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageReceived other = (MessageReceived) obj;
        if (this.i != other.i) {
            return false;
        }
        if (this.msg != other.msg && (this.msg == null || !this.msg.equals(other.msg))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.i;
        hash = 67 * hash + (this.msg != null ? this.msg.hashCode() : 0);
        return hash;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    /**
     * Returns exact time in miliseconds when message arrived to listener queue
     * @return 
     */
    public long getTimeReceivedMili() {
        return timeReceivedMili;
    }

    public void setTimeReceivedMili(long timeReceivedMili) {
        this.timeReceivedMili = timeReceivedMili;
    }

    public int getGateway() {
        return gateway;
    }

    public void setGateway(int gateway) {
        this.gateway = gateway;
    }
}
