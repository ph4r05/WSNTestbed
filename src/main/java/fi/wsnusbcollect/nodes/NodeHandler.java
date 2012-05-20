/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodes;

import fi.wsnusbcollect.nodeCom.MessageSentListener;
import fi.wsnusbcollect.nodeCom.MessageToSend;
import java.util.concurrent.TimeoutException;
import net.tinyos.message.Message;

/**
 *
 * @author ph4r05
 */
public interface NodeHandler {
    public void shutdown();
    public void close();
    
    /**
     * Returns type of node handler implementing this interface (connected vs. remote)
     * @return 
     */
    public int getType();
    
    /**
     * returns whether is object correctly initialized (for ex. contains non null node object...)
     * @return 
     */
    public boolean isCorrect();
    
    /**
     * Proxy method to nodeObj. Throws nullpointer exception if nodeObj is null
     * @return 
     */
    public int getNodeId();
    
    /**
     * Returns node object inside
     * @return 
     */
    public GenericNode getNodeObj();
    
    /**
     * Determining whether it is possible to register listeners
     * @return 
     */
    public boolean canListen();
    
    /**
     * Determining whether it is possible to send message to this node
     * @return 
     */
    public boolean canSend();
    
      /**
     * Method delegated to message sender if exists
     * @return 
     */
    public boolean canAddMessage2Send();

    public void addMessage2Send(MessageToSend msg) throws TimeoutException;

    /**
     * Generic send message method - send message to this node
     * @param msg
     * @param text
     * @param listener
     * @param listenerKey 
     */
    public void addMessage2Send(Message msg, String text, MessageSentListener listener, String listenerKey);
    
    /**
     * Generic send message method - send message to this node
     * 
     * @param msg
     * @param text 
     */
    public void addMessage2Send(Message msg, String text);
    
    /**
     * Update last seen indicator
     * @param mili 
     */
    public void updateLastSeen(long mili);
}
