/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.nodes.NodeHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ph4r05
 */
@Repository
@Transactional
public class USBarbitratorSenslab extends USBarbitratorImpl{

    public USBarbitratorSenslab() {
    }

    @Override
    public boolean checkNodesConnection(Map<String, NodeConfigRecord> localmotelist, boolean output) {
        //return super.checkNodesConnection(localmotelist, output);
        return true;
    }

    @Override
    public boolean checkNodesConnection(Map<String, NodeConfigRecord> localmotelist) {
        //return super.checkNodesConnection(localmotelist);
        return true;
    }

    @Override
    public void checkNodesConnection() {
        //super.checkNodesConnection();
    }

    /**
     * We are unable to detect connected nodes here, nodes are connected 
     * via SerialForwarder
     */
    @Override
    public void detectConnectedNodes() {
        //super.detectConnectedNodes();
    }

    @Override
    public Map<String, NodeConfigRecord> getConnectedNodes() {
        //return super.getConnectedNodes();
        return new HashMap<String, NodeConfigRecord>();
    }

    /**
     * Cannot reprogram nodes via serial forwarder
     * 
     * @param nodes2connect
     * @param makefileDir 
     */
    @Override
    public void reprogramNodes(List<NodeConfigRecord> nodes2connect, String makefileDir) {
        //super.reprogramNodes(nodes2connect, makefileDir);
    }

    /**
     * Cannot reset connection to node physically here
     * @param resetCommand
     * @return 
     */
    @Override
    public boolean resetNode(String resetCommand) {
        return false;
    }

    /**
     * In senslab we can reset node by nodeid calling python interface
     * @TODO: finish this according to senslab manual
     * 
     * @param nh
     * @param prop
     * @return 
     */
    @Override
    public boolean resetNode(NodeHandler nh, Properties prop) {
        return false;
        //return super.resetNode(nh, prop);
    }

    /**
     * Has no possibility to check configuration if nodes are not connected physically
     */
    @Override
    public void checkActiveConfiguration() {
        //super.checkActiveConfiguration();
    }
    
    
    
     
    
    
    
    
    
    
    
    
}
