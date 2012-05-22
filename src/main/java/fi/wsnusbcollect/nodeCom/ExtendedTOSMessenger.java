/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended messenger for TinyOS MoteIF object. This messenger contains information
 * about nodeId and parent to which are messages redirected. 
 * 
 * @author ph4r05
 */
public class ExtendedTOSMessenger implements net.tinyos.util.Messenger {
    private static final Logger log = LoggerFactory.getLogger(ExtendedTOSMessenger.class);
    
    /**
     * Gateway to which is this messenger connected
     */
    private int nodeId;
    
    /**
     * Where to forward received messages. If null then to log
     */
    private TOSMessengerListener parent;

    public ExtendedTOSMessenger(int nodeId, TOSMessengerListener parent) {
        this.nodeId = nodeId;
        this.parent = parent;
    }
    
    @Override
    public void message(String string) {
        if (this.parent==null){
            log.warn("Message from tinyOS messenger ["+this.nodeId+"]: " + string);
        } else {
            this.parent.tosMsg(nodeId, string);
        }
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public TOSMessengerListener getParent() {
        return parent;
    }

    public void setParent(TOSMessengerListener parent) {
        this.parent = parent;
    }
}