/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.ElementCollection;
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
public class ExperimentMetadata implements Serializable, DataCSVWritable {
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
    
    @ElementCollection
    private List<String> connectedNodesUsed = new ArrayList<String>();

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

    public List<String> getConnectedNodesUsed() {
        return connectedNodesUsed;
    }

    public void setConnectedNodesUsed(List<String> connectedNodesUsed) {
        this.connectedNodesUsed = connectedNodesUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentMetadata other = (ExperimentMetadata) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return "ExperimentMetadata{" + "id=" + id + ", experimentGroup=" + experimentGroup + ", name=" + name + ", datestart=" + datestart + ", datestop=" + datestop + ", miliStart=" + miliStart + ", description=" + description + ", keywords=" + keywords + ", nodeConfiguration=" + nodeConfiguration + ", owner=" + owner + ", configFile=" + configFile + ", connectedNodesUsed=" + connectedNodesUsed + '}';
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("id");
        csvOutput.write("experimentGroup");
        csvOutput.write("name");
        csvOutput.write("datestart");
        csvOutput.write("datestop");
        csvOutput.write("miliStart");
        csvOutput.write("owner");
        csvOutput.write("nodes");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.id));
        csvOutput.write((this.experimentGroup));
        csvOutput.write((this.name));
        csvOutput.write(String.valueOf(this.datestart));
        csvOutput.write(String.valueOf(this.datestop));
        csvOutput.write(String.valueOf(this.miliStart));
        csvOutput.write((this.owner));
        
        // serialize inline - nodes
        if (this.connectedNodesUsed==null){
            csvOutput.write("");
        } else {
            StringBuilder sb = new StringBuilder();
            int i=0;
            for(String curNode : this.connectedNodesUsed){
                i+=1;
                if (i>1) sb.append(" ");
                sb.append(curNode);
            }
            
            csvOutput.write(sb.toString());
        }
        
    }

    @Override
    public String getCSVname() {
        return "expMetadata";
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
}
