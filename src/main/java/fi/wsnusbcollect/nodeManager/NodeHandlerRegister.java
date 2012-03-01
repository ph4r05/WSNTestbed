/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeManager;

import fi.wsnusbcollect.nodes.AbstractNodeHandler;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node handler register implements map
 * @author ph4r05
 */
public class NodeHandlerRegister implements Map<Integer, NodeHandler> {
    private static final Logger log = LoggerFactory.getLogger(NodeHandlerRegister.class);
    
    // primary map for
    private Map<Integer, NodeHandler> primaryMap;
    
    // set of connected nodes - base stations
    private Set<Integer> connectedNodes;

    public NodeHandlerRegister() {
        this.primaryMap = new HashMap<Integer, NodeHandler>();
        this.connectedNodes = new HashSet<Integer>();
    }
    
    /**
     * Shutdowns all nodes registered
     */
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
     * Returns whether given node id is connected node
     * @param nodeid
     * @return 
     */
    public boolean isConnectedNode(int nodeid){
        return this.connectedNodes.contains(nodeid);
    }
    
    /**
     * Returns whether node id is connected node
     * @param nh
     * @return 
     */
    public boolean isConnectedNode(NodeHandler nh){
        return nh!=null && nh.getType() == AbstractNodeHandler.NODE_HANDLER_CONNECTED && NodeHandler.class.isInstance(nh);
    }
    
    /**
     * Registers message listener for all connected nodes
     * @return 
     */
    public boolean registerMessageListener(net.tinyos.message.Message msg, net.tinyos.message.MessageListener listener){
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
     * Inserts node handler to map based on NodeID
     * @param value
     * @return 
     */
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
}
