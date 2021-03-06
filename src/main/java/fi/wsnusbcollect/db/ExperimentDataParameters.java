/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import fi.wsnusbcollect.dbManager.ExperimentConverter;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Stores info about experiments
 * @author ph4r05
 */
@Entity
public class ExperimentDataParameters implements Serializable, FileWritable {
     // node id record, configuration ID
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @XStreamConverter(ExperimentConverter.class)
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private Class parameterType;
    
    private String parameterName;
    
    private String parameterValue;

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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
    
    
}
