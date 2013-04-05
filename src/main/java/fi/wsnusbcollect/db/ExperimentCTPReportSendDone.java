/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.CtpReportDataMsg;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Column;
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
public class ExperimentCTPReportSendDone implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private long nodeTime;
    private long packetNodeTimeStamp;
    private int node;
    private int nodeBS;
    
    private int data_origin;
    private int data_seqno;
    private int data_parent;
    private int data_metric;
    private int data_dataType;
    private int data_data;
    
    private long ccaWaitTime;
    private int ccaWaitRounds;	
    private int fwdRetryCount; 
    private int client;
    private int dest;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean acked;
    
    // source of message
    private int amSource;

    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CtpReportDataMsg msg){
        this.amSource = msg.get_amSource();
        this.nodeTime = msg.get_localTime32khz();
        this.packetNodeTimeStamp = msg.get_timestamp32khz();
        
        this.data_data = msg.get_response_data();
        this.data_dataType = msg.get_response_dataType();
        this.data_metric = msg.get_response_metric();
        this.data_origin = msg.get_response_origin();
        this.data_parent = msg.get_response_parent();
        this.data_seqno = msg.get_response_seqno();
        
        this.ccaWaitTime   = msg.get_data_sent_ccaWaitTime();
        this.ccaWaitRounds = msg.get_data_sent_ccaWaitRounds();
        this.fwdRetryCount = msg.get_data_sent_fwdRetryCount();
        this.client        = msg.get_data_sent_client();
        this.acked         = msg.get_data_sent_acked() > 0;
        this.dest          = msg.get_data_sent_dest();
    }   

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public long getMilitime() {
        return militime;
    }

    public void setMilitime(long militime) {
        this.militime = militime;
    }

    public long getNodeTime() {
        return nodeTime;
    }

    public void setNodeTime(long nodeTime) {
        this.nodeTime = nodeTime;
    }

    public long getPacketNodeTimeStamp() {
        return packetNodeTimeStamp;
    }

    public void setPacketNodeTimeStamp(long packetNodeTimeStamp) {
        this.packetNodeTimeStamp = packetNodeTimeStamp;
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

    public int getData_origin() {
        return data_origin;
    }

    public void setData_origin(int data_origin) {
        this.data_origin = data_origin;
    }

    public int getData_seqno() {
        return data_seqno;
    }

    public void setData_seqno(int data_seqno) {
        this.data_seqno = data_seqno;
    }

    public int getData_parent() {
        return data_parent;
    }

    public void setData_parent(int data_parent) {
        this.data_parent = data_parent;
    }

    public int getData_metric() {
        return data_metric;
    }

    public void setData_metric(int data_metric) {
        this.data_metric = data_metric;
    }

    public int getData_dataType() {
        return data_dataType;
    }

    public void setData_dataType(int data_dataType) {
        this.data_dataType = data_dataType;
    }

    public int getData_data() {
        return data_data;
    }

    public void setData_data(int data_data) {
        this.data_data = data_data;
    }

    public long getCcaWaitTime() {
        return ccaWaitTime;
    }

    public void setCcaWaitTime(long ccaWaitTime) {
        this.ccaWaitTime = ccaWaitTime;
    }

    public int getCcaWaitRounds() {
        return ccaWaitRounds;
    }

    public void setCcaWaitRounds(int ccaWaitRounds) {
        this.ccaWaitRounds = ccaWaitRounds;
    }

    public int getFwdRetryCount() {
        return fwdRetryCount;
    }

    public void setFwdRetryCount(int fwdRetryCount) {
        this.fwdRetryCount = fwdRetryCount;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public boolean isAcked() {
        return acked;
    }

    public void setAcked(boolean acked) {
        this.acked = acked;
    }

    public int getAmSource() {
        return amSource;
    }

    public void setAmSource(int amSource) {
        this.amSource = amSource;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPReportSendDone other = (ExperimentCTPReportSendDone) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExperimentCTPReportSendDone{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", nodeTime=" + nodeTime + ", packetNodeTimeStamp=" + packetNodeTimeStamp + ", node=" + node + ", nodeBS=" + nodeBS + ", data_origin=" + data_origin + ", data_seqno=" + data_seqno + ", data_parent=" + data_parent + ", data_metric=" + data_metric + ", data_dataType=" + data_dataType + ", data_data=" + data_data + ", ccaWaitTime=" + ccaWaitTime + ", ccaWaitRounds=" + ccaWaitRounds + ", fwdRetryCount=" + fwdRetryCount + ", client=" + client + ", dest=" + dest + ", acked=" + acked + ", amSource=" + amSource + '}';
    }
    
    @Override
    public String getCSVname() {
        return "ctpReportSendDone";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.data_origin));
        csvOutput.write(String.valueOf(this.data_seqno));
        csvOutput.write(String.valueOf(this.data_parent));
        csvOutput.write(String.valueOf(this.data_metric));
        csvOutput.write(String.valueOf(this.data_dataType));
        csvOutput.write(String.valueOf(this.data_data));

        // source of message
        csvOutput.write(String.valueOf(this.amSource));
        csvOutput.write(String.valueOf(this.nodeTime));
        csvOutput.write(String.valueOf(this.packetNodeTimeStamp));
        csvOutput.write(String.valueOf(this.ccaWaitTime));
        csvOutput.write(String.valueOf(this.ccaWaitRounds));
        csvOutput.write(String.valueOf(this.fwdRetryCount));
        csvOutput.write(String.valueOf(this.client));
        csvOutput.write(String.valueOf(this.acked));
        csvOutput.write(String.valueOf(this.dest));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        
        csvOutput.write("data_origin");
        csvOutput.write("data_seqno");
        csvOutput.write("data_parent");
        csvOutput.write("data_metric");
        csvOutput.write("data_dataType");
        csvOutput.write("data_data");


        // source of message
        csvOutput.write("amSource");
        csvOutput.write("spoofed");
        csvOutput.write("regularCTP");
        csvOutput.write("sent");
        
        csvOutput.write("sendDone");
        csvOutput.write("nodeTime");
        csvOutput.write("packetNodeTimeStamp");
        csvOutput.write("ccaWaitTime");
        csvOutput.write("ccaWaitRounds");
        csvOutput.write("fwdRetryCount");
        csvOutput.write("client");
        csvOutput.write("acked");
        csvOutput.write("dest");
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
    
    
}
