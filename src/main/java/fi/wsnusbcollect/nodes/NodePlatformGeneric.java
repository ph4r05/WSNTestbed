/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

/**
 *
 * @author ph4r05
 */
public class NodePlatformGeneric implements NodePlatform {
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
}
