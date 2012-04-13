/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Stores info about previous/current USB node configuration
 * @author ph4r05
 */
@Entity
public class USBconfiguration implements Serializable, FileWritable{
    private static final long serialVersionUID = 1123123523L;
    
    // node id record, configuration ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // date created
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date validFrom;
    
    // date when change was detected and new configuration was created
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=true)
    private java.util.Date validTo;
    
    // description of nodeconfiguration
    @Lob
    private String description;
    
    // configuration name
    private String configurationName;
    
    // last modification of this record in database
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date lastModification;

    public USBconfiguration() {      
        
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final USBconfiguration other = (USBconfiguration) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "USBconfiguration{" + "id=" + id + ", validFrom=" + validFrom + ", description=" + description + ", configurationName=" + configurationName + ", lastModification=" + lastModification + '}';
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
}
