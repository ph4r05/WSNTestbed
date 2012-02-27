package fi.wsnusbcollect.nodes;
import fi.wsnusbcollect.nodeManager.CoordinateRecord;
import java.io.Serializable;

/**
 * Generic node wrapper 
 * @author ph4r05
 */
public interface GenericNode extends Serializable, Cloneable{    
    public int getNodeId();

    /**
     * Returns physical position of node in Euclidean space
     * @return 
     */
    public CoordinateRecord getPosition();
    
    /**
     * Set physical position of node
     * @param position 
     */
    public void setPosition(CoordinateRecord position);

    /**
     * Returns whether node is static in space. 
     * Used for collecting data about mobile nodes and environment.
     * @return 
     */
    public boolean isAnchor();
    
    /**
     * Sets whether node is static in space.
     * Used for collecting data about mobile nodes and environment.
     * @param anchor 
     */
    public void setAnchor(boolean anchor);

    /**
     * Returns date of node registration (first packet arrived) in miliseconds
     * @return 
     */
    public long getFirstSeen();
    
    /**
     * Sets date of node registration (first packet arrived) in miliseconds
     * @param firstSeen 
     */
    public void setFirstSeen(long firstSeen);

    /**
     * Returns date of node's last packet arrived in miliseconds
     * @return 
     */
    public long getLastSeen();
    
    /**
     * Sets date of node's last packet arrived in miliseconds
     * @param lastSeen 
     */
    public void setLastSeen(long lastSeen);

    /**
     * If is mobile node, return mobile extension - object specific for mobile nodes
     * @return 
     */
    public MobileNode getMobileExtension();
    public void setMobileExtension(MobileNode mobileExtension);

    public double getTemperature();
    public void setTemperature(double  temperature);
    
    public double getHumidity();
    public void setHumidity(double humidity);

    public double getLightIntensity();
    public void setLightIntensity(double lightIntensity);
    
    public NodePlatform getPlatform();
    public void setPlatform(NodePlatform platform);
    
    /**
     * Return normalized rssi value for system, different platforms can return 
     * transformed/biased RSSI values.
     * 
     * @extension: define&register rssi normalizator for system and per platform
     * to be abstract clear, now KISS principle
     */
    public double getNormalizedRssi(double rssi, int txlevel);
    
    /**
     * Prepares object for serialization
     */
    public void sleep();
    
    /**
     * Prepares object after serialization to be used (refresh resources)
     */
    public void wakeup();
}
