/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * USB device entity keeps data about node connection
 * @author ph4r05
 */
@Entity
public class USBdevice implements Serializable, DataCSVWritable{
    private static final long serialVersionUID = 112312352;
    
    // node id record, this is record id, not node id!
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
    
    // configuration ID, 
    // TODO: link to USBconfiguration record, for now only integer
    @Id
    @ManyToOne
    private USBconfiguration usbconfig;
    
    // USB bus identification connected to
    private String bus;
    
    // path in USB tree from root to this device to identify where is node connected.
    // This information helps to detect code misplacement.
    private String usbPath;
    
    // USB serial number from node's EEPROM provided by FTDI (particular applicable for telosB nodes)
    // other types of nodes can provide usb serial as well
    @Id
    private String serial;
    
    // device path announced by "motelist" command, original interface
    private String devicePath;
    
    // public device alias created by udev rules by mapping from devicePath
    private String deviceAlias;
    
    // node id - should be fixed
    private Integer nodeId;
    
    // description of node - field returned by motelist command
    @Lob
    private String description;
    
    // should this note be checked?
    private boolean disabled=false;
    
    // @serial@{USB}:tmote
    private String connectionString;
    
    // id of platform
    private int platformId;
    
    // last modification of this record in database
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date lastModification;

    public USBdevice() {      
        
    }

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

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

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

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public USBconfiguration getUsbconfig() {
        return usbconfig;
    }

    public void setUsbconfig(USBconfiguration usbconfig) {
        this.usbconfig = usbconfig;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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
        final USBdevice other = (USBdevice) obj;
        if (this.usbconfig != other.usbconfig && (this.usbconfig == null || !this.usbconfig.equals(other.usbconfig))) {
            return false;
        }
        if ((this.serial == null) ? (other.serial != null) : !this.serial.equals(other.serial)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.usbconfig != null ? this.usbconfig.hashCode() : 0);
        hash = 67 * hash + (this.serial != null ? this.serial.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "USBdevice{" + "usbconfig=" + usbconfig + ", bus=" + bus + ", usbPath=" + usbPath + ", serial=" + serial + ", devicePath=" + devicePath + ", deviceAlias=" + deviceAlias + ", nodeId=" + nodeId + ", description=" + description + ", disabled=" + disabled + ", connectionString=" + connectionString + ", platformId=" + platformId + ", lastModification=" + lastModification + '}';
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("bus");
        csvOutput.write("usbPath");
        csvOutput.write("serial");
        csvOutput.write("devicePath");
        csvOutput.write("deviceAlias");
        csvOutput.write("nodeId");
        csvOutput.write("disabled");
        csvOutput.write("connectionString");
        csvOutput.write("platformId");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write((this.bus));
        csvOutput.write((this.usbPath));
        csvOutput.write((this.serial));
        csvOutput.write((this.devicePath));
        csvOutput.write((this.deviceAlias));
        csvOutput.write(String.valueOf(this.nodeId));
        csvOutput.write(String.valueOf(this.disabled));
        csvOutput.write((this.connectionString));
        csvOutput.write(String.valueOf(this.platformId));
    }

    @Override
    public String getCSVname() {
        return "device";
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
}
