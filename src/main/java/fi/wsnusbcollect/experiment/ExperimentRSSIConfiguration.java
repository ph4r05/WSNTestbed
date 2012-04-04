/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

/**
 * Configuration values for RSSI map experiment
 * @author ph4r05
 */
public class ExperimentRSSIConfiguration {
    // how often to receive noise floor reading?
    private int noiseFloorReadingTimeout=1000;
    // how many miliseconds can be node unreachable to be considered as unresponsive
    private long nodeAliveThreshold=4000;
    // packets requested
    private int packetsRequested = 100;
    // in miliseconds
    private int packetDelay = 100;

    public ExperimentRSSIConfiguration() {
    }

    public long getNodeAliveThreshold() {
        return nodeAliveThreshold;
    }

    public void setNodeAliveThreshold(long nodeAliveThreshold) {
        this.nodeAliveThreshold = nodeAliveThreshold;
    }

    public int getNoiseFloorReadingTimeout() {
        return noiseFloorReadingTimeout;
    }

    public void setNoiseFloorReadingTimeout(int noiseFloorReadingTimeout) {
        this.noiseFloorReadingTimeout = noiseFloorReadingTimeout;
    }

    public int getPacketDelay() {
        return packetDelay;
    }

    public void setPacketDelay(int packetDelay) {
        this.packetDelay = packetDelay;
    }

    public int getPacketsRequested() {
        return packetsRequested;
    }

    public void setPacketsRequested(int packetsRequested) {
        this.packetsRequested = packetsRequested;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentRSSIConfiguration other = (ExperimentRSSIConfiguration) obj;
        if (this.noiseFloorReadingTimeout != other.noiseFloorReadingTimeout) {
            return false;
        }
        if (this.nodeAliveThreshold != other.nodeAliveThreshold) {
            return false;
        }
        if (this.packetsRequested != other.packetsRequested) {
            return false;
        }
        if (this.packetDelay != other.packetDelay) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.noiseFloorReadingTimeout;
        hash = 19 * hash + (int) (this.nodeAliveThreshold ^ (this.nodeAliveThreshold >>> 32));
        hash = 19 * hash + this.packetsRequested;
        hash = 19 * hash + this.packetDelay;
        return hash;
    }

    @Override
    public String toString() {
        return "ExperimentRSSIConfiguration{" + "noiseFloorReadingTimeout=" + noiseFloorReadingTimeout + ", nodeAliveThreshold=" + nodeAliveThreshold + ", packetsRequested=" + packetsRequested + ", packetDelay=" + packetDelay + '}';
    }
}
