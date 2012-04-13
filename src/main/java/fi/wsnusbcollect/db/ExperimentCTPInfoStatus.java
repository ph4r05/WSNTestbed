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
public class ExperimentCTPInfoStatus implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private int node;
    private int nodeBS;
    
    private int parent;
    private int etx;
    private int neighbors;
    private int serialQueueSize;
    private int ctpSeqNo;
    private int ctpBusyCount;
    private boolean ctpBusy;

    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CtpInfoMsg msg){
        if (msg.get_type()!=0){
            throw new IllegalArgumentException("Can accept only message describing state");
        }
        
        this.ctpBusy = (msg.get_data_status_flags() & 0x1) > 0;
        this.ctpBusyCount = msg.get_data_status_ctpBusyCount();
        this.ctpSeqNo = msg.get_data_status_ctpSeqNo();
        this.etx = msg.get_data_status_etx();
        this.neighbors = msg.get_data_status_neighbors();
        this.parent = msg.get_data_status_parent();
        this.serialQueueSize = msg.get_data_status_serialQueueSize();
    }
    
    public boolean isCtpBusy() {
        return ctpBusy;
    }

    public void setCtpBusy(boolean ctpBusy) {
        this.ctpBusy = ctpBusy;
    }

    public int getCtpBusyCount() {
        return ctpBusyCount;
    }

    public void setCtpBusyCount(int ctpBusyCount) {
        this.ctpBusyCount = ctpBusyCount;
    }

    public int getCtpSeqNo() {
        return ctpSeqNo;
    }

    public void setCtpSeqNo(int ctpSeqNo) {
        this.ctpSeqNo = ctpSeqNo;
    }

    public int getEtx() {
        return etx;
    }

    public void setEtx(int etx) {
        this.etx = etx;
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

    public int getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(int neighbors) {
        this.neighbors = neighbors;
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

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getSerialQueueSize() {
        return serialQueueSize;
    }

    public void setSerialQueueSize(int serialQueueSize) {
        this.serialQueueSize = serialQueueSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPInfoStatus other = (ExperimentCTPInfoStatus) obj;
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
        if (this.parent != other.parent) {
            return false;
        }
        if (this.etx != other.etx) {
            return false;
        }
        if (this.neighbors != other.neighbors) {
            return false;
        }
        if (this.serialQueueSize != other.serialQueueSize) {
            return false;
        }
        if (this.ctpSeqNo != other.ctpSeqNo) {
            return false;
        }
        if (this.ctpBusyCount != other.ctpBusyCount) {
            return false;
        }
        if (this.ctpBusy != other.ctpBusy) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 89 * hash + (this.experiment != null ? this.experiment.hashCode() : 0);
        hash = 89 * hash + (int) (this.militime ^ (this.militime >>> 32));
        hash = 89 * hash + this.node;
        hash = 89 * hash + this.nodeBS;
        hash = 89 * hash + this.parent;
        hash = 89 * hash + this.neighbors;
        hash = 89 * hash + this.serialQueueSize;
        hash = 89 * hash + this.ctpSeqNo;
        hash = 89 * hash + this.ctpBusyCount;
        hash = 89 * hash + (this.ctpBusy ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ExperimentCTPInfoStatus{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", parent=" + parent + ", etx=" + etx + ", neighbors=" + neighbors + ", serialQueueSize=" + serialQueueSize + ", ctpSeqNo=" + ctpSeqNo + ", ctpBusyCount=" + ctpBusyCount + ", ctpBusy=" + ctpBusy + '}';
    }

    @Override
    public String getCSVname() {
        return "ctpInfoStatus";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.parent));
        csvOutput.write(String.valueOf(this.etx));
        csvOutput.write(String.valueOf(this.neighbors));
        csvOutput.write(String.valueOf(this.serialQueueSize));
        csvOutput.write(String.valueOf(this.ctpBusyCount));
        csvOutput.write(String.valueOf(this.ctpBusy));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {   
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        
        csvOutput.write("parent");
        csvOutput.write("etx");
        csvOutput.write("neighbors");
        csvOutput.write("serialQueueSize");
        csvOutput.write("ctpBusyCount");
        csvOutput.write("ctpBusy");
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
    
    
}
