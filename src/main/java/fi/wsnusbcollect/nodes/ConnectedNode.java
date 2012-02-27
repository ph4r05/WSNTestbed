/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodes;

import fi.wsnusbcollect.nodeCom.MessageSender;
import fi.wsnusbcollect.nodeCom.MyMessageListener;
import fi.wsnusbcollect.usb.NodeConfigRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents directly connected node - can be listened on local wired interface
 * directly. Base station is example of connected node.
 * In testbed with more nodes connected, each this object will represent each directly
 * connected node.
 * 
 * 
 * @author ph4r05
 */
public class ConnectedNode {
    private static final Logger log = LoggerFactory.getLogger(ConnectedNode.class);
    
    // node object (contains node id, platform, position, readings, timers)
    private GenericNode nodeObj;
    
    // hold information about connected node - data from database/auto detection
    // if node is not connected, this object has no special meaning (holds only
    // node id, product and description - can be determined from nodeObj. Only usefull
    // field can be serial)
    private NodeConfigRecord nodeConfig;
    
    // message sender bound to specified node
    private MessageSender msgSender;
    
    // message listener bound to specified node
    private MyMessageListener msgListener;

    /**
     * Proxy method to nodeObj. Throws nullpointer exception if nodeObj is null
     * @return 
     */
    public int getNodeId(){
        if (this.nodeObj==null){
            throw new NullPointerException("Cannot determine node ID, nodeObj is null");
        }
        
        return this.nodeObj.getNodeId();
    }
    
    public MyMessageListener getMsgListener() {
        return msgListener;
    }

    public void setMsgListener(MyMessageListener msgListener) {
        this.msgListener = msgListener;
    }

    public MessageSender getMsgSender() {
        return msgSender;
    }

    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }

    public NodeConfigRecord getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(NodeConfigRecord nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public GenericNode getNodeObj() {
        return nodeObj;
    }

    public void setNodeObj(GenericNode nodeObj) {
        this.nodeObj = nodeObj;
    }    
}
