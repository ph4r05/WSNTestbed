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
 * 
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataRSSI")
public class ExperimentDataRSSI implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;

    private long miliFromStart;
    
    private int connectedNode;
    
    private int sendingNode;
    
    private long sendingNodeCounter;
    
    private long connectedNodeCounter;
    
    private long rssi;
    
    private short len;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(int connectedNode) {
        this.connectedNode = connectedNode;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public long getMiliFromStart() {
        return miliFromStart;
    }

    public void setMiliFromStart(long miliFromStart) {
        this.miliFromStart = miliFromStart;
    }

    public long getRssi() {
        return rssi;
    }

    public void setRssi(long rssi) {
        this.rssi = rssi;
    }

    public int getSendingNode() {
        return sendingNode;
    }

    public void setSendingNode(int sendingNode) {
        this.sendingNode = sendingNode;
    }

    public long getConnectedNodeCounter() {
        return connectedNodeCounter;
    }

    public void setConnectedNodeCounter(long connectedNodeCounter) {
        this.connectedNodeCounter = connectedNodeCounter;
    }

    public long getSendingNodeCounter() {
        return sendingNodeCounter;
    }

    public void setSendingNodeCounter(long sendingNodeCounter) {
        this.sendingNodeCounter = sendingNodeCounter;
    }

    public short getLen() {
        return len;
    }

    public void setLen(short len) {
        this.len = len;
    } 
}
