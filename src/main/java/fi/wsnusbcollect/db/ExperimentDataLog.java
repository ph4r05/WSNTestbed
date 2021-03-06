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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 *
 * @author ph4r05
 */
@Entity
public class ExperimentDataLog implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @XStreamConverter(ExperimentConverter.class)
    @ManyToOne
    @JoinColumn(name="experiment_id")
    private ExperimentMetadata experiment;
    
    private long miliEventTime;
    private String severity;
    
    private int reasonCode;
    private String reasonName;
    
    @Lob
    private String reasonData;
    
    @Lob
    private String description;

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

    public long getMiliEventTime() {
        return miliEventTime;
    }

    public void setMiliEventTime(long miliEventTime) {
        this.miliEventTime = miliEventTime;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonData() {
        return reasonData;
    }

    public void setReasonData(String reasonData) {
        this.reasonData = reasonData;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("miliEventTime");
        csvOutput.write("severity");
        csvOutput.write("reasonCode");
        csvOutput.write("reasonName");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.miliEventTime));
        csvOutput.write((this.severity));
        csvOutput.write(String.valueOf(this.reasonCode));
        csvOutput.write((this.reasonName));
    }

    @Override
    public String getCSVname() {
        return "expLog";
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
    
    
}
