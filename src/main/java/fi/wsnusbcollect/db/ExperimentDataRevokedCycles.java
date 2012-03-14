/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * Record of repeated experiments. Experiment cycle bounded by this boundaries 
 * should be considered as invalid - error occurred during cycle.
 * @author ph4r05
 */
@Entity
public class ExperimentDataRevokedCycles implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="experiment_id")
    private ExperimentMetadata experiment;

    private long miliStart;
    private long miliEnd;  
    private int reasonCode;
    private String reasonName;
    
    @Lob
    private String reasonDescription;

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

    public long getMiliEnd() {
        return miliEnd;
    }

    public void setMiliEnd(long miliEnd) {
        this.miliEnd = miliEnd;
    }

    public long getMiliStart() {
        return miliStart;
    }

    public void setMiliStart(long miliStart) {
        this.miliStart = miliStart;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }
}
