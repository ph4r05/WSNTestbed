/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeManager;

import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.nodes.AbstractNodeHandler;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.NodeHandler;
import fi.wsnusbcollect.notify.EventMailNotifierIntf;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node handler register implements map NodeID -> Node Handler
 * Main structure that 
 * collects all nodes in one and provides some basic functionality.
 * @author ph4r05
 */
public class NodeHandlerRegisterImpl implements NodeHandlerRegister {
    private static final Logger log = LoggerFactory.getLogger(NodeHandlerRegisterImpl.class);
    
    // primary map for
    private ConcurrentMap<Integer, NodeHandler> primaryMap;
    
    // set of connected nodes - base stations
    private Set<Integer> connectedNodes;
    
    // problem notifier
    @Resource(name="mailNotifier")
    protected EventMailNotifierIntf notifier;

    public NodeHandlerRegisterImpl() {
        this.primaryMap = new ConcurrentHashMap<Integer, NodeHandler>();
        this.connectedNodes = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>());
    }
    
    /**
     * Shutdowns all nodes registered
     */
    @Override
    public void shutdownAll(){
        Iterator<Integer> iterator = this.primaryMap.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeid = iterator.next();
            NodeHandler nh = this.primaryMap.get(nodeid);
            
            nh.shutdown();
        }
    }
    
    /**
     * Starts all nodes required
     */
    @Override
    public void startAll(){
        Iterator<Integer> iterator = this.primaryMap.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeid = iterator.next();
            NodeHandler nh = this.primaryMap.get(nodeid);
            
            // connected and correct
            if (this.isConnectedNode(nh)==false || nh.isCorrect()==false) {
                continue;
            }
            
            final ConnectedNode cn = (ConnectedNode) nh;
            cn.start();
        }
    }
    
    /**
     * Restarts all nodes HW
     */
    @Override
    public void hwresetAll(){
        Iterator<Integer> iterator = this.primaryMap.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeid = iterator.next();
            NodeHandler nh = this.primaryMap.get(nodeid);
            
            // connected and correct
            if (this.isConnectedNode(nh)==false || nh.isCorrect()==false) {
                continue;
            }
            
            final ConnectedNode cn = (ConnectedNode) nh;
            if (cn.hwresetPossible()){
                cn.hwreset();
            }
        }
    }
    
    /**
     * Returns whether given node id is connected node
     * @param nodeid
     * @return 
     */
    @Override
    public boolean isConnectedNode(int nodeid){
        return this.connectedNodes.contains(nodeid);
    }
    
    /**
     * Returns whether node id is connected node
     * @param nh
     * @return 
     */
    @Override
    public boolean isConnectedNode(NodeHandler nh){
        return nh!=null && nh.getType() == AbstractNodeHandler.NODE_HANDLER_CONNECTED && NodeHandler.class.isInstance(nh);
    }
    
    /**
     * Registers message listener for all connected nodes
     * @return 
     */
    @Override
    public boolean registerMessageListener(net.tinyos.message.Message msg, MessageListener listener){
        Iterator<Integer> iterator = this.connectedNodes.iterator();
        while(iterator.hasNext()){
            Integer nodeid = iterator.next();
            
            // consisntency check
            if (this.primaryMap.containsKey(nodeid)==false){
                log.error("Consistency check failed! NodeID: " + nodeid + " claimed"
                        + " to be connected, but no such node found in primary map");
                throw new IllegalStateException("Consistency check failed! NodeID: " + nodeid + " claimed"
                        + " to be connected, but no such node found in primary map");
            }
            
            NodeHandler nh = this.primaryMap.get(nodeid);
            if (this.isConnectedNode(nh)==false){
                log.error("Node is not connected, cannot register. Consistency violation!"
                        + " NodeID: " + nh.getNodeId() + " should be connected");
                throw new IllegalStateException("Node is not connected, cannot register. Consistency violation!"
                        + " NodeID: " + nh.getNodeId() + " should be connected");
            }
            
            final ConnectedNode cn = (ConnectedNode) nh;
            try{
                cn.registerMessageListener(msg, listener);
            } catch(Exception e){
                log.error("Exception thrown when registering msglistener", e);
            }
        }
        return true;
    }
    
    /**
     * On all registered nodes will cause receiving/ignoring received packets to 
     * application.
     * 
     * @param ignore
     * @return 
     */
    @Override
    public void setDropingReceivedPackets(boolean ignore){
        Iterator<Integer> iterator = this.primaryMap.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeid = iterator.next();
            NodeHandler nh = this.primaryMap.get(nodeid);
            
            // connected and correct
            if (this.isConnectedNode(nh)==false || nh.isCorrect()==false) {
                continue;
            }
            
            final ConnectedNode cn = (ConnectedNode) nh;
            cn.setDropingReceivedPackets(ignore);
        }
    }
    
    /**
     * Inserts node handler to map based on NodeID
     * @param value
     * @return 
     */
    @Override
    public NodeHandler put(NodeHandler value) {
        if (value==null){
            throw new NullPointerException("Cannot insert empty node handler");
        }
        
        if (value.isCorrect()==false){
            log.warn("Something wrong about node handler - correct returns fail. "
                    + "Node is probably not correctly initialized. ");
            throw new IllegalArgumentException("Node handler is not correct");
        }
        
        return this.put(value.getNodeId(), value);
    }
    
    @Override
    public Collection<NodeHandler> values() {
        return primaryMap.values();
    }

    @Override
    public int size() {
        return primaryMap.size();
    }

    @Override
    public NodeHandler remove(Object key) {
        // if exists remove from set as well
        if (key==null || !(key instanceof Integer)){
            throw new IllegalArgumentException("Argument key is invalid. Is empty or not instance of Integer");
        }
        
        final Integer key2 = (Integer) key;
        if (this.containsKey(key2)==false){
            return null;
        }
        
        NodeHandler nh = this.get(key2);
        this.connectedNodes.remove(nh.getNodeId());
        
        return primaryMap.remove(key2);
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends NodeHandler> m) {
        // just iterate over given map
        if (m==null){
            throw new IllegalArgumentException("Source map is null");
        }
        
        Iterator<Integer> iterator = (Iterator<Integer>) m.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeId = iterator.next();
            this.put(m.get(nodeId));
        }
    }

    /**
     * Basic method for inserting node handlers
     * Keeps associations and meta information for better cooperation
     * @param key
     * @param value
     * @return 
     */
    @Override
    public NodeHandler put(Integer key, NodeHandler value) {
        if (value==null){
            throw new NullPointerException("Cannot insert empty node handler");
        }
        
        if (value.isCorrect()==false){
            log.warn("Something wrong about node handler - correct returns fail. "
                    + "Node is probably not correctly initialized. ");
            throw new IllegalArgumentException("Node handler is not correct");
        }
        
        // association for types
        if (this.isConnectedNode(value)){
            final ConnectedNode cn = (ConnectedNode) value;
            this.connectedNodes.add(cn.getNodeId());
        }
        
        return primaryMap.put(key, value);
    }

    @Override
    public Set<Integer> keySet() {
        return primaryMap.keySet();
    }

    @Override
    public boolean isEmpty() {
        return primaryMap.isEmpty();
    }

    @Override
    public NodeHandler get(Object key) {
        return primaryMap.get(key);
    }

    @Override
    public Set<Entry<Integer, NodeHandler>> entrySet() {
        return primaryMap.entrySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return primaryMap.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return primaryMap.containsKey(key);
    }

    @Override
    public void clear() {
        primaryMap.clear();
        connectedNodes.clear();
    }
    
    /**
     * Updates lastseen indicator for given node
     * @param nodeId
     * @param mili 
     */
    @Override
    public synchronized void updateLastSeen(int nodeId, long mili) {
        if (this.containsKey(nodeId) == false) {
            log.warn("Cannot update last seen counter, node not registered: " + nodeId);
            return;
        }

        this.get(nodeId).updateLastSeen(mili);
    }
    
    /**
     * Returns nodes as list where last seen indicator is less than given boudnary
     * @param mili
     * @return 
     */
    @Override
    public List<Integer> getNodesLastSeenLessThan(long boudnary){
        LinkedList<Integer> list = new LinkedList<Integer>();
        Iterator<Integer> iterator = this.primaryMap.keySet().iterator();
        while(iterator.hasNext()){
            Integer nodeId = iterator.next();
            NodeHandler nh = this.primaryMap.get(nodeId);
            
            if (nh.getNodeObj().getLastSeen() < boudnary){
                list.add(nodeId);
            }
            
        }
        
        return list;
    }

    /**
     * Take care about problems reported by TinyOS library
     * @param nodeid
     * @param msg 
     */
    @Override
    public void tosMsg(Integer nodeid, String msg) {
        log.warn("Message from tinyOS messenger ["+nodeid+"]: " + msg);
        
        // report this problem with notifier as well
        if (this.notifier!=null){
            this.notifier.notifyEvent(120, "ID: " + nodeid, "Message from tinyOS messenger ["+nodeid+"]: " + msg, null);
        }
    }
}
