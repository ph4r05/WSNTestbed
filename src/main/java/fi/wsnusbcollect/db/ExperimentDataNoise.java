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
 * 
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataNoise")
public class ExperimentDataNoise implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long miliFromStart;
    
    private int connectedNode;
    
    private int counter;
    
    private int noise;
    
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

    public long getMiliFromStart() {
        return miliFromStart;
    }

    public void setMiliFromStart(long miliFromStart) {
        this.miliFromStart = miliFromStart;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public int getNoise() {
        return noise;
    }

    public void setNoise(int noise) {
        this.noise = noise;
    }
    
    @Override
    public String getCSVname() {
        return "noise";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {    
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.miliFromStart));
        csvOutput.write(String.valueOf(this.connectedNode));
        csvOutput.write(String.valueOf(this.counter));
        csvOutput.write(String.valueOf(this.noise));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {   
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("connectedNode");
        csvOutput.write("counter");
        csvOutput.write("noise");
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
    
    
}
