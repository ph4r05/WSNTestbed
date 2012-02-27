/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

/**
 * Holds information about specific node from database.
 * Such node can be directly connected or not.
 *
 * Not directly connected devices has almost 
 * all fields empty, but strategically important are devicePath, deviceAlias. 
 * If deviceAlias is empty, devicePath is used. If devicePath is empty, node is
 * considered as not directly connected.
 * 
 * @author ph4r05
 */
public class NodeConfigRecord {
    private String bus;
    private String dev;
    private String usbPath;
    private String serial;
    private String devicePath;
    private String deviceAlias;
    private String description;
    private String nodeId;
    private String connectionString;
    private int platformId;

    public String getBus() {
        return bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getUsbPath() {
        return usbPath;
    }

    public void setUsbPath(String usbPath) {
        this.usbPath = usbPath;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeConfigRecord other = (NodeConfigRecord) obj;
        if ((this.serial == null) ? (other.serial != null) : !this.serial.equals(other.serial)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.serial != null ? this.serial.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "NodeConfigRecord{" + "bus=" + bus + ", dev=" + dev + ", usbPath=" + usbPath + ", serial=" + serial + ", devicePath=" + devicePath + ", deviceAlias=" + deviceAlias + ", description=" + description + ", nodeId=" + nodeId + '}';
    }
}
