/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import fi.wsnusbcollect.messages.CtpReportDataMsg;
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
public class ExperimentCTPReport implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private int node;
    private int nodeBS;
    
    
    private int data_origin;
    private int data_seqno;
    private int data_parent;
    private int data_metric;
    private int data_dataType;
    private int data_data;
    private int data_hopcount;
    private int data_sendCount;
    private int data_sendSuccessCount;
    
    private int header_options;
    private int header_thl;
    private int header_etx;
    private int header_origin;
    private int header_originSeqNo;
    private int header_type;
    
    // source of message
    private int amSource;
    // rssi of received packet
    private int rssi;
    private boolean spoofed;
    private boolean regularCTP;

    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CtpReportDataMsg msg){
        this.amSource = msg.get_amSource();
        this.spoofed = (msg.get_flags() & 1) > 0;
        this.regularCTP = (msg.get_flags() & 2) > 0;
        this.rssi = msg.get_rssi();
        
        this.data_data = msg.get_response_data();
        this.data_dataType = msg.get_response_dataType();
        this.data_hopcount = msg.get_response_hopcount();
        this.data_metric = msg.get_response_metric();
        this.data_origin = msg.get_response_origin();
        this.data_parent = msg.get_response_parent();
        this.data_sendCount = msg.get_response_sendCount();
        this.data_seqno = msg.get_response_seqno();
        this.data_sendSuccessCount = msg.get_response_sendSuccessCount();
        
        this.header_etx = msg.get_ctpDataHeader_etx();
        this.header_options = msg.get_ctpDataHeader_options();
        this.header_origin = msg.get_ctpDataHeader_origin();
        this.header_originSeqNo = msg.get_ctpDataHeader_originSeqNo();
        this.header_thl = msg.get_ctpDataHeader_thl();
        this.header_type = msg.get_ctpDataHeader_type();
    }

    public int getAmSource() {
        return amSource;
    }

    public void setAmSource(int amSource) {
        this.amSource = amSource;
    }

    public int getData_data() {
        return data_data;
    }

    public void setData_data(int data_data) {
        this.data_data = data_data;
    }

    public int getData_dataType() {
        return data_dataType;
    }

    public void setData_dataType(int data_dataType) {
        this.data_dataType = data_dataType;
    }

    public int getData_hopcount() {
        return data_hopcount;
    }

    public void setData_hopcount(int data_hopcount) {
        this.data_hopcount = data_hopcount;
    }

    public int getData_metric() {
        return data_metric;
    }

    public void setData_metric(int data_metric) {
        this.data_metric = data_metric;
    }

    public int getData_origin() {
        return data_origin;
    }

    public void setData_origin(int data_origin) {
        this.data_origin = data_origin;
    }

    public int getData_parent() {
        return data_parent;
    }

    public void setData_parent(int data_parent) {
        this.data_parent = data_parent;
    }

    public int getData_sendCount() {
        return data_sendCount;
    }

    public void setData_sendCount(int data_sendCount) {
        this.data_sendCount = data_sendCount;
    }

    public int getData_sendSuccessCount() {
        return data_sendSuccessCount;
    }

    public void setData_sendSuccessCount(int data_sendSuccessCount) {
        this.data_sendSuccessCount = data_sendSuccessCount;
    }

    public int getData_seqno() {
        return data_seqno;
    }

    public void setData_seqno(int data_seqno) {
        this.data_seqno = data_seqno;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public int getHeader_etx() {
        return header_etx;
    }

    public void setHeader_etx(int header_etx) {
        this.header_etx = header_etx;
    }

    public int getHeader_options() {
        return header_options;
    }

    public void setHeader_options(int header_options) {
        this.header_options = header_options;
    }

    public int getHeader_origin() {
        return header_origin;
    }

    public void setHeader_origin(int header_origin) {
        this.header_origin = header_origin;
    }

    public int getHeader_originSeqNo() {
        return header_originSeqNo;
    }

    public void setHeader_originSeqNo(int header_originSeqNo) {
        this.header_originSeqNo = header_originSeqNo;
    }

    public int getHeader_thl() {
        return header_thl;
    }

    public void setHeader_thl(int header_thl) {
        this.header_thl = header_thl;
    }

    public int getHeader_type() {
        return header_type;
    }

    public void setHeader_type(int header_type) {
        this.header_type = header_type;
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

    public boolean isRegularCTP() {
        return regularCTP;
    }

    public void setRegularCTP(boolean regularCTP) {
        this.regularCTP = regularCTP;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isSpoofed() {
        return spoofed;
    }

    public void setSpoofed(boolean spoofed) {
        this.spoofed = spoofed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPReport other = (ExperimentCTPReport) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ExperimentCTPReport{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", data_origin=" + data_origin + ", data_seqno=" + data_seqno + ", data_parent=" + data_parent + ", data_metric=" + data_metric + ", data_dataType=" + data_dataType + ", data_data=" + data_data + ", data_hopcount=" + data_hopcount + ", data_sendCount=" + data_sendCount + ", data_sendSuccessCount=" + data_sendSuccessCount + ", header_options=" + header_options + ", header_thl=" + header_thl + ", header_etx=" + header_etx + ", header_origin=" + header_origin + ", header_originSeqNo=" + header_originSeqNo + ", header_type=" + header_type + ", amSource=" + amSource + ", rssi=" + rssi + ", spoofed=" + spoofed + ", regularCTP=" + regularCTP + '}';
    }   
}
