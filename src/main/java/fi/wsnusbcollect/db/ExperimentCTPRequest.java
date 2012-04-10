/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.CtpSendRequestMsg;
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
public class ExperimentCTPRequest implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private int node;
    private int nodeBS;
    
    
    // SEQ number ot this request
    private int counter;

    // number of packets to send
    private int packets;

    // timer delay between message send in ms
    private int delay;
    
    // delay variability
    private int variability;

    // desired packet size in bytes
    private int packet_size;

    // datasource of CtpMessage - can be random/sensor reading
    private int dataSource;

    // target = packets. CurPacket is incremented when:
    // TRUE => only on succ sent packet => sendDone()==SUCC
    // FALSE => on every Send()==SUCC
    private boolean counterStrategySuccess;

    // if true then timer is started periodically and at each timer tick
    // message is sent 
    // if false new mesage is sent after previous message was successfully sent in 
    // sendDone()
    private boolean timerStrategyPeriodic;
    
    // if TRUE then node should send unlimited number of packets (no finish criteria)
    private boolean packetsUnlimited;

    /**
     * loads contents from message body
     * @param msg 
     */
    public void loadFromMessage(CtpSendRequestMsg msg){
        this.counter = msg.get_counter();
        this.counterStrategySuccess = (msg.get_flags() & 0x1) > 0;
        this.dataSource = msg.get_dataSource();
        this.delay = msg.get_delay();
        this.variability = msg.get_delayVariability();
        this.packet_size = msg.get_size();
        this.timerStrategyPeriodic = (msg.get_flags() & 0x2) > 0;
        this.packetsUnlimited = (msg.get_flags() & 0x4) > 0;
    }
    
    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean isCounterStrategySuccess() {
        return counterStrategySuccess;
    }

    public void setCounterStrategySuccess(boolean counterStrategySuccess) {
        this.counterStrategySuccess = counterStrategySuccess;
    }

    public int getDataSource() {
        return dataSource;
    }

    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
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

    public int getPacket_size() {
        return packet_size;
    }

    public void setPacket_size(int packet_size) {
        this.packet_size = packet_size;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(int packets) {
        this.packets = packets;
    }

    public boolean isTimerStrategyPeriodic() {
        return timerStrategyPeriodic;
    }

    public void setTimerStrategyPeriodic(boolean timerStrategyPeriodic) {
        this.timerStrategyPeriodic = timerStrategyPeriodic;
    }

    public boolean isPacketsUnlimited() {
        return packetsUnlimited;
    }

    public void setPacketsUnlimited(boolean packetsUnlimited) {
        this.packetsUnlimited = packetsUnlimited;
    }

    public int getVariability() {
        return variability;
    }

    public void setVariability(int variability) {
        this.variability = variability;
    }
    
    @Override
    public String toString() {
        return "ExperimentCTPRequest{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", counter=" + counter + ", packets=" + packets + ", delay=" + delay + ", packet_size=" + packet_size + ", dataSource=" + dataSource + ", counterStrategySuccess=" + counterStrategySuccess + ", timerStrategyPeriodic=" + timerStrategyPeriodic + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPRequest other = (ExperimentCTPRequest) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String getCSVname() {
        return "ctpRequest";
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.counter));
        csvOutput.write(String.valueOf(this.packets));
        csvOutput.write(String.valueOf(this.delay));
        csvOutput.write(String.valueOf(this.variability));
        csvOutput.write(String.valueOf(this.packet_size));
        csvOutput.write(String.valueOf(this.dataSource));
        csvOutput.write(String.valueOf(this.counterStrategySuccess));
        csvOutput.write(String.valueOf(this.timerStrategyPeriodic));
        csvOutput.write(String.valueOf(this.packetsUnlimited));
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        csvOutput.write("counter");
        csvOutput.write("packets");
        csvOutput.write("delay");
        csvOutput.write("variability");
        csvOutput.write("packet_size");
        csvOutput.write("dataSource");
        csvOutput.write("counterStrategySuccess");
        csvOutput.write("timerStrategyPeriodic");
        csvOutput.write("packetsUnlimited");
    }
}
