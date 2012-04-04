/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import java.io.IOException;
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
public class ExperimentDataAliveCheck implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long miliFromStart;
    
    private int node;
    
    private long counter;
    
    private int radioQueueFree;
    private int serialQueueFree;
    private int serialFails;
    private int aliveFails;

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

    public int getRadioQueueFree() {
        return radioQueueFree;
    }

    public void setRadioQueueFree(int radioQueueFree) {
        this.radioQueueFree = radioQueueFree;
    }

    public int getSerialQueueFree() {
        return serialQueueFree;
    }

    public void setSerialQueueFree(int serialQueueFree) {
        this.serialQueueFree = serialQueueFree;
    }

    public int getSerialFails() {
        return serialFails;
    }

    public void setSerialFails(int serialFails) {
        this.serialFails = serialFails;
    }

    public int getAliveFails() {
        return aliveFails;
    }

    public void setAliveFails(int aliveFails) {
        this.aliveFails = aliveFails;
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        
        csvOutput.write("counter");
        csvOutput.write("radioQueueFree");
        csvOutput.write("serialQueueFree");
        csvOutput.write("serialFails");
        csvOutput.write("aliveFails");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment));
        csvOutput.write(String.valueOf(this.miliFromStart));
        csvOutput.write(String.valueOf(this.node));
        
        csvOutput.write(String.valueOf(this.counter));
        csvOutput.write(String.valueOf(this.radioQueueFree));
        csvOutput.write(String.valueOf(this.serialQueueFree));
        csvOutput.write(String.valueOf(this.serialFails));
        csvOutput.write(String.valueOf(this.aliveFails));
    }

    @Override
    public String getCSVname() {
        return "dataAlive";
    }
}
