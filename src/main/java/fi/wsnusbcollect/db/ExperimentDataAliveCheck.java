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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Holds special packets from nodes indicating that node was alive + alive counter
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataAliveCheck")
public class ExperimentDataAliveCheck implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long miliFromStart;
    
    private int node;
    
    private long counter;

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
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

    public long getMiliFromStart() {
        return miliFromStart;
    }

    public void setMiliFromStart(long miliFromStart) {
        this.miliFromStart = miliFromStart;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }
}
