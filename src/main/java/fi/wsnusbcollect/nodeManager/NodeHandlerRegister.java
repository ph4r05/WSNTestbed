/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeManager;

import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.util.List;
import java.util.Map;
import net.tinyos.message.Message;

/**
 *
 * @author ph4r05
 */
public interface NodeHandlerRegister extends Map<Integer, NodeHandler> {

    /**
     * Returns nodes as list where last seen indicator is less than given boudnary
     * @param mili
     * @return
     */
    List<Integer> getNodesLastSeenLessThan(long boudnary);

    /**
     * Returns whether given node id is connected node
     * @param nodeid
     * @return
     */
    boolean isConnectedNode(int nodeid);

    /**
     * Returns whether node id is connected node
     * @param nh
     * @return
     */
    boolean isConnectedNode(NodeHandler nh);

    /**
     * Inserts node handler to map based on NodeID
     * @param value
     * @return
     */
    NodeHandler put(NodeHandler value);

    /**
     * Registers message listener for all connected nodes
     * @return
     */
    boolean registerMessageListener(Message msg, MessageListener listener);


    /**
     * On all registered nodes will cause receiving/ignoring received packets to
     * application.
     *
     * @param ignore
     * @return
     */
    void setDropingReceivedPackets(boolean ignore);

    /**
     * Shutdowns all nodes registered
     */
    void shutdownAll();

    /**
     * Starts all nodes required
     */
    void startAll();

    /**
     * Updates lastseen indicator for given node
     * @param nodeId
     * @param mili
     */
    void updateLastSeen(int nodeId, long mili);
    
    
    /**
     * Restarts all nodes HW
     */
    public void hwresetAll();
}
