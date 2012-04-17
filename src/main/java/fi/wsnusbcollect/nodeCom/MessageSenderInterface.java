/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import fi.wsnusbcollect.nodes.ConnectedNode;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import net.tinyos.message.Message;

/**
 *
 * @author ph4r05
 */
public interface MessageSenderInterface {

    /**
     * Adds message to send to send queue
     * @param target
     * @param msg
     * @param text
     */
    void add(int target, Message msg, String text);

    /**
     * Adds message to send to send queue, with wanted notification after sent
     * @param target
     * @param msg
     * @param text
     */
    void add(int target, Message msg, String text, MessageSentListener listener, String listenerKey);

    /**
     * Adds more messages at time. Can be blocking...
     * All messages are added to sending queue + if there are blocking messages
     * method waits for finishing it.
     *
     * @param msgs
     * @return  list of failed blocking messages
     * @throws TimeoutException
     */
    Collection<MessageToSend> add(Collection<MessageToSend> msgs) throws TimeoutException;

    /**
     * Adds initialized message to send to send queue.
     * @param msg
     */
    void add(MessageToSend msg) throws TimeoutException;

    /**
     * Return TRUE if is possible to add new message to send, FALSE otherwise
     * (moteInterface may be NULL => cannot add message to send)
     *
     * @return booleans
     */
    boolean canAdd();

    int getSentSleepTime();

    long getTimeLastMessageSent();

    boolean isShutdown();

    /**
     * Watch message sent event for blocking message sending
     * @param listenerKey
     * @param msg
     * @param destination
     */
    void messageSent(String listenerKey, Message msg, int destination);

    /**
     * perform hard reset to this object = clears entire memory
     */
    void reset();

    /**
     * The thread either executes tasksNotifiers or sleep.
     */
    void run();

    void setShutdown(boolean shutdown);

    /**
     * Performs shutdown
     */
    void shutdown();
    
    void start();
    
    boolean isAlive();
    
    /**
     * Disconnects connected node to this component
     */
    void disconnectNode(ConnectedNode nh, boolean resetQueues);
    void disconnectNode(ConnectedNode nh, Properties props);
    
    /**
     * Connect given node to this sender, if already in, nothing is done if
     * MoteIF equals already set one
     * 
     * @param nh
     * @param props 
     */
    void connectNode(ConnectedNode nh, Properties props);
    
}
