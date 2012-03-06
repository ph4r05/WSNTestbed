/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Stores info about experiments
 * @author ph4r05
 */
@Entity
public class ExperimentMetadata implements Serializable {
     // node id record, configuration ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String experimentGroup;
    
    private String name;
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date datestart;
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date datestop;
    
    // miliseconds at start
    private Long miliStart;
    
    // description of nodeconfiguration
    @Lob
    private String description;
    
    @Lob
    private String keywords;
    
    // configuration name
    @ManyToOne
    private USBconfiguration nodeConfiguration;
    
    private String owner;
    
    @Lob
    private String configFile;

    public Date getDatestart() {
        return datestart;
    }

    public void setDatestart(Date datestart) {
        this.datestart = datestart;
    }

    public Date getDatestop() {
        return datestop;
    }

    public void setDatestop(Date datestop) {
        this.datestop = datestop;
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

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExperimentGroup() {
        return experimentGroup;
    }

    public void setExperimentGroup(String experimentGroup) {
        this.experimentGroup = experimentGroup;
    }

    public USBconfiguration getNodeConfiguration() {
        return nodeConfiguration;
    }

    public void setNodeConfiguration(USBconfiguration nodeConfiguration) {
        this.nodeConfiguration = nodeConfiguration;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public Long getMiliStart() {
        return miliStart;
    }

    public void setMiliStart(Long miliStart) {
        this.miliStart = miliStart;
    }
}
