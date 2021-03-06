/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

import java.util.Properties;

/**
 * Node platform interface
 * @author ph4r05
 */
public interface NodePlatform {
    public int[] getTxLevels();
    public double[] getTxOutputPower();
    
    public int getPlatformId();
    public String getPlatform();
    
    /**
     * Returns string required to specify platform to makefile
     * @return 
     */
    public String getPlatformReflashId();
    
    /**
     * Returns connection string signature for particular node
     * @return 
     */
    public String getConnectionStringSignature();
    
    /**
     * Returns connection string that can be used with Listener to connect directly 
     * to specified device. Connection string is platform dependent.
     * 
     * @param device
     * @return 
     */
    public String getConnectionString(String device);
    
    /**
     * Returns connection string that can be used with Listener to connect directly 
     * to specified device. Connection string is platform dependent. Depends on
     * type of connection for node
     * 
     * @param device
     * @param connection    type of connection of node (serial, sf, network)
     * @return 
     */
    public String getConnectionString(String device, int connection);    
    
    /**
     * Returns whether node description returned via USB corresponds to this platform
     * @param desc
     * @return 
     */
    public boolean isPlatformFromNodeDescription(String desc);
    
    /**
     * Returns whether it is possible reset this node by HW -> need to be connected
     * @return 
     */
    public boolean canHwReset();
    
    /**
     * Returns command for HWreset
     * @return 
     */
    public String hwResetCommand(String device, Properties prop);
}
