/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.CtpInfoMsg;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author ph4r05
 */
@Entity
public class ExperimentCTPInfoCCA implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private int node;
    private int nodeBS;
    
    
    private long nodeTimeStampStart;
    private long cca_1;
    private long cca_2;

    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CtpInfoMsg msg){
        if (msg.get_type()!=0){
            throw new IllegalArgumentException("Can accept only message describing state");
        }
        
        int[] cca_data = msg.get_data_cca_data();
        this.nodeTimeStampStart = msg.get_data_cca_tstamp();
        this.cca_1 = cca_data[0] | ((long)cca_data[1] << 32);
        this.cca_2 = cca_data[2] | ((long)cca_data[3] << 32);
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMilitime() {
        return militime;
    }

    public void setMilitime(long militime) {
        this.militime = militime;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public int getNodeBS() {
        return nodeBS;
    }

    public void setNodeBS(int nodeBS) {
        this.nodeBS = nodeBS;
    }

    public long getNodeTimeStampStart() {
        return nodeTimeStampStart;
    }

    public void setNodeTimeStampStart(long nodeTimeStampStart) {
        this.nodeTimeStampStart = nodeTimeStampStart;
    }

    public long getCca_1() {
        return cca_1;
    }

    public void setCca_1(long cca_1) {
        this.cca_1 = cca_1;
    }

    public long getCca_2() {
        return cca_2;
    }

    public void setCca_2(long cca_2) {
        this.cca_2 = cca_2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 41 * hash + (this.experiment != null ? this.experiment.hashCode() : 0);
        hash = 41 * hash + (int) (this.militime ^ (this.militime >>> 32));
        hash = 41 * hash + this.node;
        hash = 41 * hash + this.nodeBS;
        hash = 41 * hash + (int) (this.nodeTimeStampStart ^ (this.nodeTimeStampStart >>> 32));
        hash = 41 * hash + (int) (this.cca_1 ^ (this.cca_1 >>> 32));
        hash = 41 * hash + (int) (this.cca_2 ^ (this.cca_2 >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPInfoCCA other = (ExperimentCTPInfoCCA) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.experiment != other.experiment && (this.experiment == null || !this.experiment.equals(other.experiment))) {
            return false;
        }
        if (this.militime != other.militime) {
            return false;
        }
        if (this.node != other.node) {
            return false;
        }
        if (this.nodeBS != other.nodeBS) {
            return false;
        }
        if (this.nodeTimeStampStart != other.nodeTimeStampStart) {
            return false;
        }
        if (this.cca_1 != other.cca_1) {
            return false;
        }
        if (this.cca_2 != other.cca_2) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExperimentCTPInfoCCA{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", nodeTimeStampStart=" + nodeTimeStampStart + ", cca_1=" + cca_1 + ", cca_2=" + cca_2 + '}';
    }

    @Override
    public String getCSVname() {
        return "ctpInfoCCA";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.nodeTimeStampStart));
        csvOutput.write(String.valueOf(this.cca_1));
        csvOutput.write(String.valueOf(this.cca_2));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {   
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        
        csvOutput.write("nodeTimeStampStart");
        csvOutput.write("cca_1");
        csvOutput.write("cca_2");
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
    
    
}
