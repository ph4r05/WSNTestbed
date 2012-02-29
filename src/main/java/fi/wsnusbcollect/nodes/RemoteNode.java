/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodes;

import fi.wsnusbcollect.nodeCom.MessageSentListener;
import fi.wsnusbcollect.nodeCom.MessageToSend;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Counterpart for connected node
 * @author ph4r05
 */
public class RemoteNode extends AbstractNodeHandler implements NodeHandler{
    private static final Logger log = LoggerFactory.getLogger(RemoteNode.class);
    
    // node object (contains node id, platform, position, readings, timers)
    private GenericNode nodeObj;
    
    // hold information about connected node - data from database/auto detection
    // if node is not connected, this object has no special meaning (holds only
    // node id, product and description - can be determined from nodeObj. Only usefull
    // field can be serial)
    private NodeConfigRecord nodeConfig;
    
    // base station for this remote node
    // acts as forwaring router for packets
    private ConnectedNode baseStation;
    
    /**
     * Proxy method to nodeObj. Throws nullpointer exception if nodeObj is null
     * @return 
     */
    @Override
    public int getNodeId(){
        if (this.nodeObj==null){
            throw new NullPointerException("Cannot determine node ID, nodeObj is null");
        }
        
        return this.nodeObj.getNodeId();
    }
    
    /**
     * Checks whether object contains non null required fields
     * @return 
     */
    @Override
    public boolean isCorrect() {
        return this.nodeObj!=null;
    }    
    
    /**
     * Returns type of node handler - connected node
     * @return 
     */
    @Override
    public int getType() {
        return AbstractNodeHandler.NODE_HANDLER_REMOTE;
    }

    public ConnectedNode getBaseStation() {
        return baseStation;
    }

    public void setBaseStation(ConnectedNode baseStation) {
        this.baseStation = baseStation;
    }

    public NodeConfigRecord getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(NodeConfigRecord nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    @Override
    public GenericNode getNodeObj() {
        return nodeObj;
    }

    public void setNodeObj(GenericNode nodeObj) {
        this.nodeObj = nodeObj;
    }

    @Override
    public boolean canListen() {
        return this.baseStation!=null && this.baseStation.canListen();
    }

    @Override
    public boolean canSend() {
        return this.baseStation!=null && this.baseStation.canSend();
    }

    @Override
    public boolean canAddMessage2Send() {
        return this.canSend() && this.baseStation.canAddMessage2Send();
    }

    @Override
    public void addMessage2Send(MessageToSend msg) {
        if (this.canSend()==false){
            log.warn("Cannot add message 2 send");
            throw new IllegalStateException("Cannot add message 2 send");
        }
        
        // target should be this node id - remote node supports sending only 
        // to itself via base station
        if (msg.getDestination() != this.getNodeId()){
            log.warn("Fail condition - wanted to send message directly to "
                    + "different node. My NodeID: " + this.getNodeId() + "; "
                    + "destination NodeID: " + msg.getDestination());
            throw new IllegalArgumentException("Cannot send message to different node");
        }
        
        this.baseStation.addMessage2Send(msg);
    }

    @Override
    public void addMessage2Send(Message msg, String text, MessageSentListener listener, String listenerKey) {
        if (this.canSend()==false){
            log.warn("Cannot add message 2 send");
            throw new IllegalStateException("Cannot add message 2 send");
        }
        
        this.baseStation.addMessage2Send(this.getNodeId(), msg, text, listener, listenerKey);
    }

    @Override
    public void addMessage2Send(Message msg, String text) {
        if (this.canSend()==false){
            log.warn("Cannot add message 2 send");
            throw new IllegalStateException("Cannot add message 2 send");
        }
        
        this.baseStation.addMessage2Send(this.getNodeId(), msg, text);
    }
}
