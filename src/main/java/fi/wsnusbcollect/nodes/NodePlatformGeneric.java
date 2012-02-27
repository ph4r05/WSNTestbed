/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class NodePlatformGeneric implements NodePlatform {
    private static final Logger log = LoggerFactory.getLogger(NodePlatformGeneric.class);
    public static final int platformId = NodePlatformFactory.NODE_PLATFORM_GENERIC;
    
    @Override
    public String getPlatform() {
        return "Unknown";
    }

    @Override
    public int getPlatformId() {
        return NodePlatformGeneric.platformId;
    }

    @Override
    public int[] getTxLevels() {
        return new int[0];
    }

    @Override
    public double[] getTxOutputPower() {
        return new double[0];
    }

    public NodePlatformGeneric() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if (!(obj instanceof NodePlatform)) return false;
        
        final NodePlatform platform = (NodePlatform) obj;
        return this.getPlatformId() == platform.getPlatformId();
    }

    @Override
    public int hashCode() {
        return this.getPlatformId();
    }

    @Override
    public String toString() {
        return "NodePlatformGeneric{" + "PlatformId=" + this.getPlatformId() 
                + "; Platform=" + this.getPlatform() + "}";
    }

    /**
     * Try connect by generic way
     * @param device
     * @return 
     */
    @Override
    public String getConnectionString(String device) {
        log.warn("Determining connection string for generic node - please inspect, device: " + device);
        return "serial@" + device;
    }

    /**
     * Always return false - when determining node platform there can be more 
     * specific platforms that matches. This should be returned manually on no match,
     * thus cannot return always true. By default we should return false since
     * this represents something generic.
     * 
     * @param desc
     * @return 
     */
    @Override
    public boolean isPlatformFromNodeDescription(String desc) {
        return false;
    }
}
