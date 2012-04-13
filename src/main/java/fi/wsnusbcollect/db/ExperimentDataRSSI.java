/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * RSSI measurements, requires high throughput
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataRSSI")
public class ExperimentDataRSSI implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="experiment_id")
    private ExperimentMetadata experiment;

    private long miliFromStart;
    
    private int connectedNode;
    
    private int sendingNode;
    
    private int sendingNodeCounter;
    
    private int connectedNodeCounter;
    
    private int rssi;
    
    private short len;
    
    /**
     * Returns SQL insert string for given list of elements.
     * WARNING! This method has to be modified if model changes
     * 
     * @param list
     * @return 
     */
    public static String getDBInsertString(List<ExperimentDataRSSI> list){
        if (list==null || list.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ExperimentDataRSSI(")
                .append("id,experiment_id,miliFromStart,connectedNode,")
                .append("sendingNode,sendingNodeCounter,connectedNodeCounter,")
                .append("rssi,len) VALUES ");
            
        int i=0;
        for (ExperimentDataRSSI entity : list) {
            if (i>0) sb.append(",");
            sb.append("(NULL,")
                    .append(entity.getExperiment().getId()).append(",")
                    .append(entity.getMiliFromStart()).append(",")
                    .append(entity.getConnectedNode()).append(",")
                    .append(entity.getSendingNode()).append(",")
                    .append(entity.getSendingNodeCounter()).append(",")
                    .append(entity.getConnectedNodeCounter()).append(",")
                    .append(entity.getRssi()).append(",")
                    .append(entity.getLen()).append(")");

            ++i;
        }
        
        return sb.toString();
    }
    
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

    public int getConnectedNodeCounter() {
        return connectedNodeCounter;
    }

    public void setConnectedNodeCounter(int connectedNodeCounter) {
        this.connectedNodeCounter = connectedNodeCounter;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getSendingNode() {
        return sendingNode;
    }

    public void setSendingNode(int sendingNode) {
        this.sendingNode = sendingNode;
    }

    public int getSendingNodeCounter() {
        return sendingNodeCounter;
    }

    public void setSendingNodeCounter(int sendingNodeCounter) {
        this.sendingNodeCounter = sendingNodeCounter;
    }

    public short getLen() {
        return len;
    }

    public void setLen(short len) {
        this.len = len;
    } 
    
    @Override
    public String getCSVname() {
        return "rssi";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {    
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.miliFromStart));
        csvOutput.write(String.valueOf(this.connectedNode));
        csvOutput.write(String.valueOf(this.sendingNode));
        csvOutput.write(String.valueOf(this.sendingNodeCounter));
        csvOutput.write(String.valueOf(this.connectedNodeCounter));
        csvOutput.write(String.valueOf(this.rssi));
        csvOutput.write(String.valueOf(this.len));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {       
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("connectedNode");
        csvOutput.write("sendingNode");
        csvOutput.write("sendingNodeCounter");
        csvOutput.write("connectedNodeCounter");
        csvOutput.write("rssi");
        csvOutput.write("len");
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
}
