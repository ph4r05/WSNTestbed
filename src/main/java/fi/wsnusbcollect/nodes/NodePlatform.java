/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

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
     * Returns connection string that can be used with Listener to connect directly 
     * to specified device. Connection string is platform dependent.
     * 
     * @param device
     * @return 
     */
    public String getConnectionString(String device);
    
    /**
     * Returns whether node description returned via USB corresponds to this platform
     * @param desc
     * @return 
     */
    public boolean isPlatformFromNodeDescription(String desc);
}
