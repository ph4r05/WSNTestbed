/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special multi key map to search nodes from database/configuration by multiple keys
 *  - serial number
 *  - node id
 *  - device path
 * 
 * Not thread safe, need to synchronize correctly if needed...
 * @author ph4r05
 */
public class NodeSearchMap {
    private static final Logger log = LoggerFactory.getLogger(NodeSearchMap.class);
    
    // primary hash map Serial -> node config
    private HashMap<String, NodeConfigRecord> primaryMap;
    
    // node id -> serial mapping
    private HashMap<Integer, String> nodeId2serial;
    
    // node device path -> serial
    private HashMap<String, String> devicePath2serial;

    /**
     * Initialization creates maps instances
     */
    public NodeSearchMap() {
        this.primaryMap = new HashMap<String, NodeConfigRecord>();
        this.nodeId2serial = new HashMap<Integer, String>();
        this.devicePath2serial = new HashMap<String, String>();
    }
    
    /**
     * Inserts new node config record
     * @param ncr
     * @return 
     */
    public NodeConfigRecord put(NodeConfigRecord ncr){
        // ncr empty -> throw exception
        if (ncr==null){
            log.error("Tried to insert null node to map");
            throw new NullPointerException("Cannot insert null object");
        }
        
        NodeConfigRecord ncrReturn = null;
        
        if (this.primaryMap.containsKey(ncr.getSerial())){
            // such key already exists, needs to remove from other mappings
            ncrReturn = this.primaryMap.get(ncr.getSerial());
            this.removeBySerial(ncr.getSerial());
        }
        
        // insert now
        this.primaryMap.put(ncr.getSerial(), ncr);
        
        if (ncr.getNodeId() != null){
            this.nodeId2serial.put(ncr.getNodeId(), ncr.getSerial());
        }
        
        if (ncr.getDevicePath() != null && ncr.getDevicePath().isEmpty()==false){
            this.devicePath2serial.put(ncr.getDevicePath(), ncr.getSerial());
        }
        
        if (ncr.getDeviceAlias() != null && ncr.getDeviceAlias().isEmpty()==false){
            this.devicePath2serial.put(ncr.getDeviceAlias(), ncr.getDeviceAlias());
        }
        
        return ncrReturn;
    }
    
    /**
     * Removes all records about node with given serial
     * @param serial
     * @return 
     */
    public boolean removeBySerial(String serial){
        if (this.primaryMap.containsKey(serial)==false){
            // serial is removed already
            return true;
        }
        
        // get mapped value
        NodeConfigRecord ncr = this.primaryMap.get(serial);
        
        // remove from aliases
        this.nodeId2serial.remove(ncr.getNodeId());
        this.devicePath2serial.remove(ncr.getDevicePath());
        this.devicePath2serial.remove(ncr.getDeviceAlias());
        
        // remove primary
        this.primaryMap.remove(serial);
        return true;
    }
    
    /**
     * Serial
     * @param serial
     * @return 
     */
    public boolean containsKey(String serial){
        return this.primaryMap.containsKey(serial);
    }
    
    public boolean containsKeyNodeId(Integer nodeId){
        return this.nodeId2serial.containsKey(nodeId);
    }
    
    public boolean containsKeyDevPath(String devicePath){
        return this.devicePath2serial.containsKey(devicePath);
    }
    
    public NodeConfigRecord getBySerial(String serial){
        return this.primaryMap.get(serial);
    }
    
    /**
     * Returns by node id
     * @param nodeId
     * @return 
     */
    public NodeConfigRecord getByNodeId(Integer nodeId){
        // first need to get serial for node id
        if (this.nodeId2serial.containsKey(nodeId)==false){
            log.warn("NodeID: " + nodeId + " was not found in map" );
            return null;
        }
        
        String serial = this.nodeId2serial.get(nodeId);
        
        // is such mapping in primary map? THERE SHOULD BE, otherwise maps are inconsistent!!!
        if (this.primaryMap.containsKey(serial)==false){
            log.error("Primary map does not contain key: " + serial + "; Inconcistent state!");
            throw new NullPointerException("Cannot find value by primary key.");
        }
        
        return this.primaryMap.get(serial);
    }
    
    /**
     * Returns by device path
     * @param devPath
     * @return 
     */
    public NodeConfigRecord getByDevPath(String devPath){
        // first need to get serial for node id
        if (this.devicePath2serial.containsKey(devPath)==false){
            log.warn("DevPath: " + devPath + " was not found in map" );
            return null;
        }
        
        String serial = this.devicePath2serial.get(devPath);
        
        // is such mapping in primary map? THERE SHOULD BE, otherwise maps are inconsistent!!!
        if (this.primaryMap.containsKey(serial)==false){
            log.error("Primary map does not contain key: " + serial + "; Inconcistent state!");
            throw new NullPointerException("Cannot find value by primary key.");
        }
        
        return this.primaryMap.get(serial);
    }
}
