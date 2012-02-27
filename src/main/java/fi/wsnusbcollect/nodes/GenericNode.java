/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

import fi.wsnusbcollect.nodeManager.CoordinateRecord;
import fi.wsnusbcollect.nodes.NodePlatform;
import java.io.Serializable;

/**
 *
 * @author ph4r05
 */
public interface GenericNode extends Serializable, Cloneable{
    public int getNodeId();

    public CoordinateRecord getPosition();
    public void setPosition(CoordinateRecord position);

    public boolean isAnchor();
    public void setAnchor(boolean anchor);

    public long getFirstSeen();
    public void setFirstSeen(long firstSeen);

    public long getLastSeen();
    public void setLastSeen(long lastSeen);

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
     * Return normalized rssi value for system
     */
    public double getNormalizedRssi(double rssi, int txlevel);
    
    // serialization
    public void sleep();
    public void wakeup();
}
