/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.db.USBconfiguration;
import fi.wsnusbcollect.nodes.NodeHandler;
import fi.wsnusbcollect.nodes.NodePlatformFactory;
import fi.wsnusbcollect.nodes.NodePlatformWSN430;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author ph4r05
 */
public class USBarbitratorSenslab extends USBarbitratorImpl{

    public static final String NODE_RESET_COMMAND = "/opt/senslab/bin/senslab-cli";
    
    private NodeSearchMap moteListPrivate;
    
    public USBarbitratorSenslab() {
    }

    @Override
    public USBconfiguration getCurrentConfiguration() {
        USBconfiguration usbConf = new USBconfiguration();
        
        return usbConf;
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
        log.info("Detecting connected nodes");
        //super.detectConnectedNodes();
    }

    /**
     * There is probably no way to obtain list of connected nodes
     * @return 
     */
    @Override
    public Map<String, NodeConfigRecord> getConnectedNodes() {
        //return super.getConnectedNodes();
        return this.moteListPrivate;
        //return new HashMap<String, NodeConfigRecord>();
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

    @Override
    public boolean isAbleNodeReset() {
        // can really reset node? -> reset command need to exist
        if (NODE_RESET_COMMAND==null){
            return false;
        }
        
        // check reset command existence
        File tmpFile = new File(NODE_RESET_COMMAND);
        if (tmpFile.exists()==false){
            return false;
        } else {
            return true;
        }
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
        
        return this.resetNode(NODE_RESET_COMMAND + " reset " + nh.getNodeId());
    }

    /**
     * Has no possibility to check configuration if nodes are not connected physically
     */
    @Override
    public void checkActiveConfiguration() {
        //super.checkActiveConfiguration();
    }

    /**
     * We dont want to check config nodes to connected
     * @return 
     */
    @Override
    public boolean isCheckConfigNodesToConnected() {
        return false;
    }

    /**
     * Call parent method and add all new node records to motelist
     * @param includeString
     * @param excludeString
     * @return 
     */
    @Override
    public List<NodeConfigRecord> getNodes2connect(String includeString, String excludeString) {
        List<NodeConfigRecord> nodes2connect = super.getNodes2connect(includeString, excludeString);
        
        // add all nodes to motelist
        this.moteListPrivate = new NodeSearchMap();
        this.moteList = new NodeSearchMap();
        for(NodeConfigRecord ncr : nodes2connect){
            this.moteListPrivate.put(ncr);
            this.moteList.put(ncr);
        }
        
        return nodes2connect;
    }

    
    
    /**
     * Create by default new
     * @param id
     * @return 
     */
    @Override
    public NodeConfigRecord getNodeById(Integer id) {
        if (this.moteList!=null && this.moteList.containsKeyNodeId(id)){
            return this.moteList.getByNodeId(id);
        }
        
        // platform is strictly defined = WSN
        NodePlatformWSN430 platform = new NodePlatformWSN430();
        
        NodeConfigRecord ncr = new NodeConfigRecord();
        ncr.setNodeId(id);
        ncr.setPhysicallyConnected(false);
        ncr.setPlatformId(platform.getPlatformId());
        ncr.setDescription(platform.getPlatform());
        ncr.setSerial(id.toString());
//        ncr.setConnectionString(platform.getConnectionString("experiment:" + (30000 + id), NodePlatformFactory.CONNECTION_SF));
                ncr.setConnectionString(platform.getConnectionString("centaur.fi.muni.cz:" + (30000), NodePlatformFactory.CONNECTION_SF));
log.info("Created new node record: " + ncr.toString());                
        return ncr;
    }

    /**
     * No directly connected node
     * @param path
     * @return 
     */
    @Override
    public NodeConfigRecord getNodeByPath(String path) {
        return null;
    }

    /**
     * Cannot have serial
     * 
     * @param serial
     * @return 
     */
    @Override
    public NodeConfigRecord getNodeBySerial(String serial) {
        return this.getNodeById(Integer.parseInt(serial));
    }
    
    
    
    
}
