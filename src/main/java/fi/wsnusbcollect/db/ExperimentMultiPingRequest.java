/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.MultiPingMsg;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Protocoling entity, only sent messages are stored here
 * @author ph4r05
 */
@Entity
public class ExperimentMultiPingRequest implements Serializable, DataCSVWritable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
//    @TableGenerator(name="generatorName", table="TableID",  
//                pkColumnName="tablename", // TableID.TableName (value = table_name, test_table, etc.)  
//                valueColumnName="id") // TableID.ID (value = 1,2,3,etc.)  
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long miliFromStart;
    
    private int node;
    
    private int nodeBS;
    //************************* END OF COMMON HEADER
    
    // where to send ping message? single node or broadcast
    private int destination;
    // SEQ number ot this request
    private int counter;
    // tx power of destination
    private int txpower;
    // channel at which to send
    private int channel;
    // number of packets to send
    private int packets;
    // timer delay between message send in ms
    private int delay;
    // desired packet size in bytes
    private int packetSize;
    // target = packets. CurPacket is incremented when:
    // TRUE => only on succ sent packet => sendDone()==SUCC
    // FALSE => on every Send()==SUCC
    private short counterStrategySuccess;
    // if true then timer is started periodically and at each timer tick
    // message is sent 
    // if false new mesage is sent after previous message was successfully sent in 
    // sendDone()
    private short timerStrategyPeriodic;

    /**
     * Initializes variables from message
     */
    public void loadFromMessage(MultiPingMsg msg){
        if (msg==null){
            throw new NullPointerException("Cannot load from null message");
        }
        
        this.setChannel(msg.get_channel());
        this.setCounter(msg.get_counter());
        this.setCounterStrategySuccess(msg.get_counterStrategySuccess());
        this.setDelay(msg.get_delay());
        this.setDestination(msg.get_destination());
        this.setPackets(msg.get_packets());
        this.setPacketSize(msg.get_size());
        this.setTimerStrategyPeriodic(msg.get_timerStrategyPeriodic());
        this.setTxpower(msg.get_txpower());
    }
    
    public String getIdentityColumnString() {
        return "not null auto_increment"; //starts with 1, implicitly
    }
    
    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public short getCounterStrategySuccess() {
        return counterStrategySuccess;
    }

    public void setCounterStrategySuccess(short counterStrategySuccess) {
        this.counterStrategySuccess = counterStrategySuccess;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
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

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(int packets) {
        this.packets = packets;
    }

    public short getTimerStrategyPeriodic() {
        return timerStrategyPeriodic;
    }

    public void setTimerStrategyPeriodic(short timerStrategyPeriodic) {
        this.timerStrategyPeriodic = timerStrategyPeriodic;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
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

    @Override
    public String toString() {
        return "ExperimentMultiPingRequest{" + "id=" + id + ", experiment=" + experiment + ", miliFromStart=" + miliFromStart + ", node=" + node + ", nodeBS=" + nodeBS + ", destination=" + destination + ", counter=" + counter + ", txpower=" + txpower + ", channel=" + channel + ", packets=" + packets + ", delay=" + delay + ", packetSize=" + packetSize + ", counterStrategySuccess=" + counterStrategySuccess + ", timerStrategyPeriodic=" + timerStrategyPeriodic + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentMultiPingRequest other = (ExperimentMultiPingRequest) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("miliFromStart");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        csvOutput.write("miliStart");
        csvOutput.write("destination");
        csvOutput.write("txpower");
        csvOutput.write("channel");
        csvOutput.write("packets");
        csvOutput.write("delay");
        csvOutput.write("packetSize");
        csvOutput.write("counterStrategySuccess");
        csvOutput.write("timerStrategyPeriodic");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment.getId()));
        csvOutput.write(String.valueOf(this.miliFromStart));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        csvOutput.write(String.valueOf(this.miliFromStart));
        csvOutput.write(String.valueOf(this.destination));
        csvOutput.write(String.valueOf(this.txpower));
        csvOutput.write(String.valueOf(this.channel));
        csvOutput.write(String.valueOf(this.packets));
        csvOutput.write(String.valueOf(this.delay));
        csvOutput.write(String.valueOf(this.packetSize));
        csvOutput.write(String.valueOf(this.counterStrategySuccess));
        csvOutput.write(String.valueOf(this.timerStrategyPeriodic));
    }

    @Override
    public String getCSVname() {
        return "multiPingReq";
    }
}
