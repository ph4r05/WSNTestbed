/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import fi.wsnusbcollect.dbManager.ExperimentConverter;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataGenericMessage")
public class ExperimentDataGenericMessage implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @XStreamConverter(ExperimentConverter.class)
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long militime;
    
    private int node;
    
    private int nodeBS;
    
    private boolean sent;
    //************************* END OF COMMON HEADER
    
    private int len;
    
    private int amtype;
    
    @Lob
    private String stringDump;


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

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
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

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getStringDump() {
        return stringDump;
    }

    public void setStringDump(String stringDump) {
        this.stringDump = stringDump;
    }

    public int getAmtype() {
        return amtype;
    }

    public void setAmtype(int amtype) {
        this.amtype = amtype;
    }

    @Override
    public String toString() {
        return "ExperimentDataGenericMessage{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", sent=" + sent + ", len=" + len + ", amtype=" + amtype + ", stringDump=" + stringDump + '}';
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        
        csvOutput.write("sent");
        csvOutput.write("len");
        csvOutput.write("amtype");
        csvOutput.write("stringDump");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.sent));
        csvOutput.write(String.valueOf(this.len));
        csvOutput.write(String.valueOf(this.amtype));
        csvOutput.write(this.stringDump);
    }

    @Override
    public String getCSVname() {
        return "genericMsg";
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
    
    
}
