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
     * @TODO: use /opt/senslab/bin/senslab-cli update 
     *      Usage: update [options] nodes
            Update some nodes' firmware

            Options:
              -h, --help            show this help message and exit
              -f UPDATEFILE, --file=UPDATEFILE
                                    update the selected nodes with this firmware.
              -u UPLOADFILE, --upload=UPLOADFILE
                                    upload a firmware file to the server
              -x XFILE, --upload-update=XFILE
                                    upload a firmware file to the server AND flash the
                                    selected nodes with it
              -l, --list            list the available uploaded firmwares.

     * 
     * @param nodes2connect
     * @param makefileDir 
     */
    @Override
    public void reprogramNodes(List<NodeConfigRecord> nodes2connect, String makefileDir) {
        //super.reprogramNodes(nodes2connect, makefileDir);
    }

    /**
     * In senslab we can reset node by nodeid calling python interface
     * @TODO: check this in real senslab
     * 
     * @param nh
     * @param prop
     * @return 
     */
    @Override
    public boolean resetNode(NodeHandler nh, Properties prop) {
        if (nh==null || nh.getNodeObj()==null){
            return false;
        }
        
        return this.resetNode("/opt/senslab/bin/senslab-cli reset " + nh.getNodeId());
    }

    /**
     * Has no possibility to check configuration if nodes are not connected physically
     */
    @Override
    public void checkActiveConfiguration() {
        //super.checkActiveConfiguration();
    }
    
    
    
     
    
    
    
    
    
    
    
    
}
